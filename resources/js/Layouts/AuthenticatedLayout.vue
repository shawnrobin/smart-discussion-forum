<template>
  <div class="flex h-screen bg-gray-50 text-gray-800 antialiased">
    
    <nav class="w-64 bg-white border-r border-gray-100 flex flex-col justify-between shrink-0 shadow-sm">
<<<<<<< HEAD
      
      <div>
        <div class="p-6 border-b border-gray-100 flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-orange-600 flex items-center justify-center text-white font-black text-lg shadow-sm">S</div>
          <h2 class="text-xl font-bold tracking-tight text-gray-900">Smart<span class="text-orange-600">Forum</span></h2>
        </div>
=======
      <Link
    :href="route('dashboard')"
    class="p-6 border-b border-gray-100 flex items-center gap-2 hover:bg-gray-50 transition"
>
    <div class="w-8 h-8 rounded-lg bg-orange-600 flex items-center justify-center text-white font-black text-lg shadow-sm">
        S
    </div>
>>>>>>> 48ceb19e08c75d6d5a3d74d1f9d037c8235d644e

    <h2 class="text-xl font-bold tracking-tight text-gray-900">
        Smart<span class="text-orange-600">Forum</span>
    </h2>
</Link>
        <div class="p-4 space-y-1">
          <Link
            v-for="link in generalLinks"
            :key="link.id"
            :href="link.href"
            :class="[
              $page.url === link.matchUrl || $page.url.startsWith(link.matchUrl + '/')
                ? 'bg-orange-50 text-orange-600 font-semibold' 
                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900',
              'flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 group'
            ]"
          >
            <component :is="link.icon" :class="[$page.url.startsWith(link.matchUrl) ? 'text-orange-600' : 'text-gray-400 group-hover:text-gray-600', 'w-4 h-4 transition-colors shrink-0']" />
            <span>{{ link.label }}</span>
          </Link>
        </div>
<<<<<<< HEAD
      </div>

      <div class="border-t border-gray-100 bg-white">
        
        <div v-if="isAdmin" class="p-4 border-b border-gray-100 space-y-1">
          <div class="px-4 pb-2 text-[10px] font-bold text-gray-400 uppercase tracking-widest">Admin</div>
          <Link
            href="/admin/dashboard"
            :class="[
              $page.url.startsWith('/admin') 
                ? 'bg-orange-50 text-orange-600 font-semibold' 
                : 'text-gray-600 hover:bg-orange-50 hover:text-orange-600',
              'flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 group'
            ]"
          >
            <ShieldIcon class="w-4 h-4 shrink-0 text-orange-500" />
            <span>Admin Panel</span>
          </Link>
        </div>

        <div class="p-4">
          <button
            @click="logout"
            class="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-rose-600 hover:bg-rose-50 rounded-xl transition-all duration-150"
          >
            <LogOutIcon class="w-4 h-4 shrink-0" />
            <span>Logout</span>
          </button>
        </div>
=======
      <div class="p-4 border-t border-gray-100 bg-white">
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-4 py-2.5 text-sm font-medium text-rose-600 hover:bg-rose-50 rounded-xl transition-all duration-150"
        >
          <LogOutIcon class="w-4 h-4 shrink-0" />
          <span>Logout</span>
        </button>
>>>>>>> 48ceb19e08c75d6d5a3d74d1f9d037c8235d644e
      </div>
    </nav>

    <div class="flex-1 flex flex-col min-w-0">
      <header class="bg-white border-b border-gray-100 h-20 px-8 flex justify-between items-center shrink-0">
        <h1 class="text-xl font-bold text-gray-900 tracking-tight">{{ pageTitle || 'Overview' }}</h1>
        
        <div class="flex items-center gap-4">
