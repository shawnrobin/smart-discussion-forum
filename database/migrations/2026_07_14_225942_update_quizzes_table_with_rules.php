<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
{
    Schema::table('quizzes', function (Blueprint $table) {
        if (!Schema::hasColumn('quizzes', 'start_time')) {
            $table->timestamp('start_time')->nullable();
        }
        if (!Schema::hasColumn('quizzes', 'end_time')) {
            $table->timestamp('end_time')->nullable();
        }
        if (!Schema::hasColumn('quizzes', 'duration_minutes')) {
            $table->integer('duration_minutes')->nullable();
        }
        if (!Schema::hasColumn('quizzes', 'target_category')) {
            $table->string('target_category')->nullable();
        }
        if (!Schema::hasColumn('quizzes', 'is_published')) {
            $table->boolean('is_published')->default(false);
        }
    });
}

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        //
    }
};
