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
        $table->timestamp('start_time')->nullable();
        $table->timestamp('end_time')->nullable();
        $table->integer('duration_minutes');
        $table->string('target_category'); // e.g., 'Year 1', 'Section B'
        $table->boolean('is_published')->default(false);
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
