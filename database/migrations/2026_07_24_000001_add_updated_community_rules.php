<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    public function up(): void
    {
        DB::table('onboarding_rules')->updateOrInsert(
            ['version' => 2],
            [
                'title' => 'SmartForum Community Rules',
                'content' => "1. Share only relevant academic material in the appropriate group and topic. Do not flood groups with unrelated, misleading, or unapproved copyrighted content.\n2. Keep questions, answers, and follow-up messages in their topic thread so members can find useful responses easily.\n3. Treat every member with respect. Harassment, hate speech, personal attacks, and disruptive behaviour are not allowed.\n4. Protect members' privacy. Do not share private communications or personal information, and use communication exclusions responsibly.\n5. Report inappropriate content to moderators instead of escalating disputes. Follow reasonable instructions from lecturers and administrators.\n6. Stay active and responsive. Extended inactivity may result in two platform warnings and a temporary blacklist under the configured moderation policy.\n7. Complete quizzes honestly and within their published schedule and duration. Late participants do not receive extra time, and unfinished quizzes may be submitted automatically when time expires.\n8. Share posts to external social platforms responsibly and only when doing so does not expose private information or violate these rules.",
                'created_at' => now(),
                'updated_at' => now(),
            ],
        );
    }

    public function down(): void
    {
        DB::table('onboarding_rules')->where('version', 2)->delete();
    }
};
