<template>
  <AuthenticatedLayout :page-title="group.name" :user-name="auth?.user?.name ?? ''">
    <div class="space-y-6">
      <!-- Back Link -->
      <Link :href="route('groups.index')" class="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-orange-600 transition font-medium">
        <ArrowLeftIcon class="w-4 h-4" /> Back to Groups
      </Link>

      <!-- Group Header -->
      <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
        <div class="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <div class="w-10 h-10 rounded-lg bg-orange-600 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                {{ (group.name || 'G').charAt(0) }}
              </div>
              <div>
                <h1 class="text-xl font-bold text-gray-900">{{ group.name }}</h1>
                <p class="text-xs text-gray-400">Created {{ group.created_at ?? 'recently' }}</p>
              </div>
            </div>
            <p class="text-sm text-gray-600 mt-3 leading-relaxed">{{ group.description }}</p>
            <div class="flex items-center gap-5 mt-4 text-sm text-gray-500">
              <span class="flex items-center gap-1.5">
                <UsersIcon class="w-4 h-4 text-orange-500" />
                {{ group.members ?? 0 }} members
              </span>
              <span class="flex items-center gap-1.5">
                <MessageSquareIcon class="w-4 h-4 text-orange-500" />
                {{ topics.length }} conversation threads
              </span>
            </div>
          </div>
          <PrimaryButton @click="openCreateModal" class="shrink-0">
            + Start conversation
          </PrimaryButton>
        </div>
      </div>

      <div class="rounded-xl border border-orange-100 bg-orange-50 px-5 py-4 text-sm text-orange-950">
        <p class="font-semibold">Keep this group focused</p>
        <p class="mt-1 leading-6">Start a clear conversation for one subject, then use replies to chat in that thread. Titles need at least 8 characters and the opening message needs at least 30.</p>
        <p v-if="topicKeywords.length" class="mt-2 font-medium">Relevant discussion keywords: {{ topicKeywords.join(', ') }}</p>
      </div>

      <!-- Search & Filter -->
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="relative flex-1">
          <SearchIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Find a conversation or topic..."
            class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
          />
        </div>
        <select
          v-model="filterBy"
          class="px-4 py-2.5 border border-gray-300 rounded-lg text-sm bg-white text-gray-700 focus:ring-2 focus:ring-orange-500 outline-none"
        >
          <option value="recent">Most Recent</option>
          <option value="popular">Most Replied</option>
          <option value="unanswered">Unanswered</option>
        </select>
      </div>

      <!-- Improved Online Indicator -->
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-bold text-gray-900">Group chat</h2>
          <p class="text-sm text-gray-500">Open a thread to read and send live replies.</p>
        </div>
        <div v-if="onlineUsers.length > 0" class="flex items-center gap-2 bg-white px-3 py-1.5 rounded-full border shadow-sm">
            <span class="relative flex h-2 w-2">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
            </span>
            <div class="flex -space-x-2">
                <div v-for="user in onlineUsers" :key="user.id" 
                     class="w-7 h-7 rounded-full bg-orange-100 border-2 border-white flex items-center justify-center text-[10px] text-orange-700 font-bold ring-1 ring-black/5" 
                     :title="user.name">
                    {{ user.name.substring(0, 2).toUpperCase() }}
                </div>
            </div>
        </div>
      </div>

      <!-- Conversation threads -->
      <div v-if="filteredTopics.length === 0" class="text-center py-16 text-gray-400">
        <MessageSquareIcon class="w-12 h-12 mx-auto mb-3 text-gray-300" />
        <p class="text-sm">No conversations yet. Be the first to start one!</p>
      </div>

      <div class="space-y-3">
        <div
          v-for="topic in paginatedTopics"
          :key="topic.id"
          class="group bg-white rounded-xl border border-gray-200 shadow-sm p-5 hover:shadow-orange-100 hover:border-orange-300 transition-all cursor-pointer"
          @click="goToTopic(topic.id)"
        >
          <div class="flex items-start gap-4">
            <!-- Author Avatar -->
            <div class="w-10 h-10 rounded-full bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center text-white font-bold text-sm shrink-0 shadow-sm">
              {{ topic.author?.initials || '??' }}
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <div class="flex items-center gap-2">
                    <span class="rounded-full bg-orange-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-orange-700">Thread</span>
                    <h3 class="font-semibold text-gray-900 truncate">{{ topic.title }}</h3>
                  </div>
                  <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ topic.body }}</p>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                <button
                  v-if="topic.author?.name === auth?.user?.name"
                  @click.stop="openEditModal(topic)"
                  :disabled="saving"
                  class="p-1.5 text-gray-400 hover:text-orange-600 hover:bg-orange-50 rounded-lg transition disabled:opacity-50"
                  title="Edit"
                >
                  <Edit2Icon class="w-4 h-4" />
                </button>
                <button
                  v-if="topic.author?.name === auth?.user?.name"
                  @click.stop="confirmDeleteTopic(topic)"
                  :disabled="deleting"
                  class="p-1.5 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition disabled:opacity-50"
                  title="Delete"
                >
                  <Trash2Icon class="w-4 h-4" />
                </button>
                  <span
                    v-if="topic.status === 'answered'"
                    class="px-2.5 py-1 bg-green-100 text-green-700 text-[11px] font-semibold rounded-full"
                  >
                    Answered
                  </span>
                  <span
                    v-else
                    class="px-2.5 py-1 bg-gray-100 text-gray-500 text-[11px] font-medium rounded-full"
                  >
                    {{ topic.is_question ? 'Open' : 'Discussion' }}
                  </span>
                </div>
              </div>

              <div class="mt-3 flex items-center justify-between text-xs text-gray-400">
                <span>Started by {{ topic.author?.name || 'Unknown' }} &middot; {{ topic.createdAt }}</span>
                <div class="flex items-center gap-3">
                  <span class="flex items-center gap-1">
                    <MessageSquareIcon class="w-3 h-3" /> {{ topic.replies ?? 0 }}
                  </span>
                  <span class="flex items-center gap-1">
                    <EyeIcon class="w-3 h-3" /> {{ topic.views ?? 0 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 pt-2">
        <button
          @click="currentPage = Math.max(1, currentPage - 1)"
          :disabled="currentPage === 1"
          class="px-3 py-2 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition"
        >
          <ChevronLeftIcon class="w-4 h-4" />
        </button>
        <button
          v-for="p in totalPages"
          :key="p"
          @click="currentPage = p"
          :class="[
            'w-9 h-9 text-sm font-medium rounded-lg transition',
            p === currentPage ? 'bg-orange-600 text-white' : 'text-gray-600 hover:bg-gray-100 border border-gray-300'
          ]"
        >
          {{ p }}
        </button>
        <button
          @click="currentPage = Math.min(totalPages, currentPage + 1)"
          :disabled="currentPage === totalPages"
          class="px-3 py-2 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50 disabled:opacity-30 disabled:cursor-not-allowed transition"
        >
          <ChevronRightIcon class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Create/Edit Topic Modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold text-gray-900">{{ isEditing ? 'Edit conversation' : 'Start a conversation' }}</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600 transition">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-4">
          <div class="rounded-lg border border-orange-100 bg-orange-50 px-3 py-2 text-xs leading-5 text-orange-900">
            Keep it relevant and respectful: no abusive language, spam, or unrelated subjects.
            <span v-if="topicKeywords.length" class="font-semibold"> Use one of: {{ topicKeywords.join(', ') }}.</span>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Conversation title</label>
            <input
              v-model="form.title"
              type="text"
              placeholder="Ask a clear, relevant question or name the subject"
              minlength="8"
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Opening message</label>
            <textarea
              v-model="form.body"
              rows="4"
              placeholder="Give enough detail for the group to help (at least 30 characters)..."
              minlength="30"
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none resize-none"
            ></textarea>
          </div>
          <p v-if="topicError" class="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{{ topicError }}</p>
          <div class="flex items-center gap-2">
            <input
              v-model="form.isQuestion"
              type="checkbox"
              id="topic-is-question"
              class="w-4 h-4 text-orange-600 rounded border-gray-300 focus:ring-orange-500"
            />
            <label for="topic-is-question" class="text-sm text-gray-700">This is a question — I'm looking for an answer</label>
          </div>
          <div v-if="!isEditing" class="rounded-xl border border-gray-200 p-4">
            <p class="text-sm font-semibold text-gray-800">Who can see this conversation?</p>
            <div class="mt-3 space-y-2 text-sm text-gray-700">
              <label class="flex items-center gap-2"><input v-model="form.visibility" value="all" type="radio" class="text-orange-600 focus:ring-orange-500" /> Everyone in this group</label>
              <label class="flex items-center gap-2"><input v-model="form.visibility" value="exclude" type="radio" class="text-orange-600 focus:ring-orange-500" /> Everyone except selected members</label>
              <label class="flex items-center gap-2"><input v-model="form.visibility" value="selected" type="radio" class="text-orange-600 focus:ring-orange-500" /> Only selected members and me</label>
            </div>
            <div v-if="form.visibility !== 'all'" class="mt-3 max-h-36 space-y-2 overflow-y-auto rounded-lg bg-gray-50 p-3">
              <p class="text-xs text-gray-500">{{ form.visibility === 'exclude' ? 'Choose members who should not see this conversation.' : 'Choose the members who may see this conversation.' }}</p>
              <label v-for="member in groupMembers" :key="member.id" class="flex items-center gap-2 text-sm text-gray-700">
                <input v-model="form.selectedUsers" :value="member.id" type="checkbox" class="rounded border-gray-300 text-orange-600 focus:ring-orange-500" /> {{ member.name }}
              </label>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button
            @click="closeModal"
            class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            Cancel
          </button>
          <PrimaryButton @click="saveTopic" :disabled="!form.title.trim() || saving">
            {{ saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Start conversation' }}
          </PrimaryButton>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="deleteTarget" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="deleteTarget = null">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
        <div class="flex items-start gap-3">
          <div class="shrink-0 w-10 h-10 rounded-full bg-rose-100 flex items-center justify-center">
            <Trash2Icon class="w-5 h-5 text-rose-600" />
          </div>
          <div>
            <h3 class="text-lg font-bold text-gray-900">Delete this topic?</h3>
            <p class="text-sm text-gray-600 mt-1">
              This will permanently remove "<strong>{{ deleteTarget?.title }}</strong>" and all its replies. This cannot be undone.
            </p>
          </div>
        </div>
        <div class="flex justify-end gap-3 mt-6">
          <button
            @click="deleteTarget = null"
            class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            Cancel
          </button>
          <button
            @click="deleteTopic"
            class="px-4 py-2 text-sm bg-rose-600 text-white rounded-lg hover:bg-rose-700 font-medium transition"
          >
            {{ deleting ? 'Deleting...' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { usePage, router, Link } from '@inertiajs/vue3';
import {
  ArrowLeft as ArrowLeftIcon,
  Users as UsersIcon,
  MessageSquare as MessageSquareIcon,
  Search as SearchIcon,
  Edit2 as Edit2Icon,
  Trash2 as Trash2Icon,
  Eye as EyeIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon,
  X as XIcon,
} from 'lucide-vue-next';

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const auth = usePage().props.auth;

const props = defineProps({
  group: { type: Object, default: () => ({ id: null, name: '', description: '', members: 0 }) },
  topics: { type: Array, default: () => [] },
  topicKeywords: { type: Array, default: () => [] },
  groupMembers: { type: Array, default: () => [] },
});

const searchQuery = ref('');
const filterBy = ref('recent');
const currentPage = ref(1);
const perPage = 10;

// Modal state
const showModal = ref(false);
const isEditing = ref(false);
const editingTopicId = ref(null);
const saving = ref(false);
const form = ref({ title: '', body: '', isQuestion: false, visibility: 'all', selectedUsers: [] });
const topicError = ref('');
const topicKeywords = computed(() => props.topicKeywords);

// Delete state
const deleteTarget = ref(null);
const deleting = ref(false);
const onlineUsers = ref([]);

onMounted(() => {
  window.Echo.join(`group.online.${props.group.id}`)
    .here((users) => {
        onlineUsers.value = users;
    })
    .joining((user) => {
        if (!onlineUsers.value.find(u => u.id === user.id)) {
            onlineUsers.value.push(user);
        }
    })
    .leaving((user) => {
        onlineUsers.value = onlineUsers.value.filter(u => u.id !== user.id);
    });
});

onUnmounted(() => {
  window.Echo.leave(`group.online.${props.group.id}`);
});

const filteredTopics = computed(() => {
  let list = props.topics.filter(t =>
    t.title.toLowerCase().includes(searchQuery.value.toLowerCase())
  );

  if (filterBy.value === 'popular') {
    list = [...list].sort((a, b) => (b.replies ?? 0) - (a.replies ?? 0));
  } else if (filterBy.value === 'unanswered') {
    list = list.filter(t => t.status !== 'answered');
  } else {
    list = [...list].sort((a, b) => new Date(b.createdAt ?? 0) - new Date(a.createdAt ?? 0));
  }

  return list;
});

const totalPages = computed(() => Math.max(1, Math.ceil(filteredTopics.value.length / perPage)));

const paginatedTopics = computed(() => {
  const start = (currentPage.value - 1) * perPage;
  return filteredTopics.value.slice(start, start + perPage);
});

// Watch filter changes to reset pagination
watch(filterBy, () => { currentPage.value = 1; });
watch(searchQuery, () => { currentPage.value = 1; });

const goToTopic = (topicId) => {
  router.visit(route('topics.show', { group: props.group.id, topic: topicId }));
};

const openCreateModal = () => {
  isEditing.value = false;
  editingTopicId.value = null;
  form.value = { title: '', body: '', isQuestion: false, visibility: 'all', selectedUsers: [] };
  topicError.value = '';
  showModal.value = true;
};

const openEditModal = (topic) => {
  isEditing.value = true;
  editingTopicId.value = topic.id;
  form.value = { title: topic.title, body: topic.body, isQuestion: false, visibility: 'all', selectedUsers: [] };
  topicError.value = '';
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const saveTopic = () => {
  if (saving.value) return;
  if (form.value.title.trim().length < 8) {
    topicError.value = 'Give the conversation a clear title of at least 8 characters.';
    return;
  }
  if (form.value.body.trim().length < 30) {
    topicError.value = 'Add an opening message of at least 30 characters so members have enough context.';
    return;
  }
  topicError.value = '';
  saving.value = true;

  if (isEditing.value) {
    saving.value = false;
    closeModal();
    goToTopic(editingTopicId.value);
  } else {
    router.post(`/groups/${props.group.id}/topics`, {
      title: form.value.title,
      description: form.value.body,
      is_question: form.value.isQuestion,
      visibility: form.value.visibility,
      selected_users: form.value.selectedUsers,
    }, {
      preserveScroll: true,
      onSuccess: () => {
        form.value = { title: '', body: '', isQuestion: false, visibility: 'all', selectedUsers: [] };
        closeModal();
        router.reload({ only: ['topics'], preserveState: false });
      },
      onError: (errors) => {
        topicError.value = errors.title || errors.description || errors.selected_users || 'This conversation could not be created.';
        saving.value = false;
      },
      onFinish: () => { saving.value = false; },
    });
  }
};

const confirmDeleteTopic = (topic) => {
  deleteTarget.value = topic;
};

const deleteTopic = () => {
  if (!deleteTarget.value || deleting.value) return;
  deleting.value = true;
  router.delete(route('topics.destroy', deleteTarget.value.id), {
    preserveScroll: true,
    onSuccess: () => {
      deleteTarget.value = null;
    },
    onFinish: () => { deleting.value = false; },
  });
};
</script>
