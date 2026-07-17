<script setup>
defineProps({ user: Object })
</script>

<template>
    <div class="p-10 max-w-4xl mx-auto bg-white shadow rounded-lg mt-10">
        <h1 class="text-3xl font-bold text-gray-800">User: {{ user.name }}</h1>
        <p class="text-lg text-gray-600 mt-2">Email: {{ user.email }}</p>
        
        <div class="mt-6">
            <a href="/admin/users" class="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700">
                Back to User List
            </a>
        </div>
    </div>
</template>