<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Topic;
use App\Models\Message;
use App\Models\Group;
use App\Models\ModerationLog;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class AdminController extends Controller
{
    /**
     * Display Admin Dashboard
     */
    public function dashboard()
    {
        Gate::authorize('manage', User::class);

        $totalUsers = User::count();
        $activeUsers = User::where('last_login', '>=', now()->subMonth())->count();
        // UPDATED: Querying the boolean flag
        $blacklistedUsers = User::where('blacklisted', true)->count();

        $totalGroups = Group::count();
        $totalTopics = Topic::count();
        $totalMessages = Message::count();

        $activityData = [];

        for ($i = 29; $i >= 0; $i--) {
            $date = now()->subDays($i)->toDateString();
            $activityData[] = [
                'date' => $date,
                'count' => Message::whereDate('created_at', $date)->count()
            ];
        }

        return view('admin.dashboard', compact(
            'totalUsers',
            'activeUsers',
            'blacklistedUsers',
            'totalGroups',
            'totalTopics',
            'totalMessages',
            'activityData'
        ));
    }

    /**
     * Statistics page
     */
    public function statistics(Request $request)
    {
        Gate::authorize('manage', User::class);
        $groupId = $request->group_id;
        $query = Topic::query();

        if ($groupId) {
            $query->where('group_id', $groupId);
        }

        $stats = [
            'total_topics' => $query->count(),
            'answered_topics' => (clone $query)->where('is_answered', true)->count(),
            'unanswered_topics' => (clone $query)->where('is_answered', false)->count(),
            'flagged_content' => Topic::where('flagged_status', 'flagged')->count(),
            'removed_content' => Topic::where('flagged_status', 'removed')->count(),
        ];

        return view('admin.statistics', compact('stats'));
    }

    /**
     * Show inactive users
     */
    public function inactiveUsers()
    {
        Gate::authorize('manage', User::class);
        $users = User::where('role', 'student')
            ->where(function ($query) {
                $query->whereNull('last_login')
                      ->orWhere('last_login', '<', now()->subDays(30));
            })
            ->paginate(20);

        return view('admin.inactive-users', compact('users'));
    }

    /**
     * Warn a user
     */
    public function warnUser(Request $request, User $user)
    {
        Gate::authorize('manage', User::class);

        $request->validate([
            'reason' => 'required|string|max:500'
        ]);

        $user->increment('inactive_warnings');

        ModerationLog::create([
            'user_id' => $user->id,
            'action' => 'warning_issued',
            'reason' => $request->reason,
            'admin_id' => $request->user()->id,
        ]);

        return back()->with('success', 'Warning issued successfully.');
    }

    /**
     * Blacklist a user (Boolean flag logic)
     */
    public function blacklistUser(Request $request, User $user)
    {
        Gate::authorize('manage', User::class);

        $request->validate([
            'reason' => 'required|string|max:500'
        ]);

        $user->update(['blacklisted' => true]);

        ModerationLog::create([
            'user_id' => $user->id,
            'action' => 'blacklisted',
            'reason' => $request->reason,
            'admin_id' => $request->user()->id,
        ]);

        return back()->with('success', 'User blacklisted.');
    }

    /**
     * Unblacklist a user
     */
    public function unblacklistUser(Request $request, User $user)
    {
        Gate::authorize('manage', User::class);

        $user->update(['blacklisted' => false]);

        ModerationLog::create([
            'user_id' => $user->id,
            'action' => 'unblacklisted',
            'reason' => 'Admin restored user access.',
            'admin_id' => $request->user()->id,
        ]);

        return back()->with('success', 'User access restored.');
    }

    /**
     * Flag content
     */
    public function flagContent(Request $request)
    {
        Gate::authorize('manage', User::class);
        $request->validate([
            'entity_type' => 'required|in:topic,message',
            'entity_id' => 'required|integer',
            'reason' => 'required'
        ]);

        if ($request->entity_type == 'topic') {
            Topic::findOrFail($request->entity_id)->update(['flagged_status' => 'flagged']);
        } else {
            Message::findOrFail($request->entity_id)->update(['flagged_status' => 'flagged']);
        }

        return back()->with('success', 'Content flagged.');
    }

    /**
     * Remove content
     */
    public function removeContent(Request $request)
    {
        Gate::authorize('manage', User::class);
        $request->validate([
            'entity_type' => 'required|in:topic,message',
            'entity_id' => 'required|integer',
            'reason' => 'required'
        ]);

        if ($request->entity_type == 'topic') {
            Topic::findOrFail($request->entity_id)->update(['flagged_status' => 'removed']);
        } else {
            Message::findOrFail($request->entity_id)->update(['flagged_status' => 'removed']);
        }

        return back()->with('success', 'Content removed.');
    }

    /**
     * Review flagged content
     */
    public function flaggedContent()
    {
        Gate::authorize('manage', User::class);
        $topics = Topic::where('flagged_status', 'flagged')->with('user', 'group')->latest()->paginate(15);
        $messages = Message::where('flagged_status', 'flagged')->with('user', 'topic')->latest()->paginate(15);

        return view('admin.flagged-content', compact('topics', 'messages'));
    }
}