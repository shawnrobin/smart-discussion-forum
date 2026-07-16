<?php

namespace App\Http\Controllers\Lecturer;

use App\Http\Controllers\Controller;
use App\Models\Report;
use Illuminate\Http\Request;
use Inertia\Inertia;

class ModerationController extends Controller
{
    /**
     * Display a listing of the pending reports.
     */
    public function index()
    {
        // Fetch reports with status 'pending' and include the user who reported
        $reports = Report::where('status', 'pending')
            ->with('user')
            ->latest()
            ->get();

        return Inertia::render('Lecturer/Moderation', [
            'reports' => $reports
        ]);
    }

    /**
     * Resolve a specific report.
     */
    public function resolve($id)
    {
        $report = Report::findOrFail($id);

        // Update status to resolved
        $report->update([
            'status' => 'resolved',
            'resolved_by' => auth()->id(),
            'resolved_at' => now(),
        ]);

        return redirect()->back()->with('success', 'Report has been marked as resolved.');
    }
}