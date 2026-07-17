<template>
  <AuthenticatedLayout page-title="Manage Groups">
    <div class="p-8 max-w-4xl mx-auto space-y-8">
      <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Create New Group</h2>
        <form @submit.prevent="createGroup" class="flex gap-4">
          <input v-model="form.name" placeholder="Group Name" class="flex-1 px-4 py-2 border border-gray-200 rounded-xl" required />
          <input v-model="form.max_members" type="number" placeholder="Max Members" class="w-32 px-4 py-2 border border-gray-200 rounded-xl" />
          <button type="submit" :disabled="form.processing" class="bg-orange-600 text-white px-6 py-2 rounded-xl font-bold hover:bg-orange-700">
            {{ form.processing ? 'Creating...' : 'Create' }}
          </button>
        </form>
      </div>

      <div class="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
        <table class="w-full text-left">
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