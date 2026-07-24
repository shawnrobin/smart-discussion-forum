<?php

use Illuminate\Http\Request;
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
use App\Http\Controllers\EngagementController;
use App\Http\Controllers\GroupStatisticsController;
use App\Http\Controllers\Auth\PasswordResetLinkController;
use App\Http\Controllers\Auth\NewPasswordController;
use App\Models\User;

Route::get('/make-admin', function () {
    return User::select('id', 'name', 'email', 'role')->get();
});

// 1. Root
Route::get('/', function () { return Auth::check() ? redirect('dashboard') : redirect('/login'); });

// 2. Authentication routes
require __DIR__ . '/auth.php';

// 3. General Authenticated User
Route::middleware('auth')->group(function () {
    Route::get('/dashboard', function (Request $request) {
        $user = Auth::user();
        $search = trim($request->query('search', ''));

        // My groups with topic and member counts
        $groups = $user->groups()->withCount(['topics', 'users'])->latest()->take(5)->get();
        $groupsCount = $groups->count();
        $topicsCount = \App\Models\Topic::whereIn('group_id', $groups->pluck('id'))->count();
        $myTopicsCount = \App\Models\Topic::where('user_id', $user->id)->count();
        $myMessagesCount = \App\Models\Message::where('user_id', $user->id)->count();
        $quizzesCount = \App\Models\Quiz::where('published', true)->count();
        $myQuizAttempts = \App\Models\QuizAttempt::where('user_id', $user->id)->count();
        $isEligibleForQuiz = function ($quiz) use ($user) {
            $category = $quiz->target_category ?: 'all';
            if ($category === 'all') return true;
            [$field, $value] = array_pad(explode(':', $category, 2), 2, null);
            return in_array($field, ['year_of_study', 'course', 'faculty'], true) && (string) $user->{$field} === (string) $value;
        };

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
            ->whereIn('group_id', $user->groups()->pluck('groups.id'))
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

        $activeQuiz = $user->role === 'student' ? \App\Models\Quiz::where('published', true)
            ->whereIn('group_id', $user->groups()->pluck('groups.id'))
            ->where('start_time', '<=', now())
            ->where('end_time', '>', now())
            ->whereDoesntHave('attempts', function ($query) use ($user) {
                $query->where('user_id', $user->id)->whereNotNull('submitted_at');
            })
            ->with('group')
            ->latest('start_time')
            ->get()
            ->first($isEligibleForQuiz) : null;

        $filter = $request->query('filter', 'latest');
        $allowedFilters = ['latest', 'group', 'replies', 'quiz'];
        if (!in_array($filter, $allowedFilters, true)) {
            $filter = 'latest';
        }

        $searchResults = [];
        if ($search !== '') {
            $groupIds = $groups->pluck('id');

            if ($filter === 'group' || $filter === 'latest') {
                $groupResults = \App\Models\Group::whereIn('id', $groupIds)
                    ->where(function ($query) use ($search) {
                        $query->where('name', 'like', "%{$search}%")
                            ->orWhere('description', 'like', "%{$search}%");
                    })
                    ->latest('updated_at')
                    ->take(6)
                    ->get();

                foreach ($groupResults as $groupResult) {
                    $searchResults[] = [
                        'id' => 'group-' . $groupResult->id,
                        'type' => 'Group',
                        'title' => $groupResult->name,
                        'subtitle' => 'Group',
                        'detail' => \Illuminate\Support\Str::limit($groupResult->description ?? '', 80),
                        'url' => route('groups.show', $groupResult->id),
                    ];
                }
            }

            if ($filter === 'replies' || $filter === 'latest') {
                $messageResults = \App\Models\Message::whereIn('topic_id', function ($query) use ($groupIds) {
                        $query->select('id')->from('topics')->whereIn('group_id', $groupIds);
                    })
                    ->where('content', 'like', "%{$search}%")
                    ->with(['topic.group', 'user'])
                    ->latest('updated_at')
                    ->take(6)
                    ->get();

                foreach ($messageResults as $message) {
                    $searchResults[] = [
                        'id' => 'message-' . $message->id,
                        'type' => 'Reply',
                        'title' => 'Reply in ' . ($message->topic->title ?? 'a topic'),
                        'subtitle' => 'by ' . ($message->user->name ?? 'Someone'),
                        'detail' => \Illuminate\Support\Str::limit($message->content, 90),
                        'url' => route('topics.show', ['group' => $message->topic->group_id, 'topic' => $message->topic_id]),
                    ];
                }
            }

            if ($filter === 'quiz' || $filter === 'latest') {
                $quizResults = \App\Models\Quiz::whereIn('group_id', $groupIds)
                    ->where(function ($query) use ($search) {
                        $query->where('title', 'like', "%{$search}%")
                            ->orWhere('description', 'like', "%{$search}%");
                    })
                    ->latest('updated_at')
                    ->take(6)
                    ->get();

                foreach ($quizResults as $quiz) {
                    $searchResults[] = [
                        'id' => 'quiz-' . $quiz->id,
                        'type' => 'Quiz',
                        'title' => $quiz->title,
                        'subtitle' => 'in ' . ($quiz->group->name ?? 'a group'),
                        'detail' => \Illuminate\Support\Str::limit($quiz->description ?? '', 90),
                        'url' => route('quizzes.show', $quiz->id),
                    ];
                }
            }

            if ($filter === 'latest') {
                $topicResults = \App\Models\Topic::whereIn('group_id', $groupIds)
                    ->where(function ($query) use ($search) {
                        $query->where('title', 'like', "%{$search}%")
                            ->orWhere('description', 'like', "%{$search}%");
                    })
                    ->with(['group', 'user'])
                    ->latest('updated_at')
                    ->take(6)
                    ->get();

                foreach ($topicResults as $topic) {
                    $searchResults[] = [
                        'id' => 'topic-' . $topic->id,
                        'type' => 'Topic',
                        'title' => $topic->title,
                        'subtitle' => 'in ' . ($topic->group->name ?? 'a group'),
                        'detail' => \Illuminate\Support\Str::limit($topic->description ?? '', 80),
                        'url' => route('topics.show', ['group' => $topic->group_id, 'topic' => $topic->id]),
                    ];
                }
            }
        }

        return Inertia::render('Dashboard', [
            'groups' => $groups->map(function ($g) {
                return ['id' => $g->id, 'name' => $g->name, 'members' => $g->users_count ?? 0, 'topics' => $g->topics_count ?? 0];
            }),
            'activities' => $activities,
            'recentQuizzes' => $recentQuizzes,
            'activeQuiz' => $activeQuiz ? ['id' => $activeQuiz->id, 'title' => $activeQuiz->title, 'group' => $activeQuiz->group->name ?? '', 'end_time' => $activeQuiz->end_time->toIso8601String()] : null,
            'stats' => [
                'groups' => $groupsCount,
                'topics' => $topicsCount,
                'my_topics' => $myTopicsCount,
                'my_replies' => $myMessagesCount,
                'quizzes' => $quizzesCount,
                'attempts' => $myQuizAttempts,
            ],
            'recommendations' => $user->role === 'student'
                ? \App\Services\RecommendationService::getRecommendations($user->id, 5)
                    ->map(fn ($r) => [
                        'id'          => $r->topic->id,
                        'title'       => $r->topic->title,
                        'group_id'    => $r->topic->group_id,
                        'group_name'  => $r->topic->group->name ?? '',
                        'author'      => $r->topic->user->name ?? 'Unknown',
                        'replies'     => $r->topic->messages_count ?? 0,
                        'score'       => round((float) $r->score, 1),
                        'is_answered' => $r->topic->is_answered,
                    ])
                : [],
            'search' => $search,
            'filter' => $filter,
            'searchResults' => $searchResults,
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
        abort_unless($group->users()->whereKey(Auth::id())->exists() || (int) $group->created_by === (int) Auth::id(), 403);
        $topics = \App\Models\Topic::where('group_id', $id)->visibleTo(Auth::id())->with('user')->withCount('messages')->latest()->get()->map(function ($t) {
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
            'topicKeywords' => \App\Services\TopicContentPolicy::keywordsFor($group),
            'groupMembers' => $group->users->map(fn ($user) => ['id' => $user->id, 'name' => $user->name]),
            'topics' => $topics,
        ]);
    })->name('groups.show');
    Route::post('/groups/{group}/join', [GeneralGroupController::class, 'join'])->name('groups.join');
    Route::post('/groups/{group}/leave', [GeneralGroupController::class, 'leave'])->name('groups.leave');

    // Topics
    Route::get('/groups/{group}/topics/{topic}', function ($group, $topicId) {
        $topicModel = \App\Models\Topic::with('user')->visibleTo(Auth::id())->findOrFail($topicId);
        $groupModel = \App\Models\Group::with('users')->findOrFail($group);
        abort_unless($groupModel->users()->whereKey(Auth::id())->exists() || (int) $groupModel->created_by === (int) Auth::id(), 403);
        abort_unless((int) $topicModel->group_id === (int) $groupModel->id, 404);

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
                'user' => ['name' => $msg->user->name ?? 'Unknown', 'initials' => substr($msg->user->name ?? 'U', 0, 2)],
                'content' => $msg->content,
                'created_at' => $msg->created_at->diffForHumans(),
                'likes' => $msg->likes ?? 0,
                'liked_by_current_user' => \App\Models\ContentLike::where('user_id', Auth::id())->where('message_id', $msg->id)->exists(),
                'isAnswer' => $msg->is_answer ?? false,
                'depth' => $depth,
                'user_id' => $msg->user_id,
                'parent_message_id' => $msg->parent_message_id,
            ];
        })->sortBy('createdAt')->values();

        $topic = [
            'id' => $topicModel->id,
            'title' => $topicModel->title,
            'description' => $topicModel->description ?? '',
            'user' => [
                'name' => $topicModel->user->name ?? 'Unknown',
                'initials' => substr($topicModel->user->name ?? 'U', 0, 2),
                'role' => $topicModel->user->role ?? 'student',
                'id' => $topicModel->user_id,
            ],
            'created_at' => $topicModel->created_at->diffForHumans(),
            'likes' => $topicModel->likes ?? 0,
            'liked_by_current_user' => \App\Models\ContentLike::where('user_id', Auth::id())->where('topic_id', $topicModel->id)->exists(),
            'messages' => $messages,
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
    Route::post('/topics/{topic}/like', [EngagementController::class, 'toggleTopicLike'])->name('topics.like');
    Route::post('/messages/{message}/like', [EngagementController::class, 'toggleMessageLike'])->name('messages.like');

    // PDF export
    Route::post('/topics/{topic}/export-pdf', [TopicController::class, 'exportPdf'])->name('topics.export-pdf');
    Route::post('/topics/{topic}/report', [AdminModerationController::class, 'reportTopic'])->name('topics.report');

    // Recommendations (ML)
    Route::get('/recommendations', [\App\Http\Controllers\RecommendationController::class, 'index'])->name('recommendations.index');
    Route::post('/recommendations/generate', [\App\Http\Controllers\RecommendationController::class, 'generate'])->name('recommendations.generate');

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
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');

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
    Route::get('/groups/{group}/statistics', [GroupStatisticsController::class, 'show'])->name('groups.statistics');
    Route::get('/moderation', [AdminModerationController::class, 'index'])->name('moderation.index');
    Route::put('/moderation/settings', [AdminModerationController::class, 'updateSettings'])->name('moderation.settings.update');
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
    Route::get('/groups/{group}/statistics', [GroupStatisticsController::class, 'show'])->name('groups.statistics');
    Route::delete('/groups/{group}', [LecturerGroupController::class, 'destroy'])->name('groups.destroy');
    Route::get('/moderation', [LecturerGroupController::class, 'moderation'])->name('moderation.index');
    Route::post('/moderation/reports/{report}/resolve', [LecturerGroupController::class, 'resolveReport'])->name('moderation.reports.resolve');

    // Quiz Management (names inherit 'lecturer.' prefix from group)
    Route::get('/quizzes', [QuizController::class, 'index']);
    Route::get('/groups/{group}/quizzes/create', [QuizController::class, 'create']);
    Route::post('/groups/{group}/quizzes', [QuizController::class, 'store']);
});
