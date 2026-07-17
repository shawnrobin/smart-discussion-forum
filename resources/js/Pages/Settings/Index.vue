<template>
  <AuthenticatedLayout page-title="Settings" :user-name="user.name">
    <div class="p-8 max-w-5xl mx-auto">
      <div class="flex gap-8">

        <!-- Sidebar tabs -->
        <div class="w-56 shrink-0">
          <nav class="space-y-1">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="[
                activeTab === tab.id
                  ? 'bg-orange-50 dark:bg-orange-900/30 text-orange-600 dark:text-orange-300 font-semibold'
                  : 'text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white',
                'w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-150'
              ]"
            >
              {{ tab.label }}
            </button>
          </nav>
        </div>

        <!-- Content -->
        <div class="flex-1 bg-white dark:bg-gray-800 dark:text-gray-100 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm p-8">

          <!-- Profile Tab -->
          <div v-if="activeTab === 'profile'" class="space-y-6">
            <ProfileHeader
              :name="user.name"
              :email="user.email"
              :gamification="{level:5, totalPoints: 1250, verifiedAnswers:14, weeklyStreak:6}"
            />

            <!-- Academic & Quiz Performance Table -->
            <div class="bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 rounded-2xl p-6 shadow-sm">
              <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Academic & Quiz Performance</h2>
              <div class="overflow-hidden border border-gray-100 dark:border-gray-700 rounded-xl">
                <table class="min-w-full divide-y divide-gray-100 dark:divide-gray-700 text-left text-sm">
                  <thead class="bg-gray-50 dark:bg-gray-700 font-semibold text-gray-700 dark:text-gray-200">
                    <tr>
                      <th class="px-4 py-3">Evaluation Element</th>
                      <th class="px-4 py-3">Completed</th>
                      <th class="px-4 py-3 text-right">Resulting Score</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-100 dark:divide-gray-700 text-gray-600 dark:text-gray-300">
                    <tr v-for="exam in exams" :key="exam.id">
                      <td class="px-4 py-3 font-medium text-gray-900 dark:text-white">{{ exam.title }}</td>
                      <td class="px-4 py-3 text-xs text-gray-400 dark:text-gray-500">{{ exam.date }}</td>
                      <td class="px-4 py-3 text-right font-bold text-emerald-600 dark:text-emerald-400">{{ exam.score }}%</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-6">Profile</h2>

            <form @submit.prevent="submitProfile" class="space-y-5">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Full Name</label>
                <input v-model="profileForm.name" type="text"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                <p v-if="profileForm.errors.name" class="text-rose-600 text-xs mt-1">{{ profileForm.errors.name }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Username</label>
                <input v-model="profileForm.username" type="text"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                <p v-if="profileForm.errors.username" class="text-rose-600 text-xs mt-1">{{ profileForm.errors.username }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Bio</label>
                <textarea v-model="profileForm.bio" rows="3"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm"></textarea>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Course</label>
                  <input v-model="profileForm.course" type="text"
                    class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Faculty</label>
                  <input v-model="profileForm.faculty" type="text"
                    class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Year of Study</label>
                <input v-model="profileForm.year_of_study" type="number"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
              </div>

              <button type="submit" :disabled="profileForm.processing"
                class="px-6 py-2.5 bg-orange-600 text-white text-sm font-semibold rounded-xl hover:bg-orange-700 transition-colors disabled:opacity-50">
                {{ profileForm.processing ? 'Saving...' : 'Save Changes' }}
              </button>

              <p v-if="profileSaved" class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">Saved successfully.</p>
            </form>
          </div>

          <!-- Account Tab -->
          <div v-else-if="activeTab === 'account'">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-6">Account</h2>

            <form @submit.prevent="submitAccount" class="space-y-5">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
                <input v-model="accountForm.email" type="email"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                <p v-if="accountForm.errors.email" class="text-rose-600 text-xs mt-1">{{ accountForm.errors.email }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Phone Number</label>
                <input v-model="accountForm.phone_number" type="text"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
              </div>

              <hr class="border-gray-100 dark:border-gray-700" />

              <p class="text-sm font-medium text-gray-700 dark:text-gray-300">Change Password</p>
              <p class="text-xs text-gray-400 dark:text-gray-500 -mt-3">Leave blank if you don't want to change your password.</p>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Current Password</label>
                <input v-model="accountForm.current_password" type="password"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                <p v-if="accountForm.errors.current_password" class="text-rose-600 text-xs mt-1">{{ accountForm.errors.current_password }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">New Password</label>
                <input v-model="accountForm.new_password" type="password"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
                <p v-if="accountForm.errors.new_password" class="text-rose-600 text-xs mt-1">{{ accountForm.errors.new_password }}</p>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Confirm New Password</label>
                <input v-model="accountForm.new_password_confirmation" type="password"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm" />
              </div>

              <button type="submit" :disabled="accountForm.processing"
                class="px-6 py-2.5 bg-orange-600 text-white text-sm font-semibold rounded-xl hover:bg-orange-700 transition-colors disabled:opacity-50">
                {{ accountForm.processing ? 'Saving...' : 'Save Changes' }}
              </button>

              <p v-if="accountSaved" class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">Saved successfully.</p>
            </form>
          </div>

          <!-- Appearance Tab -->
          <div v-else-if="activeTab === 'appearance'">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-6">Appearance</h2>

            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Theme</label>
                <div class="flex gap-3">
                  <!-- Light Button -->
                  <button
                    @click="applyTheme('light')"
                    :class="[
                      settingsForm.theme === 'light' ? 'border-orange-600 bg-orange-50 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300' : 'border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-300',
                      'flex-1 border-2 rounded-xl p-4 text-sm font-medium text-center transition-colors'
                    ]"
                  >
                    Light
                  </button>

                  <!-- Dark Button -->
                  <button
                    @click="applyTheme('dark')"
                    :class="[
                      settingsForm.theme === 'dark' ? 'border-orange-600 bg-orange-50 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300' : 'border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-300',
                      'flex-1 border-2 rounded-xl p-4 text-sm font-medium text-center transition-colors'
                    ]"
                  >
                    Dark
                  </button>
                </div>
                <p class="text-xs text-gray-400 dark:text-gray-500 mt-3">
                  Light mode uses a bright interface. Dark mode reduces eye strain in low-light settings.
                </p>
              </div>

              <p v-if="settingsSaved" class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">Saved successfully.</p>
            </div>
          </div>

          <!-- Notifications Tab -->
          <div v-else-if="activeTab === 'notifications'">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-2">Notifications</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-6">Choose which notifications should pop up on your screen.</p>

            <div class="space-y-4">
              <label class="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700">
                <span class="text-sm text-gray-700 dark:text-gray-300">New replies</span>
                <input type="checkbox" v-model="settingsForm.notify_replies" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <label class="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700">
                <span class="text-sm text-gray-700 dark:text-gray-300">Mentions (@username)</span>
                <input type="checkbox" v-model="settingsForm.notify_mentions" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <label class="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700">
                <span class="text-sm text-gray-700 dark:text-gray-300">New group posts</span>
                <input type="checkbox" v-model="settingsForm.notify_group_posts" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <label class="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700">
                <span class="text-sm text-gray-700 dark:text-gray-300">Quiz announcements</span>
                <input type="checkbox" v-model="settingsForm.notify_quiz" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <label class="flex items-center justify-between py-3">
                <span class="text-sm text-gray-700 dark:text-gray-300">Email notifications</span>
                <input type="checkbox" v-model="settingsForm.notify_email" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <button @click="submitSettings" :disabled="settingsForm.processing"
                class="mt-4 px-6 py-2.5 bg-orange-600 text-white text-sm font-semibold rounded-xl hover:bg-orange-700 transition-colors disabled:opacity-50">
                {{ settingsForm.processing ? 'Saving...' : 'Save Changes' }}
              </button>

              <p v-if="settingsSaved" class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">Saved successfully.</p>
            </div>
          </div>

          <!-- Privacy Tab -->
          <div v-else-if="activeTab === 'privacy'">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-6">Privacy</h2>

            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Profile Visibility</label>
                <div class="flex gap-3">
                  <button
                    @click="settingsForm.is_private = false"
                    :class="[
                      !settingsForm.is_private ? 'border-orange-600 bg-orange-50 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300' : 'border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-300',
                      'flex-1 border-2 rounded-xl p-4 text-sm font-medium text-center transition-colors'
                    ]"
                  >
                    Public
                  </button>
                  <button
                    @click="settingsForm.is_private = true"
                    :class="[
                      settingsForm.is_private ? 'border-orange-600 bg-orange-50 dark:bg-orange-900/30 text-orange-700 dark:text-orange-300' : 'border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-300',
                      'flex-1 border-2 rounded-xl p-4 text-sm font-medium text-center transition-colors'
                    ]"
                  >
                    Private
                  </button>
                </div>
                <p class="text-xs text-gray-400 dark:text-gray-500 mt-3">
                  Public profiles can be viewed by anyone in the forum, including your posts and activity. Private profiles are only visible to your group members — choose this if you'd rather keep your academic activity more discreet.
                </p>
              </div>

              <label class="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700">
                <span class="text-sm text-gray-700 dark:text-gray-300">Show online status</span>
                <input type="checkbox" v-model="settingsForm.show_online_status" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Who can message me</label>
                <select v-model="settingsForm.who_can_message"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm">
                  <option value="everyone">Everyone</option>
                  <option value="group_members">Group members only</option>
                  <option value="nobody">Nobody</option>
                </select>
              </div>

              <button @click="submitSettings" :disabled="settingsForm.processing"
                class="px-6 py-2.5 bg-orange-600 text-white text-sm font-semibold rounded-xl hover:bg-orange-700 transition-colors disabled:opacity-50">
                {{ settingsForm.processing ? 'Saving...' : 'Save Changes' }}
              </button>

              <p v-if="settingsSaved" class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">Saved successfully.</p>
            </div>
          </div>

          <!-- Discussion Preferences Tab -->
          <div v-else-if="activeTab === 'preferences'">
            <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-6">Discussion Preferences</h2>

            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Default Topic Sorting</label>
                <select v-model="settingsForm.default_sort"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm">
                  <option value="latest">Latest</option>
                  <option value="popular">Most Popular</option>
                  <option value="oldest">Oldest First</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Posts Per Page</label>
                <select v-model="settingsForm.posts_per_page"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm">
                  <option :value="10">10</option>
                  <option :value="20">20</option>
                  <option :value="50">50</option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Language</label>
                <select v-model="settingsForm.language"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm">
                  <option value="en">English</option>
                  <option value="lg">Luganda</option>
                  <option value="sw">Swahili</option>
                </select>
              </div>

              <label class="flex items-center justify-between py-3 border-b border-gray-100 dark:border-gray-700">
                <span class="text-sm text-gray-700 dark:text-gray-300">Auto-save drafts</span>
                <input type="checkbox" v-model="settingsForm.auto_save_drafts" class="w-5 h-5 accent-orange-600 rounded" />
              </label>

              <button @click="submitSettings" :disabled="settingsForm.processing"
                class="px-6 py-2.5 bg-orange-600 text-white text-sm font-semibold rounded-xl hover:bg-orange-700 transition-colors disabled:opacity-50">
                {{ settingsForm.processing ? 'Saving...' : 'Save Changes' }}
              </button>

              <p v-if="settingsSaved" class="text-emerald-600 dark:text-emerald-400 text-sm font-medium">Saved successfully.</p>
            </div>
          </div>

          <!-- Danger Zone Tab -->
          <div v-else-if="activeTab === 'danger'">
            <h2 class="text-lg font-bold text-rose-600 dark:text-rose-400 mb-6">Danger Zone</h2>

            <div class="space-y-8">
              <div class="border border-amber-200 dark:border-amber-700 bg-amber-50 dark:bg-amber-900/20 rounded-xl p-5">
                <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-1">Deactivate Account</h3>
                <p class="text-xs text-gray-500 dark:text-gray-400 mb-4">Your account will be hidden until you log back in.</p>
                <input v-model="deactivateForm.password" type="password" placeholder="Enter your password"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white text-sm mb-3" />
                <p v-if="deactivateForm.errors.password" class="text-rose-600 text-xs mb-2">{{ deactivateForm.errors.password }}</p>
                <button @click="submitDeactivate" :disabled="deactivateForm.processing"
                  class="px-5 py-2 bg-amber-600 text-white text-sm font-semibold rounded-xl hover:bg-amber-700 transition-colors disabled:opacity-50">
                  Deactivate Account
                </button>
              </div>

              <div class="border border-rose-200 dark:border-rose-700 bg-rose-50 dark:bg-rose-900/20 rounded-xl p-5">
                <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-1">Delete Account</h3>
                <p class="text-xs text-gray-500 dark:text-gray-400 mb-4">This permanently deletes your account and all your data. This cannot be undone.</p>
                <input v-model="deleteForm.password" type="password" placeholder="Enter your password"
                  class="w-full px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 dark:bg-gray-700 dark:text-white text-sm mb-3" />
                <p v-if="deleteForm.errors.password" class="text-rose-600 text-xs mb-2">{{ deleteForm.errors.password }}</p>
                <button @click="confirmDelete" :disabled="deleteForm.processing"
                  class="px-5 py-2 bg-rose-600 text-white text-sm font-semibold rounded-xl hover:bg-rose-700 transition-colors disabled:opacity-50">
                  Delete Account Permanently
                </button>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useForm } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import ProfileHeader from '@/Components/ProfileHeader.vue';

