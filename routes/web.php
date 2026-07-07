<?php

use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

const LOGIN_ROUTE = '/login';

// Public routes (guest only)
Route::middleware('guest')->group(function () {
    Route::get(LOGIN_ROUTE, function () {
        return Inertia::render('Auth/Login');
    })->name('login');

   

    Route::get('/register', function () {
        return Inertia::render('Auth/Register');
    })->name('register');

    Route::post('/register', [\App\Http\Controllers\AuthController::class, 'store'])->name('register.store');
    Route::post(LOGIN_ROUTE, [\App\Http\Controllers\AuthController::class, 'login'])->name('login.store');
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
 
Route::get('/topics', function () {
    return Inertia::render('Topics/Show');//existing route
});
Route::get('/groups', function () {
    return Inertia::render('Groups/Show');//existing route
});
Route::get('/grouplists', function () {
    return Inertia::render('GroupList/Index');//new route
});
Route::get('/quizzes', function () {
    return Inertia::render('Quizzes/Index');//new route
});
