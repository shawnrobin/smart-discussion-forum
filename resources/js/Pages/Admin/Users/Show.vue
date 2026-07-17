<script setup>
import { Head, router } from '@inertiajs/vue3'
import { User, Mail, Calendar, AlertTriangle, Shield, ArrowLeft, Ban } from 'lucide-vue-next'

const props = defineProps({ user: Object })

const performAction = (action) => {
    if (confirm(`Confirm ${action} for ${props.user.name}?`)) {
        router.post(`/admin/users/${props.user.id}/${action}`);
    }
}
</script>

<template>
    <Head :title="'Profile: ' + user.name" />

    <div class="min-h-screen bg-slate-50 p-6 md:p-12">
        <div class="max-w-3xl mx-auto">
            
            <button @click="router.visit('/admin/users')" class="flex items-center text-slate-500 hover:text-orange-600 transition mb-6 font-medium">
                <ArrowLeft class="w-4 h-4 mr-2" /> Back to User List
            </button>

            <div class="bg-white rounded-3xl shadow-sm border border-slate-100 overflow-hidden">
                <!-- Cool Orange Decorative Header -->
                <div class="h-24 bg-gradient-to-r from-orange-500 to-amber-500"></div>
                
                <div class="px-8 pb-8">
                    <div class="relative -mt-12 mb-4">
                        <div class="w-24 h-24 bg-white rounded-full border-4 border-white shadow-md flex items-center justify-center text-orange-500">
                            <User class="w-12 h-12" />
                        </div>
                    </div>

                    <h1 class="text-3xl font-extrabold text-slate-900">{{ user.name }}</h1>
                    <div class="flex items-center text-slate-500 mt-1 mb-6">
                        <Mail class="w-4 h-4 mr-2" /> {{ user.email }}
                    </div>

                    <div class="grid grid-cols-2 gap-4">
                        <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                            <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Status</p>
                            <div :class="user.blacklisted ? 'text-orange-700 bg-orange-100' : 'text-emerald-700 bg-emerald-100'" 
                                 class="inline-flex items-center mt-2 px-3 py-1 rounded-full text-xs font-bold">
                                <Shield class="w-3 h-3 mr-1" /> {{ user.blacklisted ? 'Blacklisted' : 'Active' }}
                            </div>
                        </div>
                        <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                            <p class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Joined</p>
                            <div class="flex items-center mt-2 text-slate-700 font-semibold">
                                <Calendar class="w-4 h-4 mr-2 text-orange-400" />
                                {{ new Date(user.created_at).toLocaleDateString() }}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Styled Action Buttons -->
                <div class="bg-slate-50 px-8 py-6 flex gap-4">
                    <button @click="performAction('warn')" 
                            class="flex-1 flex items-center justify-center bg-white border border-orange-200 text-orange-600 hover:bg-orange-50 py-3 rounded-xl font-bold transition shadow-sm">
                        <AlertTriangle class="w-4 h-4 mr-2" /> Issue Warning
                    </button>
                    <button @click="performAction('toggle-blacklist')" 
                            :class="user.blacklisted ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-orange-600 hover:bg-orange-700'"
                            class="flex-1 flex items-center justify-center text-white py-3 rounded-xl font-bold transition shadow-sm">
                        <Ban class="w-4 h-4 mr-2" /> {{ user.blacklisted ? 'Unban User' : 'Blacklist User' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>