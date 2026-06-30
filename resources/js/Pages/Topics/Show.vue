<template>
  <AuthenticatedLayout title="Discussion">
    <div class="max-w-4xl mx-auto space-y-6">
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

      <!-- Replies Section (Requirement #2: Nested/Threaded) -->
      <div>
        <h3 class="text-2xl font-bold text-gray-900 mb-4">{{ (topic?.messages || []).length }} Replies</h3>

        <!-- Threaded Replies -->
        <div class="space-y-4">
          <div
            v-for="message in topic?.messages || []"
            :key="message.id"
            :style="{ marginLeft: `${message.depth * 2}rem` }"
            class="bg-white rounded-lg shadow-md p-6 border-l-4"
            :class="message.is_answer ? 'border-green-500' : 'border-blue-500'"
          >
            <div class="flex gap-4">
              <div class="w-10 h-10 rounded-full bg-green-500 text-white flex items-center justify-center font-bold text-sm flex-shrink-0">
                {{ message.author?.name?.[0] }}
              </div>

              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-sm">{{ message.author?.name }}</span>
                  <span v-if="message.is_answer" class="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">
                    ✓ Answer
                  </span>
                </div>

                <p class="text-sm text-gray-500 mt-1">{{ formatDate(message.created_at) }}</p>
                <p class="text-gray-800 mt-3 text-sm">{{ message.content }}</p>

                <!-- Reply Actions -->
                <div class="mt-3 flex gap-4 text-xs">
                  <button @click="replyTo(message.id)" class="text-blue-600 hover:underline">
                    ↩️ Reply
                  </button>
                  <button
                    v-if="canMarkAnswer"
                    @click="markAsAnswer(message.id)"
                    class="text-green-600 hover:underline"
                  >
                    ✓ Mark as Answer
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Reply Form -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-4">Post Your Reply</h3>

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

          <button
            type="submit"
            :disabled="!newReply.content || loading"
            class="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 font-semibold"
          >
            {{ loading ? 'Posting...' : 'Post Reply' }}
          </button>
        </form>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const route = useRoute();
const authStore = useAuthStore();

const topic = ref(null);
const groupMembers = ref([]);
const loading = ref(false);

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