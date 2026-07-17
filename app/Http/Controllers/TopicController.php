<?php

namespace App\Http\Controllers;

use App\Models\Group;
use App\Models\ParticipationScore;
use App\Models\Topic;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class TopicController
{
    /**
     * Display all topics in a group.
     */
    public function index(Group $group)
    {
        $topics = $group->topics()
            ->with(['user', 'messages'])
            ->latest()
            ->paginate(10);

        return Inertia::render('Topics/Index', [
            'group' => $group,
            'topics' => $topics,
        ]);
    }

    /**
     * Display a specific topic.
     */
    public function show(Topic $topic)
    {
        $topic->load([
            'user',
            'group',
            'messages.user',
        ]);

        return Inertia::render('Topics/Show', [
            'topic' => $topic,
        ]);
    }

    /**
     * Store a new topic.
     * Requirement #2: Questions can be marked.
     */
    public function store(Request $request, Group $group)
    {
        $validated = $request->validate([
            'title' => 'required|string|max:500',
            'description' => 'required|string|min:2|max:5000',
            'is_question' => 'boolean',
        ]);

        // User must belong to the group
        if (!$group->users()->where('user_id', Auth::id())->exists()) {

            return back()->withErrors([
                'group' => 'You must be a member of this group to create a topic.',
            ]);

        }

        $topic = Topic::create([
            ...$validated,
            'group_id' => $group->id,
            'user_id' => Auth::id(),
        ]);

        /*
        |--------------------------------------------------------------------------
        | Requirement #9
        |--------------------------------------------------------------------------
        */

        $score = ParticipationScore::firstOrCreate(
            [
                'user_id' => Auth::id(),
                'group_id' => $group->id,
            ],
            [
                'topics_created' => 0,
                'topics_replied' => 0,
                'answers_provided' => 0,
                'total_score' => 0,
            ]
        );

        $score->increment('topics_created');
        $score->increment('total_score', 10);

        return redirect()
            ->route('topics.show', $topic)
            ->with('success', 'Topic created successfully.');
    }

    /**
     * Update a topic.
     */
    public function update(Request $request, Topic $topic)
    {
        if (
            $topic->user_id !== Auth::id()
            && !Auth::user()->isAdmin()
        ) {

            abort(403);

        }

        $validated = $request->validate([
            'title' => 'required|string|max:500',
            'description' => 'required|string',
            'is_question' => 'boolean',
        ]);

        $topic->update($validated);

        return back()->with(
            'success',
            'Topic updated successfully.'
        );
    }

    /**
     * Delete a topic.
     */
    public function destroy(Topic $topic)
    {
        if (
            $topic->user_id !== Auth::id()
            && !Auth::user()->isAdmin()
        ) {

            abort(403);

        }

        $group = $topic->group;

        $topic->delete();

        return redirect()
            ->route('groups.show', $group)
            ->with(
                'success',
                'Topic deleted successfully.'
            );
    }

    /**
     * Requirement #6
     * Export discussion as PDF.
     */
    public function exportPdf(Topic $topic)
    {
        $topic->load([
            'user',
            'messages.user',
        ]);

        // TODO:
        // Install barryvdh/laravel-dompdf
        // Generate PDF here.

        return back()->with(
            'success',
            'PDF export will be implemented shortly.'
        );
    }
}