<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

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
        'blacklisted',
        'is_admin',
        'inactive_warnings',
        'last_login',
        'last_communication_at',
        'last_inactivity_warning_at',
        'blacklisted_until',
        'avatar',
        'bio',
        'course',
        'faculty',
        'year_of_study',
        'phone_number',
        'agreed_to_rules', // Added for registration
        'rules_version',   // Added for registration
    ];

    protected $casts = [
        'email_verified_at' => 'datetime',
        'last_login' => 'datetime',
        'last_communication_at' => 'datetime',
        'last_inactivity_warning_at' => 'datetime',
        'blacklisted_until' => 'datetime',
        'password' => 'hashed',
        'is_admin' => 'boolean',
        'agreed_to_rules' => 'boolean', // Cast for registration
    ];

    /**
     * Moderation Warnings
     */
    public function warnings(): HasMany
    {
        return $this->hasMany(Warning::class);
    }

    /**
     * Groups created by this user.
     */
    public function createdGroups(): HasMany
    {
        return $this->hasMany(Group::class, 'created_by');
    }

    /**
     * Groups this user belongs to.
     */
    public function groups(): BelongsToMany
    {
        return $this->belongsToMany(Group::class)
                    ->withPivot('joined_at');
    }

    /**
     * Topics created by the user.
     */
    public function topics(): HasMany
    {
        return $this->hasMany(Topic::class);
    }

    /**
     * Messages posted by the user.
     */
    public function messages(): HasMany
    {
        return $this->hasMany(Message::class);
    }

    /**
     * User exclusions.
     */
    public function exclusions(): HasMany
    {
        return $this->hasMany(UserExclusion::class);
    }

    public function excludedBy(): HasMany
    {
        return $this->hasMany(UserExclusion::class, 'excluded_user_id');
    }

    public function onboarding(): HasOne
    {
        return $this->hasOne(UserOnboarding::class);
    }

    public function moderationLogs(): HasMany
    {
        return $this->hasMany(ModerationLog::class);
    }

    public function moderationActions(): HasMany
    {
        return $this->hasMany(ModerationLog::class, 'admin_id');
    }

    public function participationScores(): HasMany
    {
        return $this->hasMany(ParticipationScore::class);
    }

    public function quizzesCreated(): HasMany
    {
        return $this->hasMany(Quiz::class, 'created_by');
    }

    public function quizAttempts(): HasMany
    {
        return $this->hasMany(QuizAttempt::class);
    }

    public function notifications(): HasMany
    {
        return $this->hasMany(Notification::class);
    }

    public function syncQueue(): HasMany
    {
        return $this->hasMany(SyncQueue::class);
    }

    public function recommendations(): HasMany
    {
        return $this->hasMany(TopicRecommendation::class);
    }

    public function settings(): HasOne
    {
        return $this->hasOne(UserSetting::class);
    }

    public function isAdmin(): bool
    {
        return $this->is_admin === true || in_array($this->role, ['system_admin', 'group_admin', 'lecturer'], true);
    }
}
