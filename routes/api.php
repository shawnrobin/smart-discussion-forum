<?php

use Illuminate\Support\Facades\Route;

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\GroupController;
use App\Http\Controllers\Api\TopicController;
use App\Http\Controllers\Api\MessageController;
use App\Http\Controllers\Api\QuizController;
use App\Http\Controllers\Api\AdminController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\ShareController;
use App\Http\Controllers\Api\ParticipationController;
use App\Http\Controllers\Api\RecommendationController;
use App\Http\Controllers\Api\DesktopController;


/*
|--------------------------------------------------------------------------
| Health Check
|--------------------------------------------------------------------------
*/

Route::get('/health', function () {
    return response()->json([
        'status' => 'ok',
        'time' => now()
    ]);
});


/*
|--------------------------------------------------------------------------
| Public Routes
|--------------------------------------------------------------------------
*/

Route::post('/auth/register', [
    AuthController::class,
    'register'
]);

Route::post('/auth/login', [
    AuthController::class,
    'login'
]);

Route::get('/onboarding/rules', [
    AuthController::class,
    'getRules'
]);



/*
|--------------------------------------------------------------------------
| Protected Routes
|--------------------------------------------------------------------------
*/

Route::middleware('auth:sanctum')->group(function () {


    /*
    |--------------------------------------------------------------------------
    | Authentication
    |--------------------------------------------------------------------------
    */

    Route::post('/auth/logout', [
        AuthController::class,
        'logout'
    ]);

    Route::get('/auth/me', [
        AuthController::class,
        'me'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Groups
    |--------------------------------------------------------------------------
    */

    Route::get('/groups', [
        GroupController::class,
        'index'
    ]);

    Route::get('/desktop/groups', [
        GroupController::class,
        'desktopIndex'
    ]);

    Route::get('/desktop/bootstrap', [
        DesktopController::class,
        'bootstrap'
    ]);

    Route::get('/groups/{group}', [
        GroupController::class,
        'show'
    ]);

    Route::post('/groups/{group}/join', [
        GroupController::class,
        'join'
    ]);

    Route::post('/groups/{group}/leave', [
        GroupController::class,
        'leave'
    ]);

    Route::get('/groups/{group}/members', [
        GroupController::class,
        'getMembers'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Topics
    |--------------------------------------------------------------------------
    */

    Route::get('/groups/{group}/topics', [
        TopicController::class,
        'index'
    ]);

    Route::post('/groups/{group}/topics', [
        TopicController::class,
        'store'
    ]);

    Route::get('/topics/{topic}', [
        TopicController::class,
        'show'
    ]);

    Route::put('/topics/{topic}', [
        TopicController::class,
        'update'
    ]);

    Route::delete('/topics/{topic}', [
        TopicController::class,
        'destroy'
    ]);

    Route::post('/topics/{topic}/export-pdf', [
        TopicController::class,
        'exportPdf'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Messages
    |--------------------------------------------------------------------------
    */

    Route::get('/topics/{topic}/messages', [
        MessageController::class,
        'index'
    ]);

    Route::post('/topics/{topic}/messages', [
        MessageController::class,
        'store'
    ]);

    Route::post('/messages/{message}/mark-answer', [
        MessageController::class,
        'markAsAnswer'
    ]);

    Route::post('/messages/{message}/exclude-users', [
        MessageController::class,
        'excludeUsers'
    ]);

    Route::delete('/messages/{message}', [
        MessageController::class,
        'destroy'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Quizzes
    |--------------------------------------------------------------------------
    */


    // View all quizzes
    Route::get('/quizzes', [
        QuizController::class,
        'index'
    ]);


    // View single quiz
    Route::get('/quizzes/{quiz}', [
        QuizController::class,
        'show'
    ]);


    // Create quiz
    // Controller handles lecturer/group_admin/system_admin permission
    Route::post('/groups/{group}/quizzes', [
        QuizController::class,
        'store'
    ]);


    // Start quiz attempt
    Route::post('/quizzes/{quiz}/attempt', [
        QuizController::class,
        'startAttempt'
    ]);


    // Submit quiz
    Route::post('/quiz-attempts/{attempt}/submit', [
        QuizController::class,
        'submitAttempt'
    ]);


    // View results
    Route::get('/quiz-attempts/{attempt}/results', [
        QuizController::class,
        'getResults'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Notifications
    |--------------------------------------------------------------------------
    */

    Route::get('/notifications', [
        NotificationController::class,
        'index'
    ]);

    Route::post('/notifications/{notification}/read', [
        NotificationController::class,
        'markAsRead'
    ]);

    Route::post('/notifications/mark-all-read', [
        NotificationController::class,
        'markAllAsRead'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Profile
    |--------------------------------------------------------------------------
    */

    Route::get('/profile', [
        AuthController::class,
        'profile'
    ]);

    Route::put('/profile', [
        AuthController::class,
        'updateProfile'
    ]);

    Route::get('/participation-score', [
        AuthController::class,
        'getParticipationScore'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Admin Routes
    |--------------------------------------------------------------------------
    */

    Route::middleware('is_admin')->group(function () {


        Route::get('/admin/dashboard', [
            AdminController::class,
            'dashboard'
        ]);


        Route::get('/admin/statistics', [
            AdminController::class,
            'statistics'
        ]);


        Route::get('/admin/inactive-users', [
            AdminController::class,
            'getInactiveUsers'
        ]);


        Route::post('/admin/warn-user/{user}', [
            AdminController::class,
            'warnUser'
        ]);


        Route::post('/admin/blacklist-user/{user}', [
            AdminController::class,
            'blacklistUser'
        ]);


        Route::get('/admin/flagged-content', [
            AdminController::class,
            'getFlaggedContent'
        ]);


        Route::post('/admin/flag-content', [
            AdminController::class,
            'flagContent'
        ]);


        Route::post('/admin/remove-content', [
            AdminController::class,
            'removeContent'
        ]);

    });



    /*
    |--------------------------------------------------------------------------
    | Sharing
    |--------------------------------------------------------------------------
    */

    Route::post('/topics/{topic}/share', [
        ShareController::class,
        'shareTopic'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Participation
    |--------------------------------------------------------------------------
    */

    Route::get('/participation-scores', [
        ParticipationController::class,
        'getScores'
    ]);



    /*
    |--------------------------------------------------------------------------
    | Recommendations
    |--------------------------------------------------------------------------
    */

    Route::get('/recommendations', [
        RecommendationController::class,
        'getRecommendations'
    ]);

});
