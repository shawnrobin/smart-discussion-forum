<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class QuizAnswer extends Model
{
    protected $fillable = [
        'quiz_attempt_id',
        'quiz_question_id',
        'selected_answer',
        'is_correct',
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
            'quiz_attempt_id'
        );
    }

    /**
     * Question being answered.
     */
    public function question(): BelongsTo
    {
        return $this->belongsTo(
            QuizQuestion::class,
            'quiz_question_id'
        );
    }
}