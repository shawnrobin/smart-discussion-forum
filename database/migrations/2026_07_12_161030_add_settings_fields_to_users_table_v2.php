<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            if (!Schema::hasColumn('users', 'avatar')) {
                $table->string('avatar')->nullable();
            }
            if (!Schema::hasColumn('users', 'bio')) {
                $table->text('bio')->nullable();
            }
            if (!Schema::hasColumn('users', 'course')) {
                $table->string('course')->nullable();
            }
            if (!Schema::hasColumn('users', 'faculty')) {
                $table->string('faculty')->nullable();
            }
            if (!Schema::hasColumn('users', 'year_of_study')) {
                $table->integer('year_of_study')->nullable();
            }
            if (!Schema::hasColumn('users', 'phone_number')) {
                $table->string('phone_number')->nullable();
            }
        });
    }

    public function down(): void
    {
        // Intentionally left empty to avoid dropping columns that may already exist independently.
    }
};