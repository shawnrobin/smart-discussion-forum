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
      class="w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-orange-500 focus:outline-none focus:ring-2 focus:ring-orange-500"
    />
    <button
      type="button"
      @click="showPassword = !showPassword"
      class="mt-1 text-sm text-orange-600 hover:text-orange-700"
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
