<script setup>
import { computed } from 'vue';
import { router, Link } from '@inertiajs/vue3';
import { Clock, Award, ArrowLeft, Play } from 'lucide-vue-next';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const props = defineProps({
    quiz: Object,
    status: String,
});

const formatDate = (date) => {
    if (!date) return '';
    return new Date(date).toLocaleString();
};

const questionsCount = computed(() => props.quiz.questions?.length ?? 0);

const startQuiz = () => {
    router.visit(route('quizzes.start', props.quiz.id));
};
</script>

<template>
    <AuthenticatedLayout :page-title="quiz.title">
        <div class="max-w-3xl mx-auto p-6 space-y-6">

            <Link :href="route('quizzes.index')"
                  class="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-orange-600 transition font-medium">
                <ArrowLeft class="w-4 h-4" /> Back to Quizzes
            </Link>

            <div class="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
                <div class="bg-gradient-to-r from-orange-500 to-amber-500 p-8 text-white">
                    <h1 class="text-2xl font-bold">{{ quiz.title }}</h1>
                    <p class="text-orange-100 mt-2 text-sm">{{ quiz.description || 'No description provided.' }}</p>
                </div>

                <div class="p-6 space-y-6">
                    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
                        <div class="bg-gray-50 rounded-xl p-4 text-center">
                            <Clock class="w-5 h-5 text-orange-500 mx-auto mb-1" />
                            <p class="text-lg font-bold text-gray-900">{{ quiz.duration_minutes }}min</p>
                            <p class="text-xs text-gray-500">Duration</p>
                        </div>
                        <div class="bg-gray-50 rounded-xl p-4 text-center">
                            <Award class="w-5 h-5 text-orange-500 mx-auto mb-1" />
                            <p class="text-lg font-bold text-gray-900">{{ questionsCount }}</p>
                            <p class="text-xs text-gray-500">Questions</p>
                        </div>
                        <div class="bg-gray-50 rounded-xl p-4 text-center col-span-2">
                            <p class="text-xs text-gray-500 mb-1">Time Window</p>
                            <p class="text-sm font-semibold text-gray-900">{{ formatDate(quiz.start_time) }}</p>
                            <p class="text-xs text-gray-400">to {{ formatDate(quiz.end_time) }}</p>
                        </div>
                    </div>

                    <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 text-sm text-amber-800">
                        <p class="font-semibold">Quiz is active!</p>
                        <p class="text-amber-700 mt-1">You can start this quiz now. Once started, a timer will count down. Do not navigate away or the quiz will auto-submit after 3 tab switches.</p>
                    </div>

                    <PrimaryButton @click="startQuiz" class="w-full sm:w-auto flex items-center justify-center gap-2 text-base py-3 px-8">
                        <Play class="w-5 h-5" /> Start Quiz
                    </PrimaryButton>
                </div>
            </div>
        </div>
    </AuthenticatedLayout>
</template>
