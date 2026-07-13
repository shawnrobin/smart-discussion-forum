<?php

namespace App\Http\Controllers;

use App\Services\ParticipationService;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class ParticipationController extends Controller
{
    public function leaderboard($groupId)
    {
        $leaders = ParticipationService::getTopUsers($groupId);

        return Inertia::render('Leaderboard/Index', [
            'leaders' => $leaders
        ]);
    }

    public function myScore()
    {
        $score = ParticipationService::getUserGlobalScore(Auth::id());

        return Inertia::render('Participation/MyScore', [
            'score' => $score
        ]);
    }

    public function update($groupId)
    {
        ParticipationService::updateUserScore(
            Auth::id(),
            $groupId
        );

        return redirect()->back();
    }
}
