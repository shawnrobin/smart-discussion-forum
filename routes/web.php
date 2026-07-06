<?php

use Illuminate\Support\Facades\Route;
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
                'user' => auth()->user()
            ]
        ]);
    })->name('dashboard');

    Route::get('/profile', function () {
        return Inertia::render('Profile/Edit');
    })->name('profile.edit');
});

// Home route - redirect to dashboard if authenticated, login if guest
Route::get('/', function () {
    return auth()->check() ? redirect('/dashboard') : redirect('/login');
});