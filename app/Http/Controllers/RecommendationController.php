<?php

namespace App\Http\Controllers;

use App\Services\RecommendationService;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class RecommendationController extends Controller
{
    public function index()
    {
        $recommendations = RecommendationService::getRecommendations(Auth::id(), 20);

        return Inertia::render('Recommendations/Index', [
            'recommendations' => $recommendations,
        ]);
    }

    public function generate()
    {
        RecommendationService::generateRecommendations(Auth::id());

        return redirect()->route('recommendations.index')
            ->with('success', 'Recommendations refreshed successfully.');
    }
}
