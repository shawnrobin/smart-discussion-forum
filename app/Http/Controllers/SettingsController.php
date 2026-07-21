<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Inertia\Inertia;
use App\Models\Message;
use App\Models\ParticipationScore;
use App\Models\QuizAttempt;
use App\Models\Topic;
use Carbon\Carbon;

class SettingsController extends Controller
{
    public function index()
    {
        $user = auth()->user();
        $settings = $user->settings ?? $user->settings()->create([]);

        $totalPoints = (float) ParticipationScore::where('user_id', $user->id)->sum('total_score');
        $level = max(1, (int) floor($totalPoints / 100) + 1);

        return Inertia::render('Settings/Index', [
            'user' => $user,
            'settings' => $settings,
            'profileStats' => [
                'totalPoints' => $totalPoints,
                'level' => $level,
                'progress' => (int) ($totalPoints % 100),
                'nextLevelPoints' => 100 - (int) ($totalPoints % 100),
                'badge' => $totalPoints >= 1000 ? 'Gold contributor' : ($totalPoints >= 300 ? 'Active contributor' : 'New contributor'),
                'verifiedAnswers' => (int) ParticipationScore::where('user_id', $user->id)->sum('answers_provided'),
                'streak' => $this->dailyStreak($user->id),
                'topics' => Topic::where('user_id', $user->id)->count(),
                'replies' => Message::where('user_id', $user->id)->count(),
                'quizAverage' => round((float) QuizAttempt::where('user_id', $user->id)->whereNotNull('submitted_at')->avg('score'), 1),
            ],
            'exams' => QuizAttempt::where('user_id', $user->id)->whereNotNull('submitted_at')->with('quiz:id,title')->latest('submitted_at')->take(10)->get()->map(fn ($attempt) => [
                'id' => $attempt->id,
                'title' => $attempt->quiz->title ?? 'Quiz',
                'date' => $attempt->submitted_at->format('d M Y, H:i'),
                'score' => round((float) $attempt->score, 1),
            ]),
        ]);
    }

    private function dailyStreak(int $userId): int
    {
        $dates = Topic::where('user_id', $userId)->pluck('created_at')
            ->merge(Message::where('user_id', $userId)->pluck('created_at'))
            ->map(fn ($date) => Carbon::parse($date)->timezone(config('app.timezone'))->toDateString())
            ->unique()
            ->flip();

        $cursor = Carbon::today(config('app.timezone'));
        if (!$dates->has($cursor->toDateString())) $cursor->subDay();

        $streak = 0;
        while ($dates->has($cursor->toDateString())) {
            $streak++;
            $cursor->subDay();
        }

        return $streak;
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
