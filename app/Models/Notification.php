<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Notification extends Model
{
    protected $fillable = [
        'user_id',
        'title',
        'message',
        'type',
        'related_entity_id',
        'related_entity_type',
        'read',
    ];

    protected $casts = [
        'read' => 'boolean',
    ];

    protected static function booted(): void
    {
        static::created(fn (Notification $notification) => event(new \App\Events\UserNotificationCreated($notification)));
    }

    /**
     * User receiving notification.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
