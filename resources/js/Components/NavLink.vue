<template>
  <Link
    :href="href"
    class="block px-4 py-2 text-gray-700 hover:bg-blue-50 hover:text-blue-600 rounded-lg transition mb-1"
  >
    {{ label }}
  </Link>
</template>

<script setup>
import { Link } from '@inertiajs/vue3';

defineProps({
  href: String,
  label: String
});
</script>