<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class UserExclusion extends Model
{
    protected $fillable = [
        'user_id',
        'excluded_user_id',
        'message_id',
    ];

    /**
     * User performing the exclusion.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * User who has been excluded.
     */
    public function excludedUser(): BelongsTo
    {
        return $this->belongsTo(User::class, 'excluded_user_id');
    }

    /**
     * Message the exclusion applies to.
     */
    public function message(): BelongsTo
    {
        return $this->belongsTo(Message::class);
    }
}