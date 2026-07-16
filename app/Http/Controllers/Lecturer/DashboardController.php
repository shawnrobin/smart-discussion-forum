<?php

namespace App\Http\Controllers\Lecturer;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Quiz;
use App\Models\Topic;
use Inertia\Inertia;
use Illuminate\Support\Facades\Auth;

class DashboardController extends Controller
{
    public function index()
    {
        $user = Auth::user();
        
        // Get IDs of groups created by this lecturer to scope the moderation stats
        $myGroupIds = Group::where('created_by', $user->id)->pluck('id');

        $stats = [
            'my_groups' => $myGroupIds->count(),
            'my_quizzes' => Quiz::where('created_by', $user->id)->count(),
            // Only count flags in groups owned by this specific lecturer
            'pending_moderation' => Topic::whereIn('group_id', $myGroupIds)
                                         ->where('flagged_status', 'flagged')
                                         ->count(),
        ];

        // Fetch recent groups with member counts for the UI
        $recent_groups = Group::where('created_by', $user->id)
            ->withCount('users')
            ->latest()
            ->take(5)
            ->get();

        return Inertia::render('Lecturer/Dashboard', [
            'stats' => $stats,
            'recent_groups' => $recent_groups
        ]);
    }
}