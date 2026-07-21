<?php

namespace App\Events;

use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Broadcasting\PrivateChannel;
use Illuminate\Contracts\Broadcasting\ShouldBroadcastNow;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class LikeUpdated implements ShouldBroadcastNow
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    public function __construct(
        public int $topicId,
        public string $contentType,
        public int $contentId,
        public int $likes,
        public int $userId,
    ) {
    }

    public function broadcastOn(): array
    {
        return [new PrivateChannel('topic.' . $this->topicId)];
    }

    public function broadcastAs(): string
    {
        return 'like.updated';
    }

    public function broadcastWith(): array
    {
        return [
            'content_type' => $this->contentType,
            'content_id' => $this->contentId,
            'likes' => $this->likes,
            'user_id' => $this->userId,
        ];
    }
}
