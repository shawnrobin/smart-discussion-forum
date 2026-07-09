<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Topic extends Model
{
    protected $fillable = [
        'title',
        'description',
        'group_id',
        'user_id',
        'is_question',
        'is_answered',
        'flagged_status',
        'likes',
    ];

    protected $casts = [
        'is_question' => 'boolean',
        'is_answered' => 'boolean',
    ];

    /**
     * Group containing this topic.
     */
    public function group(): BelongsTo
    {
        return $this->belongsTo(Group::class);
    }

    /**
     * User who created the topic.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Replies/messages.
     */
    public function messages(): HasMany
    {
        return $this->hasMany(Message::class);
    }

    /**
     * Topic recommendations.
     */
    public function recommendations(): HasMany
    {
        return $this->hasMany(TopicRecommendation::class);
    }
}