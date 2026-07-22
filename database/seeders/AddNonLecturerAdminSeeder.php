<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class AddNonLecturerAdminSeeder extends Seeder
{
    public function run(): void
    {
        $email = 'admin_student@example.com';

        if (User::where('email', $email)->exists()) {
            $this->command->info('Non-lecturer admin already exists.');
            return;
        }

        User::create([
            'name' => 'Sam Admin',
            'email' => $email,
            'username' => 'sam.admin',
            'password' => Hash::make('adminpass123'),
            'role' => 'system_admin',
            'status' => 'active',
            'is_admin' => true,
        ]);

        $this->command->info('Created non-lecturer admin user: ' . $email);
    }
}
