<?php

namespace App\Http\Controllers;

use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Http\RedirectResponse;
use Inertia\Inertia;
use Inertia\Response;

class NotificationController extends Controller
{
    public function index(Request $request): Response
    {
        $notifications = Notification::where('user_id', $request->user()->id)
            ->orderByDesc('created_at')
            ->get();

        return Inertia::render('Notifications/Index', [
            'notifications' => $notifications,
        ]);
    }

    public function markAsRead(Request $request, Notification $notification): RedirectResponse
    {
        $this->authorizeOwner($request, $notification);
        $notification->update(['is_read' => true]);
        return back();
    }

    public function markAllAsRead(Request $request): RedirectResponse
    {
        Notification::where('user_id', $request->user()->id)
            ->where('is_read', false)
            ->update(['is_read' => true]);

        return back()->with('success', 'All notifications marked as read.');
    }

    public function destroy(Request $request, Notification $notification): RedirectResponse
    {
        $this->authorizeOwner($request, $notification);
        $notification->delete();
        return back()->with('success', 'Notification deleted.');
    }

    private function authorizeOwner(Request $request, Notification $notification): void
    {
        if ($notification->user_id !== $request->user()->id) {
            abort(403, 'Unauthorized');
        }
    }
}