<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useForm } from '@inertiajs/vue3';

const props = defineProps({ quiz: Object, attempt: Object, deadlineAt: String, serverNow: String });
const form = useForm({ answers: {} });
const submitting    = ref(false);
const focusWarning  = ref(false);
const showConfirm   = ref(false);   // submit confirmation dialog
const currentIndex  = ref(0);       // question navigator

// Server-clock-corrected countdown
const clockOffset      = Date.parse(props.serverNow) - Date.now();
const secondsRemaining = ref(0);

const updateTimer = () => {
  secondsRemaining.value = Math.max(0, Math.ceil(
    (Date.parse(props.deadlineAt) - (Date.now() + clockOffset)) / 1000
  ));
  if (secondsRemaining.value <= 1 && !submitting.value) submitQuiz(true);
};
updateTimer();
const timer = setInterval(updateTimer, 1000);

// Formatted time display
const timeLeft = computed(() => {
  const m = Math.floor(secondsRemaining.value / 60);
  const s = String(secondsRemaining.value % 60).padStart(2, '0');
  return `${m}:${s}`;
});

// Timer turns red + pulses under 5 minutes (SDD Table 6.2)
const timerClass = computed(() =>
  secondsRemaining.value < 300
    ? 'rounded-lg px-4 py-2 font-mono text-xl font-bold text-white bg-red-700 animate-pulse'
    : 'rounded-lg px-4 py-2 font-mono text-xl font-bold text-white bg-rose-600'
);

// Questions
const questions     = computed(() => props.quiz.questions ?? []);
const totalQ        = computed(() => questions.value.length);
const currentQ      = computed(() => questions.value[currentIndex.value]);

// How many have been answered
const answeredCount = computed(() =>
  questions.value.filter(q => (form.answers[q.id] ?? '').toString().trim() !== '').length
);

// Navigator helpers
const goTo   = (i)  => { currentIndex.value = i; };
const prev   = ()   => { if (currentIndex.value > 0) currentIndex.value--; };
const next   = ()   => { if (currentIndex.value < totalQ.value - 1) currentIndex.value++; };

// Submit flow
const handleSubmitClick = () => {
  showConfirm.value = true;   // show confirmation dialog
};

const confirmSubmit = () => {
  showConfirm.value = false;
  submitQuiz(false);
};

const submitQuiz = (automatic = false) => {
  if (submitting.value) return;
  submitting.value = true;
  const answers = questions.value.map(q => ({
    question_id: q.id,
    user_answer: form.answers[q.id] ?? '',
  }));
  form.transform(() => ({ answers })).post(
    route('quizzes.submit', props.attempt.id),
    { preserveScroll: true, onFinish: () => { submitting.value = false; } }
  );
};

