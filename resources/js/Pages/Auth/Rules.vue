<script setup>
import { Head, Link } from '@inertiajs/vue3';
import GuestLayout from '@/Layouts/GuestLayout.vue';

defineProps({
  rules: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <GuestLayout>
    <Head title="Community Rules" />

    <article>
      <p class="text-xs font-bold uppercase tracking-widest text-orange-600">Community standards</p>
      <h2 class="mt-1 text-2xl font-bold text-slate-900">{{ rules.title }}</h2>
      <p class="mt-2 text-sm leading-6 text-slate-500">These expectations help keep SmartForum safe, respectful, and useful for every learner.</p>

      <div class="mt-6 rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm leading-7 text-slate-700 whitespace-pre-line">
        {{ rules.content }}
      </div>

      <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-end">
        <Link :href="route('login')" class="inline-flex items-center justify-center rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
          Back to sign in
        </Link>
        <Link :href="route('register')" class="inline-flex items-center justify-center rounded-lg bg-orange-600 px-4 py-2 text-sm font-semibold text-white hover:bg-orange-700">
          Create an account
        </Link>
      </div>
    </article>
  </GuestLayout>
</template>
