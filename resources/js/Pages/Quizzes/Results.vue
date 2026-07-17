<script setup>
import { computed } from 'vue';
import { router, Link } from '@inertiajs/vue3';
import { ArrowLeft, CheckCircle, XCircle, Clock, Award, BarChart3 } from 'lucide-vue-next';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const props = defineProps({
    attempt: Object,
});

const timeTaken = computed(() => {
    if (!props.attempt.time_taken_seconds) return '—';
    const m = Math.floor(props.attempt.time_taken_seconds / 60);
    const s = props.attempt.time_taken_seconds % 60;
    return `${m}m ${s}s`;
});

const gradeClass = computed(() => {
    if (props.attempt.score >= 80) return 'text-green-600 bg-green-50 border-green-200';
    if (props.attempt.score >= 50) return 'text-amber-600 bg-amber-50 border-amber-200';
    return 'text-red-600 bg-red-50 border-red-200';
});

const gradeText = computed(() => {
    if (props.attempt.score >= 80) return 'Excellent';
    if (props.attempt.score >= 65) return 'Good';
    if (props.attempt.score >= 50) return 'Fair';
    return 'Needs Improvement';
});
</script>

<template>
    <AuthenticatedLayout :page-title="'Results'">
        <div class="max-w-4xl mx-auto min-h-screen bg-gray-50 p-6 space-y-8">

            <button @click="router.visit(route('quizzes.index'))" class="inline-flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-orange-600 transition-colors">
                <ArrowLeft class="w-4 h-4" /> Back to Quizzes
            </button>

            <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8 text-center">
                <h1 class="text-2xl font-bold text-gray-900">{{ attempt.quiz?.title || 'Quiz' }}</h1>
                <p class="text-sm text-gray-500 mt-1">Your Performance Report</p>
                <div class="mt-8 inline-flex items-center justify-center w-32 h-32 rounded-full border-4 border-orange-200 bg-orange-50">
                    <span class="text-4xl font-black text-orange-600">{{ Math.round(attempt.score) }}%</span>
                </div>
                <div :class="['mt-6 inline-flex items-center gap-2 px-5 py-2 rounded-full border font-semibold text-sm', gradeClass]">
                    <Award class="w-4 h-4" /> {{ gradeText }}
                </div>
                <div class="mt-6 flex justify-center gap-8 text-sm text-gray-500">
                    <div class="flex items-center gap-2"><Clock class="w-4 h-4 text-gray-400" /> {{ timeTaken }}</div>
                    <div>{{ attempt.answers?.length || 0 }} questions</div>
                </div>
            </div>

            <div class="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
                <div class="px-6 py-5 border-b border-gray-100">
                    <h2 class="text-lg font-bold text-gray-900">Detailed Breakdown</h2>
                </div>
                <div v-if="!attempt.answers || attempt.answers.length === 0" class="p-12 text-center text-gray-500">
                    No answer details available.
                </div>
                <div v-else class="divide-y divide-gray-50">
                    <div v-for="answer in attempt.answers" :key="answer.id" class="px-6 py-5">
                        <div class="flex items-start gap-4">
                            <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 mt-0.5"
                                 :class="answer.is_correct ? 'bg-green-100' : 'bg-red-100'">
                                <CheckCircle v-if="answer.is_correct" class="w-4 h-4 text-green-600" />
                                <XCircle v-else class="w-4 h-4 text-red-600" />
                            </div>
                            <div class="flex-1">
                                <p class="font-semibold text-gray-900">{{ answer.question?.question_text || 'Question' }}</p>
                                <div class="mt-2 space-y-1 text-sm">
                                    <p><span class="text-gray-500">Your answer:</span>
                                        <span :class="answer.is_correct ? 'text-green-700 font-medium' : 'text-red-700 font-medium'">{{ answer.user_answer }}</span>
                                    </p>
                                    <p v-if="!answer.is_correct" class="text-gray-500">
                                        Correct answer: <span class="text-green-700 font-medium">{{ answer.question?.correct_answer }}</span>
                                    </p>
                                </div>
                                <div class="mt-2 text-xs text-gray-400">{{ answer.points_earned || 0 }} / {{ answer.question?.points || 0 }} points</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="bg-orange-50 rounded-2xl border border-orange-200 p-6 flex items-center justify-between">
                <div class="flex items-center gap-4">
                    <div class="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center"><BarChart3 class="w-5 h-5 text-orange-600" /></div>
                    <div>
                        <p class="font-semibold text-gray-900">Group Performance Report</p>
                        <p class="text-sm text-gray-600">See how all members performed on this quiz</p>
                    </div>
                </div>
                <Link :href="route('quizzes.performance', attempt.quiz_id)"
                      class="px-5 py-2.5 bg-orange-600 text-white rounded-lg font-semibold hover:bg-orange-700 transition shadow-sm text-sm shrink-0">
                    View Full Report
                </Link>
            </div>
        </div>
    </AuthenticatedLayout>
</template>
