<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Group;
use App\Models\Message;
use App\Models\Notification;
use App\Models\Quiz;
use App\Models\Topic;
use App\Services\RecommendationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class DesktopController extends Controller
{
    /**
     * The desktop application's role-aware landing payload. Keeping this in
     * one response makes first load fast and gives the offline client a single
     * durable snapshot to cache.
     */
    public function bootstrap(Request $request): JsonResponse
    {
        $user = $request->user();
        $groupIds = Group::query()
            ->where('created_by', $user->id)
            ->orWhereHas('users', fn ($members) => $members->whereKey($user->id))
            ->pluck('id');

        $topicIds = Topic::whereIn('group_id', $groupIds)->pluck('id');
        $quizzes = Quiz::whereIn('group_id', $groupIds)
            ->where('published', true)
            ->orderByDesc('start_time')
            ->take(10)
            ->get(['id', 'group_id', 'title', 'start_time', 'end_time', 'duration_minutes', 'published'])
            ->map(fn (Quiz $quiz) => [
                ...$quiz->toArray(),
                'status' => now()->lt($quiz->start_time) ? 'upcoming' : (now()->gt($quiz->end_time) ? 'closed' : 'active'),
            ]);

        // ML recommendations — only meaningful for students
        $recommendations = $user->role === 'student'
            ? RecommendationService::getRecommendations($user->id, 6)
                ->map(fn ($r) => [
                    'topic_id'    => $r->topic->id,
                    'title'       => $r->topic->title,
                    'group_id'    => $r->topic->group_id,
                    'group_name'  => $r->topic->group->name ?? '',
                    'author'      => $r->topic->user->name ?? 'Unknown',
                    'replies'     => $r->topic->messages_count ?? 0,
                    'score'       => round((float) $r->score, 1),
                    'is_answered' => $r->topic->is_answered,
                ])
            : [];

        return response()->json([
            'success' => true,
            'user' => $user->only(['id', 'name', 'email', 'role', 'course', 'faculty', 'year_of_study','is_admin']),
            'summary' => [
                'groups'               => $groupIds->count(),
                'topics'               => $topicIds->count(),
                'replies'              => Message::whereIn('topic_id', $topicIds)->count(),
                'unread_notifications' => Notification::where('user_id', $user->id)->where('read', false)->count(),
                'active_quizzes'       => $quizzes->where('status', 'active')->count(),
            ],
            'notifications'   => Notification::where('user_id', $user->id)->latest()->take(10)->get(['id', 'type', 'title', 'message', 'read', 'created_at']),
            'quizzes'         => $quizzes,
            'recommendations' => $recommendations,
            'participation'   => $user->participationScores()
                ->with('group:id,name')
                ->get(['id', 'user_id', 'group_id', 'total_score', 'topics_created', 'topics_replied', 'answers_provided']),
        ]);
    }
}

