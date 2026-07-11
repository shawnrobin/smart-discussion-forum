<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class OnboardingRules extends Model
{
    protected $table = 'onboarding_rules';

    protected $fillable = [
        'title',
        'content',
        'version',
    ];

    /**
     * Users who accepted this version.
     */
    public function userOnboardings(): HasMany
    {
        return $this->hasMany(
            UserOnboarding::class,
            'rules_version',
            'version'
        );
    }
}