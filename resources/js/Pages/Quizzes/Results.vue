<script setup>
import { computed } from 'vue';
import { Link } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { CheckCircle, Clock, Trophy, XCircle } from 'lucide-vue-next';

const props = defineProps({
  attempt: {
    type: Object,
    required: true,
  },
});

const score = computed(() => Number(props.attempt.score ?? 0));

const scoreClass = computed(() => {
  if (score.value >= 85) return 'bg-emerald-50 text-emerald-700';
  if (score.value >= 70) return 'bg-blue-50 text-blue-700';
  if (score.value >= 50) return 'bg-amber-50 text-amber-700';
  return 'bg-rose-50 text-rose-700';
});

const scoreMessage = computed(() => {
  if (score.value >= 85) return 'Excellent work';
  if (score.value >= 70) return 'Good job';
  if (score.value >= 50) return 'Keep practising';
  return 'Review the material and try again';
});

const formatTime = (seconds) => {
  if (seconds === null || seconds === undefined) return '—';

  const totalSeconds = Math.max(0, Number(seconds));
  const minutes = Math.floor(totalSeconds / 60);
  const remainingSeconds = totalSeconds % 60;

  return `${minutes}m ${remainingSeconds}s`;
};
</script>

<template>
  <AuthenticatedLayout page-title="Quiz Results">
    <div class="mx-auto w-full max-w-4xl space-y-6 p-4 sm:p-6">
      <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="bg-gradient-to-r from-orange-600 to-amber-500 px-5 py-6 text-white sm:px-8">
          <p class="text-xs font-bold uppercase tracking-widest text-orange-100">Quiz complete</p>
          <h1 class="mt-1 text-2xl font-bold sm:text-3xl">{{ attempt.quiz.title }}</h1>
          <p v-if="attempt.auto_submitted_at" class="mt-3 text-sm text-orange-100">
            This quiz was automatically submitted when the timer expired.
          </p>
        </div>

        <div class="grid gap-4 p-5 sm:grid-cols-2 sm:p-6">
          <div>
            <p class="text-sm font-medium text-slate-500">Your score</p>
            <div class="mt-2 flex flex-wrap items-center gap-3">
              <span :class="scoreClass" class="rounded-xl px-4 py-2 text-3xl font-bold">
                {{ score }}%
              </span>
              <p class="text-sm font-semibold text-slate-700">{{ scoreMessage }}</p>
            </div>
          </div>

          <div class="sm:border-l sm:border-slate-100 sm:pl-6">
            <p class="text-sm font-medium text-slate-500">Time taken</p>
            <div class="mt-3 flex items-center gap-2 text-lg font-bold text-slate-800">
              <Clock class="h-5 w-5 text-orange-500" />
              {{ formatTime(attempt.time_taken_seconds) }}
            </div>
          </div>
        </div>
      </section>

      <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 px-5 py-4 sm:px-6">
          <h2 class="font-bold text-slate-900">Question breakdown</h2>
          <p class="mt-1 text-sm text-slate-500">Review each answer and its score.</p>
        </div>

        <div class="divide-y divide-slate-100">
          <article
            v-for="(answer, index) in attempt.answers"
            :key="answer.id"
            class="flex gap-3 px-5 py-4 sm:px-6"
          >
            <CheckCircle v-if="answer.is_correct" class="mt-0.5 h-5 w-5 shrink-0 text-emerald-500" />
            <XCircle v-else class="mt-0.5 h-5 w-5 shrink-0 text-rose-500" />

            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-slate-900">
                {{ index + 1 }}. {{ answer.question.question_text }}
              </p>
              <div class="mt-2 flex flex-wrap gap-2 text-xs">
                <span class="rounded bg-slate-100 px-2 py-1 text-slate-600">
                  Your answer: <strong>{{ answer.user_answer || '(blank)' }}</strong>
                </span>
                <span v-if="!answer.is_correct" class="rounded bg-emerald-50 px-2 py-1 text-emerald-700">
                  Correct answer: <strong>{{ answer.question.correct_answer }}</strong>
                </span>
              </div>
            </div>

            <span
              :class="answer.is_correct ? 'text-emerald-600' : 'text-rose-500'"
              class="shrink-0 text-right text-xs font-bold"
            >
              {{ answer.points_earned }} / {{ answer.question.points }} pts
            </span>
          </article>
        </div>
      </section>

      <div class="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <Link
          :href="route('dashboard')"
          class="inline-flex items-center justify-center rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
        >
          Back to dashboard
        </Link>
        <Link
          :href="route('quizzes.performance', attempt.quiz.id)"
          class="inline-flex items-center justify-center gap-2 rounded-xl bg-orange-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-orange-700"
        >
          <Trophy class="h-4 w-4" />
          View performance report
        </Link>
      </div>
    </div>
  </AuthenticatedLayout>
</template>
