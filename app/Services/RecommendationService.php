<?php

namespace App\Services;

use App\Models\Topic;
use App\Models\TopicRecommendation;
use App\Models\User;

/**
 * ML-style topic recommendation using TF-IDF + cosine similarity.
 *
 * Pipeline:
 *  1. Build a corpus of all topics the user's groups contain.
 *  2. Compute TF-IDF vectors for each topic (title + description).
 *  3. Build a user interest vector by averaging the vectors of topics
 *     the user has engaged with (created or replied to).
 *  4. Rank candidate topics by cosine similarity to the user vector.
 *  5. Persist top-N results in topic_recommendations.
 */
class RecommendationService
{
    private const TOP_N          = 10;
    private const MIN_SIMILARITY = 0.05;    // ignore near-zero matches
    private const STOP_WORDS     = [
        'a','an','the','and','or','but','in','on','at','to','for','of','with',
        'is','are','was','were','be','been','being','have','has','had','do',
        'does','did','this','that','these','those','it','its','i','we','you',
        'he','she','they','what','which','who','how','when','where','why',
        'will','would','can','could','should','may','might','must','shall',
        'not','no','so','as','by','from','about','into','through','during',
    ];

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    public static function generateRecommendations(int $userId): void
    {
        $user = User::find($userId);
        if (!$user) return;

        $groupIds   = $user->groups()->pluck('groups.id')->toArray();
        if (empty($groupIds)) return;

        // Topics the user has already engaged with
        $engagedIds = Topic::where('user_id', $userId)
            ->orWhereHas('messages', fn ($q) => $q->where('user_id', $userId))
            ->whereIn('group_id', $groupIds)
            ->pluck('id')
            ->toArray();

        // All visible topics in the user's groups
        $corpus = Topic::whereIn('group_id', $groupIds)
            ->where('flagged_status', '!=', 'removed')
            ->get(['id', 'title', 'description', 'group_id']);

        if ($corpus->isEmpty()) return;

        // Build TF-IDF
        [$tfidfMatrix, $vocab] = self::buildTfIdf($corpus);

        if (empty($vocab)) return;

        // User interest vector = mean of engaged-topic vectors
        $userVector = self::buildUserVector($tfidfMatrix, $engagedIds);

        // Score candidates (topics not yet engaged with)
        $scores = [];
        foreach ($corpus as $topic) {
            if (in_array($topic->id, $engagedIds, true)) continue;
            $vec = $tfidfMatrix[$topic->id] ?? [];
            $sim = self::cosineSimilarity($userVector, $vec, $vocab);
            if ($sim >= self::MIN_SIMILARITY) {
                $scores[$topic->id] = $sim;
            }
        }

        // Fall back to recency if no engagement history yet
        if (empty($scores) || empty($engagedIds)) {
            $corpus->whereNotIn('id', $engagedIds)
                ->sortByDesc('created_at')
                ->take(self::TOP_N)
                ->each(function ($t, $i) use (&$scores) {
                    $scores[$t->id] = 1.0 - ($i * 0.05);
                });
        }

        arsort($scores);
        $topScores = array_slice($scores, 0, self::TOP_N, true);

        // Persist
        TopicRecommendation::where('user_id', $userId)->delete();

        foreach ($topScores as $topicId => $score) {
            TopicRecommendation::create([
                'user_id'  => $userId,
                'topic_id' => $topicId,
                'score'    => round($score * 100, 2),   // store as 0-100
            ]);
        }
    }

