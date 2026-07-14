<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class UserSetting extends Model
{
    protected $fillable = [
        'user_id',
        'theme',
        'theme_color',
        'notify_replies',
        'notify_mentions',
        'notify_group_posts',
        'notify_quiz',
        'notify_email',
        'is_private',
        'show_online_status',
        'who_can_message',
        'default_sort',
        'posts_per_page',
        'language',
        'auto_save_drafts',
    ];

    protected $casts = [
    'notify_replies' => 'boolean',
    'notify_mentions' => 'boolean',
    'notify_group_posts' => 'boolean',
    'notify_quiz' => 'boolean',
    'notify_email' => 'boolean',
    'is_private' => 'boolean',
    'show_online_status' => 'boolean',
    'auto_save_drafts' => 'boolean',
];

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}