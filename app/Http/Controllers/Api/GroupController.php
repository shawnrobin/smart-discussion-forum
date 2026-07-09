<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Group;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class GroupController extends Controller
{
    /**
     * List all groups.
     */
    public function index(): JsonResponse
    {
        $groups = Group::with('creator')->withCount('users', 'topics')->get();

        return response()->json($groups);
    }

    /**
     * Create a new group.
     */
    public function store(Request $request): JsonResponse
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

        // Automatically add the creator as a member
        $group->users()->attach($request->user()->id, [
            'joined_at' => now(),
        ]);

        return response()->json($group->load('creator'), 201);
    }

    /**
     * Show a single group with its relationships.
     */
    public function show(Group $group): JsonResponse
    {
        $group->load(['creator', 'users', 'topics']);

        return response()->json($group);
    }

    /**
     * Update a group.
     */
    public function update(Request $request, Group $group): JsonResponse
    {
        $validated = $request->validate([
            'name' => 'sometimes|required|string|max:255',
            'description' => 'nullable|string',
            'max_members' => 'nullable|integer|min:1',
        ]);

        $group->update($validated);

        return response()->json($group);
    }

    /**
     * Delete a group.
     */
    public function destroy(Group $group): JsonResponse
    {
        $group->delete();

        return response()->json(['message' => 'Group deleted successfully']);
    }

    /**
     * Join a group.
     */
    public function join(Request $request, Group $group): JsonResponse
    {
        if ($group->users()->where('user_id', $request->user()->id)->exists()) {
            return response()->json(['message' => 'Already a member of this group'], 409);
        }

        $group->users()->attach($request->user()->id, [
            'joined_at' => now(),
        ]);

        return response()->json(['message' => 'Joined group successfully']);
    }

    /**
     * Leave a group.
     */
    public function leave(Request $request, Group $group): JsonResponse
    {
        $group->users()->detach($request->user()->id);

        return response()->json(['message' => 'Left group successfully']);
    }
}