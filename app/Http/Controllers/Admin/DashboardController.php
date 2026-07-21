<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Message;
use App\Models\Quiz;
use App\Models\Topic;
use App\Models\User;
use App\Models\ModerationLog;
use Carbon\Carbon;
use Inertia\Inertia;

class DashboardController extends Controller
{
    /**
     * Display the Admin Dashboard with statistics, logs, and activity charts.
     */
    public function index()
    {
        // 1. Compile Statistics
        $stats = [
            'users'          => User::count(),
            'groups'         => Group::count(),
            'topics'         => Topic::count(),
            'messages'       => Message::count(),
            'quizzes'        => Quiz::count(),
            'flagged_topics' => Topic::where('flagged_status', 'flagged')->count(),
            'blacklisted'    => User::where('blacklisted', true)->count(),
        ];

        // 2. Fetch Recent Moderation Logs
        // Ensure user and admin relationships are defined in ModerationLog model
        $recentLogs = ModerationLog::with(['user:id,name', 'admin:id,name'])
            ->latest()
            ->take(10)
            ->get();

        $recentTopics = Topic::with(['user:id,name', 'group:id,name'])
            ->latest()
            ->take(8)
            ->get();

        $groupSummaries = Group::withCount([
            'users',
            'topics',
            'topics as unanswered_topics_count' => fn ($query) => $query->where('is_question', true)->where('is_answered', false),
        ])
            ->orderByDesc('topics_count')
            ->take(10)
            ->get()
            ->map(fn ($group) => [
                'id' => $group->id,
                'name' => $group->name,
                'members' => $group->users_count,
                'topics' => $group->topics_count,
                'unanswered' => $group->unanswered_topics_count,
            ]);

        // 3. Prepare Chart Data for the last 7 days
        $chartData = [];
        for ($i = 6; $i >= 0; $i--) {
            $date = Carbon::today()->subDays($i);
            $chartData[] = [
                'label' => $date->format('M d'),
                'count' => Topic::whereDate('created_at', $date)->count()
            ];
        }

        // 4. Return to Inertia view
        return Inertia::render('Admin/Dashboard', [
            'stats'      => $stats,
            'recentLogs' => $recentLogs,
            'chartData'     => $chartData,
            'recentTopics'  => $recentTopics,
            'groupSummaries' => $groupSummaries,
        ]);
    }
}
