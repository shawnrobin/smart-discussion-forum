<?php

namespace App\Http\Controllers\Api;

use App\Models\Message;
use App\Models\Notification;
use App\Models\Topic;
use App\Models\Group;
use App\Models\ParticipationScore;
use Illuminate\Http\Request;

class MessageController
{
    /**
     * Get all messages/replies for a topic (Requirement #2: Nested replies)
     */
    public function index(Topic $topic)
    {
        abort_unless(Topic::whereKey($topic->id)->visibleTo(auth()->id())->exists(), 404);
        $messages = $topic->messages()
            ->visibleTo(auth()->id())
            ->with('user')
            ->whereNull('parent_message_id')
            ->latest()
            ->get();

        // Load nested replies
        foreach ($messages as $message) {
            $message->replies = $message->replies()
                ->visibleTo(auth()->id())
                ->with('user')
                ->get();
        }

        return response()->json([
            'success' => true,
            'messages' => $messages
        ]);
    }

    /**
     * Create reply/message (Requirement #3: User exclusion)
     */
    public function store(Request $request, Group $group, Topic $topic)
    {
        $validated = $request->validate([
            'content' => 'required|string|min:2|max:5000',
            'parent_message_id' => 'nullable|exists:messages,id',
            'excluded_users' => 'nullable|array',
            'excluded_users.*' => 'integer|exists:users,id'
        ]);

        $group = $topic->group;
        abort_unless(Topic::whereKey($topic->id)->visibleTo(auth()->id())->exists(), 404);
        if (!$group->users()->whereKey(auth()->id())->exists()) {
            return response()->json(['success' => false, 'message' => 'You must be a group member to post a reply.'], 403);
        }

        $excludedUsers = collect($validated['excluded_users'] ?? [])
            ->map(fn ($id) => (int) $id)
            ->unique()
            ->reject(fn ($id) => $id === auth()->id())
            ->values();

        if ($excludedUsers->isNotEmpty() && $group->users()->whereIn('users.id', $excludedUsers)->count() !== $excludedUsers->count()) {
            return response()->json(['success' => false, 'message' => 'You can only exclude members of this group.'], 422);
        }

        // Create message
        $message = Message::create([
            'content' => $validated['content'],
            'topic_id' => $topic->id,
            'user_id' => auth()->id(),
            'parent_message_id' => $validated['parent_message_id'] ?? null
        ]);

        $request->user()->update([
            'last_communication_at' => now(),
            'inactive_warnings' => 0,
            'last_inactivity_warning_at' => null,
        ]);

        $recipientIds = collect([$topic->user_id, $message->parent?->user_id])
            ->filter()
            ->reject(fn ($id) => (int) $id === (int) auth()->id())
            ->diff($excludedUsers)
            ->unique();

        foreach ($recipientIds as $recipientId) {
            Notification::create([
                'user_id' => $recipientId,
                'type' => 'new_reply',
                'title' => 'New reply in a discussion',
                'message' => $request->user()->name . " replied to: {$topic->title}",
                'related_entity_id' => $topic->id,
                'related_entity_type' => Topic::class,
            ]);
        }

        // Handle user exclusions (Requirement #3)
        if ($excludedUsers->isNotEmpty()) {
            foreach ($excludedUsers as $excluded_user_id) {
                \App\Models\UserExclusion::create([
                    'user_id' => auth()->id(),
                    'excluded_user_id' => $excluded_user_id,
                    'message_id' => $message->id
                ]);
            }
        }

        // Update participation score (Requirement #9)
        $score = ParticipationScore::firstOrCreate(
            ['user_id' => auth()->id(), 'group_id' => $topic->group_id]
        );
        $score->increment('topics_replied');
        $score->increment('total_score', 5); // 5 points for replying

        return response()->json([
            'success' => true,
            'message' => 'Reply posted successfully',
            'data' => $message
        ], 201);
    }

    /**
     * Requirement #2: Mark message as answer
     */
    public function markAsAnswer(Request $request, Message $message)
    {
        $topic = $message->topic;

        // Check if user is topic author or admin
        if ($topic->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'Only topic author can mark answers'
            ], 403);
        }

        // Mark as answer
        $message->update(['is_answer' => true]);
        $topic->update(['is_answered' => true]);

        // Update participation score for answer provider (Requirement #9)
        $score = ParticipationScore::firstOrCreate(
            ['user_id' => $message->user_id, 'group_id' => $topic->group_id]
        );
        $score->increment('answers_provided');
        $score->increment('total_score', 20); // 20 points for providing answer

        return response()->json([
            'success' => true,
            'message' => 'Marked as answer successfully'
        ]);
    }

    /**
     * Requirement #3: Exclude users from seeing a message
     */
    public function excludeUsers(Request $request, Message $message)
    {
        $validated = $request->validate([
            'excluded_users' => 'required|array',
            'excluded_users.*' => 'integer|exists:users,id'
        ]);

        if ($message->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json(['success' => false, 'message' => 'You cannot change these exclusions.'], 403);
        }

        $excludedUsers = collect($validated['excluded_users'])
            ->map(fn ($id) => (int) $id)
            ->unique()
            ->reject(fn ($id) => $id === auth()->id())
            ->values();

        $group = $message->topic->group;
        if ($group->users()->whereIn('users.id', $excludedUsers)->count() !== $excludedUsers->count()) {
            return response()->json(['success' => false, 'message' => 'You can only exclude members of this group.'], 422);
        }

        // Delete existing exclusions
        \App\Models\UserExclusion::where('message_id', $message->id)->delete();

        // Create new exclusions
        foreach ($excludedUsers as $excluded_user_id) {
            \App\Models\UserExclusion::create([
                'user_id' => auth()->id(),
                'excluded_user_id' => $excluded_user_id,
                'message_id' => $message->id
            ]);
        }

        return response()->json([
            'success' => true,
            'message' => 'Users excluded successfully'
        ]);
    }

    /**
     * Delete message
     */
    public function destroy(Message $message)
    {
        if ($message->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'You cannot delete this message'
            ], 403);
        }

        $message->delete();

        return response()->json([
            'success' => true,
            'message' => 'Message deleted successfully'
        ]);
    }
}
