<template>
  <AuthenticatedLayout title="Dashboard">
    <div class="space-y-6">
      <!-- Welcome -->
      <div class="bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg p-8">
        <h2 class="text-3xl font-bold">Welcome back, {{ authStore.user?.name }}! 👋</h2>
        <p class="mt-2 opacity-90">
          You have {{ unreadNotifications }} unread notifications
        </p>
      </div>

      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard label="My Groups" :value="myGroups.length" icon="📁" />
        <StatCard label="Active Topics" :value="24" icon="💬" />
        <StatCard label="Participation Points" :value="myScore" icon="⭐" />
      </div>

      <!-- Search Bar -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="🔍 Search topics, groups, users..."
          class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          @keyup.enter="search"
        />
      </div>

      <!-- My Groups -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-2xl font-bold text-gray-900">My Groups</h3>
          <router-link to="/groups" class="text-blue-600 hover:underline">
            View All →
          </router-link>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="group in myGroups.slice(0, 4)"
            :key="group.id"
            @click="goToGroup(group.id)"
            class="p-4 border border-gray-200 rounded-lg hover:shadow-lg cursor-pointer transition"
          >
            <h4 class="font-bold text-gray-900">{{ group.name }}</h4>
            <p class="text-sm text-gray-600 mt-2">{{ group.description }}</p>
            <div class="mt-3 flex justify-between text-xs text-gray-500">
              <span>👥 {{ group.members_count }} members</span>
              <span>💬 {{ group.topics_count }} topics</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h3 class="text-2xl font-bold text-gray-900 mb-4">Recent Activity</h3>

        <div class="space-y-3">
          <div
            v-for="activity in recentActivities"
            :key="activity.id"
            class="p-4 bg-gray-50 rounded-lg border-l-4 border-blue-500"
          >
            <p class="text-sm font-medium text-gray-900">{{ activity.message }}</p>
            <p class="text-xs text-gray-500 mt-1">{{ activity.time }}</p>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import StatCard from '@/Components/StatCard.vue';

const router = useRouter();
const authStore = useAuthStore();

const myGroups = ref([]);
const searchQuery = ref('');
const myScore = ref(0);
const unreadNotifications = ref(3);

const recentActivities = ref([
  { id: 1, message: 'Jane replied to "Understanding Recursion"', time: '2 hours ago' },
  { id: 2, message: 'Quiz "Database Design" published', time: '4 hours ago' },
  { id: 3, message: 'Bob marked your answer as correct', time: '1 day ago' }
]);

onMounted(async () => {
  // Load user's groups
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/groups`, {
      headers: { 'Authorization': `Bearer ${authStore.token}` }
    });
    myGroups.value = response.data.groups;

    // Load participation score
    const scoreResponse = await axios.get(`${import.meta.env.VITE_API_URL}/participation-score`, {
      headers: { 'Authorization': `Bearer ${authStore.token}` }
    });
    myScore.value = scoreResponse.data.total_score || 0;
  } catch (err) {
    console.error('Failed to load data:', err);
  }
});

const search = () => {
  if (searchQuery.value.trim()) {
    router.push({
      name: 'search',
      query: { q: searchQuery.value }
    });
  }
};

const goToGroup = (groupId) => {
  router.push({ name: 'group-show', params: { id: groupId } });
};
</script>