const props = defineProps({
  user: Object,
  settings: Object,
});

const tabs = [
  { id: 'profile', label: 'Profile' },
  { id: 'account', label: 'Account' },
  { id: 'appearance', label: 'Appearance' },
  { id: 'notifications', label: 'Notifications' },
  { id: 'privacy', label: 'Privacy' },
  { id: 'preferences', label: 'Discussion Preferences' },
  { id: 'danger', label: 'Danger Zone' },
];

const activeTab = ref('profile');

// Profile
const exams = ref([
  { id: 1, title: 'Introduction to Algorithms Quiz', date: 'July 01, 2026', score: 92 },
  { id: 2, title: 'Data Structures Mid-Term Assignment', date: 'June 24, 2026', score: 88 },
  { id: 3, title: 'Database Systems Final Exam', date: 'June 15, 2026', score: 95 },
]);

const profileSaved = ref(false);
const profileForm = useForm({
  name: props.user.name ?? '',
  username: props.user.username ?? '',
  bio: props.user.bio ?? '',
  course: props.user.course ?? '',
  faculty: props.user.faculty ?? '',
  year_of_study: props.user.year_of_study ?? '',
});
const submitProfile = () => {
  profileForm.put(route('settings.profile'), {
    preserveScroll: true,
    onSuccess: () => {
      profileSaved.value = true;
      setTimeout(() => (profileSaved.value = false), 2500);
    },
  });
};

