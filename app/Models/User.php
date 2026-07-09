<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;
use App\Models\Group;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = [
        'name',
        'email',
        'username',
        'password',
        'role',
        'status',
    ];

    protected $hidden = [
        'password',
    ];

    // Relationships
    public function groups()
    {
        return $this->belongsToMany(Group::class, 'group_user');
    }

    public function topics()
    {
        return $this->hasMany(Topic::class);
    }

    public function messages()
    {
        return $this->hasMany(Message::class);
    }

    public function exclusions()
    {
        return $this->hasMany(UserExclusion::class, 'excluded_user_id');
    }

    public function createdGroups()
    {
        return $this->hasMany(Group::class, 'created_by');
    }

    public function participationScores()
    {
        return $this->hasMany(ParticipationScore::class);
    }

    // Check if user is admin
    public function isAdmin()
    {
        return in_array($this->role, ['system_admin', 'group_admin']);
    }

    // Check if user is lecturer
    public function isLecturer()
    {
        return $this->role === 'lecturer';
    }

    // Check if user is blacklisted
    public function isBlacklisted()
    {
        return $this->status === 'blacklisted';
    }
}

