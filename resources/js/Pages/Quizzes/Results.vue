<script setup>
import { Link } from '@inertiajs/vue3';

defineProps({ attempt: Object });
</script>

<template>
    <div class="max-w-4xl mx-auto p-6 bg-white shadow-md rounded-lg mt-10">
        <h1 class="text-2xl font-bold mb-6">Quiz Results: {{ attempt.quiz.title }}</h1>

        <div class="bg-gray-100 p-6 rounded-lg mb-6">
            <h2 class="text-xl">Your Score: <span class="font-bold text-green-600">{{ attempt.score }}%</span></h2>
            <p class="text-gray-600">Time Taken: {{ Math.round(attempt.time_taken_seconds / 60) }} minutes</p>
        </div>

        <h3 class="font-bold mb-4">Detailed Performance:</h3>
        <div v-for="answer in attempt.answers" :key="answer.id" class="border-b py-3">
            <p>{{ answer.question.question_text }}</p>
            <p :class="answer.is_correct ? 'text-green-600' : 'text-red-600'">
                Your Answer: {{ answer.user_answer }}
                <span v-if="!answer.is_correct">(Correct: {{ answer.question.correct_answer }})</span>
            </p>
        </div>

        <div class="mt-8 flex justify-end">
            <Link :href="route('dashboard')" class="rounded-lg bg-orange-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-orange-700 transition">
                Back to Dashboard
            </Link>
        </div>
    </div>
</template>
