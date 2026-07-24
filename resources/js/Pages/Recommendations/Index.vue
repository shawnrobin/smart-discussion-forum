<template>
  <AuthenticatedLayout page-title="Recommended Topics">
    <div class="max-w-4xl mx-auto p-6 space-y-6">

      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Recommended for you</h1>
          <p class="text-sm text-gray-500 mt-1">Topics ranked by similarity to your past activity.</p>
        </div>
        <button @click="refresh" :disabled="refreshing"
          class="inline-flex items-center gap-2 rounded-xl border border-orange-200 px-4 py-2 text-sm font-semibold text-orange-600 hover:bg-orange-50 transition disabled:opacity-50">
          <svg class="w-4 h-4" :class="{ 'animate-spin': refreshing }" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
          </svg>
          {{ refreshing ? 'Refreshing...' : 'Refresh' }}
        </button>
      </div>

      <div v-if="!localRecs.length" class="rounded-2xl border border-dashed border-gray-300 p-12 text-center">
        <p class="text-gray-500 text-sm">No recommendations yet. Participate in discussions to get personalised topic suggestions.</p>
      </div>

      <div v-else class="space-y-3">
        <Link v-for="rec in localRecs" :key="rec.id"
          :href="route('topics.show', { group: rec.topic.group_id, topic: rec.topic.id })"
          class="flex items-start gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm hover:border-orange-200 hover:shadow-md transition">
          <div class="shrink-0 flex flex-col items-center justify-center w-14 h-14 rounded-full border-2 text-center"
            :class="scoreClass(rec.score)">
            <span class="text-xs font-black leading-none">{{ Math.round(rec.score) }}</span>
            <span class="text-[9px] font-medium leading-none mt-0.5">%</span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <span v-if="rec.topic.is_answered" class="px-2 py-0.5 bg-green-100 text-green-700 text-[10px] font-semibold rounded-full">Answered</span>
              <span class="px-2 py-0.5 bg-orange-100 text-orange-700 text-[10px] font-semibold rounded-full">{{ rec.topic.group?.name }}</span>
            </div>
            <p class="font-semibold text-gray-900 mt-1 truncate">{{ rec.topic.title }}</p>
            <p class="text-xs text-gray-400 mt-0.5 line-clamp-2">{{ rec.topic.description }}</p>
            <p class="text-xs text-gray-400 mt-2">by {{ rec.topic.user?.name ?? 'Unknown' }} · {{ rec.topic.messages_count ?? 0 }} replies</p>
          </div>
          <svg class="shrink-0 w-4 h-4 text-gray-300 self-center" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </Link>
      </div>

      <p class="text-center text-xs text-gray-400">Recommendations update as you engage with more topics.</p>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref } from 'vue';
import { router, Link } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const props = defineProps({ recommendations: { type: Array, default: () => [] } });
const localRecs  = ref(props.recommendations);
const refreshing = ref(false);

const refresh = () => {
  refreshing.value = true;
  router.post(route('recommendations.generate'), {}, {
    preserveScroll: true,
    onSuccess: () => router.reload({ only: ['recommendations'], onSuccess: (page) => { localRecs.value = page.props.recommendations ?? []; } }),
    onFinish: () => { refreshing.value = false; },
  });
};

const scoreClass = (score) => {
  if (score >= 70) return 'border-emerald-400 text-emerald-700 bg-emerald-50';
  if (score >= 40) return 'border-orange-400 text-orange-700 bg-orange-50';
  return 'border-gray-300 text-gray-500 bg-gray-50';
};
</script>
