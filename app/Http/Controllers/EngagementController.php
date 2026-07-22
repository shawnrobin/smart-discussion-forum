<?php

namespace App\Http\Controllers;

use App\Events\LikeUpdated;
use App\Models\ContentLike;
use App\Models\Message;
use App\Models\Topic;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class EngagementController extends Controller
{
    public function toggleTopicLike(Request $request, Topic $topic)
    {
        $this->ensureMember($request, $topic);

        return $this->toggle($request, $topic, 'topic');
    }

    public function toggleMessageLike(Request $request, Message $message)
    {
        $topic = $message->topic;
        $this->ensureMember($request, $topic);

        return $this->toggle($request, $message, 'message');
    }

    private function ensureMember(Request $request, Topic $topic): void
    {
        abort_unless(Topic::whereKey($topic->id)->visibleTo($request->user()->id)->exists(), 404);
        $group = $topic->group;
        abort_unless(
            $group->users()->whereKey($request->user()->id)->exists()
            || (int) $group->created_by === (int) $request->user()->id,
            403,
        );
    }

    private function toggle(Request $request, Topic|Message $content, string $type)
    {
        $liked = false;

        DB::transaction(function () use ($request, $content, $type, &$liked) {
            $attributes = ['user_id' => $request->user()->id, $type . '_id' => $content->id];
            $like = ContentLike::where($attributes)->first();

            if ($like) {
                $like->delete();
                $content->decrement('likes');
            } else {
                ContentLike::create($attributes);
                $content->increment('likes');
                $liked = true;
            }

            $content->refresh();
        });

        event(new LikeUpdated(
            topicId: $content instanceof Topic ? $content->id : $content->topic_id,
            contentType: $type,
            contentId: $content->id,
            likes: $content->likes,
            userId: $request->user()->id,
        ));

        return response()->json([
            'liked' => $liked,
            'likes' => $content->likes,
        ]);
    }
}
