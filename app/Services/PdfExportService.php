<?php

namespace App\Services;

use App\Models\Topic;
use Barryvdh\DomPDF\Facade\Pdf;

class PdfExportService
{
    /**
     * Requirement #6: Export topic to PDF
     */
    public static function exportTopic(Topic $topic)
    {
        $topic->load('messages.user', 'user', 'group');

        $html = view('exports.topic', [
            'topic' => $topic,
            'messages' => $topic->messages
        ])->render();

        $pdf = Pdf::loadHTML($html);
        
        return $pdf->download("topic-{$topic->id}.pdf");
    }

    /**
     * Generate thread summary
     */
    public static function generateThreadSummary($topic)
    {
        $summary = "TOPIC: " . $topic->title . "\n";
        $summary .= "Author: " . $topic->user->name . "\n";
        $summary .= "Group: " . $topic->group->name . "\n";
        $summary .= "Date: " . $topic->created_at->format('Y-m-d H:i:s') . "\n\n";
        $summary .= "DESCRIPTION:\n" . $topic->description . "\n\n";
        $summary .= "REPLIES:\n";

        foreach ($topic->messages as $message) {
            $summary .= "\n" . str_repeat("-", 50) . "\n";
            $summary .= "By: " . $message->user->name . "\n";
            $summary .= "Date: " . $message->created_at->format('Y-m-d H:i:s') . "\n";
            $summary .= "Answer: " . ($message->is_answer ? "YES" : "NO") . "\n";
            $summary .= "Content:\n" . $message->content . "\n";
        }

        return $summary;
    }
}
