<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class UserSeeder extends Seeder
{
    public function run(): void
    {
        // Clear any existing users to ensure deterministic seed
        User::truncate();

        // Create one lecturer who will be the admin
        User::create([
            'name' => 'Dr. Alice Lecturer',
            'email' => 'lecturer@example.com',
            'username' => 'alice.lecturer',
            'password' => Hash::make('password123'),
            'role' => 'lecturer',
            'status' => 'active',
            'is_admin' => true,
        ]);

        // Create four students
        $students = [
            ['name' => 'Student One', 'email' => 'student1@example.com', 'username' => 'student1'],
            ['name' => 'Student Two', 'email' => 'student2@example.com', 'username' => 'student2'],
            ['name' => 'Student Three', 'email' => 'student3@example.com', 'username' => 'student3'],
            ['name' => 'Student Four', 'email' => 'student4@example.com', 'username' => 'student4'],
        ];

        foreach ($students as $s) {
            User::create([
                'name' => $s['name'],
                'email' => $s['email'],
                'username' => $s['username'],
                'password' => Hash::make('password123'),
                'role' => 'student',
                'status' => 'active',
                'is_admin' => false,
            ]);
        }

        $this->command->info('Created 5 users (1 lecturer admin, 4 students).');
    }
}
