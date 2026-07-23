<?php

namespace App\Http\Controllers;

use App\Services\RecommendationService;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class RecommendationController extends Controller
{
    public function index()
    {
        $recommendations = RecommendationService::getRecommendations(Auth::id());

        return Inertia::render('Recommendations/Index', [
            'recommendations' => $recommendations
        ]);
    }

    public function generate()
    {
        RecommendationService::generateRecommendations(Auth::id());

        return redirect()->back()
            ->with('success', 'Recommendations generated successfully.');
    }
}
