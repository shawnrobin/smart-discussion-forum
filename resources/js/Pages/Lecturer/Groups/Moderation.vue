<template>
  <AuthenticatedLayout page-title="Lecturer Moderation">
    <div class="mx-auto max-w-6xl space-y-6 p-8">
      <div><h1 class="text-2xl font-bold text-gray-900">Group moderation</h1><p class="mt-1 text-sm text-gray-500">Review reports and flagged conversations in the groups you teach.</p></div>

      <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div class="border-b border-gray-100 px-6 py-4"><h2 class="font-bold text-gray-900">Open reports</h2></div>
        <div v-if="reports.length" class="divide-y divide-gray-100">
          <div v-for="report in reports" :key="report.id" class="flex items-center justify-between gap-4 px-6 py-4"><div><p class="font-semibold text-gray-900">{{ report.reason }}</p><p class="mt-1 text-xs text-gray-500">Reported by {{ report.user?.name ?? 'A member' }} · {{ formatDate(report.created_at) }}</p></div><button @click="resolveReport(report.id)" class="shrink-0 rounded-lg bg-orange-600 px-3 py-2 text-xs font-semibold text-white hover:bg-orange-700">Resolve</button></div>
        </div>
        <p v-else class="p-6 text-sm text-gray-500">No open reports in your groups.</p>
      </section>

      <section class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
        <div class="border-b border-gray-100 px-6 py-4"><h2 class="font-bold text-gray-900">Flagged conversations</h2></div>
        <div v-if="flaggedTopics.length" class="divide-y divide-gray-100">
          <div v-for="topic in flaggedTopics" :key="topic.id" class="flex items-center justify-between gap-4 px-6 py-4"><div><p class="font-semibold text-gray-900">{{ topic.title }}</p><p class="mt-1 text-xs text-gray-500">{{ topic.group?.name }} · by {{ topic.user?.name }}</p></div><Link :href="route('topics.show', { group: topic.group_id, topic: topic.id })" class="text-sm font-semibold text-orange-600 hover:text-orange-700">Review</Link></div>
        </div>
        <p v-else class="p-6 text-sm text-gray-500">No flagged conversations in your groups.</p>
      </section>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { Link, router } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

defineProps({ reports: { type: Array, default: () => [] }, flaggedTopics: { type: Array, default: () => [] } });
const formatDate = (date) => date ? new Date(date).toLocaleString('en-KE', { timeZone: 'Africa/Nairobi' }) : '';
const resolveReport = (id) => router.post(route('lecturer.moderation.reports.resolve', id), {}, { preserveScroll: true });
</script>
