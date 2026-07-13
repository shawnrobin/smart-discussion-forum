<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\ParticipationService;
use Illuminate\Support\Facades\Auth;

class ParticipationController extends Controller
{
    public function leaderboard($groupId)
    {
        return response()->json(
            ParticipationService::getTopUsers($groupId)
        );
    }

    public function myScore()
    {
        return response()->json([
            'score' => ParticipationService::getUserGlobalScore(Auth::id())
        ]);
    }

    public function update($groupId)
    {
        return response()->json(
            ParticipationService::updateUserScore(
                Auth::id(),
                $groupId
            )
        );
    }
}
