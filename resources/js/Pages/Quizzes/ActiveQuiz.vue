<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useForm } from '@inertiajs/vue3';

const props = defineProps({ quiz: Object, attempt: Object, duration: Number });

// Calculate remaining seconds from server-side timing
const endsAt = new Date(props.quiz.end_time).getTime();
const startedAt = new Date(props.attempt.started_at).getTime();
const maxDuration = props.duration * 60;
const elapsed = Math.max(0, Math.floor((Date.now() - startedAt) / 1000));
const remaining = Math.max(maxDuration - elapsed, 0);

const timeLeft = ref(remaining);
const lockdownViolations = ref(0);

// Answer form — track by question_id
const form = useForm({ answers: {} });

onMounted(() => {
    if (props.quiz.questions) {
        props.quiz.questions.forEach(q => {
            form.answers[q.id] = '';
        });
    }
});

// Timer countdown every second
const timer = setInterval(() => {
    if (timeLeft.value > 0) {
        timeLeft.value--;
    } else {
        clearInterval(timer);
        autoSubmit();
    }
}, 1000);

const formattedTime = computed(() => {
    const mins = Math.floor(timeLeft.value / 60);
    const secs = timeLeft.value % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
});

const timerWarning = computed(() => {
    if (timeLeft.value <= 60) return 'critical';
    if (timeLeft.value <= 300) return 'warning';
    return 'normal';
});

const buildPayload = () => {
    return Object.entries(form.answers).map(([questionId, userAnswer]) => ({
        question_id: parseInt(questionId),
        user_answer: userAnswer
    }));
};

const autoSubmit = () => {
    form.answers = buildPayload();
    form.post(route('quizzes.submit', props.attempt.id));
};

const submitQuiz = () => {
    if (confirm('Are you sure you want to submit your quiz?')) {
        form.answers = buildPayload();
        form.post(route('quizzes.submit', props.attempt.id));
    }
};

// ─── LOCKDOWN ───
const handleBeforeUnload = (event) => {
    event.preventDefault();
    event.returnValue = 'Quiz in progress! Submit or your answers will be lost.';
};

const handleVisibilityChange = () => {
    if (document.hidden && timeLeft.value > 0) {
        lockdownViolations.value++;
        if (lockdownViolations.value >= 3) autoSubmit();
    }
};

const handleBlur = () => {
    if (timeLeft.value > 0) {
        lockdownViolations.value++;
        if (lockdownViolations.value >= 3) autoSubmit();
    }
};

const handleContextMenu = (event) => event.preventDefault();

const handleKeyDown = (event) => {
    if (event.key === 'F12' ||
        (event.ctrlKey && event.shiftKey && (event.key === 'I' || event.key === 'J')) ||
        (event.ctrlKey && event.key === 'U')) {
        event.preventDefault();
    }
};

onMounted(() => {
    try { document.documentElement.requestFullscreen(); } catch(e) {}
    window.addEventListener('beforeunload', handleBeforeUnload);
    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('blur', handleBlur);
    document.addEventListener('contextmenu', handleContextMenu);
    document.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
    clearInterval(timer);
    window.removeEventListener('beforeunload', handleBeforeUnload);
    document.removeEventListener('visibilitychange', handleVisibilityChange);
    window.removeEventListener('blur', handleBlur);
    document.removeEventListener('contextmenu', handleContextMenu);
    document.removeEventListener('keydown', handleKeyDown);
    if (document.fullscreenElement) {
        try { document.exitFullscreen(); } catch(e) {}
    }
});
</script>

