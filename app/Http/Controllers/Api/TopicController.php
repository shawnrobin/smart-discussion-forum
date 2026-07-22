<?php

namespace App\Http\Controllers\Api;

use App\Models\Topic;
use App\Models\TopicExclusion;
use App\Models\Group;
use App\Models\ParticipationScore;
use App\Services\TopicContentPolicy;
use Illuminate\Http\Request;

class TopicController
{
    /**
     * Get all topics in a group
     */
    public function index(Request $request, Group $group)
    {
        abort_unless($group->users()->whereKey($request->user()->id)->exists() || (int) $group->created_by === (int) $request->user()->id, 403);
        $topics = $group->topics()
            ->visibleTo($request->user()->id)
            ->with('user', 'messages')
            ->latest()
            ->paginate(10);

        return response()->json([
            'success' => true,
            'topics' => $topics
        ]);
    }

    /**
     * Get specific topic with all replies
     */
    public function show(Topic $topic)
    {
        abort_unless(Topic::whereKey($topic->id)->visibleTo(auth()->id())->exists(), 404);
        abort_unless($topic->group->users()->whereKey(auth()->id())->exists() || (int) $topic->group->created_by === (int) auth()->id(), 403);
        $topic->load(['user', 'group']);
        $topic->setRelation('messages', $topic->messages()
            ->visibleTo(auth()->id())
            ->with('user')
            ->get());

        return response()->json([
            'success' => true,
            'topic' => $topic
        ]);
    }

    /**
     * Create new topic (Requirement #2: Can mark as question)
     */
    public function store(Request $request, Group $group)
    {
        $validated = $request->validate([
            'title' => 'required|string|min:8|max:150',
            'description' => 'required|string|min:30|max:5000',
            'is_question' => 'boolean',
            'visibility' => 'nullable|in:all,exclude,selected',
            'selected_users' => 'nullable|array',
            'selected_users.*' => 'integer|exists:users,id',
        ]);

        // Check if user is member of group
        if (!$group->users()->where('user_id', auth()->id())->exists() && (int) $group->created_by !== (int) auth()->id()) {
            return response()->json([
                'success' => false,
                'message' => 'You must be a group member to create topics'
            ], 403);
        }

        if ($violation = TopicContentPolicy::violation($group, $validated['title'], $validated['description'])) {
            return response()->json([
                'success' => false,
                'message' => $violation,
                'errors' => ['title' => [$violation]],
            ], 422);
        }

        $selectedUsers = collect($validated['selected_users'] ?? [])->map(fn ($id) => (int) $id)->unique()->values();
        if ($selectedUsers->isNotEmpty() && $group->users()->whereIn('users.id', $selectedUsers)->count() !== $selectedUsers->count()) {
            return response()->json(['success' => false, 'message' => 'You can only choose members of this group.'], 422);
        }

        // Create topic
        $topic = Topic::create([
            ...$validated,
            'group_id' => $group->id,
            'user_id' => auth()->id()
        ]);

        $visibility = $validated['visibility'] ?? 'all';
        $excludedUsers = $visibility === 'selected'
            ? $group->users()->whereNotIn('users.id', $selectedUsers->push(auth()->id())->unique())->pluck('users.id')
            : ($visibility === 'exclude' ? $selectedUsers->reject(fn ($id) => $id === auth()->id()) : collect());

        if ($excludedUsers->isNotEmpty()) {
            TopicExclusion::insert($excludedUsers->map(fn ($id) => [
                'topic_id' => $topic->id,
                'excluded_user_id' => $id,
                'created_at' => now(),
                'updated_at' => now(),
            ])->all());
        }

        $request->user()->update([
            'last_communication_at' => now(),
            'inactive_warnings' => 0,
            'last_inactivity_warning_at' => null,
        ]);

        // Update participation score (Requirement #9)
        $score = ParticipationScore::firstOrCreate(
            ['user_id' => auth()->id(), 'group_id' => $group->id]
        );
        $score->increment('topics_created');
        $score->increment('total_score', 10); // 10 points for creating topic

        return response()->json([
            'success' => true,
            'message' => 'Topic created successfully',
            'topic' => $topic
        ], 201);
    }

    /**
     * Update topic
     */
    public function update(Request $request, Topic $topic)
    {
        // Check if user is user or admin
        if ($topic->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'You cannot update this topic'
            ], 403);
        }

        $validated = $request->validate([
            'title' => 'required|string|min:8|max:150',
            'description' => 'required|string|min:30|max:5000',
            'is_question' => 'boolean'
        ]);

        if ($violation = TopicContentPolicy::violation($topic->group, $validated['title'], $validated['description'])) {
            return response()->json([
                'success' => false,
                'message' => $violation,
                'errors' => ['title' => [$violation]],
            ], 422);
        }

        $topic->update($validated);

        return response()->json([
            'success' => true,
            'group' => $topic->group,
            'topic' => $topic
        ])->with('success', 'Topic created successfully.');
    }

    /**
     * Delete topic
     */
    public function destroy(Topic $topic)
    {
        if ($topic->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'You cannot delete this topic'
            ], 403);
        }

        $topic->delete();

        return response()->json([
            'success' => true,
            'message' => 'Topic deleted successfully'
        ]);
    }

    /**
     * Requirement #6: Export topic to PDF
     */
    public function exportPdf(Topic $topic)
    {
        // Load all messages
        $topic->load('messages.user');

        // TODO: Use a PDF library like barryvdh/laravel-dompdf
        // For now, return JSON that frontend will convert to PDF

        return response()->json([
            'success' => true,
            'data' => $topic,
            'message' => 'PDF export is processing...'
        ]);
    }
}
