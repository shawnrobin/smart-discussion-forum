<template>
  <AuthenticatedLayout
    page-title="Dashboard"
    :user-name="auth.user.name"
  >
    <div class="space-y-6">
      <!-- Welcome Section -->
      <Card title="Welcome Back!">
        <p class="text-gray-700">
          Hello {{ auth.user.name }}, welcome to Smart Discussion Forum.
        </p>
      </Card>

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatsCard label="My Groups" :value="5" />
        <StatsCard label="Discussions" :value="24"  />
        <StatsCard label="Quizzes" :value="3" />
      </div>

      <!-- Search Bar -->
      <Card>
        <div class="flex gap-2">
          <TextInput
            v-model="searchQuery"
            @keyup.enter="search"
            placeholder="Search topics, posts, users..."
            class="flex-1"
          />
          <PrimaryButton @click="search">
             Search
          </PrimaryButton>
        </div>
      </Card>

      <!-- Groups Section -->
      <Card title="My Groups">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="group in groups"
            :key="group.id"
            class="p-4 border border-gray-200 rounded-lg hover:shadow-md cursor-pointer transition"
          >
            <h3 class="font-semibold text-gray-900">{{ group.name }}</h3>
            <p class="text-sm text-gray-600 mt-1">{{ group.members }} members</p>
            <p class="text-xs text-gray-500 mt-2">Last activity: {{ group.lastActivity }}</p>
          </div>
        </div>
      </Card>

      <!-- Recent Activity Section -->
      <Card title="Recent Activity">
        <div class="space-y-3">
          <div
            v-for="activity in activities"
            :key="activity.id"
            class="p-3 bg-gray-50 rounded-lg"
          >
            <p class="text-sm text-gray-900">{{ activity.message }}</p>
            <p class="text-xs text-gray-500 mt-1">{{ activity.time }}</p>
          </div>
        </div>
      </Card>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import Card from '@/Components/Card.vue';
import StatsCard from '@/Components/Statscard.vue';
import TextInput from '@/Components/TextInput.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const auth = usePage().props.auth;
const searchQuery = ref('');

const groups = ref([
  { id: 1, name: 'Data Structures', members: 18, lastActivity: '2 hours ago' },
  { id: 2, name: 'Web Development', members: 25, lastActivity: '1 hour ago' },
  { id: 3, name: 'Database Design', members: 12, lastActivity: '4 hours ago' },
  { id: 4, name: 'OOP Concepts', members: 20, lastActivity: '30 minutes ago' },
  { id: 5, name: 'UI/UX Discussion', members: 15, lastActivity: '1 day ago' }
]);

const activities = ref([
  { id: 1, message: 'John replied to "Understanding Recursion"', time: '2 hours ago' },
  { id: 2, message: 'Quiz "Database Basics" is now available', time: '4 hours ago' },
  { id: 3, message: 'You were added to "Web Development" group', time: '1 day ago' },
  { id: 4, message: 'Mary started a new discussion "REST APIs"', time: '1 day ago' }
]);

const search = () => {

    router.get(
        route('dashboard'),
        {
            search: searchQuery.value
        },
        {
            preserveState: true,
            preserveScroll: true,
            replace: true
        }
    );

};
  // Will implement search functionality later
</script>