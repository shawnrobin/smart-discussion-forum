<template>
  <AuthenticatedLayout page-title="Manage Groups">
    <div class="mx-auto max-w-4xl space-y-6 p-4 sm:p-6 lg:p-8">
      <div class="rounded-2xl border border-gray-100 bg-white p-4 shadow-sm sm:p-6">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Create New Group</h2>
        <form @submit.prevent="createGroup" class="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-4">
          <input v-model="form.name" placeholder="Group Name" class="w-full flex-1 rounded-xl border border-gray-200 px-4 py-2 sm:w-auto" required />
          <input v-model="form.max_members" type="number" placeholder="Max Members" class="w-full rounded-xl border border-gray-200 px-4 py-2 sm:w-32" />
          <button type="submit" :disabled="form.processing" class="rounded-xl bg-orange-600 px-6 py-2 font-bold text-white hover:bg-orange-700">
            {{ form.processing ? 'Creating...' : 'Create' }}
          </button>
        </form>
      </div>

      <div class="overflow-x-auto rounded-2xl border border-gray-100 bg-white shadow-sm">
        <table class="min-w-[38rem] w-full text-left">
          <thead class="bg-gray-50 text-gray-400 text-[10px] font-bold uppercase">
            <tr>
              <th class="px-6 py-4">Group Name</th>
              <th class="px-6 py-4">Members</th>
              <th class="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="group in (groups || [])" :key="group.id" class="hover:bg-gray-50">
              <td class="px-6 py-4 font-semibold text-gray-900">{{ group.name }}</td>
              <td class="px-6 py-4 text-gray-600">{{ group.users_count || 0 }}</td>
              <td class="px-6 py-4 flex gap-4">
                <Link :href="route('lecturer.groups.show', group.id)" class="text-orange-600 font-bold hover:underline">View</Link>
                <Link :href="route('quizzes.create', group.id)" class="text-blue-600 font-bold hover:underline">Create Quiz</Link>
                <button @click.prevent="deleteGroup(group.id)" class="text-rose-600 font-bold hover:underline">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { useForm, Link, router } from '@inertiajs/vue3';

defineProps({ groups: { type: Array, default: () => [] } });

const form = useForm({ name: '', max_members: null });

const createGroup = () => {
  form.post(route('lecturer.groups.store'), { 
    onSuccess: () => form.reset() 
  });
};

const deleteGroup = (id) => {
  if (confirm('Are you sure you want to delete this group?')) {
    router.delete(route('lecturer.groups.destroy', id));
  }
};
</script>
