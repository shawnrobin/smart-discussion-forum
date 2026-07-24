<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\GroupOnboardingAcceptance;
use App\Models\OnboardingRules;
use Illuminate\Http\Request;
use Illuminate\Http\RedirectResponse;
use Inertia\Inertia;
use Inertia\Response;

class GroupController extends Controller
{
    /**
     * Desktop clients only receive groups they can actually open for chat.
     * This stays separate from the web group directory, which also lists
     * groups a student may choose to join.
     */
    public function desktopIndex(Request $request)
    {
        $user = $request->user();

        $groups = Group::query()
            ->where(function ($query) use ($user) {
                $query->where('created_by', $user->id)
                    ->orWhereHas('users', fn ($members) => $members->whereKey($user->id));
            })
            ->withCount(['users', 'topics'])
            ->orderBy('name')
            ->get(['id', 'name', 'description', 'created_by'])
            ->map(fn (Group $group) => [
                'id' => $group->id,
                'name' => $group->name,
                'description' => $group->description,
                'members' => $group->users_count,
                'topics' => $group->topics_count,
            ]);

        return response()->json(['success' => true, 'groups' => $groups]);
    }

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

        return Inertia::render('GroupList/Index', [
            'groups' => $groups,
            'rules' => OnboardingRules::latest('version')->firstOrFail(),
        ]);
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
            'topic_keywords' => 'nullable|array|max:20',
            'topic_keywords.*' => 'string|max:50',
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
            'topic_keywords' => 'nullable|array|max:20',
            'topic_keywords.*' => 'string|max:50',
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

        $validated = $request->validate([
            'rules_version' => 'required|integer|exists:onboarding_rules,version',
            'agreed_to_rules' => 'accepted',
        ]);

        GroupOnboardingAcceptance::updateOrCreate(
            ['group_id' => $group->id, 'user_id' => $request->user()->id],
            ['rules_version' => $validated['rules_version'], 'agreed_at' => now()],
        );

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
