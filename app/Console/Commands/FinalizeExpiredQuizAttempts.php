<?php

namespace App\Console\Commands;

use App\Models\QuizAnswer;
use App\Models\QuizAttempt;
use Illuminate\Console\Command;

class FinalizeExpiredQuizAttempts extends Command
{
    protected $signature = 'quizzes:finalize-expired';
    protected $description = 'Automatically submit quiz attempts whose server deadline has passed';

    public function handle(): int
    {
        QuizAttempt::whereNull('submitted_at')
            ->whereNotNull('deadline_at')
            ->where('deadline_at', '<=', now())
            ->with('quiz.questions')
            ->each(function (QuizAttempt $attempt) {
                $totalPoints = 0;
                foreach ($attempt->quiz->questions as $question) {
                    $totalPoints += $question->points;
                    QuizAnswer::updateOrCreate(
                        ['attempt_id' => $attempt->id, 'question_id' => $question->id],
                        ['user_answer' => '', 'is_correct' => false, 'points_earned' => 0],
                    );
                }

                $submittedAt = now();
                $attempt->update([
                    'submitted_at' => $submittedAt,
                    'auto_submitted_at' => $submittedAt,
                    'score' => 0,
                    'time_taken_seconds' => $attempt->deadline_at->diffInSeconds($attempt->started_at),
                ]);
            });

        return self::SUCCESS;
    }
}
