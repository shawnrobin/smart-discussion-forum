<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureIsLecturer
{
    public function handle(Request $request, Closure $next): Response
    {
        // Check if the user is logged in and has the 'lecturer' role
        if ($request->user() && ($request->user()->role === 'lecturer' || $request->user()->role === 'system_admin')) {
            return $next($request);
        }

        abort(403, 'Unauthorized access.');
    }
}