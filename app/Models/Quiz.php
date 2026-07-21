<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Quiz extends Model
{
    protected $fillable = [
        'group_id',
        'created_by',
        'title',
        'description',
        'start_time',
        'end_time',
        'duration_minutes',
        'shuffle_questions',
        'show_results_after',
        'target_category',
        'published',
    ];

    protected $casts = [
        'start_time' => 'datetime',
        'end_time' => 'datetime',
        'shuffle_questions' => 'boolean',
        'published' => 'boolean',
    ];


    public function group(): BelongsTo
    {
        return $this->belongsTo(Group::class);
    }


    public function creator(): BelongsTo
    {
        return $this->belongsTo(User::class, 'created_by');
    }


    public function questions(): HasMany
    {
        return $this->hasMany(QuizQuestion::class);
    }


    public function attempts(): HasMany
    {
        return $this->hasMany(QuizAttempt::class);
    }
}
