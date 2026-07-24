<?php

namespace App\Http\Controllers\Lecturer;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Quiz;
use App\Models\Topic;
use Inertia\Inertia;

class DashboardController extends Controller
{
    public function index()
    {
        $allGroupIds = Group::pluck('id');

        $stats = [
            'my_groups' => $allGroupIds->count(),
            'my_quizzes' => Quiz::count(),
            'pending_moderation' => Topic::whereIn('group_id', $allGroupIds)
                                         ->where('flagged_status', 'flagged')
                                         ->count(),
        ];

        $recent_groups = Group::with('creator:id,name')
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
