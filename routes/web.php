<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;

// Controllers
use App\Http\Controllers\Admin\DashboardController as AdminDashboard;
use App\Http\Controllers\Admin\ModerationController as AdminModerationController;
use App\Http\Controllers\Admin\UserManagementController;
use App\Http\Controllers\Lecturer\DashboardController as LecturerDashboard;
use App\Http\Controllers\Lecturer\GroupController as LecturerGroupController;
use App\Http\Controllers\QuizController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\Api\GroupController as GeneralGroupController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\ProfileController;
use App\Http\Controllers\TopicController;
use App\Http\Controllers\MessageController;

// 1. Root
Route::get('/', function () { return Auth::check() ? redirect('/dashboard') : redirect('/login'); });

// 2. Authentication (Guest only)
Route::middleware('guest')->group(function () {
    Route::get('/login', function () { return Inertia::render('Auth/Login'); })->name('login');
    Route::post('/login', [AuthController::class, 'login'])->name('login.store');

    Route::get('/register', function () { return Inertia::render('Auth/Register'); })->name('register');
    Route::post('/register', [AuthController::class, 'register'])->name('register.store');
});

// 3. General Authenticated User
Route::middleware('auth')->group(function () {
    Route::get('/dashboard', function () {
        $user = Auth::user();

        // My groups with topic and member counts
        $groups = $user->groups()->withCount(['topics', 'users'])->latest()->take(5)->get();
        $groupsCount = $groups->count();
        $topicsCount = \App\Models\Topic::whereIn('group_id', $groups->pluck('id'))->count();
        $myTopicsCount = \App\Models\Topic::where('user_id', $user->id)->count();
        $myMessagesCount = \App\Models\Message::where('user_id', $user->id)->count();
        $quizzesCount = \App\Models\Quiz::where('published', true)->count();
        $myQuizAttempts = \App\Models\QuizAttempt::where('user_id', $user->id)->count();

        // Recent activity across my groups
        $activities = \App\Models\Message::whereIn('topic_id', function ($q) use ($user) {
            $q->select('id')->from('topics')->whereIn('group_id', $user->groups()->pluck('groups.id'));
        })->with('user', 'topic.group')->latest()->take(10)->get()->map(function ($msg) {
            return [
                'id' => $msg->id,
                'type' => 'reply',
                'message' => ($msg->user->name ?? 'Someone') . ' replied in "' . ($msg->topic->group->name ?? '') . '"',
                'detail' => \Illuminate\Support\Str::limit($msg->content, 60),
                'time' => $msg->created_at->diffForHumans(),
            ];
        });

        // Recent quizzes available
        $recentQuizzes = \App\Models\Quiz::where('published', true)
            ->with('creator', 'group')
            ->latest()
            ->take(5)
            ->get()
            ->map(function ($q) use ($user) {
                $attempt = \App\Models\QuizAttempt::where('quiz_id', $q->id)->where('user_id', $user->id)->whereNotNull('submitted_at')->first();
                return [
                    'id' => $q->id,
                    'title' => $q->title,
                    'group_name' => $q->group->name ?? '',
                    'duration' => $q->duration_minutes,
                    'status' => $attempt ? 'completed' : (now()->lt($q->start_time) ? 'upcoming' : (now()->lt($q->end_time) ? 'active' : 'closed')),
                    'score' => $attempt ? round($attempt->score) : null,
                ];
            });

        return Inertia::render('Dashboard', [
            'groups' => $groups->map(function ($g) {
                return ['id' => $g->id, 'name' => $g->name, 'members' => $g->users_count ?? 0, 'topics' => $g->topics_count ?? 0];
            }),
            'activities' => $activities,
            'recentQuizzes' => $recentQuizzes,
            'stats' => [
                'groups' => $groupsCount,
                'topics' => $topicsCount,
                'my_topics' => $myTopicsCount,
                'my_replies' => $myMessagesCount,
                'quizzes' => $quizzesCount,
                'attempts' => $myQuizAttempts,
            ],
        ]);
    })->name('dashboard');
    Route::post('/logout', [AuthController::class, 'logout'])->name('logout');

    // Notifications
    Route::get('/notifications', [NotificationController::class, 'index'])->name('notifications.index');
    Route::post('/notifications/mark-all-read', [NotificationController::class, 'markAllAsRead'])->name('notifications.markAllAsRead');
    Route::post('/notifications/{id}/read', [NotificationController::class, 'markAsRead'])->name('notifications.markAsRead');

    // Groups
    Route::get('/groups', [GeneralGroupController::class, 'index'])->name('groups.index');
    Route::post('/groups', [GeneralGroupController::class, 'store'])->name('groups.store');
    Route::get('/groups/{id}', function ($id) {
        $group = \App\Models\Group::with('creator', 'users')->withCount('users')->findOrFail($id);
        $topics = \App\Models\Topic::where('group_id', $id)->with('user')->withCount('messages')->latest()->get()->map(function ($t) {
            return [
                'id' => $t->id,
                'title' => $t->title,
                'body' => $t->description ?? '',
                'author' => ['name' => $t->user->name ?? 'Unknown', 'initials' => substr($t->user->name ?? 'U', 0, 2)],
                'createdAt' => $t->created_at->diffForHumans(),
                'replies' => $t->messages_count ?? 0,
                'views' => $t->likes ?? 0,
                'status' => $t->is_answered ? 'answered' : 'unanswered',
            ];
        });

        return Inertia::render('Groups/Show', [
            'group' => [
                'id' => $group->id,
                'name' => $group->name,
                'description' => $group->description ?? '',
                'members' => $group->users_count,
                'created_at' => $group->created_at,
            ],
            'topics' => $topics,
        ]);
    })->name('groups.show');
    Route::post('/groups/{group}/join', [GeneralGroupController::class, 'join'])->name('groups.join');
    Route::post('/groups/{group}/leave', [GeneralGroupController::class, 'leave'])->name('groups.leave');

    // Topics
    Route::get('/groups/{group}/topics/{topic}', function ($group, $topicId) {
        $topicModel = \App\Models\Topic::with('user')->findOrFail($topicId);
        $groupModel = \App\Models\Group::with('users')->findOrFail($group);

        // Build threaded replies: compute depth for each message by walking parent chain
        $allMessages = $topicModel->messages()
            ->visibleTo(Auth::id())
            ->with('user')
            ->get()
            ->keyBy('id');

        $messages = $allMessages->map(function ($msg) use ($allMessages) {
            $depth = 0;
            $parent = $msg->parent_message_id ? $allMessages->get($msg->parent_message_id) : null;
            while ($parent) {
                $depth++;
                $parent = $parent->parent_message_id ? $allMessages->get($parent->parent_message_id) : null;
            }

            return [
                'id' => $msg->id,
                'author' => ['name' => $msg->user->name ?? 'Unknown', 'initials' => substr($msg->user->name ?? 'U', 0, 2)],
                'content' => $msg->content,
                'createdAt' => $msg->created_at->diffForHumans(),
                'likes' => $msg->likes ?? 0,
                'isAnswer' => $msg->is_answer ?? false,
                'depth' => $depth,
                'user_id' => $msg->user_id,
                'parent_message_id' => $msg->parent_message_id,
            ];
        })->sortBy('createdAt')->values();

        $topic = [
            'id' => $topicModel->id,
            'title' => $topicModel->title,
            'content' => $topicModel->description ?? '',
            'author' => [
                'name' => $topicModel->user->name ?? 'Unknown',
                'initials' => substr($topicModel->user->name ?? 'U', 0, 2),
                'role' => $topicModel->user->role ?? 'student',
                'id' => $topicModel->user_id,
            ],
            'createdAt' => $topicModel->created_at->diffForHumans(),
            'likes' => $topicModel->likes ?? 0,
            'replies' => $messages,
        ];

        return Inertia::render('Topics/Show', [
            'topic' => $topic,
            'group' => ['id' => $groupModel->id, 'name' => $groupModel->name, 'description' => $groupModel->description ?? ''],
            'groupMembers' => $groupModel->users->map(fn($u) => ['id' => $u->id, 'name' => $u->name]),
        ]);
    })->name('topics.show');

    // Topic CRUD
    Route::post('/groups/{group}/topics', [TopicController::class, 'store'])->name('topics.store');
    Route::delete('/topics/{topic}', [TopicController::class, 'destroy'])->name('topics.destroy');

    // Replies
    Route::post('/groups/{group}/topics/{topic}/replies', [MessageController::class, 'store'])->name('replies.store');
    Route::post('/messages/{message}/mark-answer', [MessageController::class, 'markAsAnswer'])->name('messages.mark-answer');

    // PDF export
    Route::post('/topics/{topic}/export-pdf', [TopicController::class, 'exportPdf'])->name('topics.export-pdf');
    Route::post('/topics/{topic}/report', [AdminModerationController::class, 'reportTopic'])->name('topics.report');

    // Quizzes
    Route::get('/quizzes', [QuizController::class, 'index'])->name('quizzes.index');
    Route::get('/groups/{group}/quizzes/create', [QuizController::class, 'create'])->name('quizzes.create');
    Route::post('/groups/{group}/quizzes', [QuizController::class, 'store'])->name('quizzes.store');
    Route::get('/quizzes/{quiz}', [QuizController::class, 'show'])->name('quizzes.show');
    Route::get('/quizzes/{quiz}/take', [QuizController::class, 'start'])->name('quizzes.start');
    Route::post('/quiz-attempts/{attempt}/submit', [QuizController::class, 'submit'])->name('quizzes.submit');
    Route::get('/quiz-attempts/{attempt}/results', [QuizController::class, 'results'])->name('quizzes.results');
    Route::get('/quizzes/{quiz}/performance', [QuizController::class, 'performance'])->name('quizzes.performance');

    // Profile & Settings
    Route::get('/profile', function () {
        $user = Auth::user();
        $quizAttempts = \App\Models\QuizAttempt::where('user_id', $user->id)
            ->whereNotNull('submitted_at')
            ->with('quiz')
            ->latest()
            ->take(10)
            ->get()
            ->map(fn($a) => [
                'id' => $a->id,
                'title' => $a->quiz->title ?? 'Quiz',
                'date' => $a->submitted_at->format('F d, Y'),
                'score' => round($a->score ?? 0),
            ]);

        $topicsCount = \App\Models\Topic::where('user_id', $user->id)->count();
        $messagesCount = \App\Models\Message::where('user_id', $user->id)->count();

        return Inertia::render('Profile/Edit', [
            'exams' => $quizAttempts,
            'total_points' => $messagesCount * 10 + $topicsCount * 25,
            'level' => intval(floor(($messagesCount + $topicsCount) / 5) + 1),
            'progress' => (($messagesCount + $topicsCount) % 5) * 20,
            'answers_provided' => $messagesCount,
            'topics_created' => $topicsCount,
        ]);
    })->name('profile.edit');
    Route::get('/settings', [SettingsController::class, 'index'])->name('settings.index');
    Route::put('/settings/profile', [SettingsController::class, 'updateProfile'])->name('settings.profile.update');
    Route::put('/settings/account', [SettingsController::class, 'updateAccount'])->name('settings.account');
    Route::put('/settings', [SettingsController::class, 'updateSettings'])->name('settings.update');
    Route::put('/settings/deactivate', [SettingsController::class, 'deactivateAccount'])->name('settings.deactivate');
    Route::delete('/settings/delete', [SettingsController::class, 'deleteAccount'])->name('settings.delete');
});

