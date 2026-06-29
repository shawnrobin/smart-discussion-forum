<?php

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

// Authentication Routes
Route::post('/auth/login', [\App\Http\Controllers\AuthController::class, 'login']);
Route::post('/auth/register', [\App\Http\Controllers\AuthController::class, 'register']);
Route::post('/auth/logout', [\App\Http\Controllers\AuthController::class, 'logout'])->middleware('auth:sanctum');

// User Routes
Route::middleware('auth:sanctum')->group(function () {
    Route::get('/user/profile', [\App\Http\Controllers\UserController::class, 'profile']);
    Route::put('/user/profile', [\App\Http\Controllers\UserController::class, 'updateProfile']);
    Route::put('/user/password', [\App\Http\Controllers\UserController::class, 'updatePassword']);
});

// Forum Routes (will add later in Phase 3)
Route::middleware('auth:sanctum')->group(function () {
    // Groups
    Route::get('/groups', [\App\Http\Controllers\GroupController::class, 'index']);
    Route::get('/groups/{id}', [\App\Http\Controllers\GroupController::class, 'show']);
    
    // Topics
    Route::get('/topics/{id}', [\App\Http\Controllers\TopicController::class, 'show']);
    Route::post('/topics', [\App\Http\Controllers\TopicController::class, 'store']);
    
    // Messages
    Route::get('/topics/{id}/messages', [\App\Http\Controllers\MessageController::class, 'index']);
    Route::post('/messages', [\App\Http\Controllers\MessageController::class, 'store']);
});