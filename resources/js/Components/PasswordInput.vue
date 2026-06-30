<template>
  <div class="mb-4">
    <label class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
    </label>
    <input
      :type="showPassword ? 'text' : 'password'"
      :placeholder="placeholder"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
    />
    <button
      type="button"
      @click="showPassword = !showPassword"
      class="mt-1 text-sm text-blue-600 hover:text-blue-800"
    >
      {{ showPassword ? 'Hide' : 'Show' }}
    </button>
    <p v-if="error" class="mt-1 text-sm text-red-600">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  modelValue: String,
  label: String,
  placeholder: String,
  error: String
});

defineEmits(['update:modelValue']);

const showPassword = ref(false);
</script>