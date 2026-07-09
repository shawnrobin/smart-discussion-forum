<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ParticipationScore extends Model
{
    protected $fillable = [
        'user_id',
        'group_id',
        'score',
    ];

    protected $casts = [
        'score' => 'decimal:2',
    ];

    /**
     * User who owns the score.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Group where the score was earned.
     */
    public function group(): BelongsTo
    {
        return $this->belongsTo(Group::class);
    }
}