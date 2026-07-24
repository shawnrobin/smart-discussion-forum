<template>
  <AuthenticatedLayout page-title="Dashboard" :user-name="auth?.user?.name">
    <div class="mx-auto max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8">

      <!-- Welcome -->
      <div class="rounded-2xl bg-gradient-to-r from-orange-500 to-amber-500 p-5 text-white shadow-md sm:p-8">
        <h1 class="text-2xl font-bold">Welcome back, {{ auth?.user?.name || 'User' }}!</h1>
        <p class="text-orange-100 mt-1 text-sm">Here's what's happening across your groups.</p>
      </div>

      <!-- Search Bar -->
      <div class="grid grid-cols-1 lg:grid-cols-[1fr_auto] gap-3 items-center">
        <div class="relative">
          <input
            v-model="searchQuery"
            @keyup.enter="search"
            type="text"
            placeholder="Search topics, posts, users..."
            class="w-full px-4 py-3 pl-10 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none bg-white shadow-sm"
          />
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
        </div>
        <select
          v-model="searchFilter"
          class="w-full lg:w-auto px-4 py-3 border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none bg-white shadow-sm"
        >
          <option value="latest">Latest</option>
          <option value="group">Group</option>
          <option value="replies">Replies</option>
          <option value="quiz">Quiz</option>
        </select>
        <button
          type="button"
          @click="search"
          class="inline-flex items-center justify-center px-5 py-3 rounded-xl bg-orange-500 text-white text-sm font-semibold hover:bg-orange-600 transition"
        >
          Search
        </button>
      </div>

      <div v-if="searchQuery.trim() !== ''" class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Search Results</h2>
        <div v-if="searchResults.length > 0" class="space-y-3">
          <Link
            v-for="result in searchResults"
            :key="result.id"
            v-if="result.url"
            :href="result.url"
            class="block p-4 rounded-xl border border-gray-100 hover:border-orange-200 hover:bg-orange-50 transition"
          >
            <p class="text-xs font-bold uppercase tracking-[0.2em] text-orange-600">{{ result.type }}</p>
            <p class="text-base font-semibold text-gray-900 mt-2">{{ result.title }}</p>
            <p class="text-sm text-gray-500 mt-1">{{ result.subtitle }}</p>
            <p class="text-sm text-gray-600 mt-2">{{ result.detail }}</p>
          </Link>
          <div
            v-for="result in searchResults"
            :key="result.id + '-plain'"
            v-if="!result.url"
            class="p-4 rounded-xl border border-gray-100 bg-gray-50"
          >
            <p class="text-xs font-bold uppercase tracking-[0.2em] text-orange-600">{{ result.type }}</p>
            <p class="text-base font-semibold text-gray-900 mt-2">{{ result.title }}</p>
            <p class="text-sm text-gray-500 mt-1">{{ result.subtitle }}</p>
            <p class="text-sm text-gray-600 mt-2">{{ result.detail }}</p>
          </div>
        </div>
        <p v-else class="text-sm text-gray-500">No results found for "{{ searchQuery }}".</p>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-center hover:shadow-md transition">
          <p class="text-2xl font-bold text-gray-900">{{ stats.groups || 0 }}</p>
          <p class="text-xs font-medium text-gray-500 mt-1">My Groups</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-center hover:shadow-md transition">
          <p class="text-2xl font-bold text-gray-900">{{ stats.my_topics || 0 }}</p>
          <p class="text-xs font-medium text-gray-500 mt-1">My Topics</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-center hover:shadow-md transition">
          <p class="text-2xl font-bold text-gray-900">{{ stats.my_replies || 0 }}</p>
          <p class="text-xs font-medium text-gray-500 mt-1">Replies</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-center hover:shadow-md transition">
          <p class="text-2xl font-bold text-orange-600">{{ stats.topics || 0 }}</p>
          <p class="text-xs font-medium text-gray-500 mt-1">Total Discussions</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-center hover:shadow-md transition">
          <p class="text-2xl font-bold text-blue-600">{{ stats.quizzes || 0 }}</p>
          <p class="text-xs font-medium text-gray-500 mt-1">Quizzes</p>
        </div>
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-4 text-center hover:shadow-md transition">
          <p class="text-2xl font-bold text-green-600">{{ stats.attempts || 0 }}</p>
          <p class="text-xs font-medium text-gray-500 mt-1">Quiz Attempts</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

        <!-- My Groups -->
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
            <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
              <svg class="w-5 h-5 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
              My Groups
            </h2>
            <Link :href="route('groups.index')" class="text-xs font-bold text-orange-600 hover:text-orange-700 hover:underline">View All</Link>
          </div>
          <div v-if="groups.length > 0" class="divide-y divide-gray-50">
            <Link v-for="g in groups" :key="g.id" :href="route('groups.show', g.id)"
                  class="px-6 py-4 flex justify-between items-center hover:bg-gray-50/50 transition">
              <div>
                <p class="font-semibold text-gray-900">{{ g.name }}</p>
                <p class="text-xs text-gray-400 mt-0.5">{{ g.members }} members &middot; {{ g.topics || 0 }} topics</p>
              </div>
              <svg class="w-4 h-4 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
            </Link>
          </div>
          <div v-else class="p-8 text-center text-gray-500 text-sm">
            <p>You haven't joined any groups yet.</p>
            <Link :href="route('groups.index')" class="text-orange-600 font-semibold hover:underline mt-1 inline-block">Browse groups</Link>
          </div>
        </div>

        <!-- Recent Quizzes -->
        <div v-if="isStudent" class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
            <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
              <svg class="w-5 h-5 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
              Recent Quizzes
            </h2>
            <Link :href="route('quizzes.index')" class="text-xs font-bold text-orange-600 hover:text-orange-700 hover:underline">View All</Link>
          </div>
          <div v-if="recentQuizzes.length > 0" class="divide-y divide-gray-50">
            <Link v-for="q in recentQuizzes" :key="q.id" :href="route('quizzes.start', q.id)"
                  class="px-6 py-4 flex justify-between items-center hover:bg-gray-50/50 transition">
              <div>
                <p class="font-semibold text-gray-900">{{ q.title }}</p>
                <p class="text-xs text-gray-400 mt-0.5">{{ q.group_name }} &middot; {{ q.duration }}min</p>
              </div>
              <div class="flex items-center gap-2">
                <span v-if="q.status === 'completed'" class="text-xs font-bold text-green-600 bg-green-50 px-2.5 py-1 rounded-full">{{ q.score }}%</span>
                <span v-else-if="q.status === 'active'" class="text-xs font-bold text-green-600 bg-green-50 px-2.5 py-1 rounded-full">Active</span>
                <span v-else-if="q.status === 'upcoming'" class="text-xs font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded-full">Upcoming</span>
                <span v-else class="text-xs font-bold text-gray-500 bg-gray-50 px-2.5 py-1 rounded-full">Closed</span>
                <svg class="w-4 h-4 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
              </div>
            </Link>
          </div>
          <div v-else class="p-8 text-center text-gray-500 text-sm">
            <p>No quizzes available yet.</p>
          </div>
        </div>

      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="px-6 py-5 border-b border-gray-100">
          <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
            <svg class="w-5 h-5 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            Recent Activity
          </h2>
        </div>
        <div v-if="activities.length > 0" class="divide-y divide-gray-50">
          <div v-for="a in activities" :key="a.id" class="px-6 py-4 hover:bg-gray-50/50 transition">
            <div class="flex items-start gap-3">
              <div class="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center shrink-0 mt-0.5">
                <svg class="w-4 h-4 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-gray-900">{{ a.message }}</p>
                <p class="text-xs text-gray-400 mt-0.5">{{ a.detail }}</p>
                <p class="text-xs text-gray-400 mt-0.5">{{ a.time }}</p>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="p-8 text-center text-gray-500 text-sm">
          No recent activity in your groups.
        </div>
      </div>

      <!-- ML Recommendations (students only) -->
      <div v-if="isStudent && recommendations.length > 0" class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
          <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
            <svg class="w-5 h-5 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/></svg>
            Recommended for you
            <span class="text-xs font-normal text-gray-400 ml-1">· Based on your activity</span>
          </h2>
        </div>
        <div class="divide-y divide-gray-50">
          <Link
            v-for="rec in recommendations"
            :key="rec.id"
            :href="route('topics.show', { group: rec.group_id, topic: rec.id })"
            class="px-6 py-4 flex justify-between items-center hover:bg-gray-50/50 transition"
          >
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span v-if="rec.is_answered" class="px-2 py-0.5 bg-green-100 text-green-700 text-[10px] font-semibold rounded-full">Answered</span>
                <p class="font-semibold text-gray-900 truncate text-sm">{{ rec.title }}</p>
              </div>
              <p class="text-xs text-gray-400 mt-0.5">{{ rec.group_name }} · by {{ rec.author }} · {{ rec.replies }} {{ rec.replies === 1 ? 'reply' : 'replies' }}</p>
            </div>
            <div class="flex items-center gap-3 ml-4 shrink-0">
              <span class="text-xs font-bold text-orange-600 bg-orange-50 px-2 py-1 rounded-full">{{ rec.score }}% match</span>
              <svg class="w-4 h-4 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
            </div>
          </Link>
        </div>
      </div>

    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onBeforeUnmount, onMounted, watch } from 'vue';
