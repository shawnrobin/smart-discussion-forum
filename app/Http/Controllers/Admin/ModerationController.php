<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\ModerationLog;
use App\Models\Report;
use App\Models\Topic;
use Illuminate\Http\Request;
use Inertia\Inertia;

class ModerationController extends Controller
{
    public function index()
    {
        return Inertia::render('Admin/Moderation/Index', [
            'reports' => Report::with(['user:id,name', 'reportable'])
                ->latest()
                ->paginate(30),
        ]);
    }

    public function reportTopic(Request $request, Topic $topic)
    {
        $validated = $request->validate([
            'reason' => 'required|string|min:5|max:1000',
        ]);

        if ($topic->user_id === $request->user()->id) {
            return back()->withErrors(['reason' => 'You cannot report your own topic.']);
        }

        Report::updateOrCreate(
            ['user_id' => $request->user()->id, 'reportable_type' => Topic::class, 'reportable_id' => $topic->id],
            ['reason' => $validated['reason'], 'status' => 'pending', 'resolved_by' => null, 'resolved_at' => null],
        );

        return back()->with('success', 'Thanks. Your report has been sent to the moderation team.');
    }

    public function resolve(Request $request, Report $report)
    {
        $validated = $request->validate([
            'decision' => 'required|in:dismiss,flag,remove',
        ]);

        $content = $report->reportable;
        if (!$content) {
            $report->update([
                'status' => 'dismissed',
                'resolved_by' => $request->user()->id,
                'resolved_at' => now(),
            ]);

            return back()->with('success', 'The report was dismissed because its content no longer exists.');
        }

        $status = match ($validated['decision']) {
            'flag' => 'flagged',
            'remove' => 'removed',
            default => null,
        };

        if ($status) {
            $content->update(['flagged_status' => $status]);
            ModerationLog::create([
                'user_id' => $content->user_id,
                'admin_id' => $request->user()->id,
                'action' => $status === 'removed' ? 'content_removed' : 'content_flagged',
                'reason' => $report->reason,
            ]);
        }

        $report->update([
            'status' => $validated['decision'] === 'dismiss' ? 'dismissed' : 'resolved',
            'resolved_by' => $request->user()->id,
            'resolved_at' => now(),
        ]);

        return back()->with('success', 'Moderation decision saved.');
    }
}
