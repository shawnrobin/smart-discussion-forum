<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
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
    'last_login',
    'remember_token',
    'avatar',
    'bio',
    'course',
    'faculty',
    'year_of_study',
    'phone_number',
   'blacklisted',
   ];



    protected $casts = [
        'email_verified_at' => 'datetime',
        'last_login' => 'datetime',
        'password' => 'hashed',
        'blacklisted' => 'boolean',
        'is_admin'=> 'boolean',
    ];

    public function warnings()
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
     * Users excluded by this user.
     */
    public function exclusions(): HasMany
    {
        return $this->hasMany(UserExclusion::class);
    }

    /**
     * Exclusions where this user was excluded.
     */
    public function excludedBy(): HasMany
    {
        return $this->hasMany(UserExclusion::class, 'excluded_user_id');
    }

    /**
     * User onboarding record.
     */
    public function onboarding(): HasOne
    {
        return $this->hasOne(UserOnboarding::class);
    }

    /**
     * Moderation logs affecting this user.
     */
    public function moderationLogs(): HasMany
    {
        return $this->hasMany(ModerationLog::class);
    }

    /**
     * Moderation actions performed by this user as admin.
     */
    public function moderationActions(): HasMany
    {
        return $this->hasMany(ModerationLog::class, 'admin_id');
    }

    /**
     * Participation scores.
     */
    public function participationScores(): HasMany
    {
        return $this->hasMany(ParticipationScore::class);
    }

    /**
     * Quizzes created by this user.
     */
    public function quizzesCreated(): HasMany
    {
        return $this->hasMany(Quiz::class, 'created_by');
    }

    /**
     * Quiz attempts.
     */
    public function quizAttempts(): HasMany
    {
        return $this->hasMany(QuizAttempt::class);
    }

    /**
     * Notifications.
     */
    public function notifications(): HasMany
    {
        return $this->hasMany(Notification::class);
    }

    /**
     * Offline synchronization queue.
     */
    public function syncQueue(): HasMany
    {
        return $this->hasMany(SyncQueue::class);
    }

    /**
     * Topic recommendations.
     */
    public function recommendations(): HasMany
    {
        return $this->hasMany(TopicRecommendation::class);
    }
    public function settings()
    {
        return $this->hasOne(UserSetting::class);
    }

}