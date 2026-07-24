<script setup>
import { onUnmounted, ref } from 'vue';
import { router } from '@inertiajs/vue3';

const props = defineProps({ quiz: Object });
const secondsToStart = ref(Math.max(0, Math.ceil((Date.parse(props.quiz.start_time) - Date.now()) / 1000)));
const timer = setInterval(() => {
    secondsToStart.value = Math.max(0, Math.ceil((Date.parse(props.quiz.start_time) - Date.now()) / 1000));
    if (secondsToStart.value === 0) {
        clearInterval(timer);
        router.reload();
    }
}, 1000);
onUnmounted(() => clearInterval(timer));
</script>

<template>
    <div class="mx-auto mt-10 max-w-2xl rounded-2xl border border-orange-200 bg-orange-50 p-5 text-center sm:mt-20 sm:p-8">
        <h1 class="mb-4 text-2xl font-bold text-slate-900 sm:text-3xl">{{ quiz.title }}</h1>
        <p class="mb-6 text-lg text-slate-600">{{ quiz.description }}</p>
        
        <div class="bg-white p-6 rounded shadow-sm inline-block">
            <p class="font-bold">Starts at:</p>
            <p class="text-xl text-red-600">{{ new Date(quiz.start_time).toLocaleString('en-KE', { timeZone: 'Africa/Nairobi' }) }}</p>
            <p class="mt-1 text-xs text-gray-500">Africa/Nairobi time</p>
            <p class="mt-4 font-bold">Duration:</p>
            <p class="text-xl">{{ quiz.duration_minutes }} minutes</p>
        </div>
        
        <p class="mt-8 text-sm text-gray-500 italic">This quiz will automatically open here at the scheduled time.</p>
        <p class="mt-2 font-mono text-sm text-orange-700">Starts in {{ Math.floor(secondsToStart / 60) }}:{{ String(secondsToStart % 60).padStart(2, '0') }}</p>
    </div>
</template>
