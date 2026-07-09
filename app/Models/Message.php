<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Message extends Model
{
    protected $fillable = [
        'content',
        'topic_id',
        'user_id',
        'parent_message_id',
        'is_answer',
        'likes',
        'flagged_status',
    ];

    protected $casts = [
        'is_answer' => 'boolean',
    ];

    /**
     * Topic the message belongs to.
     */
    public function topic(): BelongsTo
    {
        return $this->belongsTo(Topic::class);
    }

    /**
     * Author of the message.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Parent message (threaded replies).
     */
    public function parent(): BelongsTo
    {
        return $this->belongsTo(Message::class, 'parent_message_id');
    }

    /**
     * Child replies.
     */
    public function replies(): HasMany
    {
        return $this->hasMany(Message::class, 'parent_message_id');
    }

    /**
     * Users excluded from viewing this message.
     */
    public function exclusions(): HasMany
    {
        return $this->hasMany(UserExclusion::class);
    }
}