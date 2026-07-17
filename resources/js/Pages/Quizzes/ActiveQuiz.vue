<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useForm } from '@inertiajs/vue3';

const props = defineProps({ quiz: Object, attempt: Object });

// Auto-submit logic: Redirect to submit if time expires
const timeLeft = ref(props.quiz.duration_minutes * 60);

const timer = setInterval(() => {
    if (timeLeft.value > 0) {
        timeLeft.value--;
    } else {
        clearInterval(timer);
        submitQuiz(); // Force auto-submit
    }
}, 1000);

const form = useForm({ answers: [] });

const submitQuiz = () => {
    form.post(route('quizzes.submit', props.attempt.id));
};

// "Prevent user from doing other things" logic
onMounted(() => {
    // 1. Fullscreen mode
    document.documentElement.requestFullscreen();
    
    // 2. Warn before closing
    window.addEventListener('beforeunload', (event) => {
        event.preventDefault();
        event.returnValue = '';
    });
});

onUnmounted(() => {
    clearInterval(timer);
});
</script>

<template>
    <div class="fixed inset-0 z-50 bg-white p-10 overflow-y-auto">
        <div class="max-w-3xl mx-auto">
            <div class="flex justify-between items-center mb-6 border-b pb-4">
                <h1 class="text-3xl font-bold">{{ quiz.title }}</h1>
                <div class="text-xl font-mono text-red-600 font-bold">
                    Time Left: {{ Math.floor(timeLeft / 60) }}:{{ (timeLeft % 60).toString().padStart(2, '0') }}
                </div>
            </div>

            <form @submit.prevent="submitQuiz">
                <div v-for="(question, index) in quiz.questions" :key="question.id" class="mb-6">
                    <p class="font-semibold">{{ index + 1 }}. {{ question.question_text }}</p>
                    <input v-model="form.answers[index]" type="text" class="w-full border p-2 mt-2" required>
                </div>
                
                <button type="submit" class="bg-blue-600 text-white px-6 py-2 rounded">Submit Final Quiz</button>
            </form>
        </div>
    </div>
</template>