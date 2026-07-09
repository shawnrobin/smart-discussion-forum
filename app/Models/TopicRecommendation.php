<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class TopicRecommendation extends Model
{
    protected $fillable = [
        'topic_id',
        'user_id',
        'score',
    ];

    protected $casts = [
        'score' => 'decimal:2',
    ];

    /**
     * Recommended topic.
     */
    public function topic(): BelongsTo
    {
        return $this->belongsTo(Topic::class);
    }

    /**
     * User receiving recommendation.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}