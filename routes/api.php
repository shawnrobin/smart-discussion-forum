<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\GroupController;
use App\Http\Controllers\Api\TopicController;
use App\Http\Controllers\Api\MessageController;
use App\Http\Controllers\Api\QuizController;
use App\Http\Controllers\Api\AdminController;
use App\Http\Controllers\Api\NotificationController;


// Health check (no auth needed)
Route::get('/health', function () {
    return response()->json(['status' => 'ok', 'time' => now()]);
});

// ============ PUBLIC ROUTES (No Authentication) ============

// Authentication
Route::post('/auth/register', [AuthController::class, 'register']);
Route::post('/auth/login', [AuthController::class, 'login']);
Route::get('/onboarding/rules', [AuthController::class, 'getRules']);

// ============ PROTECTED ROUTES (Authentication Required) ============

Route::middleware('auth:sanctum')->group(function () {
    // Auth
    Route::post('/auth/logout', [AuthController::class, 'logout']);
    Route::get('/auth/me', [AuthController::class, 'me']);

    // Groups
    Route::get('/groups', [GroupController::class, 'index']);
    Route::get('/groups/{group}', [GroupController::class, 'show']);
    Route::post('/groups/{group}/join', [GroupController::class, 'join']);
    Route::post('/groups/{group}/leave', [GroupController::class, 'leave']);
    Route::get('/groups/{group}/members', [GroupController::class, 'getMembers']);

    // Topics
    Route::get('/groups/{group}/topics', [TopicController::class, 'index']);
    Route::post('/groups/{group}/topics', [TopicController::class, 'store']);
    Route::get('/topics/{topic}', [TopicController::class, 'show']);
    Route::put('/topics/{topic}', [TopicController::class, 'update']);
    Route::delete('/topics/{topic}', [TopicController::class, 'destroy']);
    Route::post('/topics/{topic}/export-pdf', [TopicController::class, 'exportPdf']);

    // Messages (Replies)
    Route::get('/topics/{topic}/messages', [MessageController::class, 'index']);
    Route::post('/topics/{topic}/messages', [MessageController::class, 'store']);
    Route::post('/messages/{message}/mark-answer', [MessageController::class, 'markAsAnswer']);
    Route::post('/messages/{message}/exclude-users', [MessageController::class, 'excludeUsers']);
    Route::delete('/messages/{message}', [MessageController::class, 'destroy']);

    // Quizzes
    Route::get('/quizzes', [QuizController::class, 'index']);
    Route::get('/quizzes/{quiz}', [QuizController::class, 'show']);
    Route::post('/quizzes/{quiz}/attempt', [QuizController::class, 'startAttempt']);
    Route::post('/quiz-attempts/{attempt}/submit', [QuizController::class, 'submitAttempt']);
    Route::get('/quiz-attempts/{attempt}/results', [QuizController::class, 'getResults']);

    // Notifications
    Route::get('/notifications', [NotificationController::class, 'index']);
    Route::post('/notifications/{notification}/read', [NotificationController::class, 'markAsRead']);

    // User Profile
    Route::get('/profile', [AuthController::class, 'profile']);
    Route::put('/profile', [AuthController::class, 'updateProfile']);
    Route::get('/participation-score', [AuthController::class, 'getParticipationScore']);

    // ============ ADMIN ROUTES ============
    Route::middleware('is_admin')->group(function () {
        // Dashboard & Statistics
        Route::get('/admin/dashboard', [AdminController::class, 'dashboard']);
        Route::get('/admin/statistics', [AdminController::class, 'statistics']);
        
        // User Management
        Route::get('/admin/inactive-users', [AdminController::class, 'getInactiveUsers']);
        Route::post('/admin/warn-user/{user}', [AdminController::class, 'warnUser']);
        Route::post('/admin/blacklist-user/{user}', [AdminController::class, 'blacklistUser']);
        
        // Content Moderation
        Route::get('/admin/flagged-content', [AdminController::class, 'getFlaggedContent']);
        Route::post('/admin/flag-content', [AdminController::class, 'flagContent']);
        Route::post('/admin/remove-content', [AdminController::class, 'removeContent']);
    });
});

    

});

// Quizzes (authenticated routes)
Route::middleware('auth:sanctum')->group(function () {
    Route::get('/quizzes', [QuizController::class, 'index']);
    Route::get('/quizzes/{quiz}', [QuizController::class, 'show']);
    Route::post('/quizzes/{quiz}/attempt', [QuizController::class, 'startAttempt']);
    Route::post('/quiz-attempts/{attempt}/submit', [QuizController::class, 'submitAttempt']);
    Route::get('/quiz-attempts/{attempt}/results', [QuizController::class, 'getResults']);
    
    // Create quiz (lecturer only)
    Route::middleware('is_admin')->group(function () {
        Route::post('/groups/{group}/quizzes', [QuizController::class, 'store']);
    });
});
