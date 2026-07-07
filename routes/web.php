<?php

use Illuminate\Support\Facades\Route;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

const LOGIN_ROUTE = '/login';

<<<<<<< HEAD
// Public routes (guest only)
=======
// Home route - redirect to dashboard if authenticated, login if guest
Route::get('/', function () {
    return auth()->check() ? redirect('/dashboard') : redirect('/login');
});

// Public routes (Guests only)
>>>>>>> 58d3bd8 (afew changes on the looks of our pages)
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

    // Topics Index
    Route::get('/topics', function () {
        return Inertia::render('Topics/Show'); // Adjust to 'Topics/Index' later if you make a full list view!
    })->name('topics.index');

    // Groups
    Route::get('/groups', function () {
        return Inertia::render('Groups/Show');
    })->name('groups.index');

    Route::get('/grouplists', function () {
        return Inertia::render('GroupList/Index');
    })->name('grouplists.index');

    // Quizzes
    Route::get('/quizzes', function () {
        return Inertia::render('Quizzes/Index');
    })->name('quizzes.index');

    // Profile & Settings
    Route::get('/profile', function () {
        return Inertia::render('Profile/Edit');
    })->name('profile.edit');

<<<<<<< HEAD
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
=======
    Route::get('/settings', function () {
        return Inertia::render('Settings/Edit'); // Placeholder for your settings route
    })->name('settings.edit');
>>>>>>> 58d3bd8 (afew changes on the looks of our pages)

    // Logout
    Route::post('/logout', [\App\Http\Controllers\AuthController::class, 'logout'])->name('logout');
});