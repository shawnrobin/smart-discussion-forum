<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        if (!DB::table('onboarding_rules')->exists()) {
            DB::table('onboarding_rules')->insert([
                'title' => 'SmartForum Community Rules',
                'content' => "1. Keep discussions relevant to the group topic.\n2. Be respectful; harassment, hate speech, and personal attacks are prohibited.\n3. Do not post spam, misleading content, or copyrighted material without permission.\n4. Respect private communications and other members' privacy.\n5. Report inappropriate content to the moderators rather than escalating disputes.\n6. Follow lecturer and administrator instructions for group discussions and quizzes.",
                'version' => 1,
                'created_at' => now(),
                'updated_at' => now(),
            ]);
        }
    }

    public function down(): void
    {
        DB::table('onboarding_rules')->where('version', 1)->delete();
    }
};
