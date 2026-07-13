<?php

namespace App\Http\Controllers\Api;

use App\Models\Topic;
use Illuminate\Http\Request;

class ShareController
{
    /**
     * Requirement #12: Share topic to social media
     */
    public function shareTopic(Request $request, Topic $topic)
    {
        $validated = $request->validate([
            'platform' => 'required|in:twitter,facebook,linkedin',
            'message' => 'nullable|string|max:280'
        ]);

        // Build share URL
        $topicUrl = url("/topics/{$topic->id}");
        $message = $validated['message'] ?? "{$topic->title}\n{$topicUrl}";

        // Build share link based on platform
        $shareLinks = [
            'twitter' => "https://twitter.com/intent/tweet?text=" . urlencode($message),
            'facebook' => "https://www.facebook.com/sharer/sharer.php?u=" . urlencode($topicUrl),
            'linkedin' => "https://www.linkedin.com/sharing/share-offsite/?url=" . urlencode($topicUrl)
        ];

        return response()->json([
            'success' => true,
            'platform' => $validated['platform'],
            'share_url' => $shareLinks[$validated['platform']]
        ]);
    }

    /**
     * Log share action
     */
    public function logShare(Request $request, Topic $topic)
    {
        $validated = $request->validate([
            'platform' => 'required|string'
        ]);

        // Log to database if needed
        \App\Models\ModerationLog::create([
            'action' => 'topic_shared',
            'reason' => "Topic shared to {$validated['platform']}",
            'user_id' => auth()->id()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Share logged successfully'
        ]);
    }
}
