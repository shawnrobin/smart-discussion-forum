<?php

namespace Database\Seeders;

use App\Models\Group;
use App\Models\User;
use Illuminate\Database\Seeder;

class GroupSeeder extends Seeder
{
    public function run(): void
    {
        $creator = User::first();

        if (!$creator) {
            $this->command->error('No users found. Create a user first, then re-run this seeder.');
            return;
        }

        $groups = [
            ['name' => 'Data Structures', 'description' => 'Learn about arrays, linked lists, trees, and graphs'],
            ['name' => 'Web Development', 'description' => 'Frontend and backend web development'],
            ['name' => 'Database Design', 'description' => 'SQL, NoSQL, and database design patterns'],
            ['name' => 'OOP Concepts', 'description' => 'Object-Oriented Programming in Java and C++'],
            ['name' => 'UI/UX Discussion', 'description' => 'User interface and user experience design'],
            ['name' => 'Machine Learning', 'description' => 'Introduction to ML algorithms and frameworks'],
        ];

        foreach ($groups as $groupData) {
            $group = Group::create([
                'name' => $groupData['name'],
                'description' => $groupData['description'],
                'created_by' => $creator->id,
            ]);

            $group->users()->attach($creator->id, [
                'joined_at' => now(),
            ]);
        }

        $this->command->info('Created ' . count($groups) . ' groups.');
    }
}