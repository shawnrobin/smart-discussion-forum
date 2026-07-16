<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class ModerationLog extends Model
{
    use HasFactory;

    // Explicitly mapping to the table to bypass migration tracking
    protected $table = 'moderation_logs';

    protected $fillable = ['user_id', 'admin_id', 'action', 'reason'];

    // The user who received the moderation action
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    // The admin who performed the action
    public function admin()
    {
        return $this->belongsTo(User::class, 'admin_id');
    }
}