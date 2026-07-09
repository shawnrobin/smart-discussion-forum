<?php

namespace App\Http\Controllers\Api;

use App\Models\Topic;
use App\Models\Group;
use App\Models\ParticipationScore;
use Illuminate\Http\Request;

class TopicController
{
    /**
     * Get all topics in a group
     */
    public function index(Request $request, Group $group)
    {
        $topics = $group->topics()
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
        $topic->load('user', 'messages.user', 'group');

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
            'title' => 'required|string|max:500',
            'description' => 'required|string|min:10|max:5000',
            'is_question' => 'boolean',
        ]);

        // Check if user is member of group
        if (!$group->users()->where('user_id', auth()->id())->exists()) {
            return response()->json([
                'success' => false,
                'message' => 'You must be a group member to create topics'
            ], 403);
        }

        // Create topic
        $topic = Topic::create([
            ...$validated,
            'group_id' => $group->id,
            'user_id' => auth()->id()
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
        // Check if user is author or admin
        if ($topic->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'You cannot update this topic'
            ], 403);
        }

        $validated = $request->validate([
            'title' => 'required|string|max:500',
            'description' => 'required|string',
            'is_question' => 'boolean'
        ]);

        $topic->update($validated);

        return response()->json([
            'success' => true,
            'message' => 'Topic updated successfully',
            'topic' => $topic
        ]);
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