<?php

namespace App\Http\Controllers;

use App\Models\Message;
use App\Models\Notification;
use App\Models\ParticipationScore;
use App\Models\Topic;
use App\Models\Group;
use App\Models\UserExclusion;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class MessageController
{
    /**
     * Display all replies for a topic.
     */
    public function index(Topic $topic)
    {
        $messages = $topic->messages()
            ->visibleTo(Auth::id())
            ->with('user')
            ->whereNull('parent_message_id')
            ->latest()
            ->get();

        foreach ($messages as $message) {
            $message->setRelation('replies', $message->replies()
                ->visibleTo(Auth::id())
                ->with('user')
                ->get());
        }

        return Inertia::render('Messages/Index', [
            'topic' => $topic,
            'messages' => $messages,
        ]);
    }

    /**
     * Store a reply.
     * Requirement #3
     */
    public function store(Request $request, Group $group, Topic $topic)
    {
        $validated = $request->validate([
            'content' => 'required|string|min:2|max:5000',
            'parent_message_id' => 'nullable|exists:messages,id',
            'excluded_users' => 'nullable|array',
            'excluded_users.*' => 'exists:users,id',
        ]);

        abort_unless($topic->group_id === $group->id, 404);
        abort_unless($group->users()->whereKey(Auth::id())->exists(), 403);

        $excludedUsers = collect($validated['excluded_users'] ?? [])
            ->map(fn ($id) => (int) $id)
            ->unique()
            ->reject(fn ($id) => $id === Auth::id())
            ->values();

        if ($excludedUsers->isNotEmpty() && $group->users()->whereIn('users.id', $excludedUsers)->count() !== $excludedUsers->count()) {
            return back()->withErrors(['excluded_users' => 'You can only exclude members of this group.']);
        }

        $message = Message::create([
            'content' => $validated['content'],
            'topic_id' => $topic->id,
            'user_id' => Auth::id(),
            'parent_message_id' => $validated['parent_message_id'] ?? null,
        ]);

        Auth::user()->update([
            'last_communication_at' => now(),
            'inactive_warnings' => 0,
            'last_inactivity_warning_at' => null,
        ]);

        $recipientIds = collect([$topic->user_id, $message->parent?->user_id])
            ->filter()
            ->reject(fn ($id) => (int) $id === (int) Auth::id())
            ->diff($excludedUsers)
            ->unique();

        foreach ($recipientIds as $recipientId) {
            Notification::create([
                'user_id' => $recipientId,
                'type' => 'new_reply',
                'title' => 'New reply in a discussion',
                'message' => Auth::user()->name . " replied to: {$topic->title}",
                'related_entity_id' => $topic->id,
                'related_entity_type' => Topic::class,
            ]);
        }

        /*
        |--------------------------------------------------------------------------
        | Requirement #3
        |--------------------------------------------------------------------------
        */

        if ($excludedUsers->isNotEmpty()) {

            foreach ($excludedUsers as $excludedUser) {

                UserExclusion::create([
                    'user_id' => Auth::id(),
                    'excluded_user_id' => $excludedUser,
                    'message_id' => $message->id,
                ]);

            }

        }

        /*
        |--------------------------------------------------------------------------
        | Requirement #9
        |--------------------------------------------------------------------------
        */

        $score = ParticipationScore::firstOrCreate(
            [
                'user_id' => Auth::id(),
                'group_id' => $topic->group_id,
            ],
            [
                'topics_created' => 0,
                'topics_replied' => 0,
                'answers_provided' => 0,
                'total_score' => 0,
            ]
        );

        $score->increment('topics_replied');
        $score->increment('total_score', 5);

        return back()->with(
            'success',
            'Reply posted successfully.'
        );
    }

    /**
     * Requirement #2
     * Mark reply as answer.
     */
    public function markAsAnswer(Message $message)
    {
        $topic = $message->topic;

        if (
            $topic->user_id !== Auth::id()
            && !Auth::user()->isAdmin()
        ) {

            abort(403);

        }

        $message->update([
            'is_answer' => true,
        ]);

        $topic->update([
            'is_answered' => true,
        ]);

        if ($topic->user_id !== $message->user_id) {
            Notification::create([
                'user_id' => $topic->user_id,
                'type' => 'answer_accepted',
                'title' => 'Your question has an accepted answer',
                'message' => "{$message->user->name} posted an accepted answer to: {$topic->title}",
                'related_entity_id' => $topic->id,
                'related_entity_type' => Topic::class,
            ]);
        }

        $score = ParticipationScore::firstOrCreate(
            [
                'user_id' => $message->user_id,
                'group_id' => $topic->group_id,
            ],
            [
                'topics_created' => 0,
                'topics_replied' => 0,
                'answers_provided' => 0,
                'total_score' => 0,
            ]
        );

        $score->increment('answers_provided');
        $score->increment('total_score', 20);

        return back()->with(
            'success',
            'Reply marked as the accepted answer.'
        );
    }

    /**
     * Requirement #3
     * Update excluded users.
     */
    public function excludeUsers(Request $request, Message $message)
    {
        $validated = $request->validate([
            'excluded_users' => 'required|array',
            'excluded_users.*' => 'exists:users,id',
        ]);

        abort_unless($message->user_id === Auth::id() || Auth::user()->isAdmin(), 403);

        $excludedUsers = collect($validated['excluded_users'])
            ->map(fn ($id) => (int) $id)
            ->unique()
            ->reject(fn ($id) => $id === Auth::id())
            ->values();

        $group = $message->topic->group;
        if ($group->users()->whereIn('users.id', $excludedUsers)->count() !== $excludedUsers->count()) {
            return back()->withErrors(['excluded_users' => 'You can only exclude members of this group.']);
        }

        UserExclusion::where(
            'message_id',
            $message->id
        )->delete();

        foreach ($excludedUsers as $user) {

            UserExclusion::create([
                'user_id' => Auth::id(),
                'excluded_user_id' => $user,
                'message_id' => $message->id,
            ]);

        }

        return back()->with(
            'success',
            'Excluded users updated successfully.'
        );
    }

    /**
     * Delete a reply.
     */
    public function destroy(Message $message)
    {
        if (
            $message->user_id !== Auth::id()
            && !Auth::user()->isAdmin()
        ) {

            abort(403);

        }

        $message->delete();

        return back()->with(
            'success',
            'Reply deleted successfully.'
        );
    }
}
