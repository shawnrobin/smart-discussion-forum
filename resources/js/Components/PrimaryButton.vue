<template>
  <button
    :type="type"
    :disabled="disabled"
    class="px-4 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-red-700 transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
  >
    <slot></slot>
  </button>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'button'
  },
  disabled: {
    type: Boolean,
    default: false
  }
});
</script>