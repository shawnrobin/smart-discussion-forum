<template>
  <div
    v-if="visible"
    :class="[
      'p-4 rounded-lg mb-4 flex items-center justify-between',
      alertClasses
    ]"
  >
    <div class="flex items-center gap-2">
      <span :class="iconClass">
        {{ icon }}
      </span>
      <p>{{ message }}</p>
    </div>
    <button
      @click="visible = false"
      class="text-xl leading-none"
    >
      ×
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  message: String,
  type: {
    type: String,
    default: 'info' // 'success', 'error', 'warning', 'info'
  }
});

const visible = ref(true);

const alertClasses = computed(() => {
  const classes = {
    success: 'bg-green-100 text-green-800 border border-green-300',
    error: 'bg-red-100 text-red-800 border border-red-300',
    warning: 'bg-yellow-100 text-yellow-800 border border-yellow-300',
    info: 'bg-blue-100 text-blue-800 border border-blue-300'
  };
  return classes[props.type] || classes.info;
});

const icon = computed(() => {
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };
  return icons[props.type] || icons.info;
});

const iconClass = computed(() => {
  const classes = {
    success: 'text-green-600 font-bold',
    error: 'text-red-600 font-bold',
    warning: 'text-yellow-600 font-bold',
    info: 'text-blue-600 font-bold'
  };
  return classes[props.type] || classes.info;
});
</script>