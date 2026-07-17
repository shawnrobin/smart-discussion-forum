<script setup>
import { computed } from 'vue';
import { router, Link, Head } from '@inertiajs/vue3';
import { ArrowLeft, Trophy, Users, Award, Clock, BarChart3, CheckCircle, XCircle } from 'lucide-vue-next';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const props = defineProps({
    quiz: Object,
    group: Object,
    attempts: Array,
    stats: Object,
});

const formatTime = (seconds) => {
    if (!seconds) return '—';
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}m ${s}s`;
};

const rankClass = (index) => {
    if (index === 0) return 'text-yellow-600 bg-yellow-50 border-yellow-200';
    if (index === 1) return 'text-gray-500 bg-gray-50 border-gray-200';
    if (index === 2) return 'text-amber-700 bg-amber-50 border-amber-200';
    return 'text-gray-600 bg-white border-gray-100';
};

const rankIcon = (index) => {
    if (index === 0) return '🥇';
    if (index === 1) return '🥈';
    if (index === 2) return '🥉';
    return `#${index + 1}`;
};
</script>

<template>
    <AuthenticatedLayout :page-title="'Performance: ' + quiz.title">
        <div class="max-w-5xl mx-auto p-6 space-y-8">

            <!-- Back & Header -->
            <div>
                <button @click="router.visit(route('quizzes.index'))" class="inline-flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-orange-600 transition-colors mb-3">
                    <ArrowLeft class="w-4 h-4" />
                    Back to Quizzes
                </button>
                <h1 class="text-2xl font-bold text-gray-900">Performance Report</h1>
                <p class="text-sm text-gray-500 mt-1">{{ quiz.title }} &middot; {{ group?.name || 'Group' }}</p>
            </div>

            <!-- Summary Cards -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center"><Users class="w-5 h-5 text-blue-600" /></div>
                        <div><p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Total Students</p><p class="text-xl font-bold text-gray-900 mt-0.5">{{ stats.total_students }}</p></div>
                    </div>
                </div>
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-green-100 flex items-center justify-center"><CheckCircle class="w-5 h-5 text-green-600" /></div>
                        <div><p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Participated</p><p class="text-xl font-bold text-gray-900 mt-0.5">{{ stats.participated }}</p></div>
                    </div>
                </div>
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center"><BarChart3 class="w-5 h-5 text-orange-600" /></div>
                        <div><p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Average Score</p><p class="text-xl font-bold text-gray-900 mt-0.5">{{ stats.average_score }}%</p></div>
                    </div>
                </div>
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center"><Award class="w-5 h-5 text-purple-600" /></div>
                        <div><p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Top Score</p><p class="text-xl font-bold text-orange-600 mt-0.5">{{ attempts.length > 0 ? Math.max(...attempts.map(a => a.score)) : 0 }}%</p></div>
                    </div>
                </div>
            </div>

            <!-- Leaderboard -->
            <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <Trophy class="w-5 h-5 text-orange-500" />
                        <h2 class="text-lg font-bold text-gray-900">Leaderboard</h2>
                    </div>
                    <span class="text-xs text-gray-400 bg-gray-50 px-3 py-1 rounded-full">{{ attempts.length }} participant{{ attempts.length !== 1 ? 's' : '' }}</span>
                </div>

                <div v-if="attempts.length === 0" class="p-12 text-center text-gray-500">
                    No participants yet.
                </div>

                <div v-else class="divide-y divide-gray-50">
                    <div v-for="(attempt, index) in attempts" :key="index"
                         :class="['flex items-center gap-4 px-6 py-4 transition hover:bg-gray-50/50', rankClass(index)]">
                        <!-- Rank -->
                        <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold shrink-0"
                             :class="index < 3 ? 'bg-white shadow-sm border' : 'bg-gray-50 text-gray-400'">
                            {{ rankIcon(index) }}
                        </div>

                        <!-- Name -->
                        <div class="flex-1 min-w-0">
                            <p class="font-semibold text-gray-900 truncate">{{ attempt.user_name }}</p>
                            <p class="text-xs text-gray-400">
                                Submitted {{ attempt.submitted_at ? new Date(attempt.submitted_at).toLocaleString() : '—' }}
                            </p>
                        </div>

                        <!-- Time Taken -->
                        <div class="hidden sm:flex items-center gap-1.5 text-sm text-gray-500">
                            <Clock class="w-3.5 h-3.5" />
                            {{ formatTime(attempt.time_taken) }}
                        </div>

                        <!-- Score -->
                        <div class="text-right">
                            <div class="text-lg font-bold" :class="attempt.score >= 80 ? 'text-green-600' : attempt.score >= 50 ? 'text-amber-600' : 'text-red-600'">
                                {{ Math.round(attempt.score) }}%
                            </div>
                            <div class="w-20 h-1.5 bg-gray-200 rounded-full mt-1 overflow-hidden">
                                <div class="h-full rounded-full transition-all"
                                     :class="attempt.score >= 80 ? 'bg-green-500' : attempt.score >= 50 ? 'bg-amber-500' : 'bg-red-500'"
                                     :style="{ width: Math.min(attempt.score, 100) + '%' }"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Note -->
            <div class="text-center text-xs text-gray-400 pb-6">
                Only students who submitted their quiz appear in the report.
                Students who did not participate are not listed.
            </div>
        </div>
    </AuthenticatedLayout>
</template>
