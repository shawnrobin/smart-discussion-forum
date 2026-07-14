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

      <!-- Create Group Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Create Group</h3>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Group Name</label>
            <TextInput v-model="newGroup.name" class="w-full" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea v-model="newGroup.description" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg"></textarea>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button @click="showCreateModal = false" class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg">
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

const newGroup = ref({ name: '', description: '' });

const createGroup = () => {
  const newId = groups.value.length ? Math.max(...groups.value.map(g => g.id)) + 1 : 1;
  groups.value.unshift({
    id: newId,
    name: newGroup.value.name,
    description: newGroup.value.description,
    members: 1,
    discussions: 0,
    joined: true,
  });
  newGroup.value = { name: '', description: '' };
  showCreateModal.value = false;
};

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