// 4. Admin Routes
Route::middleware(['auth', 'ensureIsAdmin'])->prefix('admin')->as('admin.')->group(function () {
    Route::get('/dashboard', [AdminDashboard::class, 'index'])->name('dashboard');
    Route::get('/moderation', [AdminModerationController::class, 'index'])->name('moderation.index');
    Route::post('/moderation/reports/{report}/resolve', [AdminModerationController::class, 'resolve'])->name('moderation.resolve');

    // User Management
    Route::get('/users', [UserManagementController::class, 'index'])->name('users.index');
    Route::get('/users/{id}', [UserManagementController::class, 'show'])->name('users.show');
    Route::post('/users/{id}/warn', [UserManagementController::class, 'warn'])->name('users.warn');
    Route::post('/users/{id}/toggle-blacklist', [UserManagementController::class, 'toggleBlacklist'])->name('users.toggle-blacklist');
    Route::post('/users/{id}/toggle-admin', [UserManagementController::class, 'toggleAdmin'])->name('users.toggle-admin');
});

// 5. Lecturer Routes (Groups & Quizzes)
Route::middleware(['auth', 'isLecturer'])->prefix('lecturer')->as('lecturer.')->group(function () {
    Route::get('/dashboard', [LecturerDashboard::class, 'index'])->name('dashboard');

    // Group Management
    Route::get('/groups', [LecturerGroupController::class, 'index'])->name('groups.index');
    Route::post('/groups', [LecturerGroupController::class, 'store'])->name('groups.store');
    Route::get('/groups/{group}', [LecturerGroupController::class, 'show'])->name('groups.show');
    Route::delete('/groups/{group}', [LecturerGroupController::class, 'destroy'])->name('groups.destroy');
    Route::get('/moderation', [LecturerGroupController::class, 'moderation'])->name('moderation.index');

    // Quiz Management (names inherit 'lecturer.' prefix from group)
    Route::get('/quizzes', [QuizController::class, 'index']);
    Route::get('/groups/{group}/quizzes/create', [QuizController::class, 'create']);
    Route::post('/groups/{group}/quizzes', [QuizController::class, 'store']);
});
