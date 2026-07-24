<?php

use Illuminate\Support\Facades\Broadcast;
use App\Models\Topic;
use App\Models\TopicExclusion;

Broadcast::channel('App.Models.User.{id}', function ($user, $id) {
    return (int) $user->id === (int) $id;
});

Broadcast::channel('topic.{topicId}', function ($user, $topicId) {
    $topic = Topic::find($topicId);

    return $topic
        && !$topic->exclusions()->where('excluded_user_id', $user->id)->exists()
        && ($topic->group->users()->whereKey($user->id)->exists() || (int) $topic->group->created_by === (int) $user->id);
});

Broadcast::channel('group.online.{groupId}', function ($user, $groupId) {
    if ($user->groups()->where('groups.id', $groupId)->exists()) {
        return ['id' => $user->id, 'name' => $user->name];
    }
});
