<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Discussion;
use App\Models\Report;
use App\Models\ModerationLog;
use Carbon\Carbon;
use Inertia\Inertia;

class DashboardController extends Controller
{
    public function index()
    {
        $stats = [
            'users'       => User::count(),
            'discussions' => class_exists(Discussion::class) ? Discussion::count() : 0,
            'reports'     => class_exists(Report::class) ? Report::where('status', 'pending')->count() : 0,
            'blacklisted' => User::where('blacklisted', true)->count()
        ];

        $recentLogs = ModerationLog::with(['user:id,name', 'admin:id,name'])
            ->latest()
            ->take(10)
            ->get();

        $chartData = [];
        for ($i = 6; $i >= 0; $i--) {
            $date = Carbon::today()->subDays($i);
            $chartData[] = [
                'label' => $date->format('M d'),
                'count' => ModerationLog::whereDate('created_at', $date)->count()
            ];
        }

        return Inertia::render('Admin/Dashboard', [
            'stats'      => $stats,
            'recentLogs' => $recentLogs,
            'chartData'  => $chartData
        ]);
    }
}