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
        <div v-else class="mt-4 flex flex-wrap justify-center gap-2">
          <span v-for="member in group.users" :key="member.id" class="rounded-full bg-gray-100 px-3 py-1 text-sm text-gray-700">{{ member.name }}</span>
        </div>
      </div>

      <section class="overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-sm">
        <div class="border-b border-gray-100 px-5 py-4 sm:px-6">
          <h3 class="text-lg font-bold text-gray-900">Group Quizzes</h3>
          <p class="mt-1 text-sm text-gray-500">Quizzes created by any lecturer for this group.</p>
        </div>
        <div v-if="group.quizzes?.length" class="divide-y divide-gray-100">
          <div v-for="quiz in group.quizzes" :key="quiz.id" class="flex flex-col gap-2 px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
            <div>
              <p class="font-semibold text-gray-900">{{ quiz.title }}</p>
              <p class="mt-1 text-xs text-gray-500">Created by {{ quiz.creator?.name ?? 'Unknown lecturer' }} · {{ quiz.duration_minutes }} minutes</p>
            </div>
            <span class="w-fit rounded-full bg-orange-50 px-3 py-1 text-xs font-semibold text-orange-700">{{ quiz.published ? 'Published' : 'Draft' }}</span>
          </div>
        </div>
        <p v-else class="px-5 py-6 text-sm text-gray-500 sm:px-6">No quizzes have been created for this group yet.</p>
      </section>
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