<template>
    <div class="fixed inset-0 z-[9999] bg-white flex flex-col">

        <!-- Top Lockdown Bar -->
        <div class="bg-orange-600 text-white text-center text-xs py-1.5 font-medium tracking-wide flex items-center justify-center gap-2 shrink-0">
            <span class="w-2 h-2 bg-white rounded-full animate-pulse"></span>
            QUIZ IN PROGRESS — Do not navigate away or switch tabs
            <span v-if="lockdownViolations > 0" class="ml-2 bg-red-500 px-2 py-0.5 rounded-full text-[10px]">
                {{ lockdownViolations }} warning{{ lockdownViolations > 1 ? 's' : '' }}
            </span>
        </div>

        <!-- Header -->
        <div class="border-b border-gray-200 bg-white px-6 py-4 shrink-0 shadow-sm">
            <div class="max-w-4xl mx-auto flex items-center justify-between">
                <div class="flex items-center gap-4">
                    <h1 class="text-xl font-bold text-gray-900 truncate">{{ quiz.title }}</h1>
                    <span class="text-xs bg-gray-100 text-gray-500 px-2.5 py-1 rounded-full font-medium">
                        {{ quiz.questions?.length || 0 }} questions
                    </span>
                </div>
                <div class="flex items-center gap-4">
                    <div :class="[
                        'font-mono font-bold text-lg px-4 py-1.5 rounded-lg transition-colors',
                        timerWarning === 'critical' ? 'bg-red-100 text-red-700 animate-pulse' :
                        timerWarning === 'warning' ? 'bg-amber-100 text-amber-700' :
                        'bg-gray-100 text-gray-700'
                    ]">
                        <span v-if="timerWarning === 'critical'" class="mr-1.5">⚠</span>
                        {{ formattedTime }}
                    </div>
                    <button @click="submitQuiz"
                            class="px-5 py-2 bg-orange-600 text-white rounded-lg font-semibold hover:bg-orange-700 transition shadow-sm text-sm">
                        Submit Quiz
                    </button>
                </div>
            </div>
        </div>

        <!-- Questions Body -->
        <div class="flex-1 overflow-y-auto bg-gray-50">
            <div class="max-w-3xl mx-auto p-6 space-y-6">
                <div v-for="(question, index) in quiz.questions" :key="question.id"
                     class="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
                    <div class="flex items-start gap-3 mb-4">
                        <span class="w-7 h-7 rounded-full bg-orange-600 text-white text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">
                            {{ index + 1 }}
                        </span>
                        <div class="flex-1">
                            <p class="font-semibold text-gray-900 leading-relaxed">{{ question.question_text }}</p>
                            <span class="text-xs text-gray-400 mt-1 inline-block">{{ question.points || 1 }} point{{ question.points > 1 ? 's' : '' }}</span>
                        </div>
                    </div>

                    <div v-if="question.question_type === 'multiple_choice'" class="space-y-2 pl-10">
                        <label v-for="(opt, oIdx) in (question.options || [])" :key="oIdx"
                               :class="[
                                   'flex items-center gap-3 px-4 py-3 border rounded-lg cursor-pointer transition',
                                   form.answers[question.id] === opt
                                       ? 'border-orange-500 bg-orange-50 ring-1 ring-orange-500'
                                       : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                               ]">
                            <input type="radio" :name="'q_' + question.id" :value="opt"
                                   v-model="form.answers[question.id]"
                                   class="w-4 h-4 text-orange-600 accent-orange-600">
                            <span class="text-sm text-gray-700">{{ opt }}</span>
                        </label>
                    </div>

                    <div v-if="question.question_type === 'true_false'" class="flex gap-3 pl-10">
                        <label v-for="opt in ['True', 'False']" :key="opt"
                               :class="[
                                   'flex-1 flex items-center justify-center gap-2 px-4 py-3 border rounded-lg cursor-pointer transition font-medium',
                                   form.answers[question.id] === opt
                                       ? 'border-orange-500 bg-orange-50 ring-1 ring-orange-500 text-orange-700'
                                       : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50 text-gray-600'
                               ]">
                            <input type="radio" :name="'q_' + question.id" :value="opt" v-model="form.answers[question.id]" class="sr-only">
                            <span>{{ opt }}</span>
                        </label>
                    </div>

                    <div v-if="question.question_type === 'short_answer'" class="pl-10">
                        <textarea v-model="form.answers[question.id]" rows="2" placeholder="Type your answer..."
                                  class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition resize-none text-sm">
                        </textarea>
                    </div>
                </div>

                <div v-if="!quiz.questions || quiz.questions.length === 0"
                     class="text-center py-12 text-gray-500">
                    No questions available for this quiz.
                </div>
            </div>
        </div>

        <!-- Bottom Bar -->
        <div class="border-t border-gray-200 bg-white px-6 py-3 shrink-0 flex items-center justify-between">
            <div class="flex items-center gap-2 text-xs text-gray-500">
                <span v-if="lockdownViolations >= 3" class="text-red-600 font-semibold">
                    Quiz auto-submitted due to tab switches
                </span>
                <span v-else>
                    Tab switches remaining: {{ Math.max(0, 3 - lockdownViolations) }}
                </span>
            </div>
            <button @click="submitQuiz"
                    class="px-8 py-2.5 bg-orange-600 text-white rounded-lg font-bold hover:bg-orange-700 transition shadow-sm">
                {{ form.processing ? 'Submitting...' : 'Submit Final Quiz' }}
            </button>
        </div>
    </div>
</template>
