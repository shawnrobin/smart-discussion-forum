
<?php
namespace App\Events;

use App\Models\Topic;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class TopicCreated
{
    use Dispatchable, SerializesModels;

    public function __construct(public Topic $topic)
    {
    }
}