// Account
const accountSaved = ref(false);
const accountForm = useForm({
  email: props.user.email ?? '',
  phone_number: props.user.phone_number ?? '',
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

// Appearance / Notifications / Privacy / Preferences (all share user_settings table)
const settingsSaved = ref(false);
const settingsForm = useForm({
  theme: props.settings.theme ?? 'light',
  theme_color: props.settings.theme_color ?? '#ea580c',
  notify_replies: props.settings.notify_replies ?? true,
  notify_mentions: props.settings.notify_mentions ?? true,
  notify_group_posts: props.settings.notify_group_posts ?? true,
  notify_quiz: props.settings.notify_quiz ?? true,
  notify_email: props.settings.notify_email ?? true,
  is_private: props.settings.is_private ?? false,
  show_online_status: props.settings.show_online_status ?? true,
  who_can_message: props.settings.who_can_message ?? 'everyone',
  default_sort: props.settings.default_sort ?? 'latest',
  posts_per_page: props.settings.posts_per_page ?? 10,
  language: props.settings.language ?? 'en',
  auto_save_drafts: props.settings.auto_save_drafts ?? true,
});
const submitSettings = () => {
  settingsForm.put(route('settings.update'), {
    preserveScroll: true,
    onSuccess: () => {
      settingsSaved.value = true;
      setTimeout(() => (settingsSaved.value = false), 2500);
    },
  });
};

const applyTheme = (theme) => {
  settingsForm.theme = theme;

  if (theme === 'dark') {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }

  submitSettings();
};

onMounted(() => {
  if (settingsForm.theme === 'dark') {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
});

const deactivateForm = useForm({ password: '' });
const submitDeactivate = () => {
  if (!confirm('Are you sure you want to deactivate your account?')) return;
  deactivateForm.put(route('settings.deactivate'));
};

const deleteForm = useForm({ password: '' });
const confirmDelete = () => {
  if (!confirm('This will PERMANENTLY delete your account. Are you absolutely sure?')) return;
  deleteForm.delete(route('settings.delete'));
};
</script>