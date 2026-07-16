<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;

// Controllers
use App\Http\Controllers\Admin\DashboardController as AdminDashboard;
use App\Http\Controllers\Lecturer\DashboardController as LecturerDashboard;
use App\Http\Controllers\Lecturer\GroupController as LecturerGroupController;
use App\Http\Controllers\QuizController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\Api\GroupController as GeneralGroupController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\ProfileController;

// 1. Root
Route::get('/', function () { return Auth::check() ? redirect('/dashboard') : redirect('/login'); });

// 2. Authentication
Route::middleware('guest')->group(function () {
    Route::get('/login', function () { return Inertia::render('Auth/Login'); })->name('login');
    Route::post('/login', [AuthController::class, 'login'])->name('login.store');
});

// 3. General Authenticated User
Route::middleware('auth')->group(function () {
    Route::get('/dashboard', function () { return Inertia::render('Dashboard'); })->name('dashboard');
    Route::post('/logout', [AuthController::class, 'logout'])->name('logout');

    // Notifications
    Route::post('/notifications/{id}/read', [NotificationController::class, 'markAsRead'])->name('notifications.markAsRead');

    // Groups & Topics (Managed under groups)
    Route::get('/groups', [GeneralGroupController::class, 'index'])->name('groups.index');
    Route::post('/groups', [GeneralGroupController::class, 'store'])->name('groups.store');
    Route::get('/groups/{id}', function ($id) { return Inertia::render('Groups/Show'); })->name('groups.show');
    Route::post('/groups/{group}/join', [GeneralGroupController::class, 'join'])->name('groups.join');
    Route::post('/groups/{group}/leave', [GeneralGroupController::class, 'leave'])->name('groups.leave');

    Route::get('/groups/{group}/topics/{topic}', function ($group, $topic) {
        return Inertia::render('Topics/Show', ['group' => ['id' => $group], 'topic' => null]);
    })->name('topics.show');

    // Quizzes
    Route::get('/quizzes', [QuizController::class, 'index'])->name('quizzes.index');
    Route::get('/quizzes/{quiz}/take', [QuizController::class, 'start'])->name('quizzes.start');
    Route::post('/quiz-attempts/{attempt}/submit', [QuizController::class, 'submit'])->name('quizzes.submit');
    Route::get('/quiz-attempts/{attempt}/results', [QuizController::class, 'results'])->name('quizzes.results');

    // Profile & Settings
    Route::get('/profile', function () { return Inertia::render('Profile/Edit'); })->name('profile.edit');
    Route::get('/settings', [SettingsController::class, 'index'])->name('settings.index');
});

// 4. Admin Routes
Route::middleware(['auth', 'ensureIsAdmin'])->prefix('admin')->as('admin.')->group(function () {
    Route::get('/dashboard', [AdminDashboard::class, 'index'])->name('dashboard');
});

// 5. Lecturer Routes (Groups & Quizzes)
Route::middleware(['auth', 'isLecturer'])->prefix('lecturer')->as('lecturer.')->group(function () {
    Route::get('/dashboard', [LecturerDashboard::class, 'index'])->name('dashboard');

    // Group Management
    Route::get('/groups', [LecturerGroupController::class, 'index'])->name('groups.index');
    Route::post('/groups', [LecturerGroupController::class, 'store'])->name('groups.store');
    Route::get('/groups/{group}', [LecturerGroupController::class, 'show'])->name('groups.show');
    Route::delete('/groups/{group}', [LecturerGroupController::class, 'destroy'])->name('groups.destroy');
    Route::get('/moderation', [LecturerGroupController::class, 'moderation'])->name('moderation.index');

    // Quiz Management
    Route::get('/quizzes', [QuizController::class, 'index'])->name('quizzes.index');
    Route::get('/groups/{group}/quizzes/create', [QuizController::class, 'create'])->name('quizzes.create');
    Route::post('/groups/{group}/quizzes', [QuizController::class, 'store'])->name('quizzes.store');
    Route::get('/quizzes/{quiz}', [QuizController::class, 'show'])->name('quizzes.show');
    Route::post('/quizzes/{quiz}/start', [QuizController::class, 'start'])->name('quizzes.start');
    Route::post('/attempts/{attempt}/submit', [QuizController::class, 'submit'])->name('quizzes.submit');
    Route::get('/attempts/{attempt}/results', [QuizController::class, 'results'])->name('quizzes.results');
});
