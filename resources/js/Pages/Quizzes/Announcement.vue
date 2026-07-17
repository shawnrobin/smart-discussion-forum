<script setup>
defineProps({ quiz: Object });
</script>

<template>
    <div class="max-w-2xl mx-auto mt-20 p-8 bg-blue-50 border border-blue-200 rounded-lg text-center">
        <h1 class="text-3xl font-bold text-blue-800 mb-4">{{ quiz.title }}</h1>
        <p class="text-lg text-blue-600 mb-6">{{ quiz.description }}</p>
        
        <div class="bg-white p-6 rounded shadow-sm inline-block">
            <p class="font-bold">Starts at:</p>
            <p class="text-xl text-red-600">{{ new Date(quiz.start_time).toLocaleString() }}</p>
            <p class="mt-4 font-bold">Duration:</p>
            <p class="text-xl">{{ quiz.duration_minutes }} minutes</p>
        </div>
        
        <p class="mt-8 text-sm text-gray-500 italic">This quiz will automatically appear here at the scheduled time.</p>
    </div>
</template>