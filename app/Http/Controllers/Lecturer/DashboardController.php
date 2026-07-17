<?php

namespace App\Http\Controllers\Lecturer;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Quiz;
use App\Models\QuizAttempt;
use App\Models\Topic;
use Inertia\Inertia;
use Illuminate\Support\Facades\Auth;

class DashboardController extends Controller
{
    public function index()
    {
        $user = Auth::user();
        
        // Get IDs of groups created by this lecturer
        $myGroupIds = Group::where('created_by', $user->id)->pluck('id');

        $stats = [
            'my_groups' => $myGroupIds->count(),
            'my_quizzes' => Quiz::where('created_by', $user->id)->count(),
            'pending_moderation' => Topic::whereIn('group_id', $myGroupIds)
                                         ->where('flagged_status', 'flagged')
                                         ->count(),
        ];

        // Fetch recent groups with member counts
        $recent_groups = Group::where('created_by', $user->id)
            ->withCount('users')
            ->latest()
            ->take(5)
            ->get();

        // Fetch quizzes with attempt stats
        $quizzes = Quiz::where('created_by', $user->id)
            ->withCount(['attempts' => function ($q) {
                $q->whereNotNull('submitted_at');
            }])
            ->latest()
            ->take(10)
            ->get()
            ->map(function ($quiz) {
                $quiz->status = 'completed';
                if (now()->lt($quiz->start_time)) $quiz->status = 'upcoming';
                elseif (now()->lt($quiz->end_time)) $quiz->status = 'active';
                $quiz->total_attempts = $quiz->attempts_count;
                return $quiz;
            });

        return Inertia::render('Lecturer/Dashboard', [
            'stats' => $stats,
            'recent_groups' => $recent_groups,
            'quizzes' => $quizzes,
        ]);
    }
}