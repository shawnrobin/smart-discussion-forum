<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\Admin\UserManagementController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\NotificationController; // Added this
use App\Http\Controllers\SettingsController;

const LOGIN_ROUTE = '/login';

// Home route - redirect to dashboard if authenticated, login if guest
Route::get('/', function () {
    return Auth::check() ? redirect('/dashboard') : redirect(LOGIN_ROUTE);
});

// Public routes (Guests only)
Route::middleware('guest')->group(function () {
    Route::get(LOGIN_ROUTE, function () {
        return Inertia::render('Auth/Login');
    })->name('login');

    Route::get('/register', function () {
        return Inertia::render('Auth/Register');
    })->name('register');

    Route::post('/register', [AuthController::class, 'register'])->name('register.store');
    Route::post(LOGIN_ROUTE, [AuthController::class, 'login'])->name('login.store');
});

// Protected routes (Authenticated users only)
Route::middleware('auth')->group(function () {
    
    // Core User Routes
    Route::get('/dashboard', function () {
        return Inertia::render('Dashboard');
    })->name('dashboard');

    
    // Notification Routes
    Route::post('/notifications/{id}/read', [NotificationController::class, 'markAsRead'])->name('notifications.markAsRead');
    
    // Logout must be a POST request
   
});

// Admin Routes Group
Route::middleware(['auth', 'is_admin'])->prefix('admin')->as('admin.')->group(function () {
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard');
    
    // User Management
    Route::get('/users', [UserManagementController::class, 'index'])->name('users.index');
    Route::get('/users/{id}', [UserManagementController::class, 'show'])->name('users.show');
    Route::post('/users/{id}/warn', [UserManagementController::class, 'warn'])->name('users.warn');
    Route::post('/users/{id}/toggle-blacklist', [UserManagementController::class, 'toggleBlacklist'])->name('users.blacklist');
    
    // Toggle Admin Status Route
    Route::post('/users/{id}/toggle-admin', [UserManagementController::class, 'toggleAdmin'])->name('users.toggle-admin');
    // Topic detail page, nested under its group
Route::get('/groups/{group}/topics/{topic}', function ($group, $topic) {
    return Inertia::render('Topics/Show', [
        'group' => ['id' => $group, 'name' => 'Data Structures'],
        'topic' => null,
    ]);
})->name('topics.show');

    // Groups
    Route::get('/groups/{id}', function ($id) {
        return Inertia::render('Groups/Show');
    })->name('groups.show');

    Route::get('/groups', [\App\Http\Controllers\GroupController::class, 'index'])->name('groups.index');
    Route::post('/groups', [\App\Http\Controllers\GroupController::class, 'store'])->name('groups.store');
    Route::post('/groups/{group}/join', [\App\Http\Controllers\GroupController::class, 'join'])->name('groups.join');
    Route::post('/groups/{group}/leave', [\App\Http\Controllers\GroupController::class, 'leave'])->name('groups.leave');

    // Quizzes
    Route::get('/quizzes', function () {
        return Inertia::render('Quizzes/Index');
    })->name('quizzes.index');

    // Profile & Settings
    Route::get('/profile', function () {
        return Inertia::render('Profile/Edit');
    })->name('profile.edit');

    Route::get('/settings', function () {
    return Inertia::render('Settings/Edit'); // Placeholder for your settings route
})->name('settings.edit');
    Route::middleware(['auth', 'is_admin'])->group(function () {
    Route::get('/admin', function () {
        return Inertia::render('Admin/Dashboard');
    });
    });
    // Logout
    Route::post('/logout', [\App\Http\Controllers\AuthController::class, 'logout'])->name('logout');
    Route::get('/settings', [SettingsController::class, 'index'])->name('settings.index');
Route::put('/settings/profile', [SettingsController::class, 'updateProfile'])->name('settings.profile');
Route::put('/settings/account', [SettingsController::class, 'updateAccount'])->name('settings.account');
Route::put('/settings', [SettingsController::class, 'updateSettings'])->name('settings.update');
Route::put('/settings/deactivate', [SettingsController::class, 'deactivateAccount'])->name('settings.deactivate');
Route::delete('/settings/delete', [SettingsController::class, 'deleteAccount'])->name('settings.delete');

});