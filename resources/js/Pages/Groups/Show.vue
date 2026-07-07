<template>
  <AuthenticatedLayout :page-title="group.name" :user-name="auth.user.name">
    <div class="space-y-6">
      <!-- Group Info & Create Button -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <div class="flex justify-between items-start">
          <div>
<<<<<<< HEAD
            <h2 class="text-2xl font-bold text-gray-900">{{ group.name }}</h2>
            <p class="text-gray-600 mt-2">{{ group.description }}</p>
            <p class="text-sm text-gray-500 mt-4">
              👥 {{ group.members }} members • 💬 {{ group.topics }} topics
            </p>
=======
            <h2 class="text-2xl font-bold text-gray-900">{{ group?.name || 'Data Structures & Algorithms' }}</h2>
            <p class="text-gray-600 mt-2">{{ group?.description || 'Discussion forum for mastering core engineering concepts.' }}</p>
            <div class="flex items-center gap-4 text-sm text-gray-500 mt-4">
              <span class="flex items-center gap-1">
                <UsersIcon class="w-4 h-4 text-orange-500" /> 18 members
              </span>
              <span class="flex items-center gap-1">
                <MessageSquareIcon class="w-4 h-4 text-orange-500" /> {{ topics.length }} topics
              </span>
            </div>
>>>>>>> 58d3bd8 (afew changes on the looks of our pages)
          </div>
          <PrimaryButton @click="showCreateTopic = true">
            + New Topic
          </PrimaryButton>
        </div>
      </div>

      <!-- Search & Filter -->
      <div class="flex gap-4">
        <TextInput
          v-model="searchQuery"
          placeholder="Search topics..."
          class="flex-1"
        />
        <div class="flex items-center">
          <label for="filterBy" class="sr-only">Filter topics</label>
          <select id="filterBy" v-model="filterBy" class="px-4 py-2 border border-gray-300 rounded-lg">
            <option value="recent">Recent</option>
            <option value="popular">Most Replied</option>
            <option value="unanswered">Unanswered</option>
          </select>
        </div>
      </div>

      <!-- Topics List -->
      <div class="space-y-4">
        <div
          v-for="topic in filteredTopics"
          :key="topic.id"
          @click="goToTopic(topic.id)"
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg cursor-pointer transition"
        >
          <div class="flex gap-4">
            <!-- Author Avatar -->
            <div class="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center text-white font-bold">
              {{ topic.author.initials }}
            </div>

            <!-- Content -->
            <div class="flex-1">
              <div class="flex items-start justify-between">
                <div>
<<<<<<< HEAD
                  <h3 class="text-lg font-semibold text-gray-900">{{ topic.title }}</h3>
                  <p class="text-sm text-gray-600 mt-1">{{ topic.excerpt }}</p>
=======
                  <h3 
                    @click="goToTopic(topic.id)" 
                    class="text-lg font-semibold text-gray-900 hover:text-orange-600 transition cursor-pointer"
                  >
                    {{ topic.title }}
                  </h3>
                  <p class="text-gray-600 mt-1 text-sm whitespace-pre-line">{{ topic.body }}</p>
                </div>
                
                <div class="flex gap-2 shrink-0">
                  <button @click="openEditModal(topic)" class="flex items-center gap-1 text-xs text-amber-700 hover:bg-amber-100 px-2 py-1 bg-amber-50 rounded transition font-medium">
                    <Edit2Icon class="w-3 h-3" /> Edit
                  </button>
                  <button @click="deleteTopic(topic.id)" class="flex items-center gap-1 text-xs text-rose-700 hover:bg-rose-100 px-2 py-1 bg-rose-50 rounded transition font-medium">
                    <Trash2Icon class="w-3 h-3" /> Delete
                  </button>
