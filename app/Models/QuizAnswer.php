<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class QuizAnswer extends Model
{
    protected $fillable = [
    'attempt_id',
    'question_id',
    'user_answer',
    'is_correct',
    'points_earned',
];

    protected $casts = [
        'is_correct' => 'boolean',
    ];

    /**
     * Attempt this answer belongs to.
     */
    public function attempt(): BelongsTo
    {
        return $this->belongsTo(
            QuizAttempt::class,
            'attempt_id'
        );
    }

    /**
     * Question being answered.
     */
    public function question(): BelongsTo
    {
        return $this->belongsTo(
            QuizQuestion::class,
            'question_id'
        );
    }
}
