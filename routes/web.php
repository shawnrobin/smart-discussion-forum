<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;
use App\Http\Controllers\SettingsController;

const LOGIN_ROUTE = '/login';

// Home route - redirect to dashboard if authenticated, login if guest
Route::get('/', function () {
    return Auth::check() ? redirect('/dashboard') : redirect('/login');
});

// Public routes (Guests only)
Route::middleware('guest')->group(function () {
    Route::get(LOGIN_ROUTE, function () {
        return Inertia::render('Auth/Login');
    })->name('login');

    Route::get('/register', function () {
        return Inertia::render('Auth/Register');
    })->name('register');

Route::post('/register', [\App\Http\Controllers\AuthController::class, 'register'])->name('register.store');
Route::post(LOGIN_ROUTE, [\App\Http\Controllers\AuthController::class, 'login'])->name('login.store');
});

// Protected routes (Authenticated users only)
Route::middleware('auth')->group(function () {
    
    // Dashboard
    Route::get('/dashboard', function () {
        return Inertia::render('Dashboard', [
            'auth' => [
                'user' => Auth::user()
            ]
        ]);
    })->name('dashboard');

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

    Route::get('/groups', function () {
        return Inertia::render('GroupList/Index');
    })->name('groups.index');

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