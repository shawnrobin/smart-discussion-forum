<?php

namespace App\Http\Controllers;

use App\Models\Group;
use App\Models\Quiz;
use App\Models\QuizAttempt;
use App\Models\QuizAnswer;
use App\Models\QuizQuestion;
use App\Models\Notification;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Inertia\Inertia;
use Illuminate\Support\Facades\Auth;

class QuizController extends Controller
{
    /**
     * Display all quizzes.
     */
    public function index()
    {
        abort_unless(auth()->user()->role === 'student', 403, 'Only students can take quizzes.');
        $quizzes = Quiz::with([
            'creator',
            'group',
            'attempts' => function ($query) {
                $query->where('user_id', auth()->id());
            }
        ])
            ->withCount('questions')
            ->where('published', true)
            ->whereIn('group_id', auth()->user()->groups()->pluck('groups.id'))
            ->latest()
            ->get()
            ->filter(fn ($quiz) => $this->canTakeQuiz($quiz, auth()->user()))
            ->map(function ($quiz) {

                // Get the logged-in student's attempt
                $attempt = $quiz->attempts->first();

                // If student already submitted
                if ($attempt && $attempt->submitted_at) {
                    $quiz->status = 'completed';
                    $quiz->score = $attempt->score;
                } else {
                    // Otherwise use normal quiz status
                    $quiz->status = $this->getQuizStatus($quiz);
                }

                $quiz->instructor = $quiz->creator->name ?? 'Lecturer';
                $quiz->duration = $quiz->duration_minutes;
                $quiz->questions = $quiz->questions_count;

                return $quiz;
            });

        return Inertia::render('Quizzes/Index', [
            'quizzes' => $quizzes
        ]);
    }

    /**
     * Show quiz creation form.
     */
    public function create(Group $group)
    {
        $this->ensureCanManageGroup($group);

        return Inertia::render('Quizzes/Create', [
            'group' => $group,
            'targetCategories' => $this->targetCategories($group),
        ]);
    }

    /**
     * Show quiz details — routes to Announcement, Show, or Results based on status.
     */
    public function show(Quiz $quiz)
    {
        abort_unless($this->canTakeQuiz($quiz, Auth::user()), 403);
        $status = $this->getQuizStatus($quiz);

        if ($status === 'upcoming') {
            return Inertia::render('Quizzes/Announcement', ['quiz' => $quiz]);
        }

        if ($status === 'closed') {
            // All eligible group members can see the performance report once closed
            return redirect()->route('quizzes.performance', $quiz);
        }

        // Active — hide answers before attempt
        $quiz->load('questions');
        $quiz->questions->each(function ($question) {
            unset($question->correct_answer);
        });

        return Inertia::render('Quizzes/Show', [
            'quiz' => $quiz,
            'status' => $status
        ]);
    }

