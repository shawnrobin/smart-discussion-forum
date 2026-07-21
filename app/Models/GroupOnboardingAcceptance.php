<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class GroupOnboardingAcceptance extends Model
{
    protected $fillable = ['group_id', 'user_id', 'rules_version', 'agreed_at'];

    protected $casts = ['agreed_at' => 'datetime'];
}
