<script setup>
import { Link } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

defineProps({ quiz: Object, summary: Object });
</script>

<template>
  <AuthenticatedLayout :page-title="`${quiz.title} Performance`">
    <div class="mx-auto max-w-3xl space-y-6 p-6">
      <Link :href="route('quizzes.index')" class="text-sm font-medium text-orange-600 hover:text-orange-700">← Back to quizzes</Link>
      <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="bg-gradient-to-r from-orange-600 to-amber-500 p-7 text-white"><p class="text-sm text-orange-100">Quiz performance report</p><h1 class="mt-1 text-2xl font-bold">{{ quiz.title }}</h1></div>
        <div class="grid grid-cols-2 gap-px bg-slate-200 sm:grid-cols-4">
          <div class="bg-white p-5 text-center"><p class="text-2xl font-bold text-slate-900">{{ summary.attempts }}</p><p class="text-xs text-slate-500">Attempts</p></div>
          <div class="bg-white p-5 text-center"><p class="text-2xl font-bold text-slate-900">{{ summary.average }}%</p><p class="text-xs text-slate-500">Class average</p></div>
          <div class="bg-white p-5 text-center"><p class="text-2xl font-bold text-slate-900">{{ summary.highest }}%</p><p class="text-xs text-slate-500">Highest score</p></div>
          <div class="bg-white p-5 text-center"><p class="text-2xl font-bold text-orange-600">{{ summary.myScore === null ? '—' : `${summary.myScore}%` }}</p><p class="text-xs text-slate-500">Your score</p></div>
        </div>
      </section>
      <p class="text-center text-sm text-slate-500">This report is available because the quiz has closed.</p>
    </div>
  </AuthenticatedLayout>
</template>
