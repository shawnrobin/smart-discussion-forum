<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\RecommendationService;
use Illuminate\Support\Facades\Auth;

class RecommendationController extends Controller
{
    public function index()
    {
        return response()->json(
            RecommendationService::getRecommendations(Auth::id())
        );
    }

    public function generate()
    {
        RecommendationService::generateRecommendations(Auth::id());

        return response()->json([
            'message' => 'Recommendations generated.'
        ]);
    }
}
