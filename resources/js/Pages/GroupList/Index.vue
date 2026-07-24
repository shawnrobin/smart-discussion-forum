<template>
  <AuthenticatedLayout page-title="Groups" :user-name="auth?.user?.name ?? ''">
    <div class="mx-auto w-full max-w-7xl space-y-6 p-4 sm:p-6 lg:p-8">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Discussion Groups</h1>
          <p class="text-sm text-gray-500 mt-1">Browse and join discussion groups</p>
        </div>
        <PrimaryButton v-if="canCreateGroups" @click="showCreateModal = true">
          + Create Group
        </PrimaryButton>
      </div>

      <!-- Search & Sort -->
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="relative flex-1">
          <SearchIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search groups..."
            class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
          />
        </div>
        <select
          v-model="sortBy"
          class="px-4 py-2.5 border border-gray-300 rounded-lg text-sm bg-white text-gray-700 focus:ring-2 focus:ring-orange-500 outline-none"
        >
          <option value="recent">Most Recent</option>
          <option value="members">Most Members</option>
          <option value="active">Most Active</option>
        </select>
      </div>

      <!-- Groups Grid -->
      <div v-if="filteredGroups.length === 0" class="text-center py-16 text-gray-400">
        <UsersIcon class="w-12 h-12 mx-auto mb-3 text-gray-300" />
        <p class="text-sm">No groups found</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="group in filteredGroups"
          :key="group.id"
          class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-all duration-200 hover:border-orange-200 cursor-pointer flex flex-col"
          @click="goToGroup(group.id)"
        >
          <div class="flex-1">
            <h3 class="text-lg font-bold text-gray-900 group-hover:text-orange-600">{{ group.name }}</h3>
            <p class="text-sm text-gray-500 mt-2 line-clamp-2">{{ group.description }}</p>
          </div>

          <div class="mt-4 flex items-center justify-between text-xs text-gray-400">
            <div class="flex items-center gap-4">
              <span class="flex items-center gap-1.5">
                <UsersIcon class="w-3.5 h-3.5 text-orange-500" />
                {{ group.users_count ?? 0 }} members
              </span>
              <span class="flex items-center gap-1.5">
                <MessageSquareIcon class="w-3.5 h-3.5 text-orange-500" />
                {{ group.topics_count ?? 0 }} topics
              </span>
            </div>
            <span class="text-[10px] text-gray-300">{{ group.created_at ? new Date(group.created_at).toLocaleDateString() : '' }}</span>
          </div>

          <div class="mt-4 flex gap-2" @click.stop>
            <button
              v-if="!group.joined"
              @click="openJoinRules(group)"
              :disabled="loadingId === group.id"
              class="flex-1 px-4 py-2 bg-orange-600 text-white text-sm font-medium rounded-lg hover:bg-orange-700 disabled:opacity-40 transition"
            >
              {{ loadingId === group.id ? 'Joining...' : 'Join Group' }}
            </button>
            <button
              v-else
              @click="leaveGroup(group.id)"
              :disabled="loadingId === group.id"
              class="flex-1 px-4 py-2 border border-gray-300 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 disabled:opacity-40 transition"
            >
              {{ loadingId === group.id ? 'Leaving...' : 'Leave' }}
            </button>
            <button
              v-if="group.joined"
              @click="goToGroup(group.id)"
              class="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 transition"
            >
              <ArrowRightIcon class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Group Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/40 flex items-center justify-center z-50" @click.self="showCreateModal = false">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-bold text-gray-900">Create New Group</h3>
          <button @click="showCreateModal = false" class="text-gray-400 hover:text-gray-600 transition">
            <XIcon class="w-5 h-5" />
          </button>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Group Name</label>
            <input
              v-model="newGroup.name"
              type="text"
              placeholder="Enter group name"
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea
              v-model="newGroup.description"
              rows="3"
              placeholder="What is this group about?"
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none resize-none"
            ></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Allowed discussion keywords</label>
            <input
              v-model="newGroup.keywords"
              type="text"
              placeholder="e.g. algorithms, arrays, trees"
              class="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
            <p class="mt-1 text-xs text-gray-500">Separate keywords with commas. New conversation threads must match one of them.</p>
          </div>
        </div>

        <div class="flex justify-end gap-3 mt-6">
          <button
            @click="showCreateModal = false"
            class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg transition"
          >
            Cancel
          </button>
          <PrimaryButton @click="createGroup" :disabled="creating || !newGroup.name.trim()">
            {{ creating ? 'Creating...' : 'Create Group' }}
          </PrimaryButton>
        </div>
      </div>
    </div>

    <div v-if="joiningGroup" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4" @click.self="cancelJoin">
      <div class="w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-2xl">
        <div class="bg-gradient-to-r from-orange-600 to-amber-500 px-8 py-7 text-white">
          <p class="text-xs font-bold uppercase tracking-[0.18em] text-orange-100">Before you join</p>
          <h3 class="mt-1 text-2xl font-bold">{{ joiningGroup.name }} group rules</h3>
          <p class="mt-2 text-sm leading-6 text-orange-50">Please read these guidelines carefully. Agreeing means you will follow them whenever you participate in this group.</p>
        </div>
        <div class="p-8">
          <div class="flex items-center justify-between gap-4">
            <h4 class="text-lg font-bold text-slate-900">{{ rules?.title }}</h4>
            <span class="shrink-0 rounded-full bg-orange-100 px-3 py-1 text-xs font-semibold text-orange-800">Version {{ rules?.version }}</span>
          </div>
          <div class="mt-4 max-h-80 overflow-y-auto whitespace-pre-line rounded-xl border border-slate-200 bg-slate-50 p-6 text-base leading-8 text-slate-700 shadow-inner">{{ rules?.content }}</div>
          <label class="mt-6 flex items-start gap-3 rounded-xl border border-orange-200 bg-orange-50 p-4 text-sm leading-6 text-slate-800 cursor-pointer">
            <input v-model="agreedToRules" type="checkbox" class="mt-1 h-5 w-5 rounded border-gray-300 text-orange-600 focus:ring-orange-500" />
            <span><strong>I have read and agree to these group rules.</strong><br />I understand that breaking them may lead to moderation action.</span>
          </label>
        <p v-if="joinError" class="mt-2 text-sm text-rose-600">{{ joinError }}</p>
        <div class="mt-6 flex flex-col-reverse sm:flex-row sm:justify-end gap-3">
          <button @click="cancelJoin" class="rounded-xl px-5 py-3 text-sm font-semibold text-slate-600 hover:bg-slate-100">Decline</button>
          <button @click="confirmJoin" :disabled="!agreedToRules || joining" class="rounded-xl bg-orange-600 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-orange-700 disabled:cursor-not-allowed disabled:opacity-50">{{ joining ? 'Joining group...' : 'Agree & Join Group' }}</button>
        </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import { Search as SearchIcon, Users as UsersIcon, MessageSquare as MessageSquareIcon, ArrowRight as ArrowRightIcon, X as XIcon } from 'lucide-vue-next';

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const page = usePage();
const auth = computed(() => page.props.auth);

