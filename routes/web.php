<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;
use App\Http\Controllers\Admin\DashboardController;
use App\Http\Controllers\Admin\UserManagementController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\NotificationController; // Added this

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

    Route::get('/topics', function () { return Inertia::render('Topics/Show'); })->name('topics.index');
    Route::get('/groups/{id}', function ($id) { return Inertia::render('Groups/Show'); })->name('groups.show');
    Route::get('/groups', function () { return Inertia::render('GroupList/Index'); })->name('groups.index');
    Route::get('/quizzes', function () { return Inertia::render('Quizzes/Index'); })->name('quizzes.index');
    Route::get('/profile', function () { return Inertia::render('Profile/Edit'); })->name('profile.edit');
    Route::get('/settings', function () { return Inertia::render('Settings/Edit'); })->name('settings.edit');
    
    // Notification Routes
    Route::post('/notifications/{id}/read', [NotificationController::class, 'markAsRead'])->name('notifications.markAsRead');
    
    // Logout must be a POST request
    Route::post('/logout', [AuthController::class, 'logout'])->name('logout');
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
});