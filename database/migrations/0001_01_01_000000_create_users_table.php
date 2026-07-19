<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // 1. Create Base Tables
        Schema::create('users', function (Blueprint $table) {
            $table->id();
            $table->string('name');
            $table->string('email')->unique();
            $table->string('username')->unique();
            $table->string('password');
            $table->enum('role', ['student', 'lecturer', 'group_admin', 'system_admin'])->default('student');
            $table->enum('status', ['active', 'inactive', 'blacklisted'])->default('active');
            $table->boolean('blacklisted')->default(false); // Added missing column
            $table->boolean('is_admin')->default(false);    // Added missing column
            $table->timestamp('last_login')->nullable();
            $table->integer('inactive_warnings')->default(0);
            $table->timestamps();
        });

        // 2. Add extra profile fields to users
        Schema::table('users', function (Blueprint $table) {
            $table->string('avatar')->nullable();
            $table->text('bio')->nullable();
            $table->string('course')->nullable();
            $table->string('faculty')->nullable();
            $table->integer('year_of_study')->nullable();
            $table->string('phone_number')->nullable();
        });

        // ... Keep all other Schema::create blocks exactly as they were
        Schema::create('groups', function (Blueprint $table) {
            $table->id();
            $table->string('name');
            $table->text('description')->nullable();
            $table->foreignId('created_by')->constrained('users');
            $table->integer('max_members')->nullable();
            $table->timestamps();
        });

        Schema::create('group_user', function (Blueprint $table) {
            $table->id();
            $table->foreignId('group_id')->constrained('groups')->onDelete('cascade');
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->timestamp('joined_at')->useCurrent();
            $table->unique(['group_id', 'user_id']);
        });

        Schema::create('topics', function (Blueprint $table) {
            $table->id();
            $table->string('title', 500);
            $table->text('description')->nullable();
            $table->foreignId('group_id')->constrained('groups')->onDelete('cascade');
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->boolean('is_question')->default(false);
            $table->boolean('is_answered')->default(false);
            $table->enum('flagged_status', ['clean', 'flagged', 'removed'])->default('clean');
            $table->integer('likes')->default(0);
            $table->timestamps();
        });

        Schema::create('messages', function (Blueprint $table) {
            $table->id();
            $table->text('content');
            $table->foreignId('topic_id')->constrained('topics')->onDelete('cascade');
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->foreignId('parent_message_id')->nullable()->constrained('messages')->onDelete('cascade');
            $table->boolean('is_answer')->default(false);
            $table->integer('likes')->default(0);
            $table->enum('flagged_status', ['clean', 'flagged', 'removed'])->default('clean');
            $table->timestamps();
        });

        Schema::create('user_exclusions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->foreignId('excluded_user_id')->constrained('users')->onDelete('cascade');
            $table->foreignId('message_id')->nullable()->constrained('messages')->onDelete('cascade');
            $table->timestamps();
            $table->unique(['user_id', 'excluded_user_id', 'message_id'], 'unique_exclusion');
        });

        Schema::create('onboarding_rules', function (Blueprint $table) {
            $table->id();
            $table->string('title');
            $table->text('content');
            $table->integer('version')->default(1);
            $table->timestamps();
        });

        Schema::create('user_onboarding', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->integer('rules_version');
            $table->timestamp('agreed_at');
            $table->timestamps();
        });

        Schema::create('moderation_logs', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->enum('action', ['warning_issued', 'blacklisted', 'content_flagged', 'content_removed']);
            $table->text('reason')->nullable();
            $table->foreignId('admin_id')->constrained('users');
            $table->timestamp('expires_at')->nullable();
            $table->timestamps();
        });

        Schema::create('participation_scores', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->foreignId('group_id')->constrained('groups')->onDelete('cascade');
            $table->integer('topics_created')->default(0);
            $table->integer('topics_replied')->default(0);
            $table->integer('answers_provided')->default(0);
            $table->decimal('total_score', 8, 2)->default(0);
            $table->timestamps();
            $table->unique(['user_id', 'group_id']);
        });

        Schema::create('quizzes', function (Blueprint $table) {
            $table->id();
            $table->string('title');
            $table->text('description')->nullable();
            $table->foreignId('group_id')->constrained('groups')->onDelete('cascade');
            $table->foreignId('created_by')->constrained('users');
            $table->dateTime('start_time');
            $table->dateTime('end_time');
            $table->integer('duration_minutes');
            $table->boolean('shuffle_questions')->default(true);
            $table->enum('show_results_after', ['immediately', 'after_end', 'manually'])->default('immediately');
            $table->boolean('published')->default(false);
            $table->timestamps();
        });

        Schema::create('quiz_questions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('quiz_id')->constrained('quizzes')->onDelete('cascade');
            $table->text('question_text');
            $table->enum('question_type', ['multiple_choice', 'short_answer', 'true_false']);
            $table->json('options')->nullable();
            $table->text('correct_answer');
            $table->integer('points')->default(1);
            $table->integer('order_index');
            $table->timestamps();
        });

        Schema::create('quiz_attempts', function (Blueprint $table) {
            $table->id();
            $table->foreignId('quiz_id')->constrained('quizzes')->onDelete('cascade');
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->dateTime('started_at');
            $table->dateTime('submitted_at')->nullable();
            $table->integer('time_taken_seconds')->nullable();
            $table->decimal('score', 5, 2)->nullable();
            $table->timestamps();
        });

        Schema::create('quiz_answers', function (Blueprint $table) {
            $table->id();
            $table->foreignId('attempt_id')->constrained('quiz_attempts')->onDelete('cascade');
            $table->foreignId('question_id')->constrained('quiz_questions')->onDelete('cascade');
            $table->text('user_answer')->nullable();
            $table->boolean('is_correct')->nullable();
            $table->integer('points_earned')->default(0);
            $table->timestamps();
        });

        Schema::create('notifications', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->string('type');
            $table->string('title')->nullable();
            $table->text('message')->nullable();
            $table->integer('related_entity_id')->nullable();
            $table->string('related_entity_type')->nullable();
            $table->boolean('read')->default(false);
            $table->timestamps();
        });

        Schema::create('sync_queue', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->string('entity_type');
            $table->integer('entity_id')->nullable();
            $table->enum('operation', ['create', 'update', 'delete']);
            $table->json('data')->nullable();
            $table->boolean('synced')->default(false);
            $table->timestamps();
        });

        // TOPIC RECOMMENDATIONS TABLE (Requirement #11: ML recommendations)
        Schema::create('topic_recommendations', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->foreignId('topic_id')->constrained('topics')->onDelete('cascade');
            $table->decimal('score', 5, 2);
            $table->string('reason')->nullable();
            $table->timestamps();
        });
        // Add extra columns to users table
        Schema::table('users', function (Blueprint $table) {
            $table->string('avatar')->nullable();
            $table->text('bio')->nullable();
            $table->string('course')->nullable();
            $table->string('faculty')->nullable();
            $table->integer('year_of_study')->nullable();
            $table->string('phone_number')->nullable();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('topic_recommendations');
        Schema::dropIfExists('sync_queue');
        Schema::dropIfExists('notifications');
        Schema::dropIfExists('quiz_answers');
        Schema::dropIfExists('quiz_attempts');
        Schema::dropIfExists('quiz_questions');
        Schema::dropIfExists('quizzes');
        Schema::dropIfExists('participation_scores');
        Schema::dropIfExists('moderation_logs');
        Schema::dropIfExists('user_onboarding');
        Schema::dropIfExists('onboarding_rules');
        Schema::dropIfExists('user_exclusions');
        Schema::dropIfExists('messages');
        Schema::dropIfExists('topics');
        Schema::dropIfExists('group_user');
        Schema::dropIfExists('groups');
        Schema::dropIfExists('users');
    }
};
