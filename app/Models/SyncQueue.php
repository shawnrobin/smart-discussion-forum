<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class SyncQueue extends Model
{
    protected $table = 'sync_queue';

    protected $fillable = [
        'user_id',
        'action',
        'table_name',
        'record_id',
        'payload',
        'status',
    ];

    protected $casts = [
        'payload' => 'array',
    ];

    /**
     * User who initiated the sync.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}