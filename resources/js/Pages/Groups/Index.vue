<template>
  <AuthenticatedLayout title="All Discussion Groups">
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-3xl font-bold text-gray-900">Discussion Groups</h1>
        <button v-if="authStore.isAdmin" class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
          + Create Group
        </button>
      </div>

      <!-- Search & Filter -->
      <input
        v-model="searchQuery"
        placeholder="Search groups..."
        class="w-full px-4 py-2 border border-gray-300 rounded-lg"
      />

      <!-- Groups Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="group in filteredGroups"
          :key="group.id"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition"
        >
          <h3 class="text-lg font-bold text-gray-900">{{ group.name }}</h3>
          <p class="text-gray-600 text-sm mt-2">{{ group.description }}</p>

          <div class="mt-4 flex justify-between text-sm text-gray-500">
            <span>👥 {{ group.members_count || 0 }} members</span>
            <span>💬 {{ group.topics_count || 0 }} topics</span>
          </div>

          <div class="mt-4 flex gap-2">
            <button
              v-if="!isMember(group.id)"
              @click="joinGroup(group.id)"
              class="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 text-sm"
            >
              Join
            </button>
            <button
              v-else
              @click="goToGroup(group.id)"
              class="flex-1 bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 text-sm"
            >
              Enter Group
            </button>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const router = useRouter();
const authStore = useAuthStore();

const groups = ref([]);
const searchQuery = ref('');
const myGroupIds = ref([]);

const filteredGroups = computed(() => {
  return groups.value.filter(g =>
    g.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

onMounted(async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_URL}/groups`, {
      headers: { 'Authorization': `Bearer ${authStore.token}` }
    });
    groups.value = response.data.groups;
  } catch (err) {
    console.error('Failed to load groups:', err);
  }
});

const isMember = (groupId) => myGroupIds.value.includes(groupId);

const joinGroup = async (groupId) => {
  try {
    await axios.post(
      `${import.meta.env.VITE_API_URL}/groups/${groupId}/join`,
      {},
      { headers: { 'Authorization': `Bearer ${authStore.token}` } }
    );
    myGroupIds.value.push(groupId);
    alert('Joined group successfully!');
  } catch (err) {
    alert('Failed to join group');
  }
};

const goToGroup = (groupId) => {
  router.push({ name: 'group-show', params: { id: groupId } });
};
</script>