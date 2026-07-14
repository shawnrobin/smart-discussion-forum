<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('user_settings', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->unique()->constrained()->onDelete('cascade');

            $table->string('theme')->default('light');
            $table->string('theme_color')->nullable();

            $table->boolean('notify_replies')->default(true);
            $table->boolean('notify_mentions')->default(true);
            $table->boolean('notify_group_posts')->default(true);
            $table->boolean('notify_quiz')->default(true);
            $table->boolean('notify_email')->default(true);

            $table->boolean('is_private')->default(false);
            $table->boolean('show_online_status')->default(true);
            $table->string('who_can_message')->default('everyone');

            $table->string('default_sort')->default('latest');
            $table->integer('posts_per_page')->default(10);
            $table->string('language')->default('en');
            $table->boolean('auto_save_drafts')->default(true);

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('user_settings');
    }
};