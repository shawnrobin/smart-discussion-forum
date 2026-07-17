<template>
  <AuthenticatedLayout page-title="Groups" :user-name="auth?.user?.name || 'User'">
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white">All Discussion Groups</h2>
        <PrimaryButton @click="showCreateModal = true">
          + Create Group
        </PrimaryButton>
      </div>

      <div class="flex gap-4">
        <TextInput
          v-model="searchQuery"
          placeholder="Search groups..."
          class="flex-1"
        />
        <select v-model="sortBy" class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white">
          <option value="recent">Recent</option>
          <option value="members">Most Members</option>
          <option value="active">Most Active</option>
        </select>
      </div>

     <div
          v-for="group in filteredGroups"
          :key="group.id"
          @click="goToGroup(group.id)"
          class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 hover:shadow-lg cursor-pointer transition hover:translate-y-[-2px] border border-transparent dark:border-gray-700"
        >
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ group.name }}</h3>
          <p class="text-sm text-gray-600 dark:text-gray-300 mt-2">{{ group.description }}</p>
          <div class="mt-4 flex justify-between text-sm text-gray-500 dark:text-gray-400">
            <span>👥 {{ group.members }} members</span>
            <span>💬 {{ group.discussions }} discussions</span>
          </div>

          <div class="mt-4 flex gap-2" @click.stop>
            <button
              @click="joinGroup(group.id)"
              :disabled="group.joined || loadingId === group.id"
              class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Join
            </button>
            <button
              @click="leaveGroup(group.id)"
              :disabled="!group.joined || loadingId === group.id"
              class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Leave
            </button>
          </div>
        </div>
      </div>

      <!-- Create Group Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Create Group</h3>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Group Name</label>
            <TextInput v-model="newGroup.name" class="w-full" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Description</label>
            <textarea v-model="newGroup.description" rows="3" class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-white rounded-lg"></textarea>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button @click="showCreateModal = false" class="px-4 py-2 text-sm text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg">
            Cancel
          </button>
          <PrimaryButton @click="createGroup">
            Create Group
          </PrimaryButton>
        </div>
      </div>
    </div>
    
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import { Heart, ShoppingCart, Search } from 'lucide-vue-next'

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import TextInput from '@/Components/TextInput.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const page = usePage();
const auth = computed(() => page.props.auth);

// Real groups data passed from GroupController@index (includes users_count,
// topics_count, and a "joined" boolean for the current user)
const groups = computed(() => page.props.groups || []);

const searchQuery = ref('');
const sortBy = ref('recent');
const showCreateModal = ref(false);
const creating = ref(false);
const loadingId = ref(null); // disables buttons on the group currently being joined/left

const newGroup = ref({ name: '', description: '' });

const createGroup = () => {
  if (!newGroup.value.name.trim()) return;
  creating.value = true;

  router.post('/groups', {
    name: newGroup.value.name,
    description: newGroup.value.description,
  }, {
    preserveScroll: true,
    onSuccess: () => {
      newGroup.value = { name: '', description: '' };
      showCreateModal.value = false;
    },
    onFinish: () => {
      creating.value = false;
    },
  });
};

const filteredGroups = computed(() => {
  return groups.value.filter(group =>
    group.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    (group.description || '').toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const goToGroup = (groupId) => {
  router.visit(`/groups/${groupId}`);
};

const joinGroup = (groupId) => {
  loadingId.value = groupId;

  const group = groups.value.find(g => g.id === groupId);
  if (group) group.joined = true;

  router.post(`/groups/${groupId}/join`, {}, {
    preserveScroll: true,
    onError: () => {
      if (group) group.joined = false;
    },
    onFinish: () => {
      loadingId.value = null;
    },
  });
};

const leaveGroup = (groupId) => {
  loadingId.value = groupId;

  const group = groups.value.find(g => g.id === groupId);
  if (group) group.joined = false;

  router.post(`/groups/${groupId}/leave`, {}, {
    preserveScroll: true,
    onError: () => {
      if (group) group.joined = true;
    },
    onFinish: () => {
      loadingId.value = null;
    },
  });
};
</script>