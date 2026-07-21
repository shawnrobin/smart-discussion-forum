<template>
  <AuthenticatedLayout page-title="Discussion" :user-name="auth?.user?.name ?? ''">
    <div class="space-y-6 max-w-4xl mx-auto">
      <!-- Back Link -->
      <Link :href="route('groups.show', group.id)" class="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-orange-600 transition font-medium">
        <ArrowLeftIcon class="w-4 h-4" /> Back to {{ group.name }}
      </Link>

      <!-- Topic Card -->
      <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="p-6">
          <div class="flex flex-col sm:flex-row gap-4">
            <!-- user Avatar -->
            <div class="flex sm:flex-col items-center gap-3 shrink-0">
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center text-white font-bold shadow-sm">
                {{ topic.user?.initials ?? '??' }}
              </div>
              <div class="sm:hidden">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-gray-900 text-sm">{{ topic.user?.name }}</span>
                  <span v-if="topic.user?.role === 'lecturer'" class="px-2 py-0.5 bg-amber-100 text-amber-800 text-xs font-medium rounded-full">Lecturer</span>
                </div>
                <p class="text-xs text-gray-400 mt-0.5">{{ topic.created_at }}</p>
              </div>
            </div>

            <div class="flex-1 min-w-0">
              <!-- Desktop user row -->
              <div class="hidden sm:flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-gray-900">{{ topic.user?.name }}</span>
                  <span v-if="topic.user?.role === 'lecturer'" class="px-2 py-0.5 bg-amber-100 text-amber-800 text-xs font-medium rounded-full">Lecturer</span>
                </div>
                <p class="text-xs text-gray-400">{{ topic.createdAt }}</p>
              </div>

              <h1 class="text-xl font-bold text-gray-900 mt-2">{{ topic.title }}</h1>
              <p class="mt-3 text-gray-700 leading-relaxed text-sm sm:text-base whitespace-pre-line">{{ topic.description }}</p>

              <hr class="my-4 border-gray-100" />

              <!-- Action Bar -->
              <div class="flex flex-wrap items-center justify-between gap-4">
                <div class="flex items-center gap-4 text-sm">
                  <button @click="toggleTopicLike" :class="topicLiked ? 'text-orange-600' : 'text-gray-500 hover:text-orange-600'" class="flex items-center gap-1.5 font-medium transition-colors">
                    <ThumbsUpIcon class="w-4 h-4" :fill="topicLiked ? 'currentColor' : 'none'" /> {{ topicLikes }}
                  </button>
                  <button @click="repliesOpen = !repliesOpen" class="flex items-center gap-1.5 text-gray-500 hover:text-orange-600 font-medium transition-colors">
                    <MessageSquareIcon class="w-4 h-4" /> {{ replyCount }} {{ replyCount === 1 ? 'reply' : 'replies' }}
                  </button>
                  <button
                    @click="showReportModal = true"
                    class="flex items-center gap-1.5 text-gray-400 hover:text-rose-600 font-medium transition-colors"
                  >
                    <AlertTriangleIcon class="w-4 h-4" /> Report
                  </button>
                </div>

                <div class="flex items-center gap-2">
                  <button
                    @click="exportPDF"
                    :disabled="exporting"
                    class="inline-flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-orange-600 px-2.5 py-1.5 rounded-md hover:bg-orange-50 transition"
                  >
                    <FileTextIcon class="w-3.5 h-3.5" /> {{ exporting ? 'Preparing...' : 'Export PDF' }}
                  </button>
                  <button
                    @click="shareTopic"
                    class="inline-flex items-center gap-1.5 text-xs font-medium text-gray-500 hover:text-orange-600 px-2.5 py-1.5 rounded-md hover:bg-orange-50 transition"
                  >
                    <Share2Icon class="w-3.5 h-3.5" /> Share
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Replies Section -->
      <div>
        <div class="flex items-center gap-2 mb-4">
          <h3 class="text-lg font-bold text-gray-900">{{ replyCount }} {{ replyCount === 1 ? 'Reply' : 'Replies' }}</h3>
          <div class="h-px flex-1 bg-gray-200"></div>
          <button @click="repliesOpen = !repliesOpen" class="text-xs font-semibold text-orange-600 hover:text-orange-700">{{ repliesOpen ? 'Hide replies' : `View ${replyCount} replies` }}</button>
        </div>

        <div v-if="repliesOpen" class="space-y-2">
          <div
              v-for="reply in visibleReplies"
            :key="reply.id"
            :style="{ marginLeft: (reply.depth || 0) * 1.5 + 'rem' }"
            :class="[
              reply.isAnswer ? 'border-l-4 border-orange-500 bg-orange-50/30' : 'border border-gray-200 bg-white',
              'rounded-xl shadow-sm p-4 sm:p-5 transition-all duration-200'
            ]"
          >
            <div class="flex gap-3 sm:gap-4">
              <div class="w-9 h-9 rounded-full bg-gradient-to-br from-slate-600 to-slate-700 flex items-center justify-center text-white font-bold text-xs shrink-0 shadow-sm">
                {{ reply.user?.initials ?? '??' }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="font-semibold text-gray-900 text-sm">{{ reply.user?.name }}</span>
                    <span v-if="reply.isAnswer" class="inline-flex items-center gap-1 px-2.5 py-0.5 bg-orange-100 text-orange-800 text-[11px] font-semibold rounded-full">
                      <CheckCircleIcon class="w-3 h-3" /> Answer
                    </span>
                  </div>
                  <span class="text-[11px] text-gray-400 shrink-0">{{ reply.created_at }}</span>
                </div>

                <p class="mt-2 text-gray-700 text-sm leading-relaxed whitespace-pre-line">{{ reply.content }}</p>

                <div class="mt-3 flex items-center gap-4 text-xs border-t border-dashed border-gray-100 pt-2.5">
                  <button
                    @click="toggleReplyLike(reply)"
                    :class="reply.liked_by_current_user ? 'text-orange-600' : 'text-gray-500 hover:text-orange-600'"
                    class="flex items-center gap-1 transition-colors font-medium"
                  >
                    <ThumbsUpIcon class="w-3.5 h-3.5" :fill="reply.liked_by_current_user ? 'currentColor' : 'none'" /> {{ reply.likes ?? 0 }}
                  </button>
                  <button
                    @click="setReplyParent(reply)"
                    class="flex items-center gap-1 text-gray-500 hover:text-orange-600 transition-colors font-medium"
                  >
                    <CornerUpLeftIcon class="w-3.5 h-3.5" /> Reply
                  </button>
                  <button
                    v-if="canMarkAnswer(reply)"
                    @click="markAsAnswer(reply)"
                    class="flex items-center gap-1 text-gray-500 hover:text-green-600 transition-colors font-medium"
                  >
                    <CheckCircleIcon class="w-3.5 h-3.5" /> {{ reply.isAnswer ? 'Unmark' : 'Mark as Answer' }}
                  </button>
                </div>
                <button v-if="replyChildrenCount(reply.id)" @click="toggleReplyChildren(reply.id)" class="mt-3 text-xs font-semibold text-orange-600 hover:text-orange-700">
                  {{ expandedReplyIds.has(reply.id) ? 'Hide' : `View ${replyChildrenCount(reply.id)}` }} {{ replyChildrenCount(reply.id) === 1 ? 'reply' : 'replies' }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="replies.length === 0" class="text-center py-12 text-gray-400">
            <MessageSquareIcon class="w-10 h-10 mx-auto mb-2 text-gray-300" />
            <p class="text-sm">No replies yet. Be the first to respond!</p>
          </div>
        </div>
      </div>

      <!-- Reply Form -->
      <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <h3 class="text-base font-bold text-gray-900 mb-1">
          {{ replyTo ? 'Reply to ' + replyTo.user?.name : 'Write a Reply' }}
        </h3>
        <p v-if="replyTo" class="text-xs text-gray-400 mb-3 italic">"{{ replyTo.content.substring(0, 80) }}..."</p>

        <form @submit.prevent="submitReply" class="space-y-4">
          <textarea
            ref="replyTextarea"
            v-model="replyForm.content"
            :placeholder="replyTo ? 'Write your reply...' : 'Share your thoughts...'"
            rows="4"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none resize-none"
          ></textarea>
          <p v-if="replyError" class="text-sm text-rose-600">{{ replyError }}</p>

          <div class="flex flex-col sm:flex-row sm:items-center gap-4">
            <div class="flex items-center gap-4">
              <label class="flex items-center gap-2 cursor-pointer">
                <input
                  v-model="replyForm.isAnswer"
                  type="checkbox"
                  class="w-4 h-4 text-orange-600 rounded border-gray-300 focus:ring-orange-500"
                />
                <span class="text-sm text-gray-700">Mark as answer</span>
              </label>

              <button
                type="button"
                @click="showExcludeModal = true"
                class="text-xs text-gray-400 hover:text-gray-600 transition underline"
              >
                Exclude users
              </button>
            </div>

            <div class="flex items-center gap-2 sm:ml-auto">
              <button
                v-if="replyTo"
                type="button"
                @click="cancelReply"
                class="px-3 py-2 text-xs text-gray-600 hover:bg-gray-100 rounded-lg transition"
              >
                Cancel
              </button>
              <PrimaryButton type="submit" :disabled="!replyForm.content.trim() || submitting">
                {{ submitting ? 'Posting...' : 'Post Reply' }}
              </PrimaryButton>
            </div>
          </div>
        </form>
      </div>

      <!-- Exclude Users Modal -->
      <div v-if="showExcludeModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showExcludeModal = false">
        <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-gray-900">Exclude Users</h3>
            <button @click="showExcludeModal = false" class="text-gray-400 hover:text-gray-600 transition">
              <XIcon class="w-5 h-5" />
            </button>
          </div>
          <p class="text-xs text-gray-500 mb-4">Selected users won't see this reply.</p>
          <div class="space-y-2 max-h-60 overflow-y-auto">
            <label
              v-for="member in groupMembers"
              :key="member.id"
              class="flex items-center gap-3 px-3 py-2 hover:bg-gray-50 rounded-lg cursor-pointer"
            >
              <input
                type="checkbox"
                :value="member.id"
                v-model="excludedUserIds"
                class="w-4 h-4 text-orange-600 rounded border-gray-300 focus:ring-orange-500"
              />
              <span class="text-sm text-gray-700">{{ member.name }}</span>
            </label>
          </div>
          <div class="flex justify-end mt-4">
            <button
              @click="showExcludeModal = false"
              class="px-4 py-2 text-sm bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition"
            >
              Done
            </button>
          </div>
        </div>
      </div>

      <!-- Share Modal -->
      <div v-if="showReportModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showReportModal = false">
        <form @submit.prevent="submitReport" class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-gray-900">Report topic</h3>
            <button type="button" @click="showReportModal = false" class="text-gray-400 hover:text-gray-600 transition">
              <XIcon class="w-5 h-5" />
            </button>
          </div>
          <p class="text-sm text-gray-500 mb-3">Tell the moderators why this topic is irrelevant or violates the group rules.</p>
          <textarea v-model="reportReason" rows="4" required minlength="5" maxlength="1000" placeholder="Describe the issue..." class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none resize-none"></textarea>
          <div class="flex justify-end gap-3 mt-4">
            <button type="button" @click="showReportModal = false" class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg">Cancel</button>
            <PrimaryButton type="submit" :disabled="reportSubmitting || reportReason.trim().length < 5">{{ reportSubmitting ? 'Submitting...' : 'Submit report' }}</PrimaryButton>
          </div>
        </form>
      </div>

      <div v-if="showShareModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showShareModal = false">
        <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-gray-900">Share Topic</h3>
            <button @click="showShareModal = false" class="text-gray-400 hover:text-gray-600 transition">
              <XIcon class="w-5 h-5" />
            </button>
          </div>
          <p class="text-xs text-gray-500 mb-4">Share this topic on social media:</p>
          <div class="flex flex-col gap-3">
            <a
              v-for="platform in sharePlatforms"
              :key="platform.key"
              :href="platform.url"
              target="_blank"
              rel="noopener noreferrer"
              class="flex items-center gap-3 px-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 transition text-sm font-medium"
            >
              <span class="w-6 h-6 rounded flex items-center justify-center text-white text-xs font-bold" :class="platform.bg">{{ platform.icon }}</span>
              {{ platform.label }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, nextTick, onBeforeUnmount, onMounted, watch } from 'vue';
import axios from 'axios';
import { usePage, router, Link } from '@inertiajs/vue3';
import {
  ArrowLeft as ArrowLeftIcon,
  ThumbsUp as ThumbsUpIcon,
  MessageSquare as MessageSquareIcon,
  CornerUpLeft as CornerUpLeftIcon,
  AlertTriangle as AlertTriangleIcon,
  CheckCircle as CheckCircleIcon,
  FileText as FileTextIcon,
  Share2 as Share2Icon,
  X as XIcon,
} from 'lucide-vue-next';

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const auth = usePage().props.auth;

const props = defineProps({
  topic: { type: Object, default: () => ({}) },
  group: { type: Object, default: () => ({ id: null, name: '' }) },
  groupMembers: { type: Array, default: () => [] },
});

// Use server data only — no mock fallbacks
const replies = ref(props.topic.messages ?? []);
const replyCount = computed(() => replies.value.length);
const topicLikes = ref(props.topic.likes ?? 0);
const topicLiked = ref(Boolean(props.topic.liked_by_current_user));
const repliesOpen = ref(true);
const expandedReplyIds = ref(new Set());
const exporting = ref(false);
const replyTextarea = ref(null);

const repliesByParent = computed(() => {
  const byParent = new Map();
  for (const reply of replies.value) {
    const parentId = reply.parent_message_id ?? null;
    if (!byParent.has(parentId)) byParent.set(parentId, []);
    byParent.get(parentId).push(reply);
  }
  for (const children of byParent.values()) children.sort((a, b) => a.id - b.id);
  return byParent;
});

const visibleReplies = computed(() => {
  const result = [];
  const appendChildren = (parentId, depth = 0) => {
    for (const reply of repliesByParent.value.get(parentId) ?? []) {
      result.push({ ...reply, depth });
      if (expandedReplyIds.value.has(reply.id)) appendChildren(reply.id, depth + 1);
    }
  };
  appendChildren(null);
  return result;
});

const replyChildrenCount = (replyId) => (repliesByParent.value.get(replyId) ?? []).length;
const toggleReplyChildren = (replyId) => {
  const expanded = new Set(expandedReplyIds.value);
  expanded.has(replyId) ? expanded.delete(replyId) : expanded.add(replyId);
  expandedReplyIds.value = expanded;
};

// Reply form
const replyForm = ref({ content: '', isAnswer: false });
const replyTo = ref(null);
const submitting = ref(false);
const replyError = ref('');
const excludedUserIds = ref([]);

// Modals
const showExcludeModal = ref(false);
const showShareModal = ref(false);
const showReportModal = ref(false);
const reportReason = ref('');
const reportSubmitting = ref(false);

const setReplyParent = (reply) => {
  replyTo.value = reply;
  nextTick(() => replyTextarea.value?.focus());
};

const cancelReply = () => {
  replyTo.value = null;
};

const submitReply = () => {
  if (!replyForm.value.content.trim() || submitting.value) return;
  submitting.value = true;
  replyError.value = '';

  const payload = {
    content: replyForm.value.content,
    is_answer: replyForm.value.isAnswer,
    parent_message_id: replyTo.value?.id ?? null,
    excluded_users: excludedUserIds.value,
  };

  router.post(route('replies.store', { group: props.group.id, topic: props.topic.id }), payload, {
    preserveScroll: true,
    onSuccess: () => {
      replyForm.value = { content: '', isAnswer: false };
      excludedUserIds.value = [];
      replyTo.value = null;
      router.reload({ only: ['topic'] });
    },
    onError: (errors) => {
      replyError.value = errors.content || errors.group || errors.excluded_users || 'Your reply could not be posted. Please try again.';
    },
    onFinish: () => { submitting.value = false; },
  });
};

const canMarkAnswer = (reply) => {
  // Topic user or admin can mark answers
  const userId = auth?.user?.id;
  const isAdmin = auth?.user?.is_admin || ['system_admin', 'group_admin', 'lecturer'].includes(auth?.user?.role);
  return (props.topic.user?.id === userId || isAdmin) && !reply.isAnswer;
};

const markAsAnswer = (reply) => {
  router.post(route('messages.mark-answer', reply.id), {}, {
    preserveScroll: true,
    onSuccess: () => {
      // Update local state optimistically
      replies.value = replies.value.map(r => ({ ...r, isAnswer: r.id === reply.id ? true : r.isAnswer }));
    },
  });
};

const toggleTopicLike = async () => {
  const { data } = await axios.post(route('topics.like', props.topic.id));
  topicLikes.value = data.likes;
  topicLiked.value = data.liked;
};

const toggleReplyLike = async (reply) => {
  const { data } = await axios.post(route('messages.like', reply.id));
  replies.value = replies.value.map(item => item.id === reply.id
    ? { ...item, likes: data.likes, liked_by_current_user: data.liked }
    : item);
};

const exportPDF = () => {
  exporting.value = true;
  const escapeHtml = (value) => String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
  const replyHtml = replies.value.map(reply => `<article><strong>${escapeHtml(reply.user?.name)}</strong><p>${escapeHtml(reply.content)}</p><small>${escapeHtml(reply.created_at)}</small></article>`).join('');
  const printWindow = window.open('', '_blank');
  if (!printWindow) {
    exporting.value = false;
    return;
  }
  printWindow.document.write(`<!doctype html><html><head><title>${escapeHtml(props.topic.title)}</title><style>body{font:15px Arial;color:#1f2937;max-width:760px;margin:40px auto;line-height:1.55}h1{color:#111827}article{border-top:1px solid #e5e7eb;padding:16px 0}p{white-space:pre-wrap}small{color:#6b7280}@media print{body{margin:20px}}</style></head><body><h1>${escapeHtml(props.topic.title)}</h1><p>${escapeHtml(props.topic.description)}</p><h2>Replies (${replyCount.value})</h2>${replyHtml}</body></html>`);
  printWindow.document.close();
  printWindow.focus();
  setTimeout(() => { printWindow.print(); exporting.value = false; }, 250);
};

const sharePlatforms = computed(() => [
  { key: 'twitter', label: 'Twitter / X', icon: 'X', bg: 'bg-black', url: `https://twitter.com/intent/tweet?text=${encodeURIComponent(props.topic.title + ' ' + window.location.href)}` },
  { key: 'facebook', label: 'Facebook', icon: 'f', bg: 'bg-blue-600', url: `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(window.location.href)}` },
  { key: 'linkedin', label: 'LinkedIn', icon: 'in', bg: 'bg-blue-700', url: `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(window.location.href)}` },
]);

const shareTopic = () => {
  showShareModal.value = true;
};

const submitReport = () => {
  if (reportReason.value.trim().length < 5 || reportSubmitting.value) return;
  reportSubmitting.value = true;

  router.post(route('topics.report', props.topic.id), { reason: reportReason.value }, {
    preserveScroll: true,
    onSuccess: () => {
      reportReason.value = '';
      showReportModal.value = false;
    },
    onFinish: () => { reportSubmitting.value = false; },
  });
};

// Client-side hydration: overwrite replies with server data when it arrives
onMounted(() => {
  if (props.topic.messages) {
    replies.value = props.topic.messages;
  }

  if (window.Echo && props.topic.id) {
    window.Echo.private(`topic.${props.topic.id}`)
      .listen('.reply.created', ({ reply }) => {
        if (!replies.value.some((existing) => existing.id === reply.id)) replies.value.push(reply);
        if (reply.parent_message_id) {
          expandedReplyIds.value = new Set([...expandedReplyIds.value, reply.parent_message_id]);
        }
      })
      .listen('.like.updated', (update) => {
        if (update.content_type === 'topic' && update.content_id === props.topic.id) {
          topicLikes.value = update.likes;
          return;
        }
        if (update.content_type === 'message') {
          replies.value = replies.value.map(reply => reply.id === update.content_id ? { ...reply, likes: update.likes } : reply);
        }
      });
  }
});

watch(() => props.topic.messages, (messages) => {
  replies.value = messages ?? [];
});

watch(() => props.topic.likes, (likes) => { topicLikes.value = likes ?? 0; });
watch(() => props.topic.liked_by_current_user, (liked) => { topicLiked.value = Boolean(liked); });

onBeforeUnmount(() => {
  if (window.Echo && props.topic.id) window.Echo.leave(`topic.${props.topic.id}`);
});
</script>
