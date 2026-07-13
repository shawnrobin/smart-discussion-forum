<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Inertia\Inertia;

class SettingsController extends Controller
{
    public function index()
    {
        $user = auth()->user();
        $settings = $user->settings ?? $user->settings()->create([]);

        return Inertia::render('Settings/Index', [
            'user' => $user,
            'settings' => $settings,
        ]);
    }

    public function updateProfile(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'username' => 'required|string|max:255',
            'bio' => 'nullable|string|max:1000',
            'course' => 'nullable|string|max:255',
            'faculty' => 'nullable|string|max:255',
            'year_of_study' => 'nullable|integer',
        ]);

        auth()->user()->update($request->only([
            'name', 'username', 'bio', 'course', 'faculty', 'year_of_study',
        ]));

        return back()->with('success', 'Profile updated.');
    }

    public function updateSettings(Request $request)
    {
        $user = auth()->user();
        $settings = $user->settings ?? $user->settings()->create([]);

        $settings->update($request->all());

        return back()->with('success', 'Settings updated.');
    }

    public function updateAccount(Request $request)
{
    $user = auth()->user();

    $request->validate([
        'email' => 'required|email|unique:users,email,' . $user->id,
        'phone_number' => 'nullable|string|max:20',
        'current_password' => 'nullable|required_with:new_password|current_password',
        'new_password' => 'nullable|min:8|confirmed',
    ]);

    $user->update($request->only(['email', 'phone_number']));

    if ($request->filled('new_password')) {
        $user->update(['password' => bcrypt($request->new_password)]);
    }

    return back()->with('success', 'Account updated.');
}

public function deactivateAccount(Request $request)
{
    $request->validate(['password' => 'required|current_password']);

    auth()->user()->update(['status' => 'inactive']);

    auth()->logout();
    $request->session()->invalidate();
    $request->session()->regenerateToken();

    return redirect('/login')->with('success', 'Account deactivated.');
}

public function deleteAccount(Request $request)
{
    $request->validate(['password' => 'required|current_password']);

    $user = auth()->user();
    auth()->logout();
    $user->delete();

    $request->session()->invalidate();
    $request->session()->regenerateToken();

    return redirect('/login')->with('success', 'Account deleted.');
}


}