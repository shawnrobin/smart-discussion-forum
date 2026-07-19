<template>
<template>
<div class="flex h-screen bg-gray-50 dark:bg-gray-900 text-gray-800 dark:text-gray-100 antialiased">
    <nav class="w-64 bg-white dark:bg-gray-800 border-r border-gray-100 dark:border-gray-700 flex flex-col justify-between shrink-0 shadow-sm">
      <div>
        <Link
          :href="route('dashboard')"
          class="p-6 border-b border-gray-100 dark:border-gray-700 flex items-center gap-2 hover:bg-gray-50 dark:hover:bg-gray-700 transition"
        >
          <div class="w-8 h-8 rounded-lg bg-orange-600 flex items-center justify-center text-white font-black text-lg shadow-sm">
            S
          </div>

          <h2 class="text-xl font-bold tracking-tight text-gray-900 dark:text-white">
            Smart<span class="text-orange-600">Forum</span>
          </h2>
        </Link>

        <div class="p-4 space-y-1">
          <Link
            v-for="link in navLinks"
            :key="link.id"
            :href="link.href"
            :class="[
              $page.url === link.matchUrl || $page.url.startsWith(link.matchUrl + '/')
                ? 'bg-orange-50 dark:bg-orange-900/30 text-orange-600 dark:text-orange-300 font-semibold'
                : 'text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 hover:text-gray-900 dark:hover:text-white',
              'flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 group'
            ]"
          >
            <component
              :is="link.icon"
              :class="[
                $page.url === link.matchUrl || $page.url.startsWith(link.matchUrl + '/')
                  ? 'text-orange-600 dark:text-orange-300'
                  : 'text-gray-400 dark:text-gray-500 group-hover:text-gray-600 dark:group-hover:text-gray-300',
                'w-4 h-4 transition-colors shrink-0'
              ]"
            />
            <span class="block opacity-100 visible text-current">{{ link.label }}</span>
          </Link>
        </div>

        <div class="border-t border-gray-100 bg-white">
          <div v-if="isAdmin" class="p-4 border-b border-gray-100 space-y-1">
            <div class="px-4 pb-2 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Admin</div>
            <Link :href="route('admin.dashboard')" class="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-orange-50 hover:text-orange-600">
              <ShieldIcon class="w-4 h-4 text-orange-500" /> <span>Admin Panel</span>
            </Link>
          </div>

          <div v-if="isLecturer" class="p-4 border-b border-gray-100 space-y-1">
            <div class="px-4 pb-2 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Lecturer</div>
            <Link :href="route('lecturer.dashboard')" class="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-orange-50 hover:text-orange-600">
              <BookOpenIcon class="w-4 h-4 text-orange-500" /> <span>Lecturer View</span>
            </Link>
            <Link :href="route('lecturer.groups.index')" class="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-orange-50 hover:text-orange-600">
              <Users class="w-4 h-4 text-orange-500" /> <span>Manage Groups</span>
            </Link>
            <Link :href="route('lecturer.moderation.index')" class="flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-orange-50 hover:text-orange-600">
              <AlertTriangleIcon class="w-4 h-4 text-orange-500" /> <span>Moderation</span>
            </Link>
          </div>

          <div class="p-4 border-t border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800">
            <button
              @click="logout"
              class="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-900/20 rounded-xl transition-all duration-150"
            >
              <LogOutIcon class="w-4 h-4 shrink-0" />
              <span>Logout</span>
            </button>
          </div>
        </div>
      </div>
    </nav>

    <div class="flex-1 flex flex-col min-w-0">
      <header class="bg-white dark:bg-gray-800 border-b border-gray-100 dark:border-gray-700 h-20 px-8 flex justify-between items-center shrink-0">
        <div class="flex items-center gap-4">
          <h1 class="text-xl font-bold text-gray-900 dark:text-white tracking-tight">
            {{ pageTitle || 'Overview' }}
          </h1>
        </div>

        <div class="flex items-center gap-4">
          <!-- Notification Bell -->
          <div class="relative">
            <button
              @click="showNotifications = !showNotifications"
              class="relative p-2 text-amber-400 hover:text-amber-500 dark:hover:text-amber-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition"
            >
              🔔

              <span
                v-if="notifications.length > 0"
                class="absolute top-0 right-0 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white border-2 border-white dark:border-gray-800"
              >
                {{ notifications.length }}
              </span>
            </button>

            <div
              v-if="showNotifications"
              class="absolute right-0 mt-2 w-80 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-100 dark:border-gray-700 overflow-hidden z-50"
            >
              <div class="p-3 border-b border-gray-100 dark:border-gray-700 font-bold text-sm text-gray-900 dark:text-white">
                Notifications
              </div>

              <div class="max-h-64 overflow-y-auto">
                <div
                  v-for="note in notifications"
                  :key="note.id"
                  class="p-4 border-b border-gray-50 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 flex justify-between items-center"
                >
                  <p class="text-sm text-gray-700 dark:text-gray-300">
                    {{ note.message }}
                  </p>

                  <button
                    @click="markAsRead(note.id)"
                    class="ml-2 text-[10px] text-orange-600 dark:text-orange-400 font-bold uppercase hover:underline"
                  >
                    Read
                  </button>
                </div>

                <div
                  v-if="notifications.length === 0"
                  class="p-4 text-center text-sm text-gray-400 dark:text-gray-500"
                >
                  No new notifications
                </div>
              </div>
            </div>
          </div>

          <div class="h-4 w-px bg-gray-200 dark:bg-gray-600"></div>

          <span
            class="text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-50 dark:bg-gray-700 px-3 py-1.5 rounded-lg border border-gray-100 dark:border-gray-600"
          >
            {{ user?.name ?? userName ?? 'Guest Account' }}
          </span>
        </div>
      </header>

      <main class="flex-1 overflow-auto bg-gray-50/50 dark:bg-gray-900">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { router, Link, usePage } from '@inertiajs/vue3';
import { 
  LayoutDashboard, Users, Award, User, Settings, LogOut as LogOutIcon, 
  Shield as ShieldIcon, BookOpen as BookOpenIcon, AlertTriangle as AlertTriangleIcon 
} from 'lucide-vue-next';

const props = defineProps({ pageTitle: String, userName: String });

const page = usePage();
const user = computed(() => page.props.auth?.user || null);
const isAdmin = computed(() => user.value?.role === 'system_admin');
const isLecturer = computed(() => user.value?.role === 'lecturer' || user.value?.role === 'system_admin');

const showNotifications = ref(false);
const notifications = ref(page.props.notifications || []);

const navLinks = computed(() => [
  { id: 1, label: 'Dashboard', href: route('dashboard'),     matchUrl: '/dashboard', icon: LayoutDashboard },
  { id: 3, label: 'Groups',    href: route('groups.index'),  matchUrl: '/groups',    icon: Users },
  { id: 4, label: 'Quizzes',   href: route('quizzes.index'), matchUrl: '/quizzes',   icon: Award },
  { id: 5, label: 'Profile',   href: route('profile.edit'),  matchUrl: '/profile',   icon: User },
  { id: 6, label: 'Settings',  href: route('settings.index'),matchUrl: '/settings',  icon: Settings }
]);

const logout = () => router.post(route('logout'));

const markAsRead = (id) => {
  router.post(route('notifications.markAsRead', id));
};
</script>
