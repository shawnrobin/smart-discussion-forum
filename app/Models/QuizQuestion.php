<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class QuizQuestion extends Model
{
    protected $fillable = [
    'quiz_id',
    'question_text',
    'question_type',
    'options',
    'correct_answer',
    'points',
    'order_index',
];

protected $casts = [
    'options' => 'array',
];

    /**
     * Quiz containing this question.
     */
    public function quiz(): BelongsTo
    {
        return $this->belongsTo(Quiz::class);
    }

    /**
     * Student answers.
     */
    public function answers(): HasMany
    {
        return $this->hasMany(QuizAnswer::class);
    }
}