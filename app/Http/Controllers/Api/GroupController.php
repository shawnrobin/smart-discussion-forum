<?php

namespace App\Http\Controllers;

use App\Models\Group;
use Illuminate\Http\Request;
use Illuminate\Http\RedirectResponse;
use Inertia\Inertia;
use Inertia\Response;

class GroupController extends Controller
{
    public function index(Request $request): Response
    {
        $userId = $request->user()->id;

        $groups = Group::with('creator')
            ->withCount([
                'users',
                'topics',
                'users as joined_count' => function ($query) use ($userId) {
                    $query->where('user_id', $userId);
                },
            ])
            ->get()
            ->map(function ($group) {
                $group->joined = $group->joined_count > 0;
                unset($group->joined_count);
                return $group;
            });

        return Inertia::render('GroupList/Index', ['groups' => $groups]);
    }

    public function create(): Response
    {
        return Inertia::render('Groups/Create');
    }

    public function store(Request $request): RedirectResponse
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'description' => 'nullable|string',
            'max_members' => 'nullable|integer|min:1',
        ]);

        $group = Group::create([
            ...$validated,
            'created_by' => $request->user()->id,
        ]);

        $group->users()->attach($request->user()->id, [
            'joined_at' => now(),
        ]);

        return redirect()->route('groups.index')
            ->with('success', 'Group created successfully.');
    }

    public function show(Group $group): Response
    {
        $group->load(['creator', 'users', 'topics']);
        return Inertia::render('Groups/Show', ['group' => $group]);
    }

    public function edit(Group $group): Response
    {
        return Inertia::render('Groups/Edit', ['group' => $group]);
    }

    public function update(Request $request, Group $group): RedirectResponse
    {
        $validated = $request->validate([
            'name' => 'sometimes|required|string|max:255',
            'description' => 'nullable|string',
            'max_members' => 'nullable|integer|min:1',
        ]);

        $group->update($validated);

        return redirect()->route('groups.show', $group)
            ->with('success', 'Group updated successfully.');
    }

    public function destroy(Group $group): RedirectResponse
    {
        $group->delete();
        return redirect()->route('groups.index')
            ->with('success', 'Group deleted successfully.');
    }

    public function join(Request $request, Group $group): RedirectResponse
    {
        if ($group->users()->where('user_id', $request->user()->id)->exists()) {
            return back()->with('error', 'Already a member of this group.');
        }

        $group->users()->attach($request->user()->id, [
            'joined_at' => now(),
        ]);

        return back()->with('success', 'Joined group successfully.');
    }

    public function leave(Request $request, Group $group): RedirectResponse
    {
        $group->users()->detach($request->user()->id);
        return back()->with('success', 'Left group successfully.');
    }
}