<script setup>
import { Link } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { CheckCircle, XCircle, Clock, Trophy } from 'lucide-vue-next';

defineProps({ attempt: Object });

const bandColor = (score) => {
    if (score >= 85) return 'text-emerald-600';
    if (score >= 70) return 'text-blue-600';
    if (score >= 50) return 'text-amber-600';
    return 'text-rose-600';
};

const formatTime = (secs) => {
    if (!secs) return '—';
    return `${Math.floor(secs / 60)}m ${secs % 60}s`;
};
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
            <p class="mt-1 text-xs text-slate-500">Your score</p>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-500">
            <Clock class="h-4 w-4 text-orange-400" />
            Time taken: {{ formatTime(attempt.time_taken_seconds) }}
          </div>
          <div v-if="attempt.auto_submitted_at"
               class="rounded-lg bg-amber-50 px-3 py-2 text-xs font-medium text-amber-700">
            ⚠ Auto-submitted when timer expired
          </div>
        </div>
      </section>

      <!-- Question-by-question breakdown -->
      <section class="rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
        <div class="border-b border-slate-100 px-6 py-4">
          <h2 class="font-bold text-slate-900">Question breakdown</h2>
        </div>
        <div class="divide-y divide-slate-100">
          <div v-for="(answer, i) in attempt.answers" :key="answer.id" class="px-6 py-4">
            <div class="flex items-start gap-3">
              <div class="mt-0.5 shrink-0">
                <CheckCircle v-if="answer.is_correct" class="h-5 w-5 text-emerald-500" />
                <XCircle v-else class="h-5 w-5 text-rose-500" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-medium text-slate-900 text-sm">
                  {{ i + 1 }}. {{ answer.question.question_text }}
                </p>
                <div class="mt-2 flex flex-wrap gap-3 text-xs">
                  <span class="rounded bg-slate-100 px-2 py-1 text-slate-600">
                    Your answer: <strong>{{ answer.user_answer || '(blank)' }}</strong>
                  </span>
                  <span v-if="!answer.is_correct"
                        class="rounded bg-emerald-50 px-2 py-1 text-emerald-700">
                    Correct: <strong>{{ answer.question.correct_answer }}</strong>
                  </span>
                </div>
              </div>
              <div class="shrink-0 text-right">
                <span class="text-xs font-bold"
                      :class="answer.is_correct ? 'text-emerald-600' : 'text-rose-500'">
                  {{ answer.points_earned }} / {{ answer.question.points }} pts
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Actions -->
      <div class="flex flex-wrap gap-3 justify-end">
        <Link :href="route('quizzes.performance', attempt.quiz.id)"
              class="inline-flex items-center gap-2 rounded-xl border border-orange-200 px-5 py-2.5 text-sm font-semibold text-orange-600 hover:bg-orange-50 transition">
          <Trophy class="h-4 w-4" />
          View class performance report
        </Link>
        <Link :href="route('dashboard')"
              class="inline-flex items-center rounded-xl bg-orange-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-orange-700 transition">
          Back to Dashboard
        </Link>
      </div>

    </div>
  </AuthenticatedLayout>
</template>
