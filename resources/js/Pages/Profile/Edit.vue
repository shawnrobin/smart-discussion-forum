<script setup>
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import { Head } from '@inertiajs/vue3';
import { ref } from 'vue';

// Placeholder/dummy data — will come from backend later
const name = ref('John Doe');
const email = ref('john@example.com');
const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
</script>

<template>
    <Head title="Profile & Settings" />

    <AuthenticatedLayout>
        <template #header>
            <h2 class="font-semibold text-xl text-gray-800 leading-tight">
                Profile & Settings
            </h2>
        </template>

        <div class="py-12">
            <div class="max-w-3xl mx-auto sm:px-6 lg:px-8 space-y-6">

                <!-- Profile Information -->
                <div class="p-4 sm:p-8 bg-white shadow sm:rounded-lg">
                    <h2 class="text-lg font-medium text-gray-900">Profile Information</h2>
                    <p class="mt-1 text-sm text-gray-600">
                        Update your account's profile information.
                    </p>

                    <form class="mt-6 space-y-6" @submit.prevent>
                        <div>
                            <label class="block text-sm font-medium text-gray-700">Name</label>
                            <input
                                type="text"
                                v-model="name"
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700">Email</label>
                            <input
                                type="email"
                                v-model="email"
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>

                        <div class="flex items-center gap-4">
                            <button
                                type="submit"
                                class="inline-flex items-center px-4 py-2 bg-gray-800 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-gray-700"
                            >
                                Save
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Update Password -->
                <div class="p-4 sm:p-8 bg-white shadow sm:rounded-lg">
                    <h2 class="text-lg font-medium text-gray-900">Update Password</h2>
                    <p class="mt-1 text-sm text-gray-600">
                        Ensure your account is using a long, random password.
                    </p>

                    <form class="mt-6 space-y-6" @submit.prevent>
                        <div>
                            <label class="block text-sm font-medium text-gray-700">Current Password</label>
                            <input
                                type="password"
                                v-model="currentPassword"
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700">New Password</label>
                            <input
                                type="password"
                                v-model="newPassword"
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700">Confirm Password</label>
                            <input
                                type="password"
                                v-model="confirmPassword"
                                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>

                        <div class="flex items-center gap-4">
                            <button
                                type="submit"
                                class="inline-flex items-center px-4 py-2 bg-gray-800 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-gray-700"
                            >
                                Save
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Delete Account -->
                <div class="p-4 sm:p-8 bg-white shadow sm:rounded-lg">
                    <h2 class="text-lg font-medium text-gray-900">Delete Account</h2>
                    <p class="mt-1 text-sm text-gray-600">
                        Once your account is deleted, all its data will be permanently removed.
                    </p>

                    <button
                        class="mt-4 inline-flex items-center px-4 py-2 bg-red-600 border border-transparent rounded-md font-semibold text-xs text-white uppercase tracking-widest hover:bg-red-500"
                    >
                        Delete Account
                    </button>
                </div>

            </div>
        </div>
    </AuthenticatedLayout>
</template>