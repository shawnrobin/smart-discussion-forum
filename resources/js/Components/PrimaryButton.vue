<template>
  <button
    v-bind="$attrs"
    :type="type"
    :disabled="disabled"
    class="rounded-lg bg-orange-600 px-4 py-2 font-medium text-white transition duration-200 hover:bg-orange-700 disabled:cursor-not-allowed disabled:opacity-50"
  >
    <slot></slot>
  </button>
</template>


<script setup>

defineOptions({
  inheritAttrs: false
});


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