<<<<<<< HEAD
            <div class="relative">
                <button @click="showNotifications = !showNotifications" 
                        class="relative p-2 text-amber-400 hover:text-amber-500 transition text-2xl">
                    🔔
                    <span v-if="notifications.length > 0" 
                          class="absolute top-0 right-0 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white border-2 border-white">
                        {{ notifications.length }}
                    </span>
                </button>

                <div v-if="showNotifications" class="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50">
                    <div class="p-3 border-b border-gray-100 font-bold text-sm text-gray-900">Notifications</div>
                    <div class="max-h-64 overflow-y-auto">
                        <div v-for="note in notifications" :key="note.id" class="p-4 border-b border-gray-50 hover:bg-gray-50 flex justify-between items-center">
                            <p class="text-sm text-gray-700">{{ note.message }}</p>
                            <button @click="markAsRead(note.id)" class="ml-2 text-[10px] text-orange-600 font-bold uppercase hover:underline">Read</button>
                        </div>
                        <div v-if="notifications.length === 0" class="p-4 text-center text-sm text-gray-400">No new notifications</div>
                    </div>
                </div>
            </div>

            <span class="text-sm font-medium text-gray-700 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100">{{ user?.name ?? 'Guest' }}</span>
=======
          <div class="p-2 text-gray-400 hover:text-orange-600 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
            <NotificationBell />
          </div>
          
          <div class="h-4 w-px bg-gray-200"></div>
          
          <Link
    :href="route('settings.index')"
    class="text-sm font-medium text-gray-700 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-100 hover:bg-orange-50 hover:text-orange-600 transition"
>
    {{ userName ?? 'Guest Account' }}
</Link>
>>>>>>> 48ceb19e08c75d6d5a3d74d1f9d037c8235d644e
        </div>
      </header>
      <main class="flex-1 overflow-auto bg-gray-50/50">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { router, Link, usePage } from '@inertiajs/vue3';
import { 
  LayoutDashboard, BookOpen, Users, Award, User, Settings, LogOut as LogOutIcon, Shield as ShieldIcon 
} from 'lucide-vue-next';

defineProps({ pageTitle: String });

const page = usePage();
const user = computed(() => page.props.auth?.user || null);
const isAdmin = computed(() => !!user.value?.is_admin);
const notifications = computed(() => page.props.notifications || []);
const showNotifications = ref(false);

const markAsRead = (id) => {
  router.post(route('notifications.markAsRead', id), {}, {
    preserveScroll: true,
  });
};

// Polling: Refresh notifications every 5 seconds
onMounted(() => {
    setInterval(() => {
        router.reload({ only: ['notifications'], preserveState: true });
    }, 5000); 
});

<<<<<<< HEAD
const generalLinks = [
  { id: 1, label: 'Dashboard', href: route('dashboard'), matchUrl: '/dashboard', icon: LayoutDashboard },
  { id: 2, label: 'Topics',    href: route('topics.index'), matchUrl: '/topics', icon: BookOpen },
  { id: 3, label: 'Groups',    href: route('groups.index'), matchUrl: '/groups', icon: Users },
  { id: 4, label: 'Quizzes',   href: route('quizzes.index'), matchUrl: '/quizzes', icon: Award },
  { id: 5, label: 'Profile',   href: route('profile.edit'), matchUrl: '/profile', icon: User },
  { id: 6, label: 'Settings',  href: route('settings.edit'), matchUrl: '/settings', icon: Settings }
];
=======


// FIXED: We now use standard Laravel named route configurations instead of hardcoded paths
const navLinks = computed(() => [
  { id: 1, label: 'Dashboard', href: route('dashboard'),    matchUrl: '/dashboard', icon: LayoutDashboardIcon },
  { id: 3, label: 'Groups',    href: route('groups.index'), matchUrl: '/groups',    icon: UsersIcon },
  { id: 4, label: 'Quizzes',   href: route('quizzes.index'),matchUrl: '/quizzes',   icon: AwardIcon },
  { id: 5, label: 'Profile',   href: route('profile.edit'), matchUrl: '/profile',   icon: UserIcon },
  { id: 6, label: 'Settings',  href: route('settings.index'),matchUrl: '/settings',  icon: SettingsIcon }

]);
>>>>>>> 48ceb19e08c75d6d5a3d74d1f9d037c8235d644e

const logout = () => {
  router.post(route('logout'), {}, {
    preserveState: false,
    preserveScroll: false,
  });
};
</script>