// Anti-cheat hooks
const onVisibilityChange = () => { if (document.hidden) focusWarning.value = true; };
const beforeUnload = (event) => {
  if (!submitting.value) { event.preventDefault(); event.returnValue = ''; }
};

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
  <div class="fixed inset-0 z-50 flex flex-col bg-slate-50">

    <!-- ── Sticky header ─────────────────────────────────────── -->
    <header class="shrink-0 border-b border-slate-200 bg-slate-50 px-4 py-3 sm:px-8">
      <div class="mx-auto flex max-w-3xl items-center justify-between">
        <div>
          <h1 class="text-lg font-bold text-slate-900">{{ quiz.title }}</h1>
          <p class="text-xs text-slate-500">
            {{ answeredCount }} / {{ totalQ }} answered ·
            Submit before the timer reaches zero.
          </p>
        </div>
        <!-- Timer — turns red + pulses under 5 minutes (SDD Table 6.2) -->
        <div :class="timerClass">{{ timeLeft }}</div>
      </div>

      <!-- Focus-loss warning -->
      <div v-if="focusWarning"
           class="mx-auto mt-2 max-w-3xl rounded-lg border border-amber-200 bg-amber-50 p-2 text-xs text-amber-800">
        ⚠ You left the quiz window. Stay on this page until you submit.
      </div>
    </header>

    <!-- ── Question navigator (SDD Table 6.2) ───────────────── -->
    <div class="shrink-0 border-b border-slate-200 bg-white px-4 py-3 sm:px-8">
      <div class="mx-auto max-w-3xl">
        <p class="mb-2 text-[11px] font-semibold uppercase tracking-wide text-slate-400">
          Questions — click to jump
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="(q, i) in questions"
            :key="q.id"
            @click="goTo(i)"
            :class="[
              'flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold transition',
              i === currentIndex
                ? 'bg-orange-600 text-white ring-2 ring-orange-300'
                : (form.answers[q.id] ?? '').toString().trim()
                  ? 'bg-emerald-100 text-emerald-700'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            ]"
          >
            {{ i + 1 }}
          </button>
        </div>
        <p class="mt-2 text-[11px] text-slate-400">
          <span class="inline-block h-2.5 w-2.5 rounded-full bg-orange-600 mr-1"></span>Current &nbsp;
          <span class="inline-block h-2.5 w-2.5 rounded-full bg-emerald-100 border border-emerald-400 mr-1"></span>Answered &nbsp;
          <span class="inline-block h-2.5 w-2.5 rounded-full bg-slate-100 border border-slate-300 mr-1"></span>Unanswered
        </p>
      </div>
    </div>

    <!-- ── Scrollable question body ──────────────────────────── -->
    <div class="flex-1 overflow-y-auto px-4 py-6 sm:px-8">
      <div class="mx-auto max-w-3xl">

        <!-- Single question card (SDD Table 6.2) -->
        <article v-if="currentQ"
                 class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <!-- Question number + text -->
          <p class="mb-1 text-xs font-semibold uppercase tracking-wide text-slate-400">
            Question {{ currentIndex + 1 }} of {{ totalQ }}
          </p>
          <p class="text-base font-semibold text-slate-900">
            {{ currentQ.question_text }}
          </p>

          <!-- Multiple choice -->
          <div v-if="currentQ.question_type === 'multiple_choice'" class="mt-4 space-y-2">
            <label
              v-for="option in currentQ.options"
              :key="option"
              :class="[
                'flex cursor-pointer items-center gap-3 rounded-lg border p-3 transition',
                form.answers[currentQ.id] === option
                  ? 'border-orange-400 bg-orange-50'
                  : 'border-slate-200 hover:border-orange-300'
              ]"
            >
              <input
                v-model="form.answers[currentQ.id]"
                :value="option"
                type="radio"
                class="text-orange-600 focus:ring-orange-500"
              />
              {{ option }}
            </label>
          </div>

          <!-- True / False -->
          <div v-else-if="currentQ.question_type === 'true_false'" class="mt-4 flex gap-3">
            <label
              v-for="option in ['True', 'False']"
              :key="option"
              :class="[
                'flex flex-1 cursor-pointer items-center justify-center gap-2 rounded-lg border px-4 py-3 transition',
                form.answers[currentQ.id] === option
                  ? 'border-orange-400 bg-orange-50 font-semibold text-orange-700'
                  : 'border-slate-200 hover:border-orange-300'
              ]"
            >
              <input
                v-model="form.answers[currentQ.id]"
                :value="option"
                type="radio"
                class="text-orange-600 focus:ring-orange-500"
              />
              {{ option }}
            </label>
          </div>

          <!-- Short answer -->
          <textarea
            v-else
            v-model="form.answers[currentQ.id]"
            rows="4"
            placeholder="Write your answer here…"
            class="mt-4 w-full rounded-lg border border-slate-300 p-3 text-sm focus:border-orange-500 focus:outline-none focus:ring-1 focus:ring-orange-500"
          />
        </article>

        <!-- ── Prev / Next navigation (SDD Table 6.2) ─────── -->
        <div class="mt-6 flex items-center justify-between gap-3">
          <button
            @click="prev"
            :disabled="currentIndex === 0"
            class="flex items-center gap-2 rounded-lg border border-slate-300 px-5 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
          >
            ← Previous
          </button>

          <span class="text-xs text-slate-400">{{ currentIndex + 1 }} / {{ totalQ }}</span>

          <button
            v-if="currentIndex < totalQ - 1"
            @click="next"
            class="flex items-center gap-2 rounded-lg border border-slate-300 px-5 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          >
            Next →
          </button>

          <!-- Show Submit on last question -->
          <button
            v-else
            @click="handleSubmitClick"
            :disabled="submitting"
            class="rounded-lg bg-orange-600 px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-orange-700 disabled:opacity-50"
          >
            {{ submitting ? 'Submitting…' : 'Submit Quiz' }}
          </button>
        </div>

        <!-- Global submit button (always visible at bottom) -->
        <button
          @click="handleSubmitClick"
          :disabled="submitting"
          class="mt-4 w-full rounded-lg border-2 border-orange-600 bg-white px-6 py-3 font-semibold text-orange-600 transition hover:bg-orange-600 hover:text-white disabled:opacity-50"
        >
          {{ submitting ? 'Submitting…' : 'Submit final quiz' }}
        </button>

      </div>
    </div>

    <!-- ── Submit confirmation dialog (SDD Table 6.2) ───────── -->
    <Teleport to="body">
      <div
        v-if="showConfirm"
        class="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 p-4"
      >
        <div class="w-full max-w-sm rounded-2xl bg-white p-6 shadow-2xl">
          <h2 class="text-lg font-bold text-slate-900">Submit quiz?</h2>
          <p class="mt-2 text-sm text-slate-500">
            You have answered
            <strong class="text-slate-800">{{ answeredCount }} of {{ totalQ }}</strong>
            questions.
            <span v-if="answeredCount < totalQ" class="text-amber-600">
              {{ totalQ - answeredCount }} question(s) are unanswered and will be scored as blank.
            </span>
            <span v-else class="text-emerald-600">All questions answered.</span>
          </p>
          <p class="mt-2 text-xs text-slate-400">
            You cannot change your answers after submitting.
          </p>
          <div class="mt-5 flex gap-3">
            <button
              @click="showConfirm = false"
              class="flex-1 rounded-xl border border-slate-300 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
            >
              Go back
            </button>
            <button
              @click="confirmSubmit"
              class="flex-1 rounded-xl bg-orange-600 py-2.5 text-sm font-semibold text-white transition hover:bg-orange-700"
            >
              Yes, submit
            </button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>
