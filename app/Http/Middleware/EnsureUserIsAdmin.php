<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureUserIsAdmin
{
    /**
     * Handle an incoming request.
     */
    public function handle(Request $request, Closure $next): Response
    {
        // Define the roles allowed to access admin/quiz-creation features
        $authorizedRoles = ['system_admin', 'group_admin', 'lecturer'];

        // Check if the user is logged in and has either an authorized role or an admin flag
        if ($request->user() && (in_array($request->user()->role, $authorizedRoles) || $request->user()->is_admin)) {
            return $next($request);
        }

        // If they don't have the role, block them
        abort(403, 'Unauthorized access: You do not have the required permissions.');
    }
}