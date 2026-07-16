<template>
  <div class="flex h-screen bg-gray-50 text-gray-800 antialiased">
    <nav class="w-64 bg-white border-r border-gray-100 flex flex-col justify-between shrink-0 shadow-sm">
      <div>
        <Link :href="route('dashboard')" class="p-6 border-b border-gray-100 flex items-center gap-2 hover:bg-gray-50 transition">
          <div class="w-8 h-8 rounded-lg bg-orange-600 flex items-center justify-center text-white font-black text-lg shadow-sm">S</div>
          <h2 class="text-xl font-bold tracking-tight text-gray-900">Smart<span class="text-orange-600">Forum</span></h2>
        </Link>

        <div class="p-4 space-y-1">
          <Link
            v-for="link in navLinks"
            :key="link.id"
            :href="link.href"
            :class="[
              $page.url === link.matchUrl || $page.url.startsWith(link.matchUrl + '/')
                ? 'bg-orange-50 text-orange-600 font-semibold' 
                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
              'flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 group'
            ]"
          >
            <component :is="link.icon" class="w-4 h-4 transition-colors shrink-0" />
            <span>{{ link.label }}</span>
          </Link>
        </div>
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

        <div class="p-4">
          <button @click="logout" class="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-rose-600 hover:bg-rose-50 rounded-xl">
            <LogOutIcon class="w-4 h-4" /> <span>Logout</span>
          </button>
        </div>
      </div>
    </nav>

    <div class="flex-1 flex flex-col min-w-0">
      <header class="bg-white border-b border-gray-100 h-20 px-8 flex justify-between items-center">
        <h1 class="text-xl font-bold text-gray-900">{{ pageTitle || 'Overview' }}</h1>
        <div class="flex items-center gap-4">
            <span class="text-sm font-medium text-gray-700 bg-gray-50 px-3 py-1.5 rounded-lg border">{{ user?.name ?? 'Guest' }}</span>
        </div>
      </header>
      <main class="flex-1 overflow-auto bg-gray-50/50"><slot></slot></main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { router, Link, usePage } from '@inertiajs/vue3';
import { 
  LayoutDashboard, Users, Award, User, Settings, LogOut as LogOutIcon, 
  Shield as ShieldIcon, BookOpen as BookOpenIcon, AlertTriangle as AlertTriangleIcon 
} from 'lucide-vue-next';

defineProps({ pageTitle: String });
const page = usePage();
const user = computed(() => page.props.auth?.user || null);
const isAdmin = computed(() => user.value?.role === 'system_admin');
const isLecturer = computed(() => user.value?.role === 'lecturer' || user.value?.role === 'system_admin');

const navLinks = computed(() => [
  { id: 1, label: 'Dashboard', href: route('dashboard'), matchUrl: '/dashboard', icon: LayoutDashboard },
  { id: 3, label: 'Groups', href: route('groups.index'), matchUrl: '/groups', icon: Users },
  { id: 4, label: 'Quizzes', href: route('quizzes.index'), matchUrl: '/quizzes', icon: Award },
  { id: 5, label: 'Profile', href: route('profile.edit'), matchUrl: '/profile', icon: User },
  { id: 6, label: 'Settings', href: route('settings.index'), matchUrl: '/settings', icon: Settings }
]);

const logout = () => router.post(route('logout'));
</script>