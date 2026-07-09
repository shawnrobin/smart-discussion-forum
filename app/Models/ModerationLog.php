<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ModerationLog extends Model
{
    protected $fillable = [
        'admin_id',
        'user_id',
        'action',
        'reason',
    ];

    /**
     * Administrator who performed the action.
     */
    public function admin(): BelongsTo
    {
        return $this->belongsTo(User::class, 'admin_id');
    }

    /**
     * User who received the moderation action.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}