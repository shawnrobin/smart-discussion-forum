<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;
use Illuminate\Support\Facades\Auth;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'username' => 'required|string|min:3|max:100|unique:users,username',
            'password' => 'required|string|min:8|confirmed',
        ]);

        // Trim whitespace
        $validated['email'] = trim($validated['email']);

        $user = User::create([
            'name' => $validated['name'],
            'email' => $validated['email'],
            'username' => $validated['username'],
            'password' => Hash::make($validated['password']),
            'role' => 'student',
            'status' => 'active',
            'blacklisted' => false,
        ]);

        Auth::login($user);
        return redirect('/dashboard');
    }

    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        // Trim whitespace from email to avoid subtle mismatches
        $credentials['email'] = trim($credentials['email']);

        if (!Auth::attempt($credentials, $request->boolean('remember'))) {
            throw ValidationException::withMessages([
                'email' => ['Invalid email or password. Please try again.']
            ]);
        }

        $user = Auth::user();

        if ($user->status === 'blacklisted' || $user->blacklisted) {
            Auth::logout();
            $request->session()->invalidate();
            return back()->withErrors(['email' => 'Your account has been blacklisted. Contact administrator.']);
        }

        return redirect()->intended('/dashboard');
    }

    /**
     * Log the user out of the application.
     */
    public function logout(Request $request)
    {
        Auth::logout();

        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect('/login');
    }
}