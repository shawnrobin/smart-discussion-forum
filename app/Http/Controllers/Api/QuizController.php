<?php

namespace App\Http\Controllers\Api;

use App\Models\Quiz;
use App\Models\QuizQuestion;
use App\Models\QuizAttempt;
use App\Models\QuizAnswer;
use App\Models\Group;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class QuizController
{
    /**
     * Get all quizzes for user (Requirement #10)
     */
    public function index(Request $request)
    {
        $quizzes = Quiz::with('creator', 'group')
            ->where('published', true)
            ->latest()
            ->get()
            ->map(function ($quiz) {
                $quiz->status = $this->getQuizStatus($quiz);
                return $quiz;
            });

        return response()->json([
            'success' => true,
            'quizzes' => $quizzes
        ]);
    }

    /**
     * Get specific quiz details
     */
    public function show(Quiz $quiz)
    {
        $quiz->load('questions');

        // Don't show correct answers before attempt
        $quiz->questions->each(function ($question) {
            unset($question->correct_answer);
        });

        return response()->json([
            'success' => true,
            'quiz' => $quiz,
            'status' => $this->getQuizStatus($quiz)
        ]);
    }

    /**
     * Requirement #10: Create quiz (lecturer/admin only)
     */
    public function store(Request $request, Group $group)
    {
        // Only lecturers and admins can create quizzes
        if (!in_array(auth()->user()->role, ['lecturer', 'system_admin', 'group_admin'])) {
            return response()->json([
                'success' => false,
                'message' => 'Only lecturers can create quizzes'
            ], 403);
        }

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'nullable|string|max:1000',
            'start_time' => 'required|date_format:Y-m-d H:i:s|after:now',
            'end_time' => 'required|date_format:Y-m-d H:i:s|after:start_time',
            'duration_minutes' => 'required|integer|min:5|max:480',
            'shuffle_questions' => 'boolean',
            'show_results_after' => 'in:immediately,after_end,manually',
            'questions' => 'required|array|min:1',
            'questions.*.question_text' => 'required|string',
            'questions.*.question_type' => 'required|in:multiple_choice,short_answer,true_false',
            'questions.*.options' => 'nullable|array',
            'questions.*.correct_answer' => 'required|string',
            'questions.*.points' => 'required|integer|min:1'
        ]);

        // Create quiz
        $quiz = Quiz::create([
            'title' => $validated['title'],
            'description' => $validated['description'] ?? null,
            'group_id' => $group->id,
            'created_by' => auth()->id(),
            'start_time' => $validated['start_time'],
            'end_time' => $validated['end_time'],
            'duration_minutes' => $validated['duration_minutes'],
            'shuffle_questions' => $validated['shuffle_questions'] ?? true,
            'show_results_after' => $validated['show_results_after'] ?? 'immediately',
            'published' => true
        ]);

        // Create questions
        foreach ($validated['questions'] as $index => $questionData) {
            QuizQuestion::create([
                'quiz_id' => $quiz->id,
                'question_text' => $questionData['question_text'],
                'question_type' => $questionData['question_type'],
                'options' => $questionData['options'] ?? null,
                'correct_answer' => $questionData['correct_answer'],
                'points' => $questionData['points'],
                'order_index' => $index + 1
            ]);
        }

        return response()->json([
            'success' => true,
            'message' => 'Quiz created successfully',
            'quiz' => $quiz
        ], 201);
    }

    /**
     * Requirement #10: Start quiz attempt (user takes quiz)
     * Requirement #10: Timer starts, lockdown begins on desktop
     */
    public function startAttempt(Request $request, Quiz $quiz)
    {
        // Check if quiz is active
        if (now() < $quiz->start_time || now() > $quiz->end_time) {
            return response()->json([
                'success' => false,
                'message' => 'Quiz is not available at this time'
            ], 403);
        }

        // Check if user already attempted
        $existingAttempt = QuizAttempt::where('quiz_id', $quiz->id)
            ->where('user_id', auth()->id())
            ->where('submitted_at', null)
            ->first();

        if ($existingAttempt) {
            return response()->json([
                'success' => true,
                'message' => 'Resuming existing attempt',
                'attempt' => $existingAttempt,
                'quiz' => $quiz->load('questions')
            ]);
        }

        // Create new attempt
        $attempt = QuizAttempt::create([
            'quiz_id' => $quiz->id,
            'user_id' => auth()->id(),
            'started_at' => now()
        ]);

        $quiz->load('questions');

        // Shuffle questions if needed
        if ($quiz->shuffle_questions) {
            $quiz->questions = $quiz->questions->shuffle();
        }

        // Remove correct answers from response
        $quiz->questions->each(function ($question) {
            unset($question->correct_answer);
        });

        return response()->json([
            'success' => true,
            'message' => 'Quiz attempt started',
            'attempt' => $attempt,
            'quiz' => $quiz,
            'duration_minutes' => $quiz->duration_minutes,
            'lockdown' => true  // Desktop should enter lockdown mode
        ]);
    }

    /**
     * Requirement #10: Submit quiz answers
     * Requirement #10: Auto-submit if time expires
     */
    public function submitAttempt(Request $request, QuizAttempt $attempt)
    {
        $validated = $request->validate([
            'answers' => 'required|array',
            'answers.*.question_id' => 'required|integer|exists:quiz_questions,id',
            'answers.*.user_answer' => 'required|string'
        ]);

        // Check if quiz time has expired
        $quiz = $attempt->quiz;
        if (now() > $quiz->end_time) {
            return response()->json([
                'success' => false,
                'message' => 'Quiz time has expired'
            ], 403);
        }

        // Calculate time taken
        $timeTaken = now()->diffInSeconds($attempt->started_at);
        
        // Check if exceeded duration
        if ($timeTaken > ($quiz->duration_minutes * 60)) {
            return response()->json([
                'success' => false,
                'message' => 'Quiz time limit exceeded'
            ], 403);
        }

        // Grade and store answers
        $totalPoints = 0;
        $earnedPoints = 0;

        foreach ($validated['answers'] as $answerData) {
            $question = QuizQuestion::findOrFail($answerData['question_id']);
            $totalPoints += $question->points;

            // Check if answer is correct
            $isCorrect = strtolower(trim($answerData['user_answer'])) === 
                        strtolower(trim($question->correct_answer));

            $pointsEarned = $isCorrect ? $question->points : 0;
            $earnedPoints += $pointsEarned;

            // Store answer
            QuizAnswer::create([
                'attempt_id' => $attempt->id,
                'question_id' => $question->id,
                'user_answer' => $answerData['user_answer'],
                'is_correct' => $isCorrect,
                'points_earned' => $pointsEarned
            ]);
        }

        // Calculate score as percentage
        $score = ($earnedPoints / $totalPoints) * 100;

        // Update attempt
        $attempt->update([
            'submitted_at' => now(),
            'time_taken_seconds' => $timeTaken,
            'score' => $score
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Quiz submitted successfully',
            'score' => $score,
            'attempt' => $attempt
        ]);
    }

    /**
     * Get quiz results (Requirement #10: Show results after completion)
     */
    public function getResults(QuizAttempt $attempt)
    {
        // Only attempt owner or admins can view results
        if ($attempt->user_id !== auth()->id() && !auth()->user()->isAdmin()) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized'
            ], 403);
        }

        $attempt->load('answers.question', 'quiz');

        // Format results
        $results = [
            'attempt_id' => $attempt->id,
            'score' => $attempt->score,
            'time_taken' => $attempt->time_taken_seconds,
            'submitted_at' => $attempt->submitted_at,
            'answers' => $attempt->answers->map(function ($answer) {
                return [
                    'question_id' => $answer->question_id,
                    'question_text' => $answer->question->question_text,
                    'user_answer' => $answer->user_answer,
                    'correct_answer' => $answer->question->correct_answer,
                    'is_correct' => $answer->is_correct,
                    'points_earned' => $answer->points_earned,
                    'total_points' => $answer->question->points
                ];
            })
        ];

        return response()->json([
            'success' => true,
            'results' => $results
        ]);
    }

    /**
     * Helper: Get quiz status (upcoming, active, closed)
     */
    private function getQuizStatus($quiz)
    {
        if (now() < $quiz->start_time) {
            return 'upcoming';
        } elseif (now() > $quiz->end_time) {
            return 'closed';
        } else {
            return 'active';
        }
    }
}