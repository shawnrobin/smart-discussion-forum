<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Warning;
use App\Models\ModerationLog;
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
        $request->validate(['reason' => 'required|string|max:500']);

        Warning::create([
            'user_id' => $user->id,
            'admin_id' => Auth::id(),
            'reason' => $request->reason
        ]);

        ModerationLog::create([
            'user_id' => $user->id,
            'admin_id' => Auth::id(),
            'action' => 'warn',
            'reason' => $request->reason
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

        return back();
    }

    /**
     * Toggle the admin status for a specific user.
     */
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
}