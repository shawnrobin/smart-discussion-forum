<?php

namespace App\Services;

use App\Models\Group;

class TopicContentPolicy
{
    private const BLOCKED_TERMS = ['idiot', 'stupid', 'moron', 'scam', 'spam'];

    public static function violation(Group $group, string $title, string $description): ?string
    {
        $text = strtolower(trim($title . ' ' . $description));

        foreach (self::BLOCKED_TERMS as $term) {
            if (preg_match('/\\b' . preg_quote($term, '/') . '\\b/i', $text)) {
                return 'Your topic contains language that is not allowed in this group.';
            }
        }

        if (preg_match('/(.)\\1{6,}/u', $text) || substr_count($text, 'http') > 2) {
            return 'Your topic appears to be spam. Use clear, relevant discussion content.';
        }

        $keywords = self::keywordsFor($group);
        if ($keywords && !collect($keywords)->contains(fn ($keyword) => str_contains($text, strtolower($keyword)))) {
            return 'This topic does not match the group discussion keywords: ' . implode(', ', $keywords) . '.';
        }

        return null;
    }

    public static function keywordsFor(Group $group): array
    {
        if (!empty($group->topic_keywords)) {
            return array_values(array_filter(array_map('trim', $group->topic_keywords)));
        }

        $source = strtolower($group->name . ' ' . ($group->description ?? ''));
        $stopWords = ['about', 'this', 'that', 'with', 'from', 'group', 'discussion', 'forum', 'and', 'the', 'for'];

        return array_values(array_unique(array_filter(
            preg_split('/[^a-z0-9]+/', $source),
            fn ($word) => strlen($word) >= 4 && !in_array($word, $stopWords, true)
        )));
    }
}
