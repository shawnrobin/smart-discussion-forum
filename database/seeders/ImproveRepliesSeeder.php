<?php

namespace Database\Seeders;

use App\Models\Message;
use Illuminate\Database\Seeder;

class ImproveRepliesSeeder extends Seeder
{
    public function run(): void
    {
        $templates = [
            "Nice explanation — I tried this approach and it worked well for small datasets.",
            "Can you share which version you used? I ran into a similar error on v2.1.",
            "I implemented a solution with an index on the foreign key; that improved performance significantly.",
            "Great question — a workaround is to normalize that field and cache results.",
            "Thanks for sharing — this helped me debug my own implementation.",
            "Have you considered using pagination here? It reduces memory usage.",
            "I think the issue is related to the query planner — adding an index may help.",
            "This is a helpful discussion; here's a small snippet that solved it for me.",
        ];

        // Update messages that were created by the original ForumSeeder placeholders.
        $messages = Message::where('content', 'like', 'Reply % to %')->get();

        foreach ($messages as $msg) {
            $msg->content = $templates[array_rand($templates)];
            $msg->save();
        }

        $this->command->info('Updated ' . $messages->count() . ' placeholder replies to more realistic content.');
    }
}