    /**
     * Store a new quiz.
     */
    public function store(Request $request, Group $group)
    {
        $this->ensureCanManageGroup($group);

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string|max:1000',
            'start_time' => 'required|date',
            'end_time' => 'required|date|after:start_time',
            'duration_minutes' => 'required|integer|min:5|max:480',
            'target_category' => 'required|string|max:255',
            'shuffle_questions' => 'boolean',
            'show_results_after' => 'nullable|in:immediately,after_end,manually',
            'questions' => 'required|array|min:1',
            'questions.*.question_text' => 'required|string',
            'questions.*.question_type' => 'required|in:multiple_choice,true_false,short_answer',
            'questions.*.options' => 'nullable|array',
            'questions.*.correct_answer' => 'required|string',
            'questions.*.points' => 'required|integer|min:1',
        ]);

        $startTime = Carbon::parse($validated['start_time'], config('app.timezone'));
        $endTime = Carbon::parse($validated['end_time'], config('app.timezone'));

        $quiz = Quiz::create([
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'group_id' => $group->id,
            'created_by' => Auth::id(),
            'start_time' => $startTime,
            'end_time' => $endTime,
            'duration_minutes' => $validated['duration_minutes'],
            'target_category' => $validated['target_category'],
            'shuffle_questions' => $validated['shuffle_questions'] ?? true,
            'show_results_after' => $validated['show_results_after'] ?? 'immediately',
            'published' => true,
        ]);

        foreach ($validated['questions'] as $index => $question) {
            QuizQuestion::create([
                'quiz_id' => $quiz->id,
                'question_text' => $question['question_text'],
                'question_type' => $question['question_type'],
                'options' => $question['options'] ?? null,
                'correct_answer' => $question['correct_answer'],
                'points' => $question['points'],
                'order_index' => $index + 1,
            ]);
        }

        $group->users->filter(fn ($user) => $this->canTakeQuiz($quiz, $user))->each(function ($user) use ($quiz) {
            Notification::create([
                'user_id' => $user->id,
                'type' => 'quiz_announcement',
                'title' => 'New quiz announced: ' . $quiz->title,
                'message' => 'Starts ' . $quiz->start_time->format('d M Y, H:i') . ' (Africa/Nairobi). Duration: ' . $quiz->duration_minutes . ' minutes.',
                'related_entity_id' => $quiz->id,
                'related_entity_type' => Quiz::class,
            ]);
        });

        return redirect()->route('lecturer.groups.show', $group)
            ->with('success', 'Quiz published and announced to eligible students.');
    }

    /**
     * Start quiz attempt.
     */
    public function start(Quiz $quiz)
    {
        abort_unless($this->canTakeQuiz($quiz, Auth::user()), 403);
        if ($this->getQuizStatus($quiz) !== 'active') {
            return redirect()->back()->with('error', 'Quiz is not currently available.');
        }

        $attempt = QuizAttempt::firstOrCreate(
            [
                'quiz_id' => $quiz->id,
                'user_id' => auth()->id(),
            ],
            ['started_at' => now(), 'deadline_at' => now()->addMinutes($quiz->duration_minutes)->min($quiz->end_time)]
        );

        if ($attempt->submitted_at) {
            return redirect()->route('quizzes.results', $attempt);
        }

        if (!$attempt->deadline_at || $attempt->deadline_at->gt($quiz->end_time)) {
            $attempt->update(['deadline_at' => $attempt->started_at->copy()->addMinutes($quiz->duration_minutes)->min($quiz->end_time)]);
        }

        if (now()->gte($attempt->deadline_at)) {
            $this->finalizeAttempt($attempt, [], true);
            return redirect()->route('quizzes.results', $attempt);
        }

        $quiz->load('questions');

        if ($quiz->shuffle_questions) {
            $quiz->setRelation(
                'questions',
                $quiz->questions->shuffle()->values()
            );
        }

        $quiz->questions->each(function ($question) {
            unset($question->correct_answer);
        });

        return Inertia::render('Quizzes/ActiveQuiz', [
            'quiz' => $quiz,
            'attempt' => $attempt,
            'deadlineAt' => $attempt->deadline_at->toIso8601String(),
            'serverNow' => now()->toIso8601String(),
        ]);
    }

    /**
     * Submit quiz.
     */
    public function submit(Request $request, QuizAttempt $attempt)
    {
        $validated = $request->validate([
            'answers' => 'nullable|array',
            'answers.*.question_id' => 'required|exists:quiz_questions,id',
            'answers.*.user_answer' => 'nullable|string',
        ]);

        abort_unless($attempt->user_id === Auth::id(), 403);
        if ($attempt->submitted_at) return redirect()->route('quizzes.results', $attempt);
        $expired = now()->gte($attempt->deadline_at);
        $this->finalizeAttempt($attempt, $expired ? [] : ($validated['answers'] ?? []), $expired);

        return redirect()
            ->route('quizzes.results', $attempt)
            ->with('success', 'Quiz submitted successfully!');
    }

    /**
     * Results report.
     */
    public function results(QuizAttempt $attempt)
    {
        if ($attempt->user_id !== auth()->id() && !in_array(auth()->user()->role, ['lecturer', 'system_admin', 'group_admin'], true)) {
            abort(403);
        }

        $attempt->load([
            'quiz',
            'answers.question'
        ]);

        return Inertia::render('Quizzes/Results', [
            'attempt' => $attempt
        ]);
    }

    /**
     * Helper: get quiz status.
     */
    private function getQuizStatus($quiz)
    {
        if (now()->lt($quiz->start_time)) {
            return 'upcoming';
        }

        if (now()->gte($quiz->end_time)) {
            return 'closed';
        }

        return 'active';
    }

    private function ensureCanManageGroup(Group $group): void
    {
        $user = auth()->user();

        abort_unless(
            $user && (
                ($user->role === 'lecturer' && (int) $group->created_by === (int) $user->id)
                || $user->role === 'system_admin'
            ),
            403
        );
    }

    /**
     * Performance report — visible to ALL eligible group members after the quiz closes.
     * Students see their own score + class summary.
     * Lecturers/admins see the full per-student breakdown.
     */
    public function performance(Quiz $quiz)
    {
        $user = Auth::user();
        $canManage = $user && (
            $user->role === 'system_admin' ||
            ($user->role === 'lecturer' && (int) $quiz->group->created_by === (int) $user->id)
        );

        // Any group member who could have taken the quiz (or a manager) can view the report
        $isMember = $quiz->group->users()->whereKey($user->id)->exists();
        abort_unless($isMember || $canManage, 403);
        abort_unless($this->getQuizStatus($quiz) === 'closed', 403, 'Performance report is available after the quiz closes.');

        $attempts = $quiz->attempts()
            ->whereNotNull('submitted_at')
            ->with('user:id,name,course,year_of_study')
            ->get();

        $myAttempt = $attempts->firstWhere('user_id', Auth::id());

        // Per-student leaderboard (visible to lecturers/admins only)
        $leaderboard = $canManage
            ? $attempts->sortByDesc('score')->values()->map(fn ($a) => [
                'name'       => $a->user->name ?? 'Unknown',
                'score'      => round((float) $a->score, 1),
                'time_taken' => $a->time_taken_seconds,
                'submitted_at' => $a->submitted_at?->toDateTimeString(),
                'auto'       => (bool) $a->auto_submitted_at,
              ])
            : null;

        // Distribution buckets: 0-49, 50-69, 70-84, 85-100
        $distribution = [
            '0–49'  => $attempts->where('score', '<',  50)->count(),
            '50–69' => $attempts->whereBetween('score', [50, 69])->count(),
            '70–84' => $attempts->whereBetween('score', [70, 84])->count(),
            '85–100'=> $attempts->where('score', '>=', 85)->count(),
        ];

        return Inertia::render('Quizzes/Performance', [
            'quiz'         => $quiz->only('id', 'title', 'description', 'start_time', 'end_time', 'duration_minutes'),
            'summary'      => [
                'attempts'     => $attempts->count(),
                'average'      => round((float) $attempts->avg('score'), 1),
                'highest'      => round((float) $attempts->max('score'), 1),
                'lowest'       => round((float) $attempts->min('score'), 1),
                'myScore'      => $myAttempt ? round((float) $myAttempt->score, 1) : null,
                'myTime'       => $myAttempt?->time_taken_seconds,
                'myAutoSubmit' => $myAttempt ? (bool) $myAttempt->auto_submitted_at : false,
                'distribution' => $distribution,
            ],
            'leaderboard'  => $leaderboard,
            'canManage'    => $canManage,
        ]);
    }

    private function canTakeQuiz(Quiz $quiz, $user): bool
    {
        if (!$user || $user->role !== 'student' || !$quiz->group->users()->whereKey($user->id)->exists()) return false;
        $category = $quiz->target_category ?: 'all';
        if ($category === 'all') return true;
        [$field, $value] = array_pad(explode(':', $category, 2), 2, null);
        return in_array($field, ['year_of_study', 'course', 'faculty'], true) && (string) $user->{$field} === (string) $value;
    }

    private function targetCategories(Group $group): array
    {
        $categories = [['value' => 'all', 'label' => 'All members of this group']];
        foreach (['year_of_study' => 'Year', 'course' => 'Course', 'faculty' => 'Faculty'] as $field => $label) {
            $group->users()->whereNotNull($field)->distinct()->pluck($field)->filter()->each(function ($value) use (&$categories, $field, $label) {
                $categories[] = ['value' => $field . ':' . $value, 'label' => $label . ': ' . $value];
            });
        }
        return $categories;
    }

    private function finalizeAttempt(QuizAttempt $attempt, array $answers, bool $automatic): void
    {
        $quiz = $attempt->quiz;
        $provided = collect($answers)->keyBy('question_id');
        $totalPoints = 0;
        $earnedPoints = 0;

        foreach ($quiz->questions as $question) {
            $answer = trim((string) data_get($provided->get($question->id), 'user_answer', ''));
            $correct = $answer !== '' && strcasecmp($answer, trim($question->correct_answer)) === 0;
            $points = $correct ? $question->points : 0;
            $totalPoints += $question->points;
            $earnedPoints += $points;
            QuizAnswer::updateOrCreate(['attempt_id' => $attempt->id, 'question_id' => $question->id], ['user_answer' => $answer, 'is_correct' => $correct, 'points_earned' => $points]);
        }

        $submittedAt = now();
        $attempt->update([
            'submitted_at' => $submittedAt,
            'auto_submitted_at' => $automatic ? $submittedAt : null,
            'score' => ($earnedPoints / max($totalPoints, 1)) * 100,
            'time_taken_seconds' => min($submittedAt->diffInSeconds($attempt->started_at), $attempt->deadline_at->diffInSeconds($attempt->started_at)),
        ]);
    }
}
