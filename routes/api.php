<?php

use App\Http\Controllers\AuthController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application.
|
*/

// Public Authentication Routes (no token required)
Route::post('/auth/register', [AuthController::class, 'register']);
Route::post('/auth/login', [AuthController::class, 'login']);

// Protected Routes (require authentication token)
Route::middleware('auth:sanctum')->group(function () {
    // Auth endpoints
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/auth/me', [AuthController::class, 'profile']);
    
    // User endpoints (we'll add these next)
    // Route::get('/user/profile', [UserController::class, 'profile']);
    // Route::put('/user/profile', [UserController::class, 'update']);
});

// Health check endpoint (useful for testing)
Route::get('/health', function () {
    return response()->json([
        'status' => 'ok',
        'timestamp' => now(),
    ]);
});