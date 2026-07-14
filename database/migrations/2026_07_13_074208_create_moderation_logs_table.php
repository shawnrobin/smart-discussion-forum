<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        // This 'if' check is the final defense against your error
        if (!Schema::hasTable('moderation_logs')) {
            Schema::create('moderation_logs', function (Blueprint $table) {
                $table->id();
                $table->foreignId('user_id')->constrained()->onDelete('cascade');
                $table->foreignId('admin_id')->constrained('users');
                $table->string('action');
                $table->text('reason')->nullable();
                $table->timestamps();
            });
        }
    }

    public function down()
    {
        Schema::dropIfExists('moderation_logs');
    }
};