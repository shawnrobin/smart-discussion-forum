<template>
  <div class="flex h-screen bg-gray-50 text-gray-800 antialiased">
    
    <nav class="w-64 bg-white border-r border-gray-100 flex flex-col justify-between shrink-0 shadow-sm">
      <div>
        <div class="p-6 border-b border-gray-100 flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-orange-600 flex items-center justify-center text-white font-black text-lg shadow-sm">
            S
          </div>
          <h2 class="text-xl font-bold tracking-tight text-gray-900">
            Smart<span class="text-orange-600">Forum</span>
          </h2>
        </div>

        <div class="p-4 space-y-1">
          <Link
            v-for="link in navLinks"
            :key="link.id"
            :href="link.href"
            :class="[
              $page.url.startsWith(link.matchUrl) 
                ? 'bg-orange-50 text-orange-600 font-semibold' 
                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
              'flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 group'
            ]"
          >
            <component 
              :is="link.icon" 
              :class="[
                $page.url.startsWith(link.matchUrl) ? 'text-orange-600' : 'text-gray-400 group-hover:text-gray-600',
                'w-4 h-4 transition-colors shrink-0'
              ]" 
            />
            <span class="block opacity-100 visible text-current">{{ link.label }}</span>
          </Link>
        </div>
      </div>

      <div class="p-4 border-t border-gray-100 bg-white">
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-rose-600 hover:bg-rose-50 rounded-xl transition-all duration-150"
        >
          <LogOutIcon class="w-4 h-4 shrink-0" />
          <span>Logout</span>
        </button>
      </div>
    </nav>

    <div class="flex-1 flex flex-col min-w-0">
      
      <header class="bg-white border-b border-gray-100 h-20 px-8 flex justify-between items-center shrink-0">
        <div class="flex items-center gap-4">
          <h1 class="text-xl font-bold text-gray-900 tracking-tight">
            {{ pageTitle || 'Overview' }}
          </h1>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="p-2 text-gray-400 hover:text-orange-600 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
            <NotificationBell />
          </div>
          
          <div class="h-4 w-px bg-gray-200"></div>
          
          <span class="text-sm font-medium text-gray-700 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100">
            {{ userName ?? 'Guest Account' }}
          </span>
        </div>
      </header>

      <main class="flex-1 overflow-auto bg-gray-50/50">
        <slot></slot>
      </main>
    </div>

  </div>
</template>

<script setup>
import { computed } from 'vue';
import { router, Link } from '@inertiajs/vue3';
import NotificationBell from '@/Components/NotificationBell.vue';

import { 
  LayoutDashboard as LayoutDashboardIcon,
  BookOpen as BookOpenIcon,
  Users as UsersIcon, 
  Award as AwardIcon, 
  User as UserIcon, 
  Settings as SettingsIcon,
  LogOut as LogOutIcon
} from 'lucide-vue-next';

defineProps({
  pageTitle: String,
  userName: String
});



// FIXED: We now use standard Laravel named route configurations instead of hardcoded paths
const navLinks = computed(() => [
  { id: 1, label: 'Dashboard', href: route('dashboard'),    matchUrl: '/dashboard', icon: LayoutDashboardIcon },
  { id: 2, label: 'Topics',    href: route('topics.index'), matchUrl: '/topics',    icon: BookOpenIcon },
  { id: 3, label: 'Groups',    href: route('groups.index'), matchUrl: '/groups',    icon: UsersIcon },
  { id: 4, label: 'Quizzes',   href: route('quizzes.index'),matchUrl: '/quizzes',   icon: AwardIcon },
  { id: 5, label: 'Profile',   href: route('profile.edit'), matchUrl: '/profile',   icon: UserIcon },
  { id: 6, label: 'Settings',  href: route('settings.edit'),matchUrl: '/settings',  icon: SettingsIcon }

]);

const logout = () => {
  router.post(route('logout'));
};
</script>