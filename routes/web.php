<?php

use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Route;
use Inertia\Inertia;

// Controllers
use App\Http\Controllers\Admin\DashboardController as AdminDashboard;
use App\Http\Controllers\Admin\UserManagementController;
use App\Http\Controllers\Lecturer\DashboardController as LecturerDashboard;
use App\Http\Controllers\Lecturer\GroupController as LecturerGroupController;
use App\Http\Controllers\QuizController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\Api\GroupController as GeneralGroupController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\ProfileController;

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
        $topicModel = \App\Models\Topic::with('user', 'messages.user')->findOrFail($topicId);
        $groupModel = \App\Models\Group::findOrFail($group);

        $messages = $topicModel->messages->whereNull('parent_message_id')->values()->map(function ($msg) {
            return [
                'id' => $msg->id,
                'author' => ['name' => $msg->user->name ?? 'Unknown', 'initials' => substr($msg->user->name ?? 'U', 0, 2)],
                'content' => $msg->content,
                'createdAt' => $msg->created_at->diffForHumans(),
                'likes' => $msg->likes ?? 0,
                'isAnswer' => $msg->is_answer ?? false,
                'depth' => 0,
            ];
        });

        $topic = [
            'id' => $topicModel->id,
            'title' => $topicModel->title,
            'content' => $topicModel->description ?? '',
            'author' => ['name' => $topicModel->user->name ?? 'Unknown', 'initials' => substr($topicModel->user->name ?? 'U', 0, 2), 'role' => $topicModel->user->role ?? 'student'],
            'createdAt' => $topicModel->created_at->diffForHumans(),
            'likes' => $topicModel->likes ?? 0,
            'replies' => $messages,
        ];

        return Inertia::render('Topics/Show', [
            'topic' => $topic,
            'group' => ['id' => $groupModel->id, 'name' => $groupModel->name, 'description' => $groupModel->description ?? ''],
        ]);
    })->name('topics.show');

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
    Route::get('/profile', function () { return Inertia::render('Profile/Edit'); })->name('profile.edit');
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

    // Quiz Management
    Route::get('/quizzes', [QuizController::class, 'index'])->name('quizzes.index');
    Route::get('/groups/{group}/quizzes/create', [QuizController::class, 'create'])->name('quizzes.create');
    Route::post('/groups/{group}/quizzes', [QuizController::class, 'store'])->name('quizzes.store');
});
