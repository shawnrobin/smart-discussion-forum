<template>
  <AuthenticatedLayout page-title="Lecturer Dashboard">
    <div class="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8">

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="p-6 bg-white rounded-2xl border border-gray-100 shadow-sm transition hover:shadow-md">
          <p class="text-sm font-medium text-gray-500">My Groups</p>
          <h2 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.my_groups }}</h2>
        </div>
        <div class="p-6 bg-white rounded-2xl border border-gray-100 shadow-sm transition hover:shadow-md">
          <p class="text-sm font-medium text-gray-500">My Quizzes</p>
          <h2 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.my_quizzes }}</h2>
        </div>
        <div class="p-6 bg-white rounded-2xl border border-gray-100 shadow-sm transition hover:shadow-md">
          <p class="text-sm font-medium text-gray-500">Pending Moderation</p>
          <h2 class="text-3xl font-bold text-orange-600 mt-2">{{ stats.pending_moderation }}</h2>
        </div>
      </div>

      <div class="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-sm">
        <div class="flex items-center justify-between gap-4 border-b border-gray-100 p-4 sm:p-6">
          <h3 class="text-lg font-bold text-gray-900">Recent Groups</h3>
          <Link :href="route('lecturer.groups.index')" class="text-sm text-orange-600 font-bold hover:underline">
            View All
          </Link>
        </div>

        <div v-if="recent_groups.length > 0" class="divide-y divide-gray-50">
          <div v-for="group in recent_groups" :key="group.id" class="flex flex-col gap-3 p-4 transition hover:bg-gray-50 sm:flex-row sm:items-center sm:justify-between sm:p-6">
            <div>
              <p class="font-bold text-gray-900">{{ group.name }}</p>
              <p class="text-xs text-gray-400">Created: {{ new Date(group.created_at).toLocaleDateString() }}</p>
            </div>
            <div class="text-sm bg-gray-100 text-gray-600 px-3 py-1 rounded-full font-medium">
              {{ group.users_count }} Members
            </div>
          </div>
        </div>
        <div v-else class="p-12 text-center text-gray-500">
          <p>No groups found yet.</p>
          <Link :href="route('lecturer.groups.index')" class="text-orange-600 font-semibold text-sm hover:underline">Create your first group</Link>
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
    recent_groups: Array
});
</script>
