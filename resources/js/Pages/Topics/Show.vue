<template>
  <AuthenticatedLayout page-title="Discussion" :user-name="auth.user.name">
    <div class="space-y-6 max-w-4xl mx-auto">
      <!-- Original Topic -->
      <div class="bg-white rounded-lg shadow-md p-8">
        <div class="flex gap-4">
          <div class="w-12 h-12 rounded-full bg-blue-500 text-white flex items-center justify-center font-bold flex-shrink-0">
            {{ topic?.author?.name?.[0] }}
          </div>

          <div class="flex-1">
            <h2 class="text-3xl font-bold text-gray-900">{{ topic?.title }}</h2>
            <p class="text-sm text-gray-500 mt-1">
              by {{ topic?.author?.name }} • {{ formatDate(topic?.created_at) }}
            </p>

            <div class="mt-4 text-gray-800 text-lg">{{ topic?.description }}</div>

            <!-- Status Badges -->
            <div class="mt-4 flex gap-2">
              <span v-if="topic?.is_question" class="px-3 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
                ❓ Question
              </span>
              <span v-if="topic?.is_answered" class="px-3 py-1 bg-green-100 text-green-800 text-xs rounded-full">
                ✓ Answered
              </span>
            </div>

            <!-- Actions -->
            <div class="mt-6 flex gap-4 text-sm">
              <button @click="exportPdf" class="text-blue-600 hover:underline flex items-center gap-1">
                📥 Export PDF
              </button>
              <button @click="shareToSocial" class="text-blue-600 hover:underline flex items-center gap-1">
                📤 Share
              </button>
              <button class="text-gray-600 hover:underline flex items-center gap-1">
                👍 Like ({{ topic?.likes || 0 }})
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Replies Section -->
      <div>
        <h3 class="text-xl font-bold text-gray-900 mb-4">{{ topic.replies.length }} Replies</h3>

        <!-- Reply Thread -->
        <div class="space-y-4">
          <div
            v-for="reply in topic.replies"
            :key="reply.id"
            :style="{ marginLeft: `${reply.depth * 2}rem` }"
            class="bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500"
          >
            <div class="flex gap-4">
              <div class="w-10 h-10 rounded-full bg-green-500 flex items-center justify-center text-white font-bold flex-shrink-0 text-sm">
                {{ reply.author.initials }}
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-sm">{{ reply.author.name }}</span>
                  <span v-if="reply.isAnswer" class="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">
                    ✓ Answer
                  </span>
                </div>
                <p class="text-sm text-gray-500">{{ reply.createdAt }}</p>
                <p class="mt-3 text-gray-800 text-sm">{{ reply.content }}</p>
                <div class="mt-3 flex gap-3 text-xs">
                  <button class="text-gray-600 hover:text-blue-600">👍 Like ({{ reply.likes }})</button>
                  <button class="text-gray-600 hover:text-blue-600">↩️ Reply</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Reply Form -->
      <Card title="Your Reply">
        <form @submit.prevent="submitReply" class="space-y-4">
          <textarea
            v-model="newReply.content"
            placeholder="Share your thoughts..."
            rows="6"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          ></textarea>

          <!-- User Exclusion Feature (Requirement #3) -->
          <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200">
            <label class="flex items-center gap-2 cursor-pointer">
              <input v-model="newReply.useExclusion" type="checkbox" class="w-4 h-4" />
              <span class="text-sm font-medium text-gray-700">
                Exclude certain members from seeing this message
              </span>
            </label>

            <div v-if="newReply.useExclusion" class="mt-3">
              <label class="text-xs font-medium text-gray-600 block mb-2">Select members to exclude:</label>
              <div class="space-y-1 max-h-32 overflow-y-auto">
                <label v-for="member in groupMembers" :key="member.id" class="flex items-center gap-2 text-sm">
                  <input
                    :value="member.id"
                    v-model.number="newReply.excludedUsers"
                    type="checkbox"
                    class="w-3 h-3"
                  />
                  <span>{{ member.name }}</span>
                </label>
              </div>
            </div>
          </div>
          <PrimaryButton type="submit">
            Post Reply
          </PrimaryButton>
        </form>
      </Card>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref } from 'vue';
import { usePage } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import Card from '@/Components/Card.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const auth = usePage().props.auth;

const topic = ref({
  title: 'Understanding Recursion',
  author: {
    name: 'John Doe',
    initials: 'JD',
    role: 'student'
  },
  createdAt: '2 days ago',
  content: 'I\'m struggling to understand how recursion works. Can someone explain with simple examples? Every time I try to understand it, my brain just hurts. Please help!',
  likes: 5,
  replies: [
    {
      id: 1,
      author: { name: 'Jane Smith', initials: 'JS', role: 'lecturer' },
      content: 'Great question! Recursion is when a function calls itself. Think of it like a mirror reflecting another mirror...',
      createdAt: '2 days ago',
      likes: 28,
      isAnswer: true,
      depth: 0
    },
    {
      id: 2,
      author: { name: 'Bob Wilson', initials: 'BW' },
      content: 'Thanks for the clear explanation! This really helped me understand it.',
      createdAt: '1 day ago',
      likes: 5,
      isAnswer: false,
      depth: 1
    },
    {
      id: 3,
      author: { name: 'Alice Brown', initials: 'AB' },
      content: 'This example was really helpful for me too!',
      createdAt: '1 day ago',
      likes: 3,
      isAnswer: false,
      depth: 1
    }
  ]
});

const newReply = ref({
  content: '',
  useExclusion: false,
  excludedUsers: []
});

const canMarkAnswer = computed(() => {
  return authStore.user?.id === topic.value?.user_id ||
         authStore.isAdmin ||
         authStore.isLecturer;
});

onMounted(async () => {
  try {
    const response = await axios.get(
      `${import.meta.env.VITE_API_URL}/topics/${route.params.id}`,
      { headers: { 'Authorization': `Bearer ${authStore.token}` } }
    );
    topic.value = response.data.topic;

    // Load group members
    const membersResponse = await axios.get(
      `${import.meta.env.VITE_API_URL}/groups/${topic.value.group_id}/members`,
      { headers: { 'Authorization': `Bearer ${authStore.token}` } }
    );
    groupMembers.value = membersResponse.data.members;
  } catch (err) {
    console.error('Failed to load topic:', err);
  }
});

const submitReply = async () => {
  loading.value = true;

  try {
    await axios.post(
      `${import.meta.env.VITE_API_URL}/topics/${topic.value.id}/messages`,
      {
        content: newReply.value.content,
        excluded_users: newReply.value.useExclusion ? newReply.value.excludedUsers : []
      },
      { headers: { 'Authorization': `Bearer ${authStore.token}` } }
    );

    // Reset form
    newReply.value = { content: '', useExclusion: false, excludedUsers: [] };
    
    // Reload topic
    location.reload();
  } catch (err) {
    alert('Failed to post reply');
  } finally {
    loading.value = false;
  }
};

const markAsAnswer = async (messageId) => {
  try {
    await axios.post(
      `${import.meta.env.VITE_API_URL}/messages/${messageId}/mark-answer`,
      {},
      { headers: { 'Authorization': `Bearer ${authStore.token}` } }
    );
    location.reload();
  } catch (err) {
    alert('Failed to mark answer');
  }
};

const exportPdf = () => {
  alert('PDF export feature coming soon');
};

const shareToSocial = () => {
  alert('Social sharing feature coming soon');
};

const replyTo = (messageId) => {
  alert('Reply to nested message feature coming soon');
};

const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};
</script>


