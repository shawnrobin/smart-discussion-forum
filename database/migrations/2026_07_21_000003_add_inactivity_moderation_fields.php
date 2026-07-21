<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            $table->timestamp('last_communication_at')->nullable()->after('last_login');
            $table->timestamp('last_inactivity_warning_at')->nullable()->after('last_communication_at');
            $table->timestamp('blacklisted_until')->nullable()->after('blacklisted');
        });

        Schema::create('moderation_settings', function (Blueprint $table) {
            $table->id();
            $table->unsignedSmallInteger('inactivity_days')->default(30);
            $table->unsignedSmallInteger('warning_grace_days')->default(7);
            $table->unsignedSmallInteger('warnings_before_blacklist')->default(2);
            $table->unsignedSmallInteger('blacklist_days')->default(30);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('moderation_settings');
        Schema::table('users', function (Blueprint $table) {
            $table->dropColumn(['last_communication_at', 'last_inactivity_warning_at', 'blacklisted_until']);
        });
    }
};
