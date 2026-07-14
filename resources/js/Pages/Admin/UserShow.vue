<script setup>
import { Head, Link } from '@inertiajs/vue3'

defineProps({
    user: Object
})
</script>

<template>
    <Head :title="`User Profile - ${user.name}`" />

    <div class="min-h-screen bg-gray-100 p-6 font-sans">
        <div class="max-w-3xl mx-auto">
            
            <div class="mb-6">
                <Link 
                    href="/admin/users" 
                    class="text-sm font-semibold text-blue-600 hover:text-blue-700 transition"
                >
                    &larr; Back to User Management
                </Link>
            </div>

            <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden p-6">
                <h1 class="text-2xl font-bold text-gray-800 mb-4">User Details</h1>
                
                <div class="space-y-4">
                    <div>
                        <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400">User ID</label>
                        <p class="text-gray-900 mt-1 font-mono text-sm">#{{ user.id }}</p>
                    </div>
                    
                    <hr class="border-gray-100" />
                    
                    <div>
                        <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400">Name</label>
                        <p class="text-gray-900 mt-1 text-base font-medium">{{ user.name }}</p>
                    </div>

                    <hr class="border-gray-100" />

                    <div>
                        <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400">Email Address</label>
                        <p class="text-gray-900 mt-1 text-base">{{ user.email }}</p>
                    </div>

                    <hr class="border-gray-100" />

                    <div>
                        <label class="block text-xs font-semibold uppercase tracking-wider text-gray-400">Account Status</label>
                        <div class="mt-2">
                            <span v-if="user.blacklisted" class="px-3 py-1 text-xs font-bold rounded-full bg-red-100 text-red-800">
                                Blacklisted / Restricted
                            </span>
                            <span v-else class="px-3 py-1 text-xs font-bold rounded-full bg-green-100 text-green-800">
                                Active Account
                            </span>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>
</template>