<template>
  <div class="relative min-h-dvh overflow-x-hidden bg-slate-50 md:flex">
    <div v-if="sidebarOpen" class="absolute inset-0 z-30 bg-slate-950/50 md:hidden" @click="sidebarOpen = false"></div>
    <aside
      :class="[
        sidebarOpen ? 'translate-x-0' : '-translate-x-full',
        'absolute inset-y-0 left-0 z-40 flex w-4/5 max-w-xs shrink-0 flex-col bg-slate-900 text-white shadow-xl transition-transform duration-200 md:relative md:min-h-dvh md:w-64 md:max-w-none md:translate-x-0 md:shadow-none'
      ]"
    >
      <div class="flex items-center justify-between border-b border-slate-800 p-5 text-xl font-bold tracking-tight">
        <span>Admin Portal</span>
        <button type="button" class="rounded-lg p-2 text-slate-300 hover:bg-slate-800 md:hidden" aria-label="Close navigation" @click="sidebarOpen = false">
          <X class="h-5 w-5" />
        </button>
      </div>
      
      <nav class="flex flex-col flex-1 p-4 space-y-1">
        <Link
          href="/admin/dashboard"
          @click="sidebarOpen = false"
          :class="[$page.url === '/admin/dashboard' ? 'bg-orange-600 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white', 'block px-4 py-2.5 rounded-lg transition-colors font-medium']"
        >
          Dashboard
        </Link>
        <Link
          href="/admin/users"
          @click="sidebarOpen = false"
          :class="[$page.url.startsWith('/admin/users') ? 'bg-orange-600 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white', 'block px-4 py-2.5 rounded-lg transition-colors font-medium']"
        >
          User Management
        </Link>
        <Link
          href="/admin/moderation"
          @click="sidebarOpen = false"
          :class="[$page.url === '/admin/moderation' ? 'bg-orange-600 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white', 'block px-4 py-2.5 rounded-lg transition-colors font-medium']"
        >
          Moderation
        </Link>
        
        <div class="mt-auto pt-8">
          <Link 
            href="/dashboard" 
            class="flex items-center gap-2 px-4 py-2 text-slate-400 hover:text-orange-400 transition-colors"
          >
            <ArrowLeft class="w-4 h-4" />
            <span>Return to Site</span>
          </Link>
        </div>
      </nav>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col">
      <header class="flex min-h-16 items-center border-b border-slate-200 bg-white px-4 shadow-sm sm:px-6 lg:px-8">
        <button type="button" class="mr-3 rounded-lg p-2 text-slate-600 hover:bg-slate-100 md:hidden" aria-label="Open navigation" @click="sidebarOpen = true">
          <Menu class="h-5 w-5" />
        </button>
        <h2 class="font-semibold text-slate-700">Admin Section</h2>
      </header>
      <main class="min-w-0 flex-1 p-4 sm:p-6 lg:p-8">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { Link, usePage } from '@inertiajs/vue3';
import { ref } from 'vue';
import { ArrowLeft, Menu, X } from 'lucide-vue-next';
const $page = usePage();
const sidebarOpen = ref(false);
</script>
