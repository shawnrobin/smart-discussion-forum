<?php

namespace App\Http\Controllers;

use App\Models\Group;
use App\Models\Quiz;
use App\Models\QuizAttempt;
use App\Models\QuizAnswer;
use App\Models\QuizQuestion;
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
        $quizzes = Quiz::with([
            'creator',
            'group',
            'attempts' => function ($query) {
                $query->where('user_id', auth()->id());
            }
        ])
            ->where('published', true)
            ->latest()
            ->get()
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

                // Add display fields
                $quiz->instructor = $quiz->creator->name ?? 'Unknown';
                $quiz->duration = $quiz->duration_minutes;
                $quiz->questions_count = $quiz->questions()->count();
                $quiz->attempted = $attempt && !$attempt->submitted_at;

                return $quiz;
            });

        return Inertia::render('Quizzes/Index', [
            'quizzes' => $quizzes
        ]);
    }

    /**
     * Show quiz creation form.
     */
    /**
     * Create a new quiz.
     */
    public function create(Group $group)
    {
        // Only lecturers and admins can create quizzes
        if (!in_array(auth()->user()->role, ['lecturer', 'system_admin', 'group_admin'])) {
            return redirect()->back()->with('error', 'Only lecturers can create quizzes.');
        }

        return Inertia::render('Quizzes/Create', [
            'group' => $group
        ]);
    }

    /**
     * Show quiz details — routes to Announcement, Show, or Results based on status.
     */
    public function show(Quiz $quiz)
    {
        $status = $this->getQuizStatus($quiz);

        if ($status === 'upcoming') {
            return Inertia::render('Quizzes/Announcement', ['quiz' => $quiz]);
        }

        if ($status === 'closed') {
            $attempt = $quiz->attempts()->where('user_id', Auth::id())->first();
            return $attempt
                ? redirect()->route('quizzes.results', $attempt)
                : redirect()->route('quizzes.index');
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
        // Only lecturers and admins can create quizzes
        if (!in_array(auth()->user()->role, ['lecturer', 'system_admin', 'group_admin'])) {
            return redirect()->back()->with('error', 'Only lecturers can create quizzes.');
        }

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string|max:1000',
            'start_time' => 'required|date',
            'end_time' => 'required|date|after:start_time',
            'duration_minutes' => 'required|integer|min:5|max:480',
            'shuffle_questions' => 'boolean',
            'show_results_after' => 'nullable|in:immediately,after_end,manually',
            'questions' => 'required|array|min:1',
            'questions.*.question_text' => 'required|string',
            'questions.*.question_type' => 'required|in:multiple_choice,true_false,short_answer',
            'questions.*.options' => 'nullable|array',
            'questions.*.correct_answer' => 'required|string',
            'questions.*.points' => 'required|integer|min:1',
        ]);

        $quiz = Quiz::create([
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'group_id' => $group->id,
            'created_by' => Auth::id(),
            'start_time' => $validated['start_time'],
            'end_time' => $validated['end_time'],
            'duration_minutes' => $validated['duration_minutes'],
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

        return redirect()->route('quizzes.show', $quiz);
    }

    /**
     * Start quiz attempt.
     */
    public function start(Quiz $quiz)
    {
        if ($this->getQuizStatus($quiz) !== 'active') {
            return redirect()->back()->with('error', 'Quiz is not currently available.');
        }

        $attempt = QuizAttempt::firstOrCreate(
            [
                'quiz_id' => $quiz->id,
                'user_id' => auth()->id(),
                'submitted_at' => null,
            ],
            ['started_at' => now()]
        );

        $quiz->load('questions');

        // Calculate remaining time for late joiners
        $quizEnd = $quiz->end_time;
        $now = now();
        $remainingSeconds = max(0, $now->diffInSeconds($quizEnd, false));
        $durationMinutes = min($quiz->duration_minutes, ceil($remainingSeconds / 60));

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
            'duration' => $durationMinutes,
        ]);
    }

    /**
     * Submit quiz.
     */
    public function submit(Request $request, QuizAttempt $attempt)
    {
        $validated = $request->validate([
            'answers' => 'required|array',
            'answers.*.question_id' => 'required|exists:quiz_questions,id',
            'answers.*.user_answer' => 'nullable|string',
        ]);

        $quiz = $attempt->quiz;

        $totalPoints = 0;
        $earnedPoints = 0;

        foreach ($validated['answers'] as $answerData) {
            $question = QuizQuestion::findOrFail($answerData['question_id']);

            $userAnswer = trim($answerData['user_answer'] ?? '');
            $correctAnswer = trim($question->correct_answer ?? '');

            // Grade: case-insensitive comparison for short answer,
            // exact match for multiple choice / true-false
            $correct = strtolower($userAnswer) === strtolower($correctAnswer);

            $points = $correct ? $question->points : 0;

            $totalPoints += $question->points;
            $earnedPoints += $points;

            QuizAnswer::create([
                'attempt_id' => $attempt->id,
                'question_id' => $question->id,
                'user_answer' => $userAnswer ?: '(no answer)',
                'is_correct' => $correct,
                'points_earned' => $points,
            ]);
        }

        $score = $totalPoints > 0 ? round(($earnedPoints / $totalPoints) * 100, 1) : 0;

        $attempt->update([
            'submitted_at' => now(),
            'score' => $score,
            'time_taken_seconds' => now()->diffInSeconds($attempt->started_at),
        ]);

        return redirect()
            ->route('quizzes.results', $attempt)
            ->with('success', 'Quiz submitted successfully!');
    }

    /**
     * Results report — visible to the attempt owner and all group members.
     */
    public function results(QuizAttempt $attempt)
    {
        $quiz = $attempt->quiz;
        $group = $quiz->group;

        // Allow: attempt owner, group members, admins
        $isOwner = $attempt->user_id === auth()->id();
        $isGroupMember = $group->users()->where('user_id', auth()->id())->exists();
        $isAdmin = auth()->user()->isAdmin();

        if (!$isOwner && !$isGroupMember && !$isAdmin) {
            abort(403);
        }

        $attempt->load([
            'quiz.questions',
            'answers.question'
        ]);

        return Inertia::render('Quizzes/Results', [
            'attempt' => $attempt,
        ]);
    }

    /**
     * Performance report — aggregate scores for all members of the group.
     * Visible to ALL group members after the quiz closes, or immediately
     * to anyone who has already submitted.
     */
    public function performance(Quiz $quiz)
    {
        $group = $quiz->group;

        // All attempts for this quiz, with user info
        $attempts = QuizAttempt::where('quiz_id', $quiz->id)
            ->with('user')
            ->whereNotNull('submitted_at')
            ->orderByDesc('score')
            ->get()
            ->map(function ($a) {
                return [
                    'user_name' => $a->user->name ?? 'Unknown',
                    'score' => $a->score,
                    'time_taken' => $a->time_taken_seconds,
                    'submitted_at' => $a->submitted_at,
                ];
            });

        $totalStudents = $group ? $group->users()->count() : 0;
        $participated = $attempts->count();
        $averageScore = $participated > 0 ? round($attempts->avg('score'), 1) : 0;

        return Inertia::render('Quizzes/Performance', [
            'quiz' => $quiz,
            'group' => $group,
            'attempts' => $attempts,
            'stats' => [
                'total_students' => $totalStudents,
                'participated' => $participated,
                'average_score' => $averageScore,
            ],
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

        if (now()->gt($quiz->end_time)) {
            return 'closed';
        }

        return 'active';
    }
}
