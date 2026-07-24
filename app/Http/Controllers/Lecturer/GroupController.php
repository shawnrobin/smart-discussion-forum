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
            'groups' => Group::with('creator:id,name')->withCount('users')->latest()->get(),
        ]);
    }

    public function store(Request $request)
    {
        abort_unless(in_array($request->user()->role, ['lecturer', 'system_admin'], true), 403, 'Only lecturers can create groups.');

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
        $group = Group::with([
            'users:id,name',
            'quizzes.creator:id,name',
        ])->withCount('users')->findOrFail($id);

        return Inertia::render('Lecturer/Groups/Show', [
            'group' => $group,
            'members' => $group->users
        ]);
    }

    public function moderation()
    {
        $topicIds = Topic::pluck('id');
        
        return Inertia::render('Lecturer/Groups/Moderation', [
            'flaggedTopics' => Topic::where('flagged_status', 'flagged')
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
        $report->update([
            'status' => 'resolved',
            'resolved_by' => auth()->id(),
            'resolved_at' => now(),
        ]);

        return back()->with('success', 'Report resolved.');
    }

    public function destroy(Group $group)
    {
        $group->delete();

        return redirect()->back()->with('success', 'Group deleted successfully.');
    }
}
