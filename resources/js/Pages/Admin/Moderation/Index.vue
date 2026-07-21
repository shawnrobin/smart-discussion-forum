<template>
  <AdminLayout>
    <Head title="Moderation" />

    <div class="max-w-6xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-slate-900">Moderation queue</h1>
        <p class="text-sm text-slate-500 mt-1">Review member reports and take action on irrelevant or harmful content.</p>
      </div>

      <section class="mb-8 bg-white rounded-xl shadow-sm border border-slate-200 p-6">
        <h2 class="text-lg font-bold text-slate-900">Automatic inactivity policy</h2>
        <p class="text-sm text-slate-500 mt-1">The daily moderation task uses these values for student accounts.</p>
        <form @submit.prevent="saveSettings" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-5">
          <label class="text-sm font-medium text-slate-700">Inactive after (days)
            <input v-model.number="settingsForm.inactivity_days" type="number" min="1" max="365" class="mt-1 w-full rounded-lg border-slate-300 focus:border-orange-500 focus:ring-orange-500" />
          </label>
          <label class="text-sm font-medium text-slate-700">Grace period (days)
            <input v-model.number="settingsForm.warning_grace_days" type="number" min="1" max="90" class="mt-1 w-full rounded-lg border-slate-300 focus:border-orange-500 focus:ring-orange-500" />
          </label>
          <label class="text-sm font-medium text-slate-700">Warnings before blacklist
            <input v-model.number="settingsForm.warnings_before_blacklist" type="number" min="1" max="10" class="mt-1 w-full rounded-lg border-slate-300 focus:border-orange-500 focus:ring-orange-500" />
          </label>
          <label class="text-sm font-medium text-slate-700">Blacklist duration (days)
            <input v-model.number="settingsForm.blacklist_days" type="number" min="1" max="365" class="mt-1 w-full rounded-lg border-slate-300 focus:border-orange-500 focus:ring-orange-500" />
          </label>
          <div class="lg:col-span-4 flex justify-end">
            <button type="submit" :disabled="settingsForm.processing" class="px-4 py-2 text-sm font-semibold rounded-lg bg-orange-600 text-white hover:bg-orange-700 disabled:opacity-50">{{ settingsForm.processing ? 'Saving...' : 'Save policy' }}</button>
          </div>
        </form>
      </section>

      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div v-if="reports.data.length" class="divide-y divide-slate-100">
          <article v-for="report in reports.data" :key="report.id" class="p-6">
            <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-5">
              <div class="min-w-0">
                <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide">
                  <span class="px-2 py-1 rounded bg-slate-100 text-slate-600">{{ contentType(report) }}</span>
                  <span :class="statusClass(report.status)" class="px-2 py-1 rounded">{{ report.status }}</span>
                </div>
                <p class="mt-3 font-semibold text-slate-900">{{ contentTitle(report) }}</p>
                <p class="mt-2 text-sm text-slate-600"><span class="font-medium">Report reason:</span> {{ report.reason }}</p>
                <p class="mt-2 text-xs text-slate-400">Reported by {{ report.user?.name || 'Unknown user' }} · {{ formatDate(report.created_at) }}</p>
              </div>

              <div v-if="report.status === 'pending'" class="flex flex-wrap gap-2 shrink-0">
                <button @click="resolve(report, 'dismiss')" :disabled="processing === report.id" class="px-3 py-2 text-sm font-semibold rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-50">Dismiss</button>
                <button @click="resolve(report, 'flag')" :disabled="processing === report.id" class="px-3 py-2 text-sm font-semibold rounded-lg border border-amber-300 text-amber-700 hover:bg-amber-50">Flag content</button>
                <button @click="resolve(report, 'remove')" :disabled="processing === report.id" class="px-3 py-2 text-sm font-semibold rounded-lg bg-rose-600 text-white hover:bg-rose-700">Remove content</button>
              </div>
            </div>
          </article>
        </div>
        <div v-else class="p-12 text-center text-slate-500">No content reports have been submitted.</div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref } from 'vue';
import { Head, router, useForm } from '@inertiajs/vue3';
import AdminLayout from '@/Layouts/AdminLayout.vue';

const props = defineProps({ reports: { type: Object, required: true }, moderationSettings: { type: Object, required: true } });
const processing = ref(null);
const settingsForm = useForm({
  inactivity_days: props.moderationSettings.inactivity_days,
  warning_grace_days: props.moderationSettings.warning_grace_days,
  warnings_before_blacklist: props.moderationSettings.warnings_before_blacklist,
  blacklist_days: props.moderationSettings.blacklist_days,
});

const resolve = (report, decision) => {
  processing.value = report.id;
  router.post(route('admin.moderation.resolve', report.id), { decision }, {
    preserveScroll: true,
    onFinish: () => { processing.value = null; },
  });
};

const saveSettings = () => settingsForm.put(route('admin.moderation.settings.update'), { preserveScroll: true });

const contentType = (report) => report.reportable_type?.split('\\').pop() || 'Content';
const contentTitle = (report) => report.reportable?.title || report.reportable?.content || 'Content is no longer available';
const formatDate = (date) => date ? new Date(date).toLocaleString() : '';
const statusClass = (status) => ({
  pending: 'bg-amber-100 text-amber-800',
  resolved: 'bg-emerald-100 text-emerald-800',
  dismissed: 'bg-slate-100 text-slate-700',
}[status] || 'bg-slate-100 text-slate-700');
</script>
