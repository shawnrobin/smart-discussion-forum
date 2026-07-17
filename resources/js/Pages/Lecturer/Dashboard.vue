<template>
  <AuthenticatedLayout page-title="Lecturer Dashboard">
    <div class="p-8 max-w-7xl mx-auto space-y-8">

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="p-6 bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center">
              <svg class="w-5 h-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500">My Groups</p>
              <h2 class="text-2xl font-bold text-gray-900">{{ stats.my_groups }}</h2>
            </div>
          </div>
        </div>
        <div class="p-6 bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center">
              <svg class="w-5 h-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500">My Quizzes</p>
              <h2 class="text-2xl font-bold text-gray-900">{{ stats.my_quizzes }}</h2>
            </div>
          </div>
        </div>
        <div class="p-6 bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center">
              <svg class="w-5 h-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"/></svg>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-500">Pending Moderation</p>
              <h2 class="text-2xl font-bold text-orange-600">{{ stats.pending_moderation }}</h2>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Groups -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-lg bg-orange-100 flex items-center justify-center">
              <svg class="w-4 h-4 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            </div>
            <h3 class="text-lg font-bold text-gray-900">Recent Groups</h3>
          </div>
          <Link :href="route('lecturer.groups.index')" class="text-sm font-bold text-orange-600 hover:text-orange-700 hover:underline">View All</Link>
        </div>
        <div v-if="recent_groups.length > 0" class="divide-y divide-gray-50">
          <div v-for="group in recent_groups" :key="group.id" class="px-6 py-4 flex justify-between items-center hover:bg-gray-50/50 transition">
            <div>
              <p class="font-semibold text-gray-900">{{ group.name }}</p>
              <p class="text-xs text-gray-400 mt-0.5">Created {{ new Date(group.created_at).toLocaleDateString() }}</p>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-xs bg-gray-100 text-gray-600 px-3 py-1 rounded-full font-medium">{{ group.users_count }} members</span>
              <Link :href="route('quizzes.create', group.id)" class="text-xs font-bold text-orange-600 hover:text-orange-700 hover:underline">Create Quiz</Link>
            </div>
          </div>
        </div>
        <div v-else class="p-12 text-center text-gray-500">
          <p>No groups found yet.</p>
          <Link :href="route('lecturer.groups.index')" class="text-orange-600 font-semibold text-sm hover:underline mt-2 inline-block">Create your first group</Link>
        </div>
      </div>

      <!-- Recent Quizzes with Results -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-lg bg-orange-100 flex items-center justify-center">
              <svg class="w-4 h-4 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
            </div>
            <h3 class="text-lg font-bold text-gray-900">My Quizzes</h3>
          </div>
          <Link :href="route('lecturer.groups.index')" class="text-sm font-bold text-orange-600 hover:text-orange-700 hover:underline">Manage Groups</Link>
        </div>
        <div v-if="quizzes && quizzes.length > 0" class="divide-y divide-gray-50">
          <div v-for="quiz in quizzes" :key="quiz.id" class="px-6 py-4 flex justify-between items-center hover:bg-gray-50/50 transition">
            <div class="flex-1 min-w-0">
              <p class="font-semibold text-gray-900 truncate">{{ quiz.title }}</p>
              <p class="text-xs text-gray-400 mt-0.5">
                <span :class="quiz.status === 'active' ? 'text-green-600' : quiz.status === 'upcoming' ? 'text-blue-600' : 'text-gray-500'">
                  {{ quiz.status }}
                </span>
                &middot; {{ quiz.total_attempts || 0 }} attempt{{ quiz.total_attempts === 1 ? '' : 's' }}
                &middot; {{ quiz.duration_minutes }}min
              </p>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <Link :href="route('quizzes.performance', quiz.id)" class="px-3 py-1.5 text-xs font-bold text-orange-600 bg-orange-50 rounded-lg hover:bg-orange-100 transition">View Results</Link>
            </div>
          </div>
        </div>
        <div v-else class="p-12 text-center text-gray-500">
          <p>No quizzes created yet.</p>
          <Link :href="route('lecturer.groups.index')" class="text-orange-600 font-semibold text-sm hover:underline mt-2 inline-block">Create quizzes from your groups</Link>
        </div>
      </div>

    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { Link } from '@inertiajs/vue3';

defineProps({
    stats: Object,
    recent_groups: Array,
    quizzes: { type: Array, default: () => [] }
});
</script>
