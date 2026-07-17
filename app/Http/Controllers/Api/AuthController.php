<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\UserOnboarding;
use App\Models\OnboardingRules;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController extends Controller
{
    /**
     * Requirement #5: Get onboarding rules
     */
    public function getRules()
    {
        $rules = OnboardingRules::latest('version')->firstOrFail();

        return response()->json([
            'success' => true,
            'rules' => $rules
        ]);
    }

    /**
     * Register new user (API)
     */
    public function register(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'username' => 'required|string|min:3|max:100|unique:users,username',
            'password' => 'required|string|min:8|confirmed',
            'rules_version' => 'required|integer|exists:onboarding_rules,version',
            'agreed_to_rules' => 'required|boolean|accepted',
        ]);

        // Trim whitespace
        $validated['email'] = trim($validated['email']);

        if (!$validated['agreed_to_rules']) {
            return response()->json([
                'success' => false,
                'message' => 'You must agree to the platform rules to register.'
            ], 422);
        }

        try {

            $user = User::create([
                'name' => $validated['name'],
                'email' => $validated['email'],
                'username' => $validated['username'],
                'password' => Hash::make($validated['password']),
                'role' => 'student',
                'status' => 'active',
            ]);

            UserOnboarding::create([
                'user_id' => $user->id,
                'rules_version' => $validated['rules_version'],
                'agreed_at' => now(),
            ]);

            $token = $user->createToken('api-token')->plainTextToken;

            return response()->json([
                'success' => true,
                'message' => 'User registered successfully.',
                'token' => $token,
                'user' => $user,
            ], 201);

        } catch (\Exception $e) {

            return response()->json([
                'success' => false,
                'message' => 'Registration failed.',
                'error' => $e->getMessage(),
            ], 500);

        }
    }

    /**
     * Login user (API)
     */
    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        // Trim whitespace from email
        $credentials['email'] = trim($credentials['email']);

        if (!Auth::attempt($credentials)) {

            throw ValidationException::withMessages([
                'email' => ['The provided credentials are incorrect.']
            ]);

        }

        /** @var User $user */
        $user = Auth::user();

        if ($user->status === 'blacklisted') {

            Auth::logout();

            return response()->json([
                'success' => false,
                'message' => 'Your account has been blacklisted. Contact administrator.'
            ], 403);

        }

        $user->update([
            'last_login' => now()
        ]);

        $token = $user->createToken('api-token')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Login successful',
            'token' => $token,
            'user' => $user,
        ]);
    }

    /**
     * Logout user (API)
     */
    public function logout(Request $request)
    {
        $request->user()
            ->currentAccessToken()
            ->delete();

        return response()->json([
            'success' => true,
            'message' => 'Logged out successfully'
        ]);
    }

    /**
     * Get authenticated user
     */
    public function me(Request $request)
    {
        return response()->json([
            'success' => true,
            'user' => $request->user()
        ]);
    }

    /**
     * Get user profile
     */
    public function profile(Request $request)
    {
        return response()->json([
            'success' => true,
            'user' => $request->user()
        ]);
    }

    /**
     * Update user profile
     */
    public function updateProfile(Request $request)
    {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email,' . $request->user()->id,
        ]);

        $request->user()->update($validated);

        return response()->json([
            'success' => true,
            'message' => 'Profile updated successfully.',
            'user' => $request->user()
        ]);
    }

    /**
     * Requirement #9: Get participation score
     */
    public function getParticipationScore(Request $request)
    {
        $scores = $request->user()
            ->participationScores()
            ->with('group')
            ->get();

        return response()->json([
            'success' => true,
            'scores' => $scores
        ]);
    }
}