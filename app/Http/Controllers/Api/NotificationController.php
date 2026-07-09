<?php

namespace App\Http\Controllers\Api;

use App\Models\Notification;
use Illuminate\Http\Request;

class NotificationController
{
    /**
     * Get user notifications
     */
    public function index(Request $request)
    {
        $notifications = Notification::where('user_id', auth()->id())
            ->latest()
            ->paginate(20);

        return response()->json([
            'success' => true,
            'notifications' => $notifications
        ]);
    }

    /**
     * Mark notification as read
     */
    public function markAsRead(Request $request, Notification $notification)
    {
        if ($notification->user_id !== auth()->id()) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized'
            ], 403);
        }

        $notification->update(['read' => true]);

        return response()->json([
            'success' => true,
            'message' => 'Notification marked as read'
        ]);
    }

    /**
     * Mark all notifications as read
     */
    public function markAllAsRead(Request $request)
    {
        Notification::where('user_id', auth()->id())
            ->update(['read' => true]);

        return response()->json([
            'success' => true,
            'message' => 'All notifications marked as read'
        ]);
    }

    /**
     * Delete notification
     */
    public function destroy(Notification $notification)
    {
        if ($notification->user_id !== auth()->id()) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized'
            ], 403);
        }

        $notification->delete();

        return response()->json([
            'success' => true,
            'message' => 'Notification deleted'
        ]);
    }

    /**
     * Helper: Send notification to user
     */
    public static function sendNotification($userId, $type, $title, $message, $relatedEntityId = null, $relatedEntityType = null)
    {
        return Notification::create([
            'user_id' => $userId,
            'type' => $type,
            'title' => $title,
            'message' => $message,
            'related_entity_id' => $relatedEntityId,
            'related_entity_type' => $relatedEntityType
        ]);
    }
}
