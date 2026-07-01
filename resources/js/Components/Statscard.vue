<template>
  <div class="bg-white rounded-lg shadow-md p-6 border-l-4 border-blue-500">
    <div class="flex items-center justify-between">
      <div>
        <p class="text-gray-600 text-sm">{{ label }}</p>
        <p class="text-3xl font-bold text-gray-900 mt-2">{{ value }}</p>
      </div>
      <span class="text-4xl">{{ icon }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: Number,
  icon: String
});
</script>