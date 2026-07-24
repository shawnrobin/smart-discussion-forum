<template>
  <AuthenticatedLayout :page-title="'Group: ' + group.name">
    <div class="mx-auto max-w-5xl space-y-6 p-4 sm:p-6 lg:p-8">
      
      <div class="flex flex-col gap-5 rounded-2xl border border-gray-100 bg-white p-5 shadow-sm sm:p-8 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h1 class="text-3xl font-black text-gray-900">{{ group.name }}</h1>
          <p class="text-gray-500 mt-1">Created on {{ new Date(group.created_at).toLocaleDateString() }}</p>
        </div>
        <div class="flex flex-wrap gap-3">
          <Link :href="route('lecturer.groups.statistics', group.id)" class="px-4 py-2 text-sm font-bold text-orange-700 bg-orange-50 rounded-xl hover:bg-orange-100 transition">View Statistics</Link>
          <Link :href="route('quizzes.create', group.id)" class="px-4 py-2 text-sm font-bold text-white bg-blue-600 rounded-xl hover:bg-blue-700 transition">Create Quiz</Link>
          <button class="px-4 py-2 text-sm font-bold text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition">Edit Group</button>
          <button 
            @click="deleteGroup" 
            :disabled="deleteForm.processing"
            class="px-4 py-2 text-sm font-bold text-rose-600 bg-rose-50 rounded-xl hover:bg-rose-100 transition"
          >
            {{ deleteForm.processing ? 'Deleting...' : 'Delete Group' }}
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Max Members</p>
          <h2 class="text-2xl font-black text-gray-900 mt-1">{{ group.max_members || 'Unlimited' }}</h2>
        </div>
        <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
          <p class="text-xs font-bold text-gray-400 uppercase tracking-widest">Current Members</p>
          <h2 class="text-2xl font-black text-gray-900 mt-1">{{ group.users_count || 0 }}</h2>
        </div>
      </div>

      <div class="rounded-2xl border border-gray-100 bg-white p-5 text-center shadow-sm sm:p-8">
        <h3 class="text-lg font-bold text-gray-900">Group Members</h3>
        <p v-if="!group.users || group.users.length === 0" class="text-gray-500 mt-2">No members have joined this group yet.</p>
        <div v-else class="mt-4">
          </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { useForm, Link } from '@inertiajs/vue3';

const props = defineProps({ 
  group: Object 
});

const deleteForm = useForm({});

const deleteGroup = () => {
  if (confirm('Are you sure you want to delete this group? This action cannot be undone.')) {
    deleteForm.delete(route('lecturer.groups.destroy', props.group.id));
  }
};
</script>
