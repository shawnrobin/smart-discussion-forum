<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class UserOnboarding extends Model
{
    protected $table = 'user_onboarding';

    protected $fillable = [
        'user_id',
        'rules_version',
        'agreed_at',
    ];

    protected $casts = [
        'agreed_at' => 'datetime',
    ];

    /**
     * User who accepted the rules.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Rules version accepted.
     */
    public function rules(): BelongsTo
    {
        return $this->belongsTo(
            OnboardingRules::class,
            'rules_version',
            'version'
        );
    }
}