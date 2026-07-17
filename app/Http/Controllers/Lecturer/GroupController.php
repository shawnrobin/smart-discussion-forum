<?php

namespace App\Http\Controllers\Lecturer;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Topic;
use Illuminate\Http\Request;
use Inertia\Inertia;

class GroupController extends Controller
{
    public function index()
    {
        return Inertia::render('Lecturer/Groups/Index', [
            'groups' => Group::where('created_by', auth()->id())->withCount('users')->get()
        ]);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'max_members' => 'nullable|integer',
        ]);

        $request->user()->createdGroups()->create($validated);

        return back()->with('success', 'Group created successfully.');
    }

    public function show($id)
    {
        $group = Group::withCount('users')->findOrFail($id);

        return Inertia::render('Lecturer/Groups/Show', [
            'group' => $group,
            'members' => $group->users
        ]);
    }

    public function moderation()
    {
        $group_ids = auth()->user()->createdGroups()->pluck('id');
        
        $flagged_topics = Topic::whereIn('group_id', $group_ids)
                                 ->where('flagged_status', 'flagged')
                                 ->with(['user', 'group'])
                                 ->get();

        $reports = $flagged_topics->map(function ($topic) {
            return [
                'id' => $topic->id,
                'user' => ['name' => $topic->user?->name ?? 'Anonymous'],
                'reason' => 'Flagged topic: ' . $topic->title,
                'type' => 'topic',
                'created_at' => $topic->created_at,
            ];
        });

        return Inertia::render('Lecturer/Moderation', [
            'reports' => $reports
        ]);
    }

    public function destroy(Group $group)
    {
        if ((int)$group->created_by !== (int)auth()->id()) {
            abort(403);
        }

        $group->delete();

        return redirect()->back()->with('success', 'Group deleted successfully.');
    }
}