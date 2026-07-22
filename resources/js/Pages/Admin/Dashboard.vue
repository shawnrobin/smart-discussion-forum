<template>
  <AdminLayout>
    <Head title="Admin Dashboard" />

    <div class="max-w-7xl mx-auto">
        <div class="mb-8">
            <h1 class="text-2xl font-bold text-gray-800">Admin Dashboard</h1>
            <p class="text-sm text-gray-500 mt-1">Overview of system health and activity.</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Total Users</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.users }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Groups</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.groups }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Topics</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.topics }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Messages</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.messages }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Quizzes</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.quizzes }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Flagged Topics</span>
                <h3 class="text-3xl font-bold text-red-600 mt-2">{{ stats.flagged_topics }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Blacklisted Users</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.blacklisted }}</h3>
            </div>
        </div>

        <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h2 class="text-lg font-bold text-gray-800 mb-4">Topics Created — Last 7 Days</h2>
            <div class="h-64"><Line :data="mappedChartData" :options="chartOptions" /></div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-8">
            <section class="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                <h2 class="text-lg font-bold text-gray-800 mb-4">Recent Discussions</h2>
                <div v-if="recentTopics.length" class="divide-y divide-gray-100">
                    <div v-for="topic in recentTopics" :key="topic.id" class="py-3">
                        <p class="font-medium text-gray-900">{{ topic.title }}</p>
                        <p class="text-sm text-gray-500">{{ topic.user?.name || 'Unknown user' }} in {{ topic.group?.name || 'Unknown group' }}</p>
                    </div>
                </div>
                <p v-else class="text-sm text-gray-500">No discussions yet.</p>
            </section>

            <section class="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                <h2 class="text-lg font-bold text-gray-800 mb-4">Recent Moderation Activity</h2>
                <div v-if="recentLogs.length" class="divide-y divide-gray-100">
                    <div v-for="log in recentLogs" :key="log.id" class="py-3">
                        <p class="font-medium text-gray-900">{{ log.action.replace('_', ' ') }}</p>
                        <p class="text-sm text-gray-500">{{ log.admin?.name || 'Admin' }} · {{ log.user?.name || 'Unknown user' }}</p>
                    </div>
                </div>
                <p v-else class="text-sm text-gray-500">No moderation activity yet.</p>
            </section>
        </div>

        <section class="mt-8 bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
                <div><h2 class="text-lg font-bold text-gray-800">Group statistics</h2><p class="text-sm text-gray-500">Open any group for its full participation and moderation report.</p></div>
            </div>
            <div v-if="groupSummaries.length" class="divide-y divide-gray-100">
                <Link v-for="group in groupSummaries" :key="group.id" :href="route('admin.groups.statistics', group.id)" class="flex items-center justify-between gap-4 px-6 py-4 hover:bg-orange-50 transition">
                    <p class="font-semibold text-gray-900">{{ group.name }}</p>
                    <div class="flex gap-4 text-xs text-gray-500"><span>{{ group.members }} members</span><span>{{ group.topics }} conversations</span><span :class="group.unanswered ? 'text-rose-600 font-semibold' : ''">{{ group.unanswered }} unanswered</span></div>
                </Link>
            </div>
            <p v-else class="px-6 py-5 text-sm text-gray-500">No groups have been created yet.</p>
        </section>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed } from 'vue';
import { Head, Link } from '@inertiajs/vue3';
import AdminLayout from '@/Layouts/AdminLayout.vue';
import { Line } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, LinearScale, PointElement, CategoryScale } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, LineElement, LinearScale, PointElement, CategoryScale);

const props = defineProps({
    stats: { type: Object, default: () => ({}) },
    recentLogs: { type: Array, default: () => [] },
    recentTopics: { type: Array, default: () => [] },
    chartData: { type: Array, default: () => [] },
    groupSummaries: { type: Array, default: () => [] },
});

const mappedChartData = computed(() => ({
    labels: props.chartData.map(d => d.label),
    datasets: [{ label: 'Topics', backgroundColor: '#f59e0b', borderColor: '#fbbf24', data: props.chartData.map(d => d.count) }]
}));

const chartOptions = { responsive: true, maintainAspectRatio: false };
</script>
