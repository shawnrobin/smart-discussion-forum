<template>
  <div class="flex h-screen bg-gray-100">
    <!-- Sidebar Navigation -->
    <nav class="w-64 bg-white shadow-md">
      <div class="p-6 border-b">
        <h2 class="text-2xl font-bold text-blue-600">Smart Forum</h2>
      </div>

      <div class="p-4">
        <NavLink
          v-for="link in navLinks"
          :key="link.id"
          :href="link.href"
          :label="link.label"
        />
      </div>

      <!-- User Menu -->
      <div class="absolute bottom-0 left-0 right-0 p-4 border-t bg-white w-64">
        <button
          @click="logout"
          class="w-full px-4 py-2 text-left text-red-600 hover:bg-red-50 rounded-lg transition"
        >
          Logout
        </button>
      </div>
    </nav>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col">
      <!-- Top Bar -->
      <header class="bg-white shadow-sm p-6 flex justify-between items-center">
        <h1 class="text-2xl font-semibold text-gray-900">{{ pageTitle }}</h1>
        <div class="flex items-center gap-4">
          <button class="relative">
            🔔
            <span class="absolute top-0 right-0 w-2 h-2 bg-red-600 rounded-full"></span>
          </button>
          <span class="text-sm text-gray-600">{{ userName }}</span>
        </div>
      </header>

      <!-- Page Content -->
      <main class="flex-1 overflow-auto p-6">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { router } from '@inertiajs/vue3';
import NavLink from '@/Components/NavLink.vue';

defineProps({
  pageTitle: String,
  userName: String
});

const navLinks = ref([
  { id: 1, label: 'Dashboard', href: '/dashboard' },
  { id: 2, label: 'Groups', href: '/groups' },
  { id: 3, label: 'Quizzes', href: '/quizzes' },
  { id: 4, label: 'Profile', href: '/profile' },
  { id: 5, label: 'Settings', href: '/settings' }
]);

const logout = () => {
  router.post(route('logout'));
};
</script>