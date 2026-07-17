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
                {{ topics.length }} topics
              </span>
            </div>
          </div>
          <PrimaryButton @click="openCreateModal" class="shrink-0">
            + New Topic
          </PrimaryButton>
        </div>
      </div>

      <!-- Search & Filter -->
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="relative flex-1">
          <SearchIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search topics..."
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

      <!-- Topics List -->
      <div v-if="filteredTopics.length === 0" class="text-center py-16 text-gray-400">
        <MessageSquareIcon class="w-12 h-12 mx-auto mb-3 text-gray-300" />
        <p class="text-sm">No topics yet. Be the first to start a discussion!</p>
      </div>

      <div class="space-y-3">
        <div
          v-for="topic in paginatedTopics"
          :key="topic.id"
          class="bg-white rounded-xl border border-gray-200 shadow-sm p-5 hover:shadow-md transition-all duration-200 hover:border-orange-200 cursor-pointer"
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
                  <h3 class="font-semibold text-gray-900 truncate">{{ topic.title }}</h3>
                  <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ topic.body }}</p>
                </div>
                <div class="flex items-center gap-2 shrink-0">
                  <button
                    v-if="topic.author?.name === auth?.user?.name"
                    @click.stop="openEditModal(topic)"
                    class="p-1.5 text-gray-400 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition"
                    title="Edit"
                  >
                    <Edit2Icon class="w-3.5 h-3.5" />
                  </button>
                  <button
                    v-if="topic.author?.name === auth?.user?.name"
                    @click.stop="confirmDeleteTopic(topic)"
                    class="p-1.5 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                    title="Delete"
                  >
                    <Trash2Icon class="w-3.5 h-3.5" />
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
                <span>by {{ topic.author?.name || 'Unknown' }} &middot; {{ topic.createdAt }}</span>
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
          <h3 class="text-lg font-bold text-gray-900">{{ isEditing ? 'Edit Topic' : 'New Topic' }}</h3>
          <button @click="closeModal" class="text-gray-400 hover:text-gray-600 transition">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
            <input
              v-model="form.title"
              type="text"
              placeholder="Enter a descriptive title"
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Body</label>
            <textarea
              v-model="form.body"
              rows="4"
              placeholder="Describe your topic..."
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none resize-none"
            ></textarea>
          </div>
          <div class="flex items-center gap-2">
            <input
              v-model="form.isQuestion"
              type="checkbox"
              id="topic-is-question"
              class="w-4 h-4 text-orange-600 rounded border-gray-300 focus:ring-orange-500"
            />
            <label for="topic-is-question" class="text-sm text-gray-700">This is a question — I'm looking for an answer</label>
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
            {{ saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Create Topic' }}
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
import { ref, computed } from 'vue';
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
const form = ref({ title: '', body: '', isQuestion: false });

// Delete state
const deleteTarget = ref(null);
const deleting = ref(false);

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
import { watch } from 'vue';
watch(filterBy, () => { currentPage.value = 1; });
watch(searchQuery, () => { currentPage.value = 1; });

const goToTopic = (topicId) => {
  router.visit(route('topics.show', { group: props.group.id, topic: topicId }));
};

const openCreateModal = () => {
  isEditing.value = false;
  editingTopicId.value = null;
  form.value = { title: '', body: '', isQuestion: false };
  showModal.value = true;
};

const openEditModal = (topic) => {
  isEditing.value = true;
  editingTopicId.value = topic.id;
  form.value = { title: topic.title, body: topic.body, isQuestion: false };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const saveTopic = () => {
  if (!form.value.title.trim() || saving.value) return;
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
    }, {
      preserveScroll: true,
      onSuccess: () => {
        form.value = { title: '', body: '', isQuestion: false };
        closeModal();
        router.reload({ only: ['topics'], preserveState: false });
      },
      onError: () => {
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
