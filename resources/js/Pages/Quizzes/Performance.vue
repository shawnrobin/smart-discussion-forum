<script setup>
import { computed } from 'vue';
import { Link } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { Trophy, Clock, Users, TrendingUp, CheckCircle, AlertCircle } from 'lucide-vue-next';

const props = defineProps({
    quiz:        { type: Object, required: true },
    summary:     { type: Object, required: true },
    leaderboard: { type: Array,  default: null  },  // only for lecturers/admins
    canManage:   { type: Boolean, default: false },
});

const formatTime = (secs) => {
    if (!secs) return '—';
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}m ${s}s`;
};

const formatDate = (d) => d ? new Date(d).toLocaleString('en-KE', { timeZone: 'Africa/Nairobi' }) : '—';

// Score band colour helper
const bandColor = (score) => {
    if (score >= 85) return 'text-emerald-600 bg-emerald-50';
    if (score >= 70) return 'text-blue-600 bg-blue-50';
    if (score >= 50) return 'text-amber-600 bg-amber-50';
    return 'text-rose-600 bg-rose-50';
};

// Distribution bar widths
const maxBucket = computed(() => Math.max(...Object.values(props.summary.distribution ?? {}), 1));
</script>

<template>
  <AuthenticatedLayout :page-title="`${quiz.title} — Performance`">
    <div class="mx-auto max-w-4xl space-y-6 p-4 sm:p-6">

      <!-- Back -->
      <Link :href="route('quizzes.index')"
            class="inline-flex items-center gap-1.5 text-sm font-medium text-orange-600 hover:text-orange-700">
        ← Back to quizzes
      </Link>

      <!-- Hero card -->
      <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="bg-gradient-to-r from-orange-600 to-amber-500 px-5 py-6 text-white sm:px-8 sm:py-7">
          <p class="text-xs font-bold uppercase tracking-widest text-orange-100">Quiz performance report</p>
          <h1 class="mt-1 text-2xl font-bold">{{ quiz.title }}</h1>
          <p v-if="quiz.description" class="mt-1 text-sm text-orange-100">{{ quiz.description }}</p>
          <p class="mt-2 text-xs text-orange-200">
            {{ formatDate(quiz.start_time) }} → {{ formatDate(quiz.end_time) }}
          </p>
        </div>

        <!-- Key stats row -->
        <div class="grid grid-cols-2 gap-px bg-slate-100 sm:grid-cols-4">
          <div class="bg-white p-5 text-center">
            <Users class="mx-auto mb-1 h-5 w-5 text-orange-500" />
            <p class="text-2xl font-bold text-slate-900">{{ summary.attempts }}</p>
            <p class="text-xs text-slate-500">Attempts</p>
          </div>
          <div class="bg-white p-5 text-center">
            <TrendingUp class="mx-auto mb-1 h-5 w-5 text-orange-500" />
            <p class="text-2xl font-bold text-slate-900">{{ summary.average }}%</p>
            <p class="text-xs text-slate-500">Class average</p>
          </div>
          <div class="bg-white p-5 text-center">
            <Trophy class="mx-auto mb-1 h-5 w-5 text-amber-500" />
            <p class="text-2xl font-bold text-slate-900">{{ summary.highest }}%</p>
            <p class="text-xs text-slate-500">Highest score</p>
          </div>
          <div class="bg-white p-5 text-center">
            <p class="text-2xl font-bold"
               :class="summary.myScore === null ? 'text-slate-400' : (summary.myScore >= 50 ? 'text-emerald-600' : 'text-rose-600')">
              {{ summary.myScore === null ? '—' : `${summary.myScore}%` }}
            </p>
            <p class="text-xs text-slate-500">Your score</p>
          </div>
        </div>
      </section>

      <!-- My result card (students only) -->
      <section v-if="summary.myScore !== null" class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="mb-4 font-bold text-slate-900">Your result</h2>
        <div class="flex flex-wrap items-center gap-6">
          <div class="flex items-center gap-3">
            <span class="rounded-xl px-4 py-2 text-2xl font-bold"
                  :class="bandColor(summary.myScore)">
              {{ summary.myScore }}%
            </span>
            <div>
              <p class="text-sm font-semibold text-slate-700">
                {{ summary.myScore >= 85 ? 'Excellent' : summary.myScore >= 70 ? 'Good' : summary.myScore >= 50 ? 'Pass' : 'Below pass' }}
              </p>
              <p class="text-xs text-slate-400">vs class average {{ summary.average }}%</p>
            </div>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-500">
            <Clock class="h-4 w-4" />
            Time taken: {{ formatTime(summary.myTime) }}
          </div>
          <div v-if="summary.myAutoSubmit" class="flex items-center gap-2 rounded-lg bg-amber-50 px-3 py-2 text-xs font-medium text-amber-700">
            <AlertCircle class="h-3.5 w-3.5" />
            Auto-submitted (timer expired)
          </div>
          <div v-else class="flex items-center gap-2 text-xs text-emerald-600">
            <CheckCircle class="h-3.5 w-3.5" />
            Manually submitted
          </div>
        </div>
      </section>

      <!-- Score distribution -->
      <section v-if="summary.distribution" class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="mb-4 font-bold text-slate-900">Score distribution</h2>
        <div class="space-y-3">
          <div v-for="(count, band) in summary.distribution" :key="band" class="flex items-center gap-3">
            <span class="w-16 shrink-0 text-xs font-medium text-slate-500">{{ band }}</span>
            <div class="relative h-6 flex-1 overflow-hidden rounded-full bg-slate-100">
              <div class="h-full rounded-full bg-orange-500 transition-all duration-500"
                   :style="{ width: `${(count / maxBucket) * 100}%` }"></div>
            </div>
            <span class="w-6 shrink-0 text-right text-xs font-bold text-slate-600">{{ count }}</span>
          </div>
        </div>
      </section>

      <!-- Leaderboard (lecturers / admins only) -->
      <section v-if="canManage && leaderboard && leaderboard.length" class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 px-6 py-4">
          <h2 class="font-bold text-slate-900">Full results ({{ leaderboard.length }} students)</h2>
          <p class="mt-0.5 text-xs text-slate-500">Sorted by score, highest first</p>
        </div>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-100 text-sm">
            <thead class="bg-slate-50 text-xs font-bold uppercase text-slate-400">
              <tr>
                <th class="px-6 py-3 text-left">#</th>
                <th class="px-6 py-3 text-left">Student</th>
                <th class="px-6 py-3 text-right">Score</th>
                <th class="px-6 py-3 text-right">Time</th>
                <th class="px-6 py-3 text-right">Submitted</th>
                <th class="px-6 py-3 text-center">Auto?</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-50">
              <tr v-for="(row, i) in leaderboard" :key="i" class="hover:bg-slate-50 transition">
                <td class="px-6 py-3 font-bold text-slate-400">{{ i + 1 }}</td>
                <td class="px-6 py-3 font-medium text-slate-900">{{ row.name }}</td>
                <td class="px-6 py-3 text-right">
                  <span class="rounded-full px-2.5 py-0.5 text-xs font-bold"
                        :class="bandColor(row.score)">
                    {{ row.score }}%
                  </span>
                </td>
                <td class="px-6 py-3 text-right text-slate-500">{{ formatTime(row.time_taken) }}</td>
                <td class="px-6 py-3 text-right text-slate-500">{{ formatDate(row.submitted_at) }}</td>
                <td class="px-6 py-3 text-center">
                  <span v-if="row.auto" class="text-xs text-amber-600">⚠ Yes</span>
                  <span v-else class="text-xs text-emerald-600">✓ No</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <p class="text-center text-xs text-slate-400">
        This report is available to all group members because the quiz has closed.
      </p>
    </div>
  </AuthenticatedLayout>
</template>
