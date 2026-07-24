<template>
  <component :is="isAdmin ? AdminLayout : AuthenticatedLayout" :page-title="`${group.name} Statistics`">
    <div class="mx-auto max-w-7xl space-y-6">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <Link :href="backUrl" class="inline-flex items-center gap-1 text-sm font-medium text-gray-500 hover:text-orange-600"><ArrowLeftIcon class="h-4 w-4" /> Back</Link>
          <h1 class="mt-3 text-2xl font-bold text-gray-900">{{ group.name }} statistics</h1>
          <p class="mt-1 text-sm text-gray-500">A group-specific view of participation, activity, and moderation. Live data refreshes every 30 seconds.</p>
        </div>
        <Link :href="route('groups.show', group.id)" class="rounded-lg bg-orange-600 px-4 py-2 text-sm font-semibold text-white hover:bg-orange-700">Open group chat</Link>
      </div>

      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div v-for="item in statCards" :key="item.label" class="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
          <p class="text-xs font-semibold uppercase tracking-wide text-gray-400">{{ item.label }}</p>
          <p :class="item.emphasis ? 'text-rose-600' : 'text-gray-900'" class="mt-2 text-3xl font-bold">{{ item.value }}</p>
        </div>
      </div>

      <section class="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-bold text-gray-900">Activity in the last 7 days</h2>
        <div class="mt-5 h-72"><Bar :data="chartData" :options="chartOptions" /></div>
      </section>

      <div class="grid gap-6 lg:grid-cols-2">
        <section class="rounded-xl border border-gray-200 bg-white shadow-sm">
          <div class="border-b border-gray-100 px-6 py-4"><h2 class="font-bold text-gray-900">Top contributors</h2></div>
          <div v-if="topContributors.length" class="divide-y divide-gray-100">
            <div v-for="(member, index) in topContributors" :key="member.id" class="flex items-center justify-between px-6 py-4">
              <div class="flex items-center gap-3"><span class="flex h-7 w-7 items-center justify-center rounded-full bg-orange-100 text-xs font-bold text-orange-700">{{ index + 1 }}</span><div><p class="font-semibold text-gray-800">{{ member.name }}</p><p class="text-xs text-gray-500">{{ member.topics }} topics · {{ member.replies }} replies · {{ member.answers }} answers</p></div></div>
              <span class="text-sm font-bold text-orange-600">{{ member.score }} pts</span>
            </div>
          </div>
          <p v-else class="p-6 text-sm text-gray-500">Participation scores will appear as members contribute.</p>
        </section>

        <section class="rounded-xl border border-gray-200 bg-white shadow-sm">
          <div class="border-b border-gray-100 px-6 py-4"><h2 class="font-bold text-gray-900">Recent conversations</h2></div>
          <div v-if="recentTopics.length" class="divide-y divide-gray-100">
            <Link v-for="topic in recentTopics" :key="topic.id" :href="route('topics.show', { group: group.id, topic: topic.id })" class="block px-6 py-4 hover:bg-orange-50">
              <div class="flex items-start justify-between gap-3"><div><p class="font-semibold text-gray-800">{{ topic.title }}</p><p class="mt-1 text-xs text-gray-500">{{ topic.author }} · {{ topic.created_at }}</p></div><span class="shrink-0 text-xs font-medium text-gray-500">{{ topic.replies }} replies</span></div>
              <span v-if="topic.answered" class="mt-2 inline-block rounded-full bg-green-100 px-2 py-0.5 text-[11px] font-semibold text-green-700">Answered</span>
            </Link>
          </div>
          <p v-else class="p-6 text-sm text-gray-500">No conversations have been started yet.</p>
        </section>
      </div>
    </div>
  </component>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue';
import { Link, router } from '@inertiajs/vue3';
import { ArrowLeft as ArrowLeftIcon } from 'lucide-vue-next';
import { Bar } from 'vue-chartjs';
import { Chart as ChartJS, BarElement, CategoryScale, LinearScale, Legend, Tooltip } from 'chart.js';
import AdminLayout from '@/Layouts/AdminLayout.vue';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

ChartJS.register(BarElement, CategoryScale, LinearScale, Legend, Tooltip);

const props = defineProps({
  group: { type: Object, required: true },
  stats: { type: Object, default: () => ({}) },
  activity: { type: Array, default: () => [] },
  topContributors: { type: Array, default: () => [] },
  recentTopics: { type: Array, default: () => [] },
  isAdmin: { type: Boolean, default: false },
});

const backUrl = computed(() => props.isAdmin ? route('admin.dashboard') : route('lecturer.groups.show', props.group.id));
const statCards = computed(() => [
  { label: 'Members', value: props.stats.members ?? 0 },
  { label: 'Conversations', value: props.stats.topics ?? 0 },
  { label: 'Replies', value: props.stats.replies ?? 0 },
  { label: 'Active members', value: props.stats.active_members ?? 0 },
  { label: 'Answered topics', value: props.stats.answered_topics ?? 0 },
  { label: 'Unanswered questions', value: props.stats.unanswered_topics ?? 0 },
  { label: 'Total likes', value: props.stats.likes ?? 0 },
  { label: 'Open reports', value: props.stats.reports ?? 0, emphasis: true },
  { label: 'Quizzes', value: props.stats.quizzes ?? 0 },
  { label: 'Quiz attempts', value: props.stats.quiz_attempts ?? 0 },
  { label: 'Quiz average', value: `${props.stats.quiz_average ?? 0}%` },
]);
const chartData = computed(() => ({ labels: props.activity.map(day => day.label), datasets: [{ label: 'Conversations', data: props.activity.map(day => day.topics), backgroundColor: '#f97316', borderRadius: 5 }, { label: 'Replies', data: props.activity.map(day => day.replies), backgroundColor: '#475569', borderRadius: 5 }] }));
const chartOptions = { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } }, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } };

let refreshTimer;

onMounted(() => {
  refreshTimer = window.setInterval(() => {
    router.reload({
      only: ['stats', 'activity', 'topContributors', 'recentTopics'],
      preserveScroll: true,
      preserveState: true,
    });
  }, 30000);
});

onBeforeUnmount(() => window.clearInterval(refreshTimer));
</script>
