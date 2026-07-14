<template>
  <AdminLayout>
    <Head title="User Management" />

    <div class="max-w-5xl mx-auto">
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-slate-900">User Management</h1>
        
        <div class="relative">
            <Search class="absolute left-3 top-3 w-4 h-4 text-slate-400" />
            <input 
                v-model="search" 
                type="text" 
                placeholder="Search users..." 
                class="pl-10 pr-4 py-2 border border-slate-200 rounded-xl focus:ring-2 focus:ring-orange-500 outline-none w-64"
            >
        </div>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
        <table class="min-w-full">
          <thead>
            <tr class="bg-slate-50 border-b border-slate-100">
              <th class="px-6 py-4 text-left text-xs font-bold text-slate-400 uppercase">User</th>
              <th class="px-6 py-4 text-left text-xs font-bold text-slate-400 uppercase">Status</th>
              <th class="px-6 py-4 text-right text-xs font-bold text-slate-400 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="u in users" :key="u.id" class="hover:bg-slate-50 transition">
              <td class="px-6 py-4">
                <div class="flex items-center">
                    <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center mr-3">
                        <UserIcon class="w-4 h-4 text-orange-600" />
                    </div>
                    <div>
                        <div class="font-bold text-slate-900">{{ u.name }}</div>
                        <div class="text-xs text-slate-500">{{ u.email }}</div>
                    </div>
                </div>
              </td>
              <td class="px-6 py-4">
                <div class="flex flex-col gap-1">
                    <span :class="u.blacklisted ? 'text-orange-700 bg-orange-50' : 'text-emerald-700 bg-emerald-50'" 
                          class="px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider w-max">
                      {{ u.blacklisted ? 'Blacklisted' : 'Active' }}
                    </span>
                    <span v-if="u.is_admin" class="text-indigo-700 bg-indigo-50 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider w-max">
                        Admin
                    </span>
                </div>
              </td>
              <td class="px-6 py-4 text-right space-x-4">
                <button 
                    @click="toggleAdmin(u.id)"
                    :class="u.is_admin ? 'text-indigo-600 hover:text-indigo-800' : 'text-slate-500 hover:text-slate-700'"
                    class="font-bold text-xs flex items-center inline-flex"
                >
                    {{ u.is_admin ? 'Revoke Admin' : 'Make Admin' }}
                </button>

                <Link :href="`/admin/users/${u.id}/warn`" method="post" as="button" class="text-amber-600 hover:text-amber-800 font-bold text-xs flex items-center inline-flex">
                    <AlertTriangle class="w-3 h-3 mr-1" /> Warn
                </Link>
                <Link :href="`/admin/users/${u.id}/toggle-blacklist`" method="post" as="button" class="text-orange-600 hover:text-orange-800 font-bold text-xs flex items-center inline-flex">
                    <Ban class="w-3 h-3 mr-1" /> {{ u.blacklisted ? 'Unban' : 'Blacklist' }}
                </Link>
                <Link :href="`/admin/users/${u.id}`" class="text-slate-500 hover:text-slate-700 font-bold text-xs flex items-center inline-flex">
                    <Eye class="w-3 h-3 mr-1" /> View
                </Link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, watch } from 'vue';
import { router, Link, Head } from '@inertiajs/vue3';
import { Search, User as UserIcon, AlertTriangle, Eye, Ban } from 'lucide-vue-next';
import AdminLayout from '@/Layouts/AdminLayout.vue';

const props = defineProps({ users: Array, filters: Object });
const search = ref(props.filters?.search || '');

// Automatically search as you type
watch(search, (value) => {
    router.get('/admin/users', { search: value }, { 
        preserveState: true, 
        replace: true 
    });
});

const toggleAdmin = (id) => {
    router.post(route('admin.users.toggle-admin', id), {}, {
        preserveScroll: true,
    });
};
</script>