<?php

namespace App\Console\Commands;

use App\Models\ModerationSetting;
use App\Models\Notification;
use App\Models\User;
use Illuminate\Console\Command;

class ProcessInactiveUsers extends Command
{
    protected $signature = 'moderation:process-inactive-users';
    protected $description = 'Issue inactivity warnings, apply timed blacklists, and restore expired accounts.';

    public function handle(): int
    {
        $settings = ModerationSetting::firstOrCreate([], [
            'inactivity_days' => 30,
            'warning_grace_days' => 7,
            'warnings_before_blacklist' => 2,
            'blacklist_days' => 30,
        ]);
        $now = now();

        $restored = User::where('status', 'blacklisted')
            ->whereNotNull('blacklisted_until')
            ->where('blacklisted_until', '<=', $now)
            ->update([
                'status' => 'active',
                'blacklisted' => false,
                'blacklisted_until' => null,
                'inactive_warnings' => 0,
                'last_inactivity_warning_at' => null,
            ]);

        $cutoff = $now->copy()->subDays($settings->inactivity_days);
        $users = User::query()
            ->where('role', 'student')
            ->where('status', 'active')
            ->where(function ($query) use ($cutoff) {
                $query->where('last_communication_at', '<', $cutoff)
                    ->orWhere(function ($query) use ($cutoff) {
                        $query->whereNull('last_communication_at')->where('created_at', '<', $cutoff);
                    });
            })
            ->get();

        $warned = 0;
        $blacklisted = 0;
        foreach ($users as $user) {
            if ($user->inactive_warnings >= $settings->warnings_before_blacklist) {
                if ($user->last_inactivity_warning_at?->lte($now->copy()->subDays($settings->warning_grace_days))) {
                    $until = $now->copy()->addDays($settings->blacklist_days);
                    $user->update([
                        'status' => 'blacklisted',
                        'blacklisted' => true,
                        'blacklisted_until' => $until,
                    ]);
                    Notification::create([
                        'user_id' => $user->id,
                        'type' => 'inactivity_blacklist',
                        'title' => 'Account temporarily blacklisted',
                        'message' => "Your account has been blacklisted until {$until->toDateString()} because the inactivity warnings were not addressed.",
                    ]);
                    $blacklisted++;
                }
                continue;
            }

            if ($user->last_inactivity_warning_at?->gt($now->copy()->subDays($settings->warning_grace_days))) {
                continue;
            }

            $user->increment('inactive_warnings');
            $user->update(['last_inactivity_warning_at' => $now]);
            $warningsRemaining = max(0, $settings->warnings_before_blacklist - $user->fresh()->inactive_warnings);
            Notification::create([
                'user_id' => $user->id,
                'type' => 'inactivity_warning',
                'title' => 'Inactivity warning',
                'message' => "You have been inactive for {$settings->inactivity_days} days. Please contribute within {$settings->warning_grace_days} days. {$warningsRemaining} warning(s) remain before a temporary blacklist.",
            ]);
            $warned++;
        }

        $this->info("Restored: {$restored}; warned: {$warned}; blacklisted: {$blacklisted}.");
        return self::SUCCESS;
    }
}
