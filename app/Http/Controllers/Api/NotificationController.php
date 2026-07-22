<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Notification;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;

class NotificationController extends Controller
{
    public function index(Request $request): JsonResponse
    {
        $notifications = Notification::where('user_id', $request->user()->id)
            ->orderByDesc('created_at')
            ->get();

        return response()->json($notifications);
    }

    public function show(Request $request, Notification $notification): JsonResponse
    {
        $this->authorizeOwner($request, $notification);
        return response()->json($notification);
    }

    public function markAsRead(Request $request, Notification $notification): JsonResponse
    {
        $this->authorizeOwner($request, $notification);
        $notification->update(['read' => true]);
        return response()->json($notification);
    }

    public function markAllAsRead(Request $request): JsonResponse
    {
        Notification::where('user_id', $request->user()->id)
            ->where('read', false)
            ->update(['read' => true]);

        return response()->json(['message' => 'All notifications marked as read']);
    }

    public function destroy(Request $request, Notification $notification): JsonResponse
    {
        $this->authorizeOwner($request, $notification);
        $notification->delete();
        return response()->json(['message' => 'Notification deleted']);
    }

    private function authorizeOwner(Request $request, Notification $notification): void
    {
        if ($notification->user_id !== $request->user()->id) {
            abort(403, 'Unauthorized');
        }
    }
}
