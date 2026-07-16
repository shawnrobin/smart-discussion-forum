<template>
  <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
    <!-- User Identity -->
    <div class="flex items-center gap-4 border-b md:border-b-0 md:border-r border-gray-100 pb-4 md:pb-0 md:pr-4">
      <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center text-white font-black text-2xl shadow-md shrink-0">
        {{ name.substring(0, 2).toUpperCase() }}
      </div>
      <div class="min-w-0"></div>
        <h3 class="text-lg font-bold text-gray-900 leading-tight truncate">{{ name }}</h3>
        <p class="text-sm text-gray-400 mt-0.5 truncate">{{ email }}</p>
        <span class="inline-flex items-center gap-1 mt-2 px-2 py-0.5 bg-orange-100 text-orange-800 text-xs font-semibold rounded-full">
          <AwardIcon class="w-3 h-3" /> Gold Contributor
        </span>
      </div>
    </div>

    <!-- Stats -->
    <div class="space-y-2 border-b md:border-b-0 md:border-r border-gray-100 pb-4 md:pb-0 md:pr-4">
      <div class="flex justify-between items-end">
        <div>
          <p class="text-xs text-gray-400 font-medium uppercase tracking-wider">Total Score</p>
          <p class="text-3xl font-black text-gray-900 tracking-tight mt-0.5">
            {{ gamification.totalPoints }} <span class="text-sm font-medium text-orange-600">XP</span>
          </p>
        </div>
        <span class="text-xs text-gray-500 font-medium bg-gray-50 px-2 py-1 rounded border border-gray-100">
          Level {{ gamification.level }}
        </span>
      </div>
      <div class="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
        <div class="bg-gradient-to-r from-orange-500 to-amber-500 h-full transition-all duration-500" style="width: 75%"></div>
      </div>
    </div>

    <!-- Counters -->
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-gray-50/60 border border-gray-100 p-3 rounded-xl text-center">
        <div class="text-orange-600 font-bold text-lg leading-none">{{ gamification.verifiedAnswers }}</div>
        <div class="text-[11px] text-gray-500 font-medium mt-1">Solutions Provided</div>
      </div>
      <div class="bg-gray-50/60 border border-gray-100 p-3 rounded-xl text-center">
        <div class="text-amber-600 font-bold text-lg leading-none">+{{ gamification.weeklyStreak }}d</div>
        <div class="text-[11px] text-gray-500 font-medium mt-1">Daily Streak</div>
      </div>
    </div>
  
</template>

<script setup>
import { Award as AwardIcon } from 'lucide-vue-next';
defineProps(['name', 'email', 'gamification']);
</script>