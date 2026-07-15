<!DOCTYPE html>
<html>

<head>
    <title>{{ $topic->title }}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
        }

        .header {
            border-bottom: 2px solid #333;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }

        .reply {
            border-left: 3px solid #007bff;
            padding-left: 15px;
            margin-bottom: 20px;
            padding-top: 10px;
        }

        .answer {
            border-left-color: #28a745;
        }

        .meta {
            font-size: 0.9em;
            color: #666;
            margin-bottom: 10px;
        }
    </style>
</head>

<body>
    <div class="header">
        <h1>{{ $topic->title }}</h1>
        <div class="meta">
            <strong>Group:</strong> {{ $topic->group->name }}<br>
            <strong>Author:</strong> {{ $topic->user->name }}<br>
            <strong>Date:</strong> {{ $topic->created_at->format('Y-m-d H:i:s') }}<br>
            <strong>Status:</strong>
            @if($topic->is_answered)
            <span style="color: green;">✓ Answered</span>
            @else
            <span style="color: orange;">? Unanswered</span>
            @endif
        </div>
    </div>

    <div class="content">
        <h3>Original Question:</h3>
        <p>{{ $topic->description }}</p>
    </div>

    <h3>Replies:</h3>
    @foreach($messages as $message)
    <div class="reply @if($message->is_answer) answer @endif">
        <div class="meta">
            <strong>{{ $message->user->name }}</strong> - {{ $message->created_at->format('Y-m-d H:i:s') }}
            @if($message->is_answer)
            <span style="color: green; font-weight: bold;">✓ ANSWER</span>
            @endif
        </div>
        <p>{{ $message->content }}</p>
    </div>
    @endforeach

    <hr>
    <p style="font-size: 0.8em; color: #999;">
        Exported from Smart Discussion Forum on {{ now()->format('Y-m-d H:i:s') }}
    </p>
</body>

</html>
