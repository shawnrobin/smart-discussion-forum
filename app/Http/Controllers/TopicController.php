<?php

namespace App\Http\Controllers;

use App\Models\Group;
use App\Models\ParticipationScore;
use App\Models\Topic;
use App\Models\TopicExclusion;
use App\Services\TopicContentPolicy;
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
        abort_unless($group->users()->whereKey(Auth::id())->exists() || (int) $group->created_by === (int) Auth::id(), 403);
        $topics = $group->topics()
            ->visibleTo(Auth::id())
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
        abort_unless(Topic::whereKey($topic->id)->visibleTo(Auth::id())->exists(), 404);
        abort_unless($topic->group->users()->whereKey(Auth::id())->exists() || (int) $topic->group->created_by === (int) Auth::id(), 403);
        $topic->load(['user', 'group']);
        $topic->setRelation('messages', $topic->messages()
            ->visibleTo(Auth::id())
            ->with('user')
            ->get());

        return Inertia::render('Topics/Show', [
            'topic' => $topic,
            'group' => $topic->group,
            
        ]);
    }

    /**
     * Store a new topic.
     * Requirement #2: Questions can be marked.
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

        // User must belong to the group
        $isMember = $group->users()->where('user_id', Auth::id())->exists()
            || (int) $group->created_by === (int) Auth::id();

        if (!$isMember) {

            return back()->withErrors([
                'group' => 'You must be a member of this group to create a topic.',
            ]);

        }

        if ($violation = TopicContentPolicy::violation($group, $validated['title'], $validated['description'])) {
            return back()->withErrors(['title' => $violation]);
        }

        $selectedUsers = collect($validated['selected_users'] ?? [])->map(fn ($id) => (int) $id)->unique()->values();
        if ($selectedUsers->isNotEmpty() && $group->users()->whereIn('users.id', $selectedUsers)->count() !== $selectedUsers->count()) {
            return back()->withErrors(['selected_users' => 'You can only choose members of this group.']);
        }

        $topic = Topic::create([
            ...$validated,
            'group_id' => $group->id,
            'user_id' => Auth::id(),
        ]);

        $visibility = $validated['visibility'] ?? 'all';
        $excludedUsers = $visibility === 'selected'
            ? $group->users()->whereNotIn('users.id', $selectedUsers->push(Auth::id())->unique())->pluck('users.id')
            : ($visibility === 'exclude' ? $selectedUsers->reject(fn ($id) => $id === Auth::id()) : collect());

        if ($excludedUsers->isNotEmpty()) {
            TopicExclusion::insert($excludedUsers->map(fn ($id) => [
                'topic_id' => $topic->id,
                'excluded_user_id' => $id,
                'created_at' => now(),
                'updated_at' => now(),
            ])->all());
        }

        Auth::user()->update([
            'last_communication_at' => now(),
            'inactive_warnings' => 0,
            'last_inactivity_warning_at' => null,
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
    ->route('topics.show', [
        'group' => $group,
        'topic' => $topic,
    ])
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
            'title' => 'required|string|min:8|max:150',
            'description' => 'required|string|min:30|max:5000',
            'is_question' => 'boolean',
        ]);

        if ($violation = TopicContentPolicy::violation($topic->group, $validated['title'], $validated['description'])) {
            return back()->withErrors(['title' => $violation]);
        }

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