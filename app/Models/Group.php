<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Group extends Model
{
    protected $fillable = [
        'name',
        'description',
        'created_by',
        'max_members',
    ];

    /**
     * User who created the group.
     */
    public function creator(): BelongsTo
    {
        return $this->belongsTo(User::class, 'created_by');
    }

    /**
     * Members of the group.
     */
    public function users(): BelongsToMany
    {
        return $this->belongsToMany(User::class)
                    ->withPivot('joined_at');
                    
    }

    /**
     * Discussion topics.
     */
    public function topics(): HasMany
    {
        return $this->hasMany(Topic::class);
    }

    /**
     * Participation scores.
     */
    public function participationScores(): HasMany
    {
        return $this->hasMany(ParticipationScore::class);
    }

    /**
     * Quizzes belonging to the group.
     */
    public function quizzes(): HasMany
    {
        return $this->hasMany(Quiz::class);
    }
}

