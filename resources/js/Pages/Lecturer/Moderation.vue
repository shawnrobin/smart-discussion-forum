<template>
  <AuthenticatedLayout page-title="Moderation Center">
    <div class="mx-auto max-w-5xl p-4 sm:p-6 lg:p-8">
      <div class="overflow-x-auto rounded-2xl border border-gray-100 bg-white shadow-sm">
        <table class="min-w-[34rem] w-full text-left">
          <thead class="bg-gray-50 text-gray-400 text-[10px] font-bold uppercase">
            <tr>
              <th class="px-6 py-4">Reporter</th>
              <th class="px-6 py-4">Reason</th>
              <th class="px-6 py-4">Actions</th>
            </tr>
          </thead>
          <tbody v-if="reports && reports.length > 0" class="divide-y divide-gray-100">
            <tr v-for="report in reports" :key="report.id" class="hover:bg-gray-50">
              <td class="px-6 py-4">{{ report.user?.name || 'Anonymous' }}</td>
              <td class="px-6 py-4 text-sm text-gray-600">{{ report.reason }}</td>
              <td class="px-6 py-4">
                <button 
                  @click="resolve(report.id)" 
                  class="text-emerald-600 font-bold hover:underline"
                >
                  Resolve
                </button>
              </td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr>
              <td colspan="3" class="px-6 py-8 text-center text-gray-500">
                No pending reports at this time.
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
import { useForm } from '@inertiajs/vue3';

const props = defineProps({ reports: Array });
const form = useForm({});

const resolve = (id) => {
  form.post(route('lecturer.moderation.resolve', id), {
    preserveScroll: true,
  });
};
</script>
