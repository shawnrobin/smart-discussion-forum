<template>
  <Head title="Profile & Settings" />

  <AuthenticatedLayout page-title="Profile & Settings">
    <div class="py-10 bg-gray-50/30">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">

        <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm grid grid-cols-1 md:grid-cols-3 gap-6 items-center">

          <div class="flex items-center gap-4 border-b md:border-b-0 md:border-r border-gray-100 pb-4 md:pb-0 md:pr-4">
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-orange-500 to-amber-600 flex items-center justify-center text-white font-black text-2xl shadow-md shrink-0">
              {{ (user?.name || 'U').substring(0, 2).toUpperCase() }}
            </div>
            <div>
              <h3 class="text-lg font-bold text-gray-900 leading-tight">{{ user?.name || 'User' }}</h3>
              <p class="text-sm text-gray-400 mt-0.5">{{ user?.email || '' }}</p>
              <span class="inline-flex items-center gap-1 mt-2 px-2 py-0.5 bg-orange-100 text-orange-800 text-xs font-semibold rounded-full">
                <AwardIcon class="w-3 h-3" /> {{ (user?.role || 'student').replace('_', ' ').replace(/\b\w/g, c => c.toUpperCase()) }}
              </span>
            </div>
          </div>

          <div v-if="!isPrivilegedProfile" class="space-y-2 border-b md:border-b-0 md:border-r border-gray-100 pb-4 md:pb-0 md:pr-4">
            <div class="flex justify-between items-end">
              <div>
                <p class="text-xs text-gray-400 font-medium uppercase tracking-wider">Total Score</p>
                <p class="text-3xl font-black text-gray-900 tracking-tight mt-0.5">
                  {{ stats.total_points || 0 }} <span class="text-sm font-medium text-orange-600">XP</span>
                </p>
              </div>
              <span class="text-xs text-gray-500 font-medium bg-gray-50 px-2 py-1 rounded border border-gray-100">
                Level {{ stats.level || 1 }}
              </span>
            </div>

            <div>
              <div class="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                <div class="bg-gradient-to-r from-orange-500 to-amber-500 h-full transition-all duration-500" :style="{ width: Math.min(stats.progress || 0, 100) + '%' }"></div>
              </div>
              <p class="text-[11px] text-gray-400 mt-1 text-right font-medium">Level up in progress</p>
            </div>
          </div>

          <div v-else class="space-y-4 border-b md:border-b-0 md:border-r border-gray-100 pb-4 md:pb-0 md:pr-4">
            <div class="rounded-2xl border border-orange-100 bg-orange-50 p-4 text-sm text-orange-800">
              Staff profiles do not display participation score or streak metrics.
            </div>
            <div class="text-xs text-gray-400">Metrics like experience points, quiz average, and streak are only shown for student profiles.</div>
          </div>

          <div v-if="!isPrivilegedProfile" class="grid grid-cols-2 gap-3">
            <div class="bg-gray-50/60 border border-gray-100 p-3 rounded-xl text-center">
              <div class="text-orange-600 font-bold text-lg leading-none">{{ stats.answers_provided || 0 }}</div>
              <div class="text-[11px] text-gray-500 font-medium mt-1">Solutions Provided</div>
            </div>
            <div class="bg-gray-50/60 border border-gray-100 p-3 rounded-xl text-center">
              <div class="text-amber-600 font-bold text-lg leading-none">{{ stats.topics_created || 0 }}</div>
              <div class="text-[11px] text-gray-500 font-medium mt-1">Topics Started</div>
            </div>
          </div>

        </div>

        <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
          <div class="flex items-center gap-2 mb-4">
            <div class="p-1.5 bg-orange-50 text-orange-600 rounded-lg">
              <GraduationCapIcon class="w-5 h-5" />
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-900">Academic & Quiz Performance</h2>
              <p class="text-xs text-gray-400">Review your automated module grading evaluations.</p>
            </div>
          </div>

          <div v-if="isPrivilegedProfile" class="rounded-2xl border border-orange-100 bg-orange-50 p-6 text-sm text-orange-900">
            Lecturer and admin profiles do not display quiz average, participation score, or streak data.
          </div>

          <div v-else>
            <div v-if="exams.length === 0" class="text-center py-8 text-gray-400 text-sm">
              No quiz results yet. Complete a quiz to see your performance here.
            </div>

            <div v-else class="overflow-hidden border border-gray-100 rounded-xl">
              <table class="min-w-full divide-y divide-gray-100 text-left text-sm">
                <thead class="bg-gray-50 font-semibold text-gray-700">
                  <tr>
                    <th class="px-4 py-3">Evaluation Element</th>
                    <th class="px-4 py-3">Completed</th>
                    <th class="px-4 py-3 text-right">Resulting Score</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 text-gray-600">
                  <tr v-for="exam in exams" :key="exam.id" class="hover:bg-gray-50/50 transition">
                    <td class="px-4 py-3 font-medium text-gray-900">{{ exam.title }}</td>
                    <td class="px-4 py-3 text-xs text-gray-400">{{ exam.date }}</td>
                    <td class="px-4 py-3 text-right font-bold">
                      <span :class="exam.score >= 75 ? 'text-emerald-600' : 'text-amber-600'">
                        {{ exam.score }}%
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

          <div class="p-6 bg-white border border-gray-100 rounded-2xl shadow-sm space-y-4">
            <div>
              <h2 class="text-lg font-bold text-gray-900">Profile Details</h2>
              <p class="text-xs text-gray-400">Update your primary identity variables.</p>
            </div>

            <form class="space-y-4" @submit.prevent="submitProfile">
              <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 tracking-wider">Full Name</label>
                <input
                  type="text"
                  v-model="profileForm.name"
                  class="mt-1 block w-full rounded-lg border-gray-200 shadow-sm focus:border-orange-500 focus:ring-orange-500 bg-gray-50/50 text-sm"
                />
                <p v-if="profileForm.errors.name" class="text-red-500 text-xs mt-1">{{ profileForm.errors.name }}</p>
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 tracking-wider">Username</label>
                <input
                  type="text"
                  v-model="profileForm.username"
                  class="mt-1 block w-full rounded-lg border-gray-200 shadow-sm focus:border-orange-500 focus:ring-orange-500 bg-gray-50/50 text-sm"
                />
                <p v-if="profileForm.errors.username" class="text-red-500 text-xs mt-1">{{ profileForm.errors.username }}</p>
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 tracking-wider">Bio</label>
                <textarea
                  v-model="profileForm.bio"
                  rows="2"
                  class="mt-1 block w-full rounded-lg border-gray-200 shadow-sm focus:border-orange-500 focus:ring-orange-500 bg-gray-50/50 text-sm"
                ></textarea>
              </div>

              <button
                type="submit"
                :disabled="profileForm.processing"
                class="w-full sm:w-auto px-4 py-2 bg-orange-600 hover:bg-orange-700 text-white rounded-lg font-medium text-sm transition shadow-sm disabled:opacity-50"
              >
                {{ profileForm.processing ? 'Saving...' : 'Save Profile Changes' }}
              </button>

              <p v-if="profileSaved" class="text-emerald-600 text-xs font-medium">Saved successfully.</p>
            </form>
          </div>

          <div class="p-6 bg-white border border-gray-100 rounded-2xl shadow-sm space-y-4">
            <div>
              <h2 class="text-lg font-bold text-gray-900">Update Password</h2>
              <p class="text-xs text-gray-400">Keep security metrics aligned using continuous hashing upgrades.</p>
            </div>

            <form class="space-y-4" @submit.prevent="submitAccount">
              <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 tracking-wider">Current Password</label>
                <input
                  type="password"
                  v-model="accountForm.current_password"
                  class="mt-1 block w-full rounded-lg border-gray-200 shadow-sm focus:border-orange-500 focus:ring-orange-500 bg-gray-50/50 text-sm"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 tracking-wider">New Password</label>
                <input
                  type="password"
                  v-model="accountForm.new_password"
                  class="mt-1 block w-full rounded-lg border-gray-200 shadow-sm focus:border-orange-500 focus:ring-orange-500 bg-gray-50/50 text-sm"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 tracking-wider">Confirm Password</label>
                <input
                  type="password"
                  v-model="accountForm.new_password_confirmation"
                  class="mt-1 block w-full rounded-lg border-gray-200 shadow-sm focus:border-orange-500 focus:ring-orange-500 bg-gray-50/50 text-sm"
                />
              </div>

              <button
                type="submit"
                :disabled="accountForm.processing"
                class="w-full sm:w-auto px-4 py-2 bg-gray-900 hover:bg-gray-800 text-white rounded-lg font-medium text-sm transition shadow-sm disabled:opacity-50"
              >
                {{ accountForm.processing ? 'Saving...' : 'Update Security Credentials' }}
              </button>

              <p v-if="accountSaved" class="text-emerald-600 text-xs font-medium">Saved successfully.</p>
            </form>
          </div>

        </div>

        <div class="p-6 bg-red-50/30 border border-red-100 rounded-2xl shadow-sm flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h2 class="text-base font-bold text-red-900">Deactivate Forum Account</h2>
            <p class="text-xs text-red-600/70 mt-0.5">This removes all accrued participation points and past submissions permanently.</p>
          </div>
          <button
            @click="confirmDelete"
            class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-semibold rounded-lg transition shadow-sm shrink-0"
          >
            Delete Account
          </button>
        </div>

      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Head, usePage, useForm } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { Award as AwardIcon, GraduationCap as GraduationCapIcon } from 'lucide-vue-next';

