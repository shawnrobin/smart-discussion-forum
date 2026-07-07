<template>
  <AuthenticatedLayout :auth="auth" page-title="Group Details">
    <div class="space-y-6 p-6">
      
      <!-- Group Info Header -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex justify-between items-start">
          <div>
            <h2 class="text-2xl font-bold text-gray-900">Data Structures & Algorithms</h2>
            <p class="text-gray-600 mt-2">Discussion forum for mastering core engineering concepts.</p>
            <p class="text-sm text-gray-500 mt-4">👥 18 members • 💬 {{ topics.length }} topics</p>
          </div>
          <button @click="openCreateModal" class="px-4 py-2 bg-indigo-600 text-white font-medium rounded-md hover:bg-indigo-700 transition">
            + New Topic
          </button>
        </div>
      </div>

      <!-- TASK 12: Search & Filter Bar -->
      <div class="flex flex-col sm:flex-row gap-4 bg-white p-4 rounded-lg shadow-sm">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="Search topics by title or content..." 
          class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
        />
       <div class="flex items-center">
    <label for="filterBy" class="mr-2 text-sm text-gray-600">
        Sort by
    </label>

    <select
        id="filterBy"
        v-model="filterBy"
        class="px-4 py-2 border border-gray-300 rounded-md"
    >
        <option value="recent">Recent</option>
        <option value="popular">Most Popular</option>
        <option value="most_replied">Most Replied</option>
        <option value="unanswered">Unanswered</option>
    </select>
</div>

      <!-- Topics List Layout -->
      <div class="space-y-4">
        <div v-if="filteredTopics.length === 0" class="text-center py-12 bg-white rounded-lg shadow-sm text-gray-500">
          No topics found matching your criteria.
        </div>

        <div 
          v-for="topic in filteredTopics" 
          :key="topic.id" 
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition relative border border-gray-100"
        >
          <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center text-white font-bold shrink-0">
              {{ topic.authorInitials }}
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-start">
                <div>
                  <h3 class="text-lg font-semibold text-gray-900">{{ topic.title }}</h3>
                  <p class="text-gray-600 mt-1 text-sm whitespace-pre-line">{{ topic.body }}</p>
                </div>
                
                <!-- TASK 11: Edit/Delete Actions -->
                <div class="flex gap-2">
                  <button @click="openEditModal(topic)" class="text-xs text-amber-600 hover:underline px-2 py-1 bg-amber-50 rounded">Edit</button>
                  <button @click="deleteTopic(topic.id)" class="text-xs text-rose-600 hover:underline px-2 py-1 bg-rose-50 rounded">Delete</button>
                </div>
              </div>

              <div class="mt-4 flex justify-between items-center text-xs text-gray-500">
                <span>Posted by <strong>{{ topic.author }}</strong> • {{ topic.date }}</span>
                <span class="bg-gray-100 px-2 py-1 rounded-full font-medium text-gray-700">💬 {{ topic.replies }} replies</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TASK 10 & 11: Unified Create / Edit Modal Interface -->
    <div v-if="showModal" class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
        <h3 class="text-xl font-bold text-gray-900 mb-4">
          {{ isEditing ? 'Edit Topic' : 'Create New Topic' }}
        </h3>
        
        <form @submit.prevent="saveTopic" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Topic Title</label>
            <input 
              v-model="form.title" 
              type="text" 
              required
              placeholder="e.g., Understanding QuickSort"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Content Description</label>
            <textarea 
              v-model="form.body" 
              rows="4" 
              required
              placeholder="Describe your question or discussion point in detail..."
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
            ></textarea>
          </div>

          <div class="flex justify-end gap-3 pt-2">
            <button type="button" @click="closeModal" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200">
              Cancel
            </button>
            <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700">
              {{ isEditing ? 'Save Changes' : 'Post Topic' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

// Safe Layout Protection Object
const auth = ref({
  user: {
    name: 'Developer Mode',
    email: 'dev@forum.com'
  }
});

// Mock Database State for Topics
const topics = ref([
  {
    id: 1,
    title: 'Understanding Recursion Stack Overflows',
    body: 'Can someone explain exactly how memory allocation behaves when a recursive function fails to hit its base case in Java?',
    author: 'Alex Namanya',
    authorInitials: 'AN',
    date: '2 hours ago',
    replies: 5
  },
  {
    id: 2,
    title: 'Best approach for balancing Binary Search Trees',
    body: 'Is it worth manually implementing an AVL tree balancing rotation sequence, or should we rely on high-level library primitives for academic projects?',
    author: 'Dianah Petra',
    authorInitials: 'DP',
    date: '1 day ago',
    replies: 14
  }
]);

// Search & Filtering State (Task 12)
const searchQuery = ref('');
const filterBy = ref('recent');

// Modal & Form State (Task 10 & 11)
const showModal = ref(false);
const isEditing = ref(false);
const currentTopicId = ref(null);
const form = ref({
  title: '',
  body: ''
});

// Task 12 Logic: Computed filtering and sorting
const filteredTopics = computed(() => {
  let list = topics.value.filter(topic => {
    const matchesTitle = topic.title.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesBody = topic.body.toLowerCase().includes(searchQuery.value.toLowerCase());
    return matchesTitle || matchesBody;
  });

  if (filterBy.value === 'popular') {
    return list.sort((a, b) => b.replies - a.replies);
  }
  return list.sort((a, b) => b.id - a.id); // Default to recent (higher ID first)
});

// Modal Actions
const openCreateModal = () => {
  isEditing.value = false;
  form.value.title = '';
  form.value.body = '';
  showModal.value = true;
};

const openEditModal = (topic) => {
  isEditing.value = true;
  currentTopicId.value = topic.id;
  form.value.title = topic.title;
  form.value.body = topic.body;
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

// Task 10 & 11 Backend Handler Simulation
const saveTopic = () => {
  if (isEditing.value) {
    // Task 11: Handle Edit
    const topic = topics.value.find(t => t.id === currentTopicId.value);
    if (topic) {
      topic.title = form.value.title;
      topic.body = form.value.body;
    }
  } else {
    // Task 10: Handle Create
    const newId = topics.value.length ? Math.max(...topics.value.map(t => t.id)) + 1 : 1;
    topics.value.unshift({
      id: newId,
      title: form.value.title,
      body: form.value.body,
      author: 'Developer Mode',
      authorInitials: 'DM',
      date: 'Just now',
      replies: 0
    });
  }
  closeModal();
};

// Task 11: Handle Delete
const deleteTopic = (id) => {
  if (confirm('Are you absolutely sure you want to delete this discussion topic?')) {
    topics.value = topics.value.filter(t => t.id !== id);
  }
};
</script>