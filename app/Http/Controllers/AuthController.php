<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\UserOnboarding;
use App\Models\OnboardingRules;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;
use Illuminate\Support\Facades\Auth;
class AuthController
{
    /**
     * Requirement #5: Get onboarding rules
     */
    public function getRules()
    {
        $rules = OnboardingRules::latest('version')->firstOrFail();
        return response()->json(['rules' => $rules]);
    }

    /**
     * Register new user (Requirement #5: Onboarding with rules agreement)
     */
    public function register(Request $request)
{
    $validated = $request->validate([
        'name' => 'required|string|max:255',
        'email' => 'required|email|unique:users,email',
        'username' => 'required|string|min:3|max:100|unique:users,username',
        'password' => 'required|string|min:8|confirmed',
        'rules_version' => 'required|integer|exists:onboarding_rules,version',
        'agreed_to_rules' => 'required|boolean|accepted',
    ]);

    $user = User::create([
        'name' => $validated['name'],
        'email' => $validated['email'],
        'username' => $validated['username'],
        'password' => Hash::make($validated['password']),
        'role' => 'student',
        'status' => 'active',
    ]);

    UserOnboarding::create([
        'user_id' => $user->id,
        'rules_version' => $validated['rules_version'],
        'agreed_at' => now(),
    ]);

    Auth::login($user);

    $request->session()->regenerate();

    return redirect()->route('dashboard');
}

    /**
     * Login user
     */
    public function login(Request $request)
{
    $credentials = $request->validate([
        'email' => 'required|email',
        'password' => 'required|string',
    ]);

    if (!Auth::attempt($credentials)) {
        throw ValidationException::withMessages([
            'email' => ['The provided credentials are incorrect.'],
        ]);
    }

    $request->session()->regenerate();

    /** @var User $user */
    $user = Auth::user();

    if ($user->status === 'blacklisted') {
        Auth::logout();

        return back()->withErrors([
            'email' => 'Your account has been blacklisted.',
        ]);
    }

    $user->update([
        'last_login' => now(),
    ]);

    return redirect()->intended('/dashboard');
}
  /**
     * Get current logged-in user
     */
    public function me(Request $request)
    {
        return response()->json([
            'success' => true,
            'user' => $request->user()
        ]);
    }

    /**
     * Update user profile
     */
    public function updateProfile(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email,' . $request->user()->id,
        ]);

        $request->user()->update($validated);

        return response()->json([
            'success' => true,
            'message' => 'Profile updated successfully',
            'user' => $request->user()
        ]);
    }

    /**
     * Get current user's profile
     */
    public function profile(Request $request)
    {
        return response()->json([
            'success' => true,
            'user' => $request->user()
        ]);
    }

    /**
     * Logout user (delete token)
     */
    public function logout(Request $request)
    {
        Auth::logout();

        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect('/login');
    }

    /**
     * Get user's participation score (Requirement #9)
     */
    public function getParticipationScore(Request $request)
    {
        $scores = $request->user()->participationScores()->with('group')->get();

        return response()->json([
            'success' => true,
            'scores' => $scores
        ]);
    }
}
