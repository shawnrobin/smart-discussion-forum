<?php

namespace App\Services;

use App\Models\ParticipationScore;
use App\Models\Topic;

class ParticipationService
{
    /**
     * Requirement #9: Calculate participation score
     * Points system:
     * - Topic created: 10 points
     * - Reply to topic: 5 points
     * - Answer marked as correct: 20 points
     */
    public static function updateUserScore($userId, $groupId)
    {
        $score = ParticipationScore::firstOrCreate(
            ['user_id' => $userId, 'group_id' => $groupId]
        );

        // Count topics created
        $topicsCreated = Topic::where('user_id', $userId)
            ->where('group_id', $groupId)
            ->count();

        // Count replies
        $repliesCount = \App\Models\Message::where('user_id', $userId)
            ->whereHas('topic', function ($query) use ($groupId) {
                $query->where('group_id', $groupId);
            })
            ->count();

        // Count answers
        $answersCount = \App\Models\Message::where('user_id', $userId)
            ->where('is_answer', true)
            ->whereHas('topic', function ($query) use ($groupId) {
                $query->where('group_id', $groupId);
            })
            ->count();

        // Calculate total score
        $totalScore = ($topicsCreated * 10) + ($repliesCount * 5) + ($answersCount * 20);

        // Update score
        $score->update([
            'topics_created' => $topicsCreated,
            'topics_replied' => $repliesCount,
            'answers_provided' => $answersCount,
            'total_score' => $totalScore
        ]);

        return $score;
    }

    /**
     * Get top users by participation
     */
    public static function getTopUsers($groupId, $limit = 10)
    {
        return ParticipationScore::where('group_id', $groupId)
            ->orderByDesc('total_score')
            ->limit($limit)
            ->with('user')
            ->get();
    }

    /**
     * Get user's global participation score
     */
    public static function getUserGlobalScore($userId)
    {
        $scores = ParticipationScore::where('user_id', $userId)->get();
        return $scores->sum('total_score');
    }
}
