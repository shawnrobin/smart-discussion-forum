<?php

namespace App\Listeners;

use App\Events\TopicCreated;
use Illuminate\Notifications\Notification as BaseNotification;
use Illuminate\Support\Facades\Notification;

class NotifyTopicCreated
{
    public function handle(TopicCreated $event): void
    {
        $groupMembers = $event->topic->group->users()->get();

        $notification = new class($event->topic, $event->topic->user) extends BaseNotification {
            private $topic;
            private $author;

            public function __construct($topic, $author)
            {
                $this->topic = $topic;
                $this->author = $author;
            }

            public function via($notifiable): array
            {
                return ['database'];
            }

            public function toDatabase($notifiable): array
            {
                return [
                    'type' => 'topic_created',
                    'title' => 'New Topic in ' . $this->topic->group->name,
                    'message' => $this->author->name . ' posted: ' . $this->topic->title,
                    'reference_id' => $this->topic->id,
                    'reference_type' => 'topic',
                ];
            }
        };

        foreach ($groupMembers as $member) {
            if ($member->id !== $event->topic->user_id) {
                Notification::send($member, $notification);
            }
        }
    }
}
