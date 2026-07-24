<?php

namespace App\Services;

use App\Models\TopicRecommendation;
use App\Models\Topic;
use App\Models\Message;

class RecommendationService
{
    /**
     * Requirement #11: Recommend topics based on user activity
     * Simple ML: Recommend topics similar to what user engages with
     */
    public static function generateRecommendations($userId)
    {
        $user = \App\Models\User::find($userId);
        
        if (!$user) {
            return;
        }

        // Get topics user has participated in
        $userTopics = Topic::whereHas('messages', function ($query) use ($userId) {
            $query->where('user_id', $userId);
        })->pluck('id')->toArray();

        // Get groups user is member of
        $userGroups = $user->groups()->pluck('groups.id')->toArray();

        // Find similar topics (same group, not yet engaged with)
        $recommendedTopics = Topic::whereIn('group_id', $userGroups)
            ->whereNotIn('id', $userTopics)
            ->where('is_answered', true) // Recommend answered topics
            ->where('flagged_status', '!=', 'removed')
            ->latest()
            ->limit(10)
            ->get();

        // Clear old recommendations
        TopicRecommendation::where('user_id', $userId)->delete();

        // Create new recommendations with score
        foreach ($recommendedTopics as $index => $topic) {
            $score = 100 - ($index * 5); // Decrease score by relevance

            TopicRecommendation::create([
                'user_id' => $userId,
                'topic_id' => $topic->id,
                'score' => $score,
                'reason' => 'Similar to topics you engage with'
            ]);
        }
    }

    /**
     * Get recommendations for user
     */
    public static function getRecommendations($userId, $limit = 5)
    {
        return TopicRecommendation::where('user_id', $userId)
            ->with('topic.user', 'topic.group')
            ->orderByDesc('score')
            ->limit($limit)
            ->get();
    }
}
