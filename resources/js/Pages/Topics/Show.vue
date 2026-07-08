<template>
  <AuthenticatedLayout
    page-title="Discussion"
    :user-name="auth.user?.name ?? 'Guest'"

  >
    <div class="space-y-6 max-w-4xl mx-auto p-4 sm:p-6">
      
      <Card :title="topic.title" class="overflow-hidden border border-gray-100 shadow-sm rounded-xl">
        <div class="flex flex-col sm:flex-row gap-4 p-2">
          
          <div class="flex sm:flex-col items-center gap-3 shrink-0">
            <div class="w-12 h-12 rounded-full bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center text-white font-bold shadow-sm">
              {{ topic.author?.initials || '??' }}
            </div>
            <div class="sm:hidden">
              <div class="flex items-center gap-2">
                <span class="font-semibold text-gray-900">{{ topic.author?.name }}</span>
                <span v-if="topic.author?.role === 'lecturer'" class="px-2 py-0.5 bg-amber-100 text-amber-800 text-xs font-medium rounded-full">
                  Lecturer
                </span>
              </div>
              <p class="text-xs text-gray-400 mt-0.5">{{ topic.createdAt }}</p>
            </div>
          </div>
          <div class="flex-1">

            <div class="hidden sm:flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="font-semibold text-gray-900">{{ topic.author?.name }}</span>
                <span v-if="topic.author?.role === 'lecturer'" class="px-2 py-0.5 bg-amber-100 text-amber-800 text-xs font-medium rounded-full">
                  Lecturer
                </span>
              </div>
              <p class="text-xs text-gray-400">{{ topic.createdAt }}</p>
            </div>

            <p class="mt-3 text-gray-700 leading-relaxed text-sm sm:text-base">{{ topic.content }}</p>
            
            <hr class="my-4 border-gray-100" />

            <div class="flex flex-wrap items-center justify-between gap-4 pt-1">
              <div class="flex items-center gap-4 text-xs sm:text-sm">
                <button class="flex items-center gap-1.5 text-gray-500 hover:text-orange-600 font-medium transition-colors">
                  <ThumbsUpIcon class="w-4 h-4" /> Like ({{ topic.likes }})
                </button>
                <button class="flex items-center gap-1.5 text-gray-500 hover:text-orange-600 font-medium transition-colors">
                  <CornerUpLeftIcon class="w-4 h-4" /> Reply
                </button>
                <button class="flex items-center gap-1.5 text-gray-400 hover:text-rose-600 font-medium transition-colors ml-auto sm:ml-0">
                  <AlertTriangleIcon class="w-4 h-4" /> Report
                </button>
              </div>

              <div class="flex items-center gap-2">
                <button @click="exportPDF" class="inline-flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-orange-600 px-2.5 py-1.5 rounded-md hover:bg-orange-50 transition">
                  <PrinterIcon class="w-3.5 h-3.5" /> Export PDF
                </button>
                <button @click="shareToSocial" class="inline-flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-orange-600 px-2.5 py-1.5 rounded-md hover:bg-orange-50 transition">
                  <Share2Icon class="w-3.5 h-3.5" /> Share
                </button>
              </div>
            </div>


          </div>
        </div>
      </Card>

      <div>
        <div class="flex items-center gap-2 mb-4">
          <h3 class="text-lg font-bold text-gray-900">{{ topic.replies?.length || 0 }} Replies</h3>
          <div class="h-1 flex-1 bg-gray-100 rounded-full"></div>
        </div>

        <div class="space-y-4">
          <div
            v-for="reply in topic.replies"
            :key="reply.id"
            :style="{ marginLeft: $viewport?.width > 640 ? `${(reply.depth || 0) * 2.5}rem` : `${(reply.depth || 0) * 1}rem` }"
            :class="[
              reply.isAnswer ? 'border-orange-500 bg-orange-50/40' : 'border-gray-200 bg-white',
              'rounded-xl shadow-sm p-4 sm:p-5 border-l-4 transition-all duration-200'
            ]"
          >
            <div class="flex gap-3 sm:gap-4">
              <div class="w-9 h-9 rounded-full bg-gradient-to-br from-slate-600 to-slate-700 flex items-center justify-center text-white font-bold shrink-0 text-xs shadow-sm">
                {{ reply.author?.initials || '??' }}
              </div>
              <div class="flex-1">
                <div class="flex items-center justify-between gap-2">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="font-semibold text-gray-900 text-sm">{{ reply.author?.name }}</span>
                    <span v-if="reply.isAnswer" class="inline-flex items-center gap-1 px-2 py-0.5 bg-orange-100 text-orange-800 text-[11px] font-semibold rounded-full">
                      <CheckCircleIcon class="w-3 h-3" /> Answer
                    </span>
                  </div>
                  <span class="text-[11px] text-gray-400 shrink-0">{{ reply.createdAt }}</span>
                </div>
                
                <p class="mt-2 text-gray-700 text-sm leading-relaxed">{{ reply.content }}</p>
                
                <div class="mt-3 flex gap-4 text-xs border-t border-dashed border-gray-100/80 pt-2.5">
                  <button class="flex items-center gap-1 text-gray-500 hover:text-orange-600 transition-colors font-medium">
                    <ThumbsUpIcon class="w-3.5 h-3.5" /> Like ({{ reply.likes }})
                  </button>
                  <button class="flex items-center gap-1 text-gray-500 hover:text-orange-600 transition-colors font-medium">
                    <CornerUpLeftIcon class="w-3.5 h-3.5" /> Reply
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Card title="Your Reply" class="border border-gray-100 shadow-sm rounded-xl">
        <form @submit.prevent="submitReply" class="space-y-4">
          <div>

            <label for="reply-content" class="block text-sm font-medium text-gray-700 mb-2">
              Your Reply
            </label>
            <textarea
              id="reply-content"
              v-model="newReply.content"
              placeholder="Share your thoughts..."
              rows="6"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            ></textarea>
          </div>
          <div class="flex items-center gap-2">
            <input
              id="reply-is-answer"
              v-model="newReply.isAnswer"
              type="checkbox"
              class="w-4 h-4 text-blue-600"
            />
            <label for="reply-is-answer" class="text-sm text-gray-700">Mark as answer</label>

          </div>
        </form>
      </Card>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import Card from '@/Components/Card.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

import { 
  Printer as PrinterIcon, 
  Share2 as Share2Icon, 
  ThumbsUp as ThumbsUpIcon, 
  CornerUpLeft as CornerUpLeftIcon, 
  AlertTriangle as AlertTriangleIcon,
  CheckCircle as CheckCircleIcon
} from 'lucide-vue-next';

const props = defineProps({
  topic: Object,
  group: Object
});

const auth = usePage().props.auth;

// Uses custom backend dynamic props data if it exists; defaults to static fallback template data if empty
const topic = ref(props.topic || {
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

const exportPDF = () => {

  console.log('Exporting topic as PDF');


  window.print();
};

const shareToSocial = () => {
  alert('Social sharing feature coming soon');
};



const submitReply = () => {
  if (!newReply.value.content.trim()) return;

  if (props.topic && props.group) {
    router.post(`/groups/${props.group.id}/topics/${props.topic.id}/replies`, {
      content: newReply.value.content,
      is_answer: newReply.value.isAnswer
    }, {
      onSuccess: () => {
        newReply.value.content = '';
        newReply.value.isAnswer = false;
      }
    });
  } else {
    console.log('Submitting local fallback mock reply:', newReply.value);
    newReply.value.content = '';
    newReply.value.isAnswer = false;
  }

};
</script>