<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\Admin\UserManagementController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\Api\GroupController;

const LOGIN_ROUTE = '/login';

Route::get('/', function () {
    return Auth::check() ? redirect('/dashboard') : redirect(LOGIN_ROUTE);
});

// Guest Routes
Route::middleware('guest')->group(function () {
    Route::get(LOGIN_ROUTE, function () { return Inertia::render('Auth/Login'); })->name('login');
    Route::get('/register', function () { return Inertia::render('Auth/Register'); })->name('register');
    Route::post('/register', [AuthController::class, 'register'])->name('register.store');
    Route::post(LOGIN_ROUTE, [AuthController::class, 'login'])->name('login.store');
});

// Authenticated User Routes
Route::middleware('auth')->group(function () {
    Route::get('/dashboard', function () { return Inertia::render('Dashboard'); })->name('dashboard');
    Route::post('/logout', [AuthController::class, 'logout'])->name('logout');
    
    // Notifications
    Route::post('/notifications/{id}/read', [NotificationController::class, 'markAsRead'])->name('notifications.markAsRead');

    // Groups & Topics (Managed under groups)
    Route::get('/groups', [GroupController::class, 'index'])->name('groups.index');
    Route::post('/groups', [GroupController::class, 'store'])->name('groups.store');
    Route::get('/groups/{id}', function ($id) { return Inertia::render('Groups/Show'); })->name('groups.show');
    Route::post('/groups/{group}/join', [GroupController::class, 'join'])->name('groups.join');
    Route::post('/groups/{group}/leave', [GroupController::class, 'leave'])->name('groups.leave');
    
    Route::get('/groups/{group}/topics/{topic}', function ($group, $topic) {
        return Inertia::render('Topics/Show', ['group' => ['id' => $group], 'topic' => null]);
    })->name('topics.show');

    // Quizzes & Profile
    Route::get('/quizzes', function () { return Inertia::render('Quizzes/Index'); })->name('quizzes.index');
    Route::get('/profile', function () { return Inertia::render('Profile/Edit'); })->name('profile.edit');

    // Settings
    Route::get('/settings', [SettingsController::class, 'index'])->name('settings.index');
    Route::put('/settings/profile', [SettingsController::class, 'updateProfile'])->name('settings.profile');
    Route::put('/settings/account', [SettingsController::class, 'updateAccount'])->name('settings.account');
    Route::put('/settings', [SettingsController::class, 'updateSettings'])->name('settings.update');
    Route::put('/settings/deactivate', [SettingsController::class, 'deactivateAccount'])->name('settings.deactivate');
    Route::delete('/settings/delete', [SettingsController::class, 'deleteAccount'])->name('settings.delete');
});

// Admin Routes
Route::middleware(['auth', 'is_admin'])->prefix('admin')->as('admin.')->group(function () {
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard');
    Route::get('/users', [UserManagementController::class, 'index'])->name('users.index');
    Route::get('/users/{id}', [UserManagementController::class, 'show'])->name('users.show');
    Route::post('/users/{id}/warn', [UserManagementController::class, 'warn'])->name('users.warn');
    Route::post('/users/{id}/toggle-blacklist', [UserManagementController::class, 'toggleBlacklist'])->name('users.blacklist');
    Route::post('/users/{id}/toggle-admin', [UserManagementController::class, 'toggleAdmin'])->name('users.toggle-admin');
});