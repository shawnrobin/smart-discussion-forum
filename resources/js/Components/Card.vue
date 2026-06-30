<template>
  <div class="bg-white rounded-lg shadow-md p-6 border border-gray-200">
    <div v-if="title" class="mb-4">
      <h2 class="text-xl font-semibold text-gray-900">{{ title }}</h2>
    </div>
    <slot></slot>
  </div>
</template>

<script setup>
defineProps({
  title: String
});
</script>