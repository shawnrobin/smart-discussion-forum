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
        // This checks if the user is logged in AND has is_admin set to true
        if ($request->user() && $request->user()->is_admin) {
            return $next($request);
        }

        // If they are not an admin, send them back to the home page
        return redirect('/');
    }
}