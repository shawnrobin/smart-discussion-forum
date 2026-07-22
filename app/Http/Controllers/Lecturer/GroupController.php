<?php

namespace App\Http\Controllers\Lecturer;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Topic;
use App\Models\Report;
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
        abort_unless($request->user()->role === 'lecturer', 403, 'Only lecturers can create groups.');

        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'max_members' => 'nullable|integer',
        ]);

        $group = $request->user()->createdGroups()->create($validated);
        $group->users()->syncWithoutDetaching([
            $request->user()->id => ['joined_at' => now()],
        ]);

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
        $topicIds = Topic::whereIn('group_id', $group_ids)->pluck('id');
        
        return Inertia::render('Lecturer/Groups/Moderation', [
            'flaggedTopics' => Topic::whereIn('group_id', $group_ids)
                                     ->where('flagged_status', 'flagged')
                                     ->with(['user', 'group'])
                                     ->get(),
            'reports' => Report::where('reportable_type', Topic::class)
                ->whereIn('reportable_id', $topicIds)
                ->where('status', 'pending')
                ->with('user:id,name')
                ->latest()
                ->get(),
        ]);
    }

    public function resolveReport(Report $report)
    {
        abort_unless($report->reportable_type === Topic::class, 404);
        $topic = Topic::findOrFail($report->reportable_id);
        abort_unless((int) $topic->group->created_by === (int) auth()->id(), 403);

        $report->update([
            'status' => 'resolved',
            'resolved_by' => auth()->id(),
            'resolved_at' => now(),
        ]);

        return back()->with('success', 'Report resolved.');
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
