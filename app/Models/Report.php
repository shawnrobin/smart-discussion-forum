<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Report extends Model
{
    protected $fillable = ['status', 'reason', 'resolved_by', 'resolved_at'];

    public function user()
    {
        return $this->belongsTo(User::class);
    }
}