import { usePage, router, Link } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const props = defineProps({
  groups: { type: Array, default: () => [] },
  activities: { type: Array, default: () => [] },
  recentQuizzes: { type: Array, default: () => [] },
  stats: { type: Object, default: () => ({}) },
  activeQuiz: { type: Object, default: null },
  recommendations: { type: Array, default: () => [] },
  search: { type: String, default: '' },
  filter: { type: String, default: 'latest' },
  searchResults: { type: Array, default: () => [] },
});

const page = usePage();
const auth = computed(() => page.props.auth || {});
const isStudent = computed(() => auth.value?.user?.role === 'student');

const searchQuery = ref(props.search || '');
const searchFilter = ref(props.filter || 'latest');
const openingQuiz = ref(false);
let activeQuizPoller;

const search = () => {
  if (searchQuery.value.trim()) {
    router.get(route('dashboard'), {
      search: searchQuery.value,
      filter: searchFilter.value,
    }, {
      preserveState: true,
      preserveScroll: true,
      replace: true
    });
  }
};

const openActiveQuiz = (quiz) => {
  if (!quiz || openingQuiz.value) return;
  openingQuiz.value = true;
  router.visit(route('quizzes.start', quiz.id), { onFinish: () => { openingQuiz.value = false; } });
};

watch(() => props.activeQuiz, (quiz) => openActiveQuiz(quiz));
onMounted(() => {
  openActiveQuiz(props.activeQuiz);
  activeQuizPoller = setInterval(() => {
    if (!openingQuiz.value) router.reload({ only: ['activeQuiz'], preserveScroll: true, preserveState: true });
  }, 15000);
});
onBeforeUnmount(() => clearInterval(activeQuizPoller));
</script>
