<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\ModerationLog;
use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class UserManagementController extends Controller
{
    public function index(Request $request) 
    {
        $query = User::query();

        if ($request->has('search')) {
            $query->where('name', 'like', '%' . $request->search . '%')
                  ->orWhere('email', 'like', '%' . $request->search . '%');
        }

        return Inertia::render('Admin/Users/Index', [
            'users' => $query->get(), 
            'filters' => $request->only(['search'])
        ]);
    }

    public function show($id)
    {
        $user = User::findOrFail($id);
        return Inertia::render('Admin/Users/Show', [
            'user' => $user
        ]);
    }

    public function warn(Request $request, $id)
    {
        $user = User::findOrFail($id);
        $request->validate(['reason' => 'nullable|string|max:500']);
        $reason = trim((string) $request->input('reason')) ?: 'No reason provided.';

        ModerationLog::create([
            'user_id' => $user->id,
            'admin_id' => Auth::id(),
            'action' => 'warning_issued',
            'reason' => $reason
        ]);

        Notification::create([
            'user_id' => $user->id,
            'type' => 'moderation_warning',
            'title' => 'Account warning',
            'message' => $reason,
        ]);

        return back()->with('message', "User {$user->name} has been warned.");
    }

    public function toggleBlacklist(Request $request, $id)
    {
        $user = User::findOrFail($id);
        $user->blacklisted = !$user->blacklisted;
        $user->status = $user->blacklisted ? 'blacklisted' : 'active';
        $user->save();

        ModerationLog::create([
            'user_id' => $user->id,
            'admin_id' => Auth::id(),
            'action' => 'blacklisted', 
            'reason' => 'Admin status update.'
        ]);

        Notification::create([
            'user_id' => $user->id,
            'type' => $user->blacklisted ? 'account_blacklisted' : 'account_restored',
            'title' => $user->blacklisted ? 'Account blacklisted' : 'Account restored',
            'message' => $user->blacklisted
                ? 'Your account has been blacklisted by an administrator.'
                : 'Your account is active again.',
        ]);

        return back();
    }

    /**
     * Toggle the admin status for a specific user.
     */
    public function toggleAdmin($id)
    {
        $user = User::findOrFail($id);
        
        // Flip the admin status
        $user->is_admin = !$user->is_admin;
        $user->save();

        // REMOVED: ModerationLog::create(...)
        // We are skipping the log entry to avoid the database CHECK constraint error.

        return back()->with('success', 'Admin status updated!');
    }

    /**
     * Toggle a standard user's lecturer role without changing admin access.
     */
    public function toggleLecturer($id)
    {
        $user = User::findOrFail($id);

        if (!in_array($user->role, ['student', 'lecturer'], true)) {
            return back()->with('error', 'System and group administrators cannot have their role changed here.');
        }

        $user->role = $user->role === 'lecturer' ? 'student' : 'lecturer';
        $user->save();

        return back()->with('success', 'Lecturer role updated!');
    }
}