const props = defineProps({
  groups: { type: Array, default: () => [] },
  rules: { type: Object, default: null },
});

const searchQuery = ref('');
const sortBy = ref('recent');
const showCreateModal = ref(false);
const creating = ref(false);
const loadingId = ref(null);
const joiningGroup = ref(null);
const agreedToRules = ref(false);
const joinError = ref('');
const joining = ref(false);

const newGroup = ref({ name: '', description: '', keywords: '' });

const canCreateGroups = computed(() => Boolean(auth.value?.user));

const createGroup = () => {
  if (!newGroup.value.name.trim() || creating.value) return;
  creating.value = true;
  router.post('/groups', {
    name: newGroup.value.name,
    description: newGroup.value.description,
    topic_keywords: newGroup.value.keywords.split(',').map(keyword => keyword.trim()).filter(Boolean),
  }, {
    preserveScroll: true,
    onSuccess: () => {
      newGroup.value = { name: '', description: '', keywords: '' };
      showCreateModal.value = false;
    },
    onFinish: () => { creating.value = false; },
  });
};

const filteredGroups = computed(() => {
  let list = props.groups.filter(g =>
    g.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    (g.description || '').toLowerCase().includes(searchQuery.value.toLowerCase())
  );

  if (sortBy.value === 'members') {
    list = [...list].sort((a, b) => (b.users_count ?? 0) - (a.users_count ?? 0));
  } else if (sortBy.value === 'active') {
    list = [...list].sort((a, b) => (b.topics_count ?? 0) - (a.topics_count ?? 0));
  } else {
    list = [...list].sort((a, b) => new Date(b.created_at ?? 0) - new Date(a.created_at ?? 0));
  }

  return list;
});

const goToGroup = (groupId) => {
  router.visit(`/groups/${groupId}`);
};

const openJoinRules = (group) => {
  joiningGroup.value = group;
  agreedToRules.value = false;
  joinError.value = '';
};

const cancelJoin = () => {
  if (joining.value) return;
  joiningGroup.value = null;
  agreedToRules.value = false;
};

const confirmJoin = () => {
  if (!joiningGroup.value || !agreedToRules.value || joining.value) return;
  const groupId = joiningGroup.value.id;
  joining.value = true;
  joinError.value = '';
  if (loadingId.value) return;
  loadingId.value = groupId;
  const group = props.groups.find(g => g.id === groupId);
  if (group) group.joined = true;
  router.post(`/groups/${groupId}/join`, {
    rules_version: props.rules?.version,
    agreed_to_rules: agreedToRules.value,
  }, {
    preserveScroll: true,
    onSuccess: () => { joiningGroup.value = null; },
    onError: (errors) => {
      if (group) group.joined = false;
      joinError.value = errors.agreed_to_rules || errors.rules_version || 'Unable to join this group.';
    },
    onFinish: () => { loadingId.value = null; joining.value = false; },
  });
};

const leaveGroup = (groupId) => {
  if (loadingId.value) return;
  loadingId.value = groupId;
  const group = props.groups.find(g => g.id === groupId);
  if (group) group.joined = false;
  router.post(`/groups/${groupId}/leave`, {}, {
    preserveScroll: true,
    onError: () => { if (group) group.joined = true; },
    onFinish: () => { loadingId.value = null; },
  });
};
</script>
