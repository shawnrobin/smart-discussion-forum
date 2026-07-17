<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // 1. Quizzes Table
        if (!Schema::hasTable('quizzes')) {
            Schema::create('quizzes', function (Blueprint $table) {
                $table->id();
                $table->string('title');
                $table->text('description')->nullable();
                $table->foreignId('lecturer_id')->constrained('users')->onDelete('cascade');
                $table->timestamps();
            });
        }

        // 2. Questions Table
        if (!Schema::hasTable('questions')) {
            Schema::create('questions', function (Blueprint $table) {
                $table->id();
                $table->foreignId('quiz_id')->constrained('quizzes')->onDelete('cascade');
                $table->text('question_text');
                $table->json('options');
                $table->string('correct_answer');
                $table->timestamps();
            });
        }

        // 3. Quiz Attempts Table
        if (!Schema::hasTable('quiz_attempts')) {
            Schema::create('quiz_attempts', function (Blueprint $table) {
                $table->id();
                $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
                $table->foreignId('quiz_id')->constrained('quizzes')->onDelete('cascade');
                $table->integer('score');
                $table->timestamps();
            });
        }
    }

    public function down(): void
    {
        Schema::dropIfExists('quiz_attempts');
        Schema::dropIfExists('questions');
        Schema::dropIfExists('quizzes');
    }
};