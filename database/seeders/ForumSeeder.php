<?php

namespace Database\Seeders;

use App\Models\Group;
use App\Models\Topic;
use App\Models\Message;
use App\Models\Quiz;
use App\Models\QuizQuestion;
use App\Models\QuizAttempt;
use App\Models\QuizAnswer;
use App\Models\User;
use Illuminate\Database\Seeder;

class ForumSeeder extends Seeder
{
    public function run(): void
    {
        $users = User::all();
        if ($users->isEmpty()) {
            $this->command->error('No users found — run UserSeeder first.');
            return;
        }

        $creator = $users->first();

        $groupData = [
            'Algorithms',
            'Web Development',
            'Databases',
            'Machine Learning',
            'UI/UX',
        ];

        foreach ($groupData as $name) {
            $group = Group::create([
                'name' => $name,
                'description' => "Discussion group for $name",
                'created_by' => $creator->id,
            ]);

            // Create 3 topics per group
            for ($t = 1; $t <= 3; $t++) {
                $topicAuthor = $users->random();
                $topic = Topic::create([
                    'title' => "$name Topic $t",
                    'description' => "Discussion post $t in $name",
                    'group_id' => $group->id,
                    'user_id' => $topicAuthor->id,
                ]);

                // Create an initial message as the topic content
                Message::create([
                    'content' => "This is the first message for $topic->title",
                    'topic_id' => $topic->id,
                    'user_id' => $topicAuthor->id,
                ]);

                // Add 3 realistic replies
                $replyTemplates = [
                    "Nice explanation — I tried this approach and it worked well for small datasets.",
                    "Can you share which version you used? I ran into a similar error on v2.1.",
                    "I implemented a solution with an index on the foreign key; that improved performance significantly.",
                    "Great question — a workaround is to normalize that field and cache results.",
                    "Thanks for sharing — this helped me debug my own implementation.",
                    "Have you considered using pagination here? It reduces memory usage.",
                ];

                for ($r = 1; $r <= 3; $r++) {
                    $replier = $users->random();
                    $template = $replyTemplates[array_rand($replyTemplates)];
                    $content = $template;
                    Message::create([
                        'content' => $content,
                        'topic_id' => $topic->id,
                        'user_id' => $replier->id,
                    ]);
                }
            }

            // Create a quiz for the group
            $quiz = Quiz::create([
                'group_id' => $group->id,
                'created_by' => $creator->id,
                'title' => "$name Quick Quiz",
                'description' => "A short quiz for $name",
                'start_time' => now()->subHour(),
                'end_time' => now()->addDay(),
                'duration_minutes' => 30,
                'shuffle_questions' => false,
                'show_results_after' => 'immediately',
                'published' => true,
            ]);

            // Add 3 questions
            for ($q = 1; $q <= 3; $q++) {
                $question = QuizQuestion::create([
                    'quiz_id' => $quiz->id,
                    'question_text' => "{$name} question $q?",
                    'question_type' => 'multiple_choice',
                    'options' => json_encode(['A', 'B', 'C', 'D']),
                    'correct_answer' => 'A',
                    'points' => 1,
                    'order_index' => $q,
                ]);
            }

            // Create one attempt by a random student (not the creator)
            $student = $users->where('id', '!=', $creator->id)->random();
            $attempt = QuizAttempt::create([
                'quiz_id' => $quiz->id,
                'user_id' => $student->id,
                'started_at' => now()->subMinutes(10),
                'submitted_at' => now(),
                'time_taken_seconds' => 600,
                'score' => 3,
            ]);

            // Add answers for the attempt
            foreach ($quiz->questions as $question) {
                QuizAnswer::create([
                    'attempt_id' => $attempt->id,
                    'question_id' => $question->id,
                    'user_answer' => 'A',
                    'is_correct' => true,
                    'points_earned' => 1,
                ]);
            }
        }

        $this->command->info('Created ' . count($groupData) . ' groups with topics, replies, and quizzes.');
    }
}
