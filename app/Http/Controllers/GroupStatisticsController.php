<?php

namespace App\Http\Controllers;

use App\Models\Group;
use App\Models\Message;
use App\Models\ParticipationScore;
use App\Models\Report;
use App\Models\Topic;
use App\Models\Quiz;
use App\Models\QuizAttempt;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Inertia\Inertia;

class GroupStatisticsController extends Controller
{
    public function show(Request $request, Group $group)
    {
        $user = $request->user();
        $isAdministrator = in_array($user->role, ['system_admin', 'group_admin'], true);

        abort_unless($isAdministrator || ((int) $group->created_by === (int) $user->id), 403);

        $topicQuery = Topic::where('group_id', $group->id);
        $messageQuery = Message::whereHas('topic', fn ($query) => $query->where('group_id', $group->id));
        $topicIds = (clone $topicQuery)->pluck('id');
        $quizQuery = Quiz::where('group_id', $group->id);
        $quizIds = (clone $quizQuery)->pluck('id');

        $activity = collect(range(6, 0))->map(function ($daysAgo) use ($group) {
            $date = Carbon::today()->subDays($daysAgo);

            return [
                'label' => $date->format('M d'),
                'topics' => Topic::where('group_id', $group->id)->whereDate('created_at', $date)->count(),
                'replies' => Message::whereHas('topic', fn ($query) => $query->where('group_id', $group->id))
                    ->whereDate('created_at', $date)
                    ->count(),
            ];
        })->values();

        return Inertia::render('Groups/Statistics', [
            'group' => [
                'id' => $group->id,
                'name' => $group->name,
                'description' => $group->description,
            ],
            'isAdmin' => $isAdministrator,
            'stats' => [
                'members' => $group->users()->count(),
                'topics' => (clone $topicQuery)->count(),
                'replies' => (clone $messageQuery)->count(),
                'answered_topics' => (clone $topicQuery)->where('is_answered', true)->count(),
                'unanswered_topics' => (clone $topicQuery)->where('is_question', true)->where('is_answered', false)->count(),
                'active_members' => (clone $topicQuery)->where('created_at', '>=', now()->subDays(7))->pluck('user_id')
                    ->merge((clone $messageQuery)->where('created_at', '>=', now()->subDays(7))->pluck('user_id'))
                    ->unique()
                    ->count(),
                'likes' => (clone $topicQuery)->sum('likes') + (clone $messageQuery)->sum('likes'),
                'reports' => Report::where('reportable_type', Topic::class)->whereIn('reportable_id', $topicIds)->where('status', 'pending')->count(),
                'quizzes' => (clone $quizQuery)->count(),
                'quiz_attempts' => QuizAttempt::whereIn('quiz_id', $quizIds)->whereNotNull('submitted_at')->count(),
                'quiz_average' => round((float) QuizAttempt::whereIn('quiz_id', $quizIds)->whereNotNull('submitted_at')->avg('score'), 1),
            ],
            'activity' => $activity,
            'topContributors' => ParticipationScore::where('group_id', $group->id)
                ->with('user:id,name')
                ->orderByDesc('total_score')
                ->take(8)
                ->get()
                ->map(fn ($score) => [
                    'id' => $score->user_id,
                    'name' => $score->user->name ?? 'Unknown member',
                    'score' => (float) $score->total_score,
                    'topics' => $score->topics_created,
                    'replies' => $score->topics_replied,
                    'answers' => $score->answers_provided,
                ]),
            'recentTopics' => (clone $topicQuery)->with('user:id,name')->withCount('messages')->latest()->take(8)->get()->map(fn ($topic) => [
                'id' => $topic->id,
                'title' => $topic->title,
                'author' => $topic->user->name ?? 'Unknown member',
                'replies' => $topic->messages_count,
                'answered' => $topic->is_answered,
                'created_at' => $topic->created_at->diffForHumans(),
            ]),
        ]);
    }
}