>>>>>>> 58d3bd8 (afew changes on the looks of our pages)
                </div>
                <span
                  v-if="topic.status === 'answered'"
                  class="px-3 py-1 bg-green-100 text-green-800 text-xs font-medium rounded-full"
                >
                  ✓ Answered
                </span>
                <span
                  v-else
                  class="px-3 py-1 bg-yellow-100 text-yellow-800 text-xs font-medium rounded-full"
                >
                  ? Unanswered
                </span>
              </div>

              <!-- Meta -->
              <div class="mt-4 flex justify-between text-sm text-gray-500">
                <span>by {{ topic.author.name }} • {{ topic.createdAt }}</span>
                <span>💬 {{ topic.replies }} replies • 👁️ {{ topic.views }} views</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="flex justify-center gap-2">
        <button class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50">
          Previous
        </button>
        <button class="px-4 py-2 bg-blue-600 text-white rounded-lg">1</button>
        <button class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50">2</button>
        <button class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50">
          Next
        </button>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import TextInput from '@/Components/TextInput.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

<<<<<<< HEAD
const auth = usePage().props.auth;
const group = ref({
  id: 1,
  name: 'Data Structures',
  description: 'Learn about arrays, linked lists, trees, graphs, and more!',
  members: 18,
  topics: 45
=======
import { 
  Search as SearchIcon, 
  Plus as PlusIcon, 
  MessageSquare as MessageSquareIcon, 
  Users as UsersIcon, 
  Edit2 as Edit2Icon, 
  Trash2 as Trash2Icon,
  SlidersHorizontal as SlidersHorizontalIcon
} from 'lucide-vue-next';

// FIXED: Define the group prop passed down from Laravel Inertia
const props = defineProps({
  group: {
    type: Object,
    default: () => ({ id: 1, name: 'Data Structures & Algorithms' })
  }
});

const auth = ref({
  user: {
    name: 'Developer Mode',
    email: 'dev@forum.com'
  }
>>>>>>> 58d3bd8 (afew changes on the looks of our pages)
});

const searchQuery = ref('');
const filterBy = ref('recent');
const showCreateTopic = ref(false);

const topics = ref([
  {
    id: 1,
    title: 'Understanding Recursion',
    excerpt: 'I\'m struggling to understand how recursion works. Can someone explain with examples?',
    author: { name: 'John Doe', initials: 'JD' },
    createdAt: '2 days ago',
    replies: 8,
    views: 52,
    status: 'answered'
  },
  {
    id: 2,
    title: 'Binary Search Trees Implementation',
    excerpt: 'How do I implement a BST efficiently in Java?',
    author: { name: 'Jane Smith', initials: 'JS' },
    createdAt: '1 day ago',
    replies: 12,
    views: 78,
    status: 'answered'
  },
  {
    id: 3,
    title: 'Sorting Algorithm Comparison',
    excerpt: 'What\'s the difference between Quick Sort and Merge Sort?',
    author: { name: 'Bob Wilson', initials: 'BW' },
    createdAt: '6 hours ago',
    replies: 0,
    views: 15,
    status: 'unanswered'
  }
]);

<<<<<<< HEAD
=======
const searchQuery = ref('');
const filterBy = ref('recent');

const showModal = ref(false);
const isEditing = ref(false);
const currentTopicId = ref(null);
const form = ref({
  title: '',
  body: ''
});

>>>>>>> 58d3bd8 (afew changes on the looks of our pages)
const filteredTopics = computed(() => {
  return topics.value
    .filter(t => t.title.toLowerCase().includes(searchQuery.value.toLowerCase()))
    .sort((a, b) => {
      if (filterBy.value === 'popular') return b.replies - a.replies;
      if (filterBy.value === 'unanswered') return a.replies - b.replies;
      return 0;
    });
});

<<<<<<< HEAD
const goToTopic = (topicId) => {
  router.visit(`/groups/${group.value.id}/topics/${topicId}`);
=======
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

const saveTopic = () => {
  if (isEditing.value) {
    const topic = topics.value.find(t => t.id === currentTopicId.value);
    if (topic) {
      topic.title = form.value.title;
      topic.body = form.value.body;
    }
  } else {
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

const deleteTopic = (id) => {
  if (confirm('Are you absolutely sure you want to delete this discussion topic?')) {
    topics.value = topics.value.filter(t => t.id !== id);
  }
>>>>>>> 58d3bd8 (afew changes on the looks of our pages)
};

// FIXED: Now correctly reads `props.group.id` rather than a missing global variable
const goToTopic = (topicId) => {
  router.visit(`/groups/${props.group.id}/topics/${topicId}`);
};
</script>