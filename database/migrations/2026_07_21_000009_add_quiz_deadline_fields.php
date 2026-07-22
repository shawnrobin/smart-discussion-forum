<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('quiz_attempts', function (Blueprint $table) {
            $table->dateTime('deadline_at')->nullable()->after('started_at');
            $table->dateTime('auto_submitted_at')->nullable()->after('submitted_at');
        });
    }

    public function down(): void
    {
        Schema::table('quiz_attempts', function (Blueprint $table) {
            $table->dropColumn(['deadline_at', 'auto_submitted_at']);
        });
    }
};
