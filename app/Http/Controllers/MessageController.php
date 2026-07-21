<?php

namespace App\Http\Controllers;

use App\Models\Message;
use App\Models\Notification;
use App\Models\ParticipationScore;
use App\Models\Topic;
use App\Models\Group;
use App\Models\UserExclusion;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class MessageController
{
    /**
     * Display all replies for a topic.
     */
    public function index(Topic $topic)
    {
        $messages = $topic->messages()
            ->with('user')
            ->whereNull('parent_message_id')
            ->latest()
            ->get();

        foreach ($messages as $message) {
            $message->load([
                'replies.user'
            ]);
        }

        return Inertia::render('Messages/Index', [
            'topic' => $topic,
            'messages' => $messages,
        ]);
    }

    /**
     * Store a reply.
     * Requirement #3
     */
    public function store(Request $request, Group $group, Topic $topic)
    {
        $validated = $request->validate([
            'content' => 'required|string|min:2|max:5000',
            'parent_message_id' => 'nullable|exists:messages,id',
            'excluded_users' => 'nullable|array',
            'excluded_users.*' => 'exists:users,id',
        ]);

        $message = Message::create([
            'content' => $validated['content'],
            'topic_id' => $topic->id,
            'user_id' => Auth::id(),
            'parent_message_id' => $validated['parent_message_id'] ?? null,
        ]);

        /*
        |--------------------------------------------------------------------------
        | Requirement #3
        |--------------------------------------------------------------------------
        */

        if (!empty($validated['excluded_users'])) {

            foreach ($validated['excluded_users'] as $excludedUser) {

                UserExclusion::create([
                    'user_id' => Auth::id(),
                    'excluded_user_id' => $excludedUser,
                    'message_id' => $message->id,
                ]);

            }

        }

        /*
        |--------------------------------------------------------------------------
        | Requirement #9
        |--------------------------------------------------------------------------
        */

        $score = ParticipationScore::firstOrCreate(
            [
                'user_id' => Auth::id(),
                'group_id' => $topic->group_id,
            ],
            [
                'topics_created' => 0,
                'topics_replied' => 0,
                'answers_provided' => 0,
                'total_score' => 0,
            ]
        );

        $score->increment('topics_replied');
        $score->increment('total_score', 5);

        return back()->with(
            'success',
            'Reply posted successfully.'
        );
    }

    /**
     * Requirement #2
     * Mark reply as answer.
     */
    public function markAsAnswer(Message $message)
    {
        $topic = $message->topic;

        if (
            $topic->user_id !== Auth::id()
            && !Auth::user()->isAdmin()
        ) {

            abort(403);

        }

        $message->update([
            'is_answer' => true,
        ]);

        $topic->update([
            'is_answered' => true,
        ]);

        if ($topic->user_id !== $message->user_id) {
            Notification::create([
                'user_id' => $topic->user_id,
                'type' => 'answer_accepted',
                'title' => 'Your question has an accepted answer',
                'message' => "{$message->user->name} posted an accepted answer to: {$topic->title}",
                'related_entity_id' => $topic->id,
                'related_entity_type' => Topic::class,
            ]);
        }

        $score = ParticipationScore::firstOrCreate(
            [
                'user_id' => $message->user_id,
                'group_id' => $topic->group_id,
            ],
            [
                'topics_created' => 0,
                'topics_replied' => 0,
                'answers_provided' => 0,
                'total_score' => 0,
            ]
        );

        $score->increment('answers_provided');
        $score->increment('total_score', 20);

        return back()->with(
            'success',
            'Reply marked as the accepted answer.'
        );
    }

    /**
     * Requirement #3
     * Update excluded users.
     */
    public function excludeUsers(Request $request, Message $message)
    {
        $validated = $request->validate([
            'excluded_users' => 'required|array',
            'excluded_users.*' => 'exists:users,id',
        ]);

        UserExclusion::where(
            'message_id',
            $message->id
        )->delete();

        foreach ($validated['excluded_users'] as $user) {

            UserExclusion::create([
                'user_id' => Auth::id(),
                'excluded_user_id' => $user,
                'message_id' => $message->id,
            ]);

        }

        return back()->with(
            'success',
            'Excluded users updated successfully.'
        );
    }

    /**
     * Delete a reply.
     */
    public function destroy(Message $message)
    {
        if (
            $message->user_id !== Auth::id()
            && !Auth::user()->isAdmin()
        ) {

            abort(403);

        }

        $message->delete();

        return back()->with(
            'success',
            'Reply deleted successfully.'
        );
    }
}
