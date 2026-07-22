<template>
  <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
    <div class="border-b border-slate-100 bg-gradient-to-r from-orange-50 via-white to-amber-50 px-6 py-6 sm:px-8">
      <div class="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex items-center gap-4">
          <div class="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-orange-500 to-amber-600 text-xl font-black text-white shadow-lg">{{ initials }}</div>
          <div class="min-w-0"><p class="text-xs font-bold uppercase tracking-widest text-orange-600">Profile overview</p><h2 class="mt-1 truncate text-xl font-bold text-slate-900">{{ name }}</h2><p class="truncate text-sm text-slate-500">{{ email }}</p></div>
        </div>
        <span class="inline-flex w-fit items-center gap-1.5 rounded-full border border-orange-200 bg-white px-3 py-1.5 text-xs font-bold text-orange-800 shadow-sm"><AwardIcon class="h-3.5 w-3.5" /> {{ gamification.badge }}</span>
      </div>
    </div>
    <div class="grid gap-6 p-6 sm:grid-cols-[1.5fr_1fr] sm:p-8">
      <div>
        <div class="flex items-end justify-between"><div><p class="text-xs font-bold uppercase tracking-wider text-slate-400">Participation score</p><p class="mt-1 text-3xl font-black text-slate-900">{{ gamification.totalPoints }} <span class="text-sm font-semibold text-orange-600">XP</span></p></div><span class="rounded-lg bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-600">Level {{ gamification.level }}</span></div>
        <div class="mt-4 h-2.5 overflow-hidden rounded-full bg-slate-100"><div class="h-full rounded-full bg-gradient-to-r from-orange-500 to-amber-500 transition-all duration-500" :style="{ width: `${gamification.progress}%` }" /></div>
        <p class="mt-2 text-xs text-slate-500">{{ gamification.nextLevelPoints }} XP to Level {{ gamification.level + 1 }}</p>
      </div>
      <div class="grid grid-cols-2 gap-3">
        <div class="rounded-xl border border-orange-100 bg-orange-50 p-4 text-center"><p class="text-2xl font-bold text-orange-700">{{ gamification.verifiedAnswers }}</p><p class="mt-1 text-[11px] font-semibold text-orange-800">Answers accepted</p></div>
        <div class="rounded-xl border border-amber-100 bg-amber-50 p-4 text-center"><p class="text-2xl font-bold text-amber-700">{{ gamification.streak }}d</p><p class="mt-1 text-[11px] font-semibold text-amber-800">Current streak</p></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';
import { Award as AwardIcon } from 'lucide-vue-next';

const props = defineProps({ name: { type: String, default: '' }, email: { type: String, default: '' }, gamification: { type: Object, required: true } });
const initials = computed(() => props.name.split(' ').filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase() || 'U');
</script>
