<template>
  <AuthenticatedLayout page-title="Discussion" :user-name="auth.user.name">
    <div class="space-y-6 max-w-4xl mx-auto">
      <!-- Original Topic -->
      <Card :title="topic.title">
        <div class="flex gap-4">
          <div class="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center text-white font-bold flex-shrink-0">
            {{ topic.author.initials }}
          </div>
          <div class="flex-1">
            <div class="flex items-center gap-2">
              <span class="font-semibold">{{ topic.author.name }}</span>
              <span v-if="topic.author.role === 'lecturer'" class="px-2 py-1 bg-purple-100 text-purple-800 text-xs rounded">
                Lecturer
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">{{ topic.createdAt }}</p>
            <p class="mt-4 text-gray-800">{{ topic.content }}</p>
            <div class="mt-4 flex gap-4 text-sm">
              <button class="text-gray-600 hover:text-blue-600">👍 Like ({{ topic.likes }})</button>
              <button class="text-gray-600 hover:text-blue-600">💬 Reply</button>
              <button class="text-gray-600 hover:text-red-600">🚩 Report</button>
            </div>
          </div>
        </div>
      </Card>

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
          <div>
            <textarea
              v-model="newReply.content"
              placeholder="Share your thoughts..."
              rows="6"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            ></textarea>
          </div>
          <div class="flex items-center gap-2">
            <input
              v-model="newReply.isAnswer"
              type="checkbox"
              class="w-4 h-4 text-blue-600"
            />
            <label class="text-sm text-gray-700">Mark as answer</label>
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
  isAnswer: false
});

const submitReply = () => {
  console.log('Submitting reply:', newReply.value);
  // API call would go here
  newReply.value.content = '';
  newReply.value.isAnswer = false;
};
</script>


