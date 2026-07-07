<?php

use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

// Public routes (guest only)
Route::middleware('guest')->group(function () {
    Route::get('/login', function () {
        return Inertia::render('Auth/Login');
    })->name('login');

   

    Route::get('/register', function () {
        return Inertia::render('Auth/Register');
    })->name('register');

    Route::post('/register', [\App\Http\Controllers\AuthController::class, 'store'])->name('register.store');
    Route::post('/login', [\App\Http\Controllers\AuthController::class, 'login'])->name('login.store');
});

// Protected routes (authenticated only)
Route::middleware('auth')->group(function () {
    Route::get('/dashboard', function () {
        return Inertia::render('Dashboard', [
            'auth' => [
                'user' => Auth::user()
            ]
        ]);
    })->name('dashboard');

    Route::get('/profile', function () {
        return Inertia::render('Profile/Edit');
    })->name('profile.edit');
});

// Home route - redirect to dashboard if authenticated, login if guest
Route::get('/', function () {
    return Auth::check() ? redirect('/dashboard') : redirect('/login');
});
 
Route::get('/Topics', function () {
    return Inertia::render('Topics/Show');//existing route
});
Route::get('/groups/{id}', function ($id) {
    return Inertia::render('Groups/Show');
})->name('groups.show');

// Main Overview List Page
Route::get('/groups', function () {
    return Inertia::render('GroupList/Index');
})->name('groups.index');

// Main Overview List Page 
Route::get('/groups', function () {
    return Inertia::render('GroupList/Index');
})->name('groups.index');
