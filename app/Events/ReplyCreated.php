<?php

namespace App\Events;

use App\Models\Message;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Broadcasting\PrivateChannel;
use Illuminate\Contracts\Broadcasting\ShouldBroadcastNow;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class ReplyCreated implements ShouldBroadcastNow
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    public function __construct(public Message $message)
    {
    }

    public function broadcastOn(): array
    {
        return [new PrivateChannel('topic.' . $this->message->topic_id)];
    }

    public function broadcastAs(): string
    {
        return 'reply.created';
    }

    public function broadcastWith(): array
    {
        $this->message->loadMissing('user');

        return [
            'reply' => [
                'id' => $this->message->id,
                'user' => [
                    'name' => $this->message->user->name ?? 'Unknown',
                    'initials' => substr($this->message->user->name ?? 'U', 0, 2),
                ],
                'content' => $this->message->content,
                'created_at' => $this->message->created_at->diffForHumans(),
                'likes' => $this->message->likes ?? 0,
                'isAnswer' => $this->message->is_answer,
                'depth' => 0,
                'user_id' => $this->message->user_id,
                'parent_message_id' => $this->message->parent_message_id,
            ],
        ];
    }
}
