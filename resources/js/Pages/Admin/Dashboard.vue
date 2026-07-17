<template>
  <AdminLayout>
    <Head title="Admin Dashboard" />

    <div class="max-w-7xl mx-auto">
        <div class="mb-8">
            <h1 class="text-2xl font-bold text-gray-800">Admin Dashboard</h1>
            <p class="text-sm text-gray-500 mt-1">Overview of system health and activity.</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Total Users</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.users }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Discussions</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.discussions }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Pending Reports</span>
                <h3 class="text-3xl font-bold text-red-600 mt-2">{{ stats.reports }}</h3>
            </div>
            <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200">
                <span class="text-xs font-semibold uppercase text-gray-400">Blacklisted</span>
                <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.blacklisted }}</h3>
            </div>
        </div>

        <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h2 class="text-lg font-bold text-gray-800 mb-4">System Activity</h2>
            <div class="h-64"><Line :data="mappedChartData" :options="chartOptions" /></div>
        </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed } from 'vue';
import { Head } from '@inertiajs/vue3';
import AdminLayout from '@/Layouts/AdminLayout.vue';
import { Line } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, LinearScale, PointElement, CategoryScale } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, LineElement, LinearScale, PointElement, CategoryScale);

const props = defineProps({ stats: Object, recentLogs: Array, chartData: Array });

const mappedChartData = computed(() => ({
    labels: props.chartData.map(d => d.label),
    datasets: [{ label: 'Actions', backgroundColor: '#f59e0b', borderColor: '#fbbf24', data: props.chartData.map(d => d.count) }]
}));

const chartOptions = { responsive: true, maintainAspectRatio: false };
</script>