    public static function getRecommendations(int $userId, int $limit = 6): \Illuminate\Support\Collection
    {
        $recs = TopicRecommendation::where('user_id', $userId)
            ->with(['topic' => fn ($q) => $q->with('user:id,name', 'group:id,name')->withCount('messages')])
            ->orderByDesc('score')
            ->limit($limit)
            ->get();

        // If empty, generate on the fly (first visit) then re-fetch
        if ($recs->isEmpty()) {
            static::generateRecommendations($userId);
            $recs = TopicRecommendation::where('user_id', $userId)
                ->with(['topic' => fn ($q) => $q->with('user:id,name', 'group:id,name')->withCount('messages')])
                ->orderByDesc('score')
                ->limit($limit)
                ->get();
        }

        return $recs->filter(fn ($r) => $r->topic !== null)->values();
    }

    // ----------------------------------------------------------------
    // TF-IDF helpers
    // ----------------------------------------------------------------

    /**
     * Returns [$tfidfMatrix, $vocab]:
     *   $tfidfMatrix[topicId][term] = tfidf_weight
     *   $vocab = array of all unique terms
     */
    private static function buildTfIdf(\Illuminate\Support\Collection $corpus): array
    {
        $N       = $corpus->count();
        $tf      = [];  // $tf[topicId][term] = count
        $df      = [];  // $df[term] = number of docs containing it

        foreach ($corpus as $topic) {
            $tokens = self::tokenize($topic->title . ' ' . ($topic->description ?? ''));
            $counts = array_count_values($tokens);
            $total  = max(array_sum($counts), 1);

            $tf[$topic->id] = [];
            foreach ($counts as $term => $count) {
                $tf[$topic->id][$term] = $count / $total;        // normalized TF
                $df[$term] = ($df[$term] ?? 0) + 1;
            }
        }

        // IDF with smoothing: log((N+1)/(df+1)) + 1
        $idf = [];
        foreach ($df as $term => $count) {
            $idf[$term] = log(($N + 1) / ($count + 1)) + 1;
        }

        $vocab   = array_keys($idf);
        $matrix  = [];
        foreach ($corpus as $topic) {
            $matrix[$topic->id] = [];
            foreach ($tf[$topic->id] ?? [] as $term => $tfVal) {
                $matrix[$topic->id][$term] = $tfVal * ($idf[$term] ?? 1);
            }
        }

        return [$matrix, $vocab];
    }

    /** Mean vector of engaged topics; zero vector if no engagement. */
    private static function buildUserVector(array $matrix, array $engagedIds): array
    {
        if (empty($engagedIds)) return [];

        $sum   = [];
        $count = 0;
        foreach ($engagedIds as $id) {
            if (!isset($matrix[$id])) continue;
            foreach ($matrix[$id] as $term => $w) {
                $sum[$term] = ($sum[$term] ?? 0) + $w;
            }
            $count++;
        }

        if ($count === 0) return [];

        return array_map(fn ($w) => $w / $count, $sum);
    }

    /** Cosine similarity between two sparse term vectors. */
    private static function cosineSimilarity(array $a, array $b, array $vocab): float
    {
        if (empty($a) || empty($b)) return 0.0;

        $dot  = 0.0;
        $normA = 0.0;
        $normB = 0.0;

        // Use union of keys for sparse computation
        $allKeys = array_unique(array_merge(array_keys($a), array_keys($b)));
        foreach ($allKeys as $k) {
            $va = $a[$k] ?? 0.0;
            $vb = $b[$k] ?? 0.0;
            $dot   += $va * $vb;
            $normA += $va * $va;
            $normB += $vb * $vb;
        }

        $denom = sqrt($normA) * sqrt($normB);
        return $denom > 0 ? $dot / $denom : 0.0;
    }

    /** Lowercase, remove punctuation, split, strip stop-words, stem lightly. */
    private static function tokenize(string $text): array
    {
        $text   = strtolower($text);
        $text   = preg_replace('/[^a-z0-9\s]/', ' ', $text);
        $words  = preg_split('/\s+/', trim($text), -1, PREG_SPLIT_NO_EMPTY);

        return array_values(array_filter($words, function ($w) {
            return strlen($w) >= 3 && !in_array($w, self::STOP_WORDS, true);
        }));
    }
}
