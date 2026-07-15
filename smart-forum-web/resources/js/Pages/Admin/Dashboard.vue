<script setup>
import { Head, Link } from '@inertiajs/vue3'

defineProps({
    stats: Object
})
</script>

<template>
    <Head title="Admin Dashboard" />

    <div class="min-h-screen bg-gray-100 p-6 font-sans">
        <div class="max-w-7xl mx-auto">
            
            <div class="mb-8">
                <h1 class="text-2xl font-bold text-gray-800">Admin Dashboard</h1>
                <p class="text-sm text-gray-500 mt-1">Overview of system health, active items, and quick management links.</p>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                
                <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col justify-between hover:shadow-md transition">
                    <div>
                        <span class="text-xs font-semibold uppercase tracking-wider text-gray-400">Total Users</span>
                        <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.users }}</h3>
                    </div>
                    <div class="mt-4">
                        <Link href="/admin/users" class="text-xs font-medium text-blue-600 hover:text-blue-700 transition">
                            Manage Users &rarr;
                        </Link>
                    </div>
                </div>

                <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col justify-between hover:shadow-md transition">
                    <div>
                        <span class="text-xs font-semibold uppercase tracking-wider text-gray-400">Discussions</span>
                        <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.discussions }}</h3>
                    </div>
                    <div class="mt-4">
                        <span class="text-xs text-gray-400">Active forum topics</span>
                    </div>
                </div>

                <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col justify-between hover:shadow-md transition">
                    <div>
                        <span class="text-xs font-semibold uppercase tracking-wider text-gray-400">Pending Reports</span>
                        <h3 class="text-3xl font-bold mt-2" :class="stats.reports > 0 ? 'text-red-600 font-extrabold' : 'text-gray-900'">
                            {{ stats.reports }}
                        </h3>
                    </div>
                    <div class="mt-4">
                        <span class="text-xs font-medium" :class="stats.reports > 0 ? 'text-red-500' : 'text-gray-400'">
                            {{ stats.reports > 0 ? 'Requires moderation' : 'System clear' }}
                        </span>
                    </div>
                </div>

                <div class="p-6 bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col justify-between hover:shadow-md transition">
                    <div>
                        <span class="text-xs font-semibold uppercase tracking-wider text-gray-400">Blacklisted Users</span>
                        <h3 class="text-3xl font-bold text-gray-900 mt-2">{{ stats.blacklisted }}</h3>
                    </div>
                    <div class="mt-4">
                        <span class="text-xs text-gray-400">Restricted from accessing platform</span>
                    </div>
                </div>

            </div>

        </div>
    </div>
</template>