<?php

use Illuminate\Foundation\Inspiring;
use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\Schedule;

Artisan::command('inspire', function () {
    $this->comment(Inspiring::quote());
})->purpose('Display an inspiring quote');

Schedule::command('moderation:process-inactive-users')->daily();
Schedule::command('quizzes:finalize-expired')->everyMinute();

// Regenerate ML recommendations for all active users nightly
Schedule::call(function () {
    \App\Models\User::where('status', 'active')->each(function ($user) {
        \App\Services\RecommendationService::generateRecommendations($user->id);
    });
})->dailyAt('02:00')->name('recommendations:regenerate-all')->withoutOverlapping();
