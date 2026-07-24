<template>
  <div class="flex h-screen bg-slate-50">
    <aside class="w-64 bg-slate-900 text-white flex flex-col shrink-0">
      <div class="p-6 font-bold text-xl tracking-tight border-b border-slate-800">
        Admin Portal
      </div>
      
      <nav class="flex flex-col flex-1 p-4 space-y-1">
        <Link
          href="/admin/dashboard"
          :class="[$page.url === '/admin/dashboard' ? 'bg-orange-600 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white', 'block px-4 py-2.5 rounded-lg transition-colors font-medium']"
        >
          Dashboard
        </Link>
        <Link
          href="/admin/users"
          :class="[$page.url.startsWith('/admin/users') ? 'bg-orange-600 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white', 'block px-4 py-2.5 rounded-lg transition-colors font-medium']"
        >
          User Management
        </Link>
        <Link
          href="/admin/moderation"
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

    <div class="flex-1 flex flex-col overflow-hidden">
      <header class="h-16 bg-white border-b border-slate-200 flex items-center px-8 shadow-sm">
        <h2 class="font-semibold text-slate-700">Admin Section</h2>
      </header>
      <main class="flex-1 overflow-auto p-8">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { Link, usePage } from '@inertiajs/vue3';
import { ArrowLeft } from 'lucide-vue-next';
const $page = usePage();
</script>
