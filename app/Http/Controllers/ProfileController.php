<?php

namespace App\Http\Controllers;

use App\Http\Requests\ProfileUpdateRequest;
use App\Models\Message;
use App\Models\ParticipationScore;
use App\Models\QuizAttempt;
use App\Models\Topic;
use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Redirect;
use Inertia\Inertia;
use Inertia\Response;

class ProfileController extends Controller
{
    /**
     * Display the user's profile form.
     */
    public function edit(Request $request): Response
    {
        $user = $request->user();
        $isPrivilegedProfile = $user->is_admin || in_array($user->role, ['lecturer', 'group_admin', 'system_admin'], true);
        $totalPoints = $isPrivilegedProfile ? 0 : (float) ParticipationScore::where('user_id', $user->id)->sum('total_score');

        return Inertia::render('Profile/Edit', [
            'mustVerifyEmail' => $user instanceof MustVerifyEmail,
            'status' => session('status'),
            'isPrivilegedProfile' => $isPrivilegedProfile,
            'stats' => [
                'total_points' => $totalPoints,
                'level' => $isPrivilegedProfile ? 1 : max(1, (int) floor($totalPoints / 100) + 1),
                'progress' => $isPrivilegedProfile ? 0 : (int) ($totalPoints % 100),
                'answers_provided' => $isPrivilegedProfile ? null : (int) ParticipationScore::where('user_id', $user->id)->sum('answers_provided'),
                'topics_created' => Topic::where('user_id', $user->id)->count(),
            ],
        ]);
    }

    /**
     * Update the user's profile information.
     */
    public function update(ProfileUpdateRequest $request): RedirectResponse
    {
        $request->user()->fill($request->validated());

        if ($request->user()->isDirty('email')) {
            $request->user()->email_verified_at = null;
        }

        $request->user()->save();

        return Redirect::route('profile.edit');
    }

    /**
     * Delete the user's account.
     */
    public function destroy(Request $request): RedirectResponse
    {
        $request->validate([
            'password' => ['required', 'current_password'],
        ]);

        $user = $request->user();

        Auth::logout();

        $user->delete();

        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return Redirect::to('/');
    }
}
