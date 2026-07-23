<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useForm } from '@inertiajs/vue3';

const props = defineProps({ quiz: Object, attempt: Object, deadlineAt: String, serverNow: String });
const form = useForm({ answers: {} });
const submitting = ref(false);
const focusWarning = ref(false);
const clockOffset = Date.parse(props.serverNow) - Date.now();
const secondsRemaining = ref(0);

const updateTimer = () => {
  secondsRemaining.value = Math.max(0, Math.ceil((Date.parse(props.deadlineAt) - (Date.now() + clockOffset)) / 1000));
  if (secondsRemaining.value <= 1 && !submitting.value) submitQuiz(true);
};
updateTimer();
const timer = setInterval(updateTimer, 1000);

const timeLeft = computed(() => `${Math.floor(secondsRemaining.value / 60)}:${String(secondsRemaining.value % 60).padStart(2, '0')}`);
const submitQuiz = (automatic = false) => {
  if (submitting.value) return;
  submitting.value = true;
  const answers = props.quiz.questions.map(question => ({ question_id: question.id, user_answer: form.answers[question.id] ?? '' }));
  form.transform(() => ({ answers })).post(route('quizzes.submit', props.attempt.id), { preserveScroll: true, onFinish: () => { submitting.value = false; } });
};

const onVisibilityChange = () => { if (document.hidden) focusWarning.value = true; };
const beforeUnload = (event) => { if (!submitting.value) { event.preventDefault(); event.returnValue = ''; } };

onMounted(() => {
  document.documentElement.requestFullscreen?.().catch(() => {});
  document.addEventListener('visibilitychange', onVisibilityChange);
  window.addEventListener('beforeunload', beforeUnload);
});
onUnmounted(() => {
  clearInterval(timer);
  document.removeEventListener('visibilitychange', onVisibilityChange);
  window.removeEventListener('beforeunload', beforeUnload);
});
</script>

<template>
  <div class="fixed inset-0 z-50 overflow-y-auto bg-slate-50 p-4 sm:p-8">
    <div class="mx-auto max-w-3xl">
      <header class="sticky top-0 z-10 mb-6 flex items-center justify-between border-b border-slate-200 bg-slate-50 py-4">
        <div><h1 class="text-xl font-bold text-slate-900">{{ quiz.title }}</h1><p class="text-xs text-slate-500">Submit before the timer reaches zero. The server will enforce the deadline.</p></div>
        <div class="rounded-lg bg-rose-600 px-4 py-2 font-mono text-xl font-bold text-white">{{ timeLeft }}</div>
      </header>

      <div v-if="focusWarning" class="mb-5 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">You left the quiz window. Stay on this page until you submit.</div>

      <form @submit.prevent="submitQuiz(false)" class="space-y-5">
        <article v-for="(question, index) in quiz.questions" :key="question.id" class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="font-semibold text-slate-900">{{ index + 1 }}. {{ question.question_text }}</p>
          <div v-if="question.question_type === 'multiple_choice'" class="mt-4 space-y-2">
            <label v-for="option in question.options" :key="option" class="flex cursor-pointer items-center gap-3 rounded-lg border border-slate-200 p-3 hover:border-orange-300"><input v-model="form.answers[question.id]" :value="option" type="radio" class="text-orange-600 focus:ring-orange-500" /> {{ option }}</label>
          </div>
          <div v-else-if="question.question_type === 'true_false'" class="mt-4 flex gap-3"><label v-for="option in ['True', 'False']" :key="option" class="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 px-4 py-3"><input v-model="form.answers[question.id]" :value="option" type="radio" class="text-orange-600 focus:ring-orange-500" /> {{ option }}</label></div>
          <textarea v-else v-model="form.answers[question.id]" rows="3" placeholder="Write your answer..." class="mt-4 w-full rounded-lg border border-slate-300 p-3 text-sm focus:border-orange-500 focus:ring-orange-500" />
        </article>
        <button type="submit" :disabled="submitting" class="w-full rounded-lg bg-orange-600 px-6 py-3 font-semibold text-white hover:bg-orange-700 disabled:opacity-50">{{ submitting ? 'Submitting...' : 'Submit final quiz' }}</button>
      </form>
    </div>
  </div>
</template>
