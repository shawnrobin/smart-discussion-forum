<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ModerationSetting extends Model
{
    protected $fillable = [
        'inactivity_days',
        'warning_grace_days',
        'warnings_before_blacklist',
        'blacklist_days',
    ];
}