const page = usePage();
const user = computed(() => page.props.auth?.user || {});
const notifications = page.props.notifications || [];
const isPrivilegedProfile = computed(() => page.props.isPrivilegedProfile ?? false);

// Stats from participation scores (or defaults)
const stats = ref(page.props.stats ?? {
  total_points: 0,
  level: 1,
  progress: 0,
  answers_provided: 0,
  topics_created: 0,
});

const props = defineProps({
  exams: { type: Array, default: () => [] },
});

// Profile form
const profileSaved = ref(false);
const profileForm = useForm({
  name: user.value.name ?? '',
  username: user.value.username ?? '',
  bio: '',
});

const submitProfile = () => {
  profileForm.put(route('settings.profile.update'), {
    preserveScroll: true,
    onSuccess: () => {
      profileSaved.value = true;
      setTimeout(() => (profileSaved.value = false), 2500);
    },
  });
};

// Account / password form
const accountSaved = ref(false);
const accountForm = useForm({
  email: user.value.email ?? '',
  current_password: '',
  new_password: '',
  new_password_confirmation: '',
});

const submitAccount = () => {
  accountForm.put(route('settings.account'), {
    preserveScroll: true,
    onSuccess: () => {
      accountForm.current_password = '';
      accountForm.new_password = '';
      accountForm.new_password_confirmation = '';
      accountSaved.value = true;
      setTimeout(() => (accountSaved.value = false), 2500);
    },
  });
};

// Delete account
const deleteForm = useForm({ password: '' });
const confirmDelete = () => {
  if (!confirm('This will PERMANENTLY delete your account. Are you absolutely sure?')) return;
  deleteForm.delete(route('settings.delete'));
};
</script>
