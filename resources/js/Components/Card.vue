<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-6 border border-gray-200 dark:border-gray-700">
    <div v-if="title" class="mb-4">
      <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ title }}</h2>
    </div>
    <slot></slot>
  </div>
</template>

<script setup>
defineProps({
  title: String
});
</script>