<template>
  <AuthenticatedLayout page-title="Groups" :user-name="auth?.user?.name || 'User'">
    <div class="space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-xl font-semibold">All Discussion Groups</h2>
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
        <select v-model="sortBy" class="px-4 py-2 border border-gray-300 rounded-lg bg-white">
          <option value="recent">Recent</option>
          <option value="members">Most Members</option>
          <option value="active">Most Active</option>
        </select>
      </div>

     <div
          v-for="group in filteredGroups"
          :key="group.id"
          @click="goToGroup(group.id)"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg cursor-pointer transition hover:translate-y-[-2px]"
        >
          <h3 class="text-lg font-semibold text-gray-900">{{ group.name }}</h3>
          <p class="text-sm text-gray-600 mt-2">{{ group.description }}</p>
          <div class="mt-4 flex justify-between text-sm text-gray-500">
            <span>👥 {{ group.members }} members</span>
            <span>💬 {{ group.discussions }} discussions</span>
          </div>
          
          <div class="mt-4" @click.stop>
            <button
              v-if="!group.joined"
              @click="joinGroup(group.id)"
              class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Join
            </button>
            <button
              v-else
              @click="leaveGroup(group.id)"
              class="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
            >
              Leave
            </button>
         
          </div>
        </div>
      </div>
    
  </AuthenticatedLayout>
</template>

<script setup>

import { ref, computed } from 'vue';
import { usePage, router } from '@inertiajs/vue3';



import { Heart, ShoppingCart, Search } from 'lucide-vue-next'



// If your alias @ fails, replace these paths with relative ones (e.g., '../../Components/TextInput.vue')
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import TextInput from '@/Components/TextInput.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

// Safely extract props using a computed property or direct hook assignment
const page = usePage();
const auth = computed(() => page.props.auth);

const searchQuery = ref('');
const sortBy = ref('recent');
const showCreateModal = ref(false);

const groups = ref([
  { id: 1, name: 'Data Structures', description: 'Learn about arrays, linked lists, trees, and graphs', members: 18, discussions: 45, joined: true },
  { id: 2, name: 'Web Development', description: 'Frontend and backend web development', members: 25, discussions: 62, joined: true },
  { id: 3, name: 'Database Design', description: 'SQL, NoSQL, and database design patterns', members: 12, discussions: 28, joined: false },
  { id: 4, name: 'OOP Concepts', description: 'Object-Oriented Programming in Java and C++', members: 20, discussions: 38, joined: true },
  { id: 5, name: 'UI/UX Discussion', description: 'User interface and user experience design', members: 15, discussions: 21, joined: false },
  { id: 6, name: 'Machine Learning', description: 'Introduction to ML algorithms and frameworks', members: 22, discussions: 55, joined: true }
]);

const filteredGroups = computed(() => {
  return groups.value.filter(group =>
    group.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    group.description.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const goToGroup = (groupId) => {
  router.visit(`/groups/${groupId}`);
};

const joinGroup = (groupId) => {
  console.log('Joining group:', groupId);
  const group = groups.value.find(g => g.id === groupId);
  if (group) group.joined = true;
};

const leaveGroup = (groupId) => {
  console.log('Leaving group:', groupId);
  const group = groups.value.find(g => g.id === groupId);
  if (group) group.joined = false;
};
</script>