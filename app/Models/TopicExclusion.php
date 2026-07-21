<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class TopicExclusion extends Model
{
    protected $fillable = ['topic_id', 'excluded_user_id'];

    public function topic(): BelongsTo
    {
        return $this->belongsTo(Topic::class);
    }

    public function excludedUser(): BelongsTo
    {
        return $this->belongsTo(User::class, 'excluded_user_id');
    }
}
