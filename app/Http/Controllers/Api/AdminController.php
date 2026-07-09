<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\ModerationLog;
use App\Models\Topic;
use App\Models\Message;
use App\Models\Group;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class AdminController extends Controller
{
    /**
     * Requirement #7: Get admin dashboard with statistics
     */
    public function dashboard(Request $request)
    {
        Gate::authorize('manage', User::class);

        $totalUsers = User::count();
        $activeUsers = User::where('last_login', '>=', now()->subMonth())->count();
        $blacklistedUsers = User::where('status', 'blacklisted')->count();
        $totalGroups = Group::count();
        $totalTopics = Topic::count();
        $totalMessages = Message::count();

        // Activity for last 30 days
        $activityData = [];
        for ($i = 29; $i >= 0; $i--) {
            $date = now()->subDays($i)->toDateString();
            $count = Message::whereDate('created_at', $date)->count();
            $activityData[] = ['date' => $date, 'count' => $count];
        }

        return response()->json([
            'success' => true,
            'dashboard' => [
                'total_users' => $totalUsers,
                'active_users' => $activeUsers,
                'blacklisted_users' => $blacklistedUsers,
                'total_groups' => $totalGroups,
                'total_topics' => $totalTopics,
                'total_messages' => $totalMessages,
                'activity_chart' => $activityData
            ]
        ]);
    }

    /**
     * Requirement #7: Get detailed statistics for a group
     */
    public function statistics(Request $request)
    {
        Gate::authorize('manage', User::class);

        $groupId = $request->query('group_id');
        
        $query = Topic::query();
        if ($groupId) {
            $query->where('group_id', $groupId);
        }

        $stats = [
            'total_topics' => $query->count(),
            'answered_topics' => $query->where('is_answered', true)->count(),
            'unanswered_topics' => $query->where('is_answered', false)->count(),
            'flagged_content' => Topic::where('flagged_status', 'flagged')->count(),
            'removed_content' => Topic::where('flagged_status', 'removed')->count()
        ];

        return response()->json([
            'success' => true,
            'statistics' => $stats
        ]);
    }

    /**
     * Requirement #4: Get inactive users (haven't posted in X days)
     * These users should receive warnings
     */
    public function getInactiveUsers(Request $request)
    {
         Gate::authorize('manage', User::class);

        $days = $request->query('days', 30); // Default: inactive for 30 days

        $inactiveUsers = User::where('role', 'student')
            ->where(function ($query) use ($days) {
                $query->whereNull('last_login')
                    ->orWhere('last_login', '<', now()->subDays($days));
            })
            ->get()
            ->map(function ($user) {
                return [
                    'id' => $user->id,
                    'name' => $user->name,
                    'email' => $user->email,
                    'last_login' => $user->last_login,
                    'days_inactive' => $user->last_login 
                        ? now()->diffInDays($user->last_login) 
                        : 999,
                    'warning_count' => $user->inactive_warnings,
                    'status' => $user->status
                ];
            });

        return response()->json([
            'success' => true,
            'inactive_users' => $inactiveUsers
        ]);
    }

    /**
     * Requirement #4: Issue warning to inactive user
     * After 2 warnings, user is blacklisted
     */
    public function warnUser(Request $request, User $user)
    {
        Gate::authorize('manage', User::class);

        $validated = $request->validate([
            'reason' => 'required|string|max:500'
        ]);

        // Increment warning count
        $user->increment('inactive_warnings');

        // Log the warning
        ModerationLog::create([
            'user_id' => $user->id,
            'action' => 'warning_issued',
            'reason' => $validated['reason'],
            'admin_id' => $request->user()->id
        ]);

        // If 2 warnings, blacklist user
        if ($user->inactive_warnings >= 2) {
            $this->blacklistUser($request, $user);
            
            return response()->json([
                'success' => true,
                'message' => 'User warned and blacklisted (2 warnings reached)',
                'user' => $user
            ]);
        }

        return response()->json([
            'success' => true,
            'message' => "Warning issued. User has {$user->inactive_warnings}/2 warnings",
            'user' => $user
        ]);
    }

    /**
     * Requirement #4: Blacklist user (disable account)
     */
    public function blacklistUser(Request $request, User $user)
    {
        Gate::authorize('manage', User::class);

        $validated = $request->validate([
            'reason' => 'required|string|max:500',
            'days' => 'nullable|integer|min:1|max:365' // Blacklist duration in days
        ]);

        $user->update(['status' => 'blacklisted']);

        $expiresAt = $validated['days'] 
            ? now()->addDays($validated['days']) 
            : null;

        ModerationLog::create([
            'user_id' => $user->id,
            'action' => 'blacklisted',
            'reason' => $validated['reason'],
            'admin_id' => $request->user()->id,
            'expires_at' => $expiresAt
        ]);

        return response()->json([
            'success' => true,
            'message' => 'User blacklisted successfully',
            'user' => $user
        ]);
    }

    /**
     * Requirement #1: Flag content as inappropriate
     */
    public function flagContent(Request $request)
    {
        Gate::authorize('manage', User::class);

        $validated = $request->validate([
            'entity_type' => 'required|in:topic,message',
            'entity_id' => 'required|integer',
            'reason' => 'required|string|max:500'
        ]);

        if ($validated['entity_type'] === 'topic') {
            $topic = Topic::findOrFail($validated['entity_id']);
            $topic->update(['flagged_status' => 'flagged']);
        } else {
            $message = Message::findOrFail($validated['entity_id']);
            $message->update(['flagged_status' => 'flagged']);
        }

        // Log the action
        ModerationLog::create([
            'action' => 'content_flagged',
            'reason' => $validated['reason'],
            'admin_id' => $request->user()->id
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Content flagged for review'
        ]);
    }

    /**
     * Requirement #1: Remove inappropriate content
     */
    public function removeContent(Request $request)
    {
        Gate ::authorize('manage', User::class);

        $validated = $request->validate([
            'entity_type' => 'required|in:topic,message',
            'entity_id' => 'required|integer',
            'reason' => 'required|string|max:500'
        ]);

        if ($validated['entity_type'] === 'topic') {
            $topic = Topic::findOrFail($validated['entity_id']);
            $topic->update(['flagged_status' => 'removed']);
            $user = $topic->user;
        } else {
            $message = Message::findOrFail($validated['entity_id']);
            $message->update(['flagged_status' => 'removed']);
            $user = $message->user;
        }

        // Log the action
        ModerationLog::create([
            'action' => 'content_removed',
            'reason' => $validated['reason'],
            'admin_id' => $request->user()->id,
            'user_id' => $user->id
        ]);

        // Issue warning to user
        $user->increment('inactive_warnings');

        return response()->json([
            'success' => true,
            'message' => 'Content removed and user warned'
        ]);
    }

    /**
     * Get flagged content for review
     */
    public function getFlaggedContent(Request $request)
    {
        Gate ::authorize('manage', User::class);

        $flaggedTopics = Topic::where('flagged_status', 'flagged')
            ->with('user', 'group')
            ->latest()
            ->get();

        $flaggedMessages = Message::where('flagged_status', 'flagged')
            ->with('user', 'topic')
            ->latest()
            ->get();

        return response()->json([
            'success' => true,
            'flagged_topics' => $flaggedTopics,
            'flagged_messages' => $flaggedMessages
        ]);
    }
}
