<template>
  <GuestLayout>
    <div>
      <div class="mb-6">
        <p class="text-xs font-bold uppercase tracking-widest text-orange-600">Welcome back</p>
        <h2 class="mt-1 text-2xl font-bold text-slate-900">Sign in to your account</h2>
        <p class="mt-2 text-sm text-slate-500">Continue your academic discussions and quizzes.</p>
      </div>

      <!-- Error Alert -->
      <Alert
        v-if="form.errors.email"
        :message="form.errors.email"
        type="error"
      />

      <!-- Login Form -->
      <form @submit.prevent="submit" class="space-y-4">
        <!-- Email Input -->
        <TextInput
          v-model="form.email"
          label="Email Address"
          type="email"
          placeholder="your@email.com"
          :error="form.errors.email"
        />

        <!-- Password Input -->
        <PasswordInput
          v-model="form.password"
          label="Password"
          placeholder="Enter your password"
          :error="form.errors.password"
        />

        <!-- Remember Me -->
        <div class="flex items-center">
          <input
            v-model="form.remember"
            type="checkbox"
            class="h-4 w-4 rounded border-gray-300 text-orange-600 focus:ring-orange-500"
          />
          <label class="ml-2 text-sm text-slate-700">
            Remember me
          </label>
        </div>

        <!-- Submit Button -->
        <PrimaryButton
          type="submit"
          :disabled="form.processing"
          class="w-full"
        >
          {{ form.processing ? 'Signing in...' : 'Sign In' }}
        </PrimaryButton>
      </form>

      <section class="mt-8 rounded-xl border border-orange-100 bg-orange-50/60 p-4">
        <div class="flex items-start gap-3">
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-orange-100 text-sm font-bold text-orange-700">✓</div>
          <div>
            <h3 class="text-sm font-bold text-slate-900">Before you enter</h3>
            <p class="mt-1 text-xs leading-5 text-slate-600">SmartForum is a respectful academic community. By continuing, you are expected to:</p>
          </div>
        </div>
        <ul class="mt-4 space-y-2 text-xs leading-5 text-slate-700">
          <li class="flex gap-2"><span class="text-orange-600">•</span><span>Post relevant academic material in the correct group and topic.</span></li>
          <li class="flex gap-2"><span class="text-orange-600">•</span><span>Keep questions and follow-up answers together in their topic thread.</span></li>
          <li class="flex gap-2"><span class="text-orange-600">•</span><span>Treat every member with respect—no harassment or personal attacks.</span></li>
          <li class="flex gap-2"><span class="text-orange-600">•</span><span>Protect other members’ privacy and avoid sharing private information.</span></li>
          <li class="flex gap-2"><span class="text-orange-600">•</span><span>Follow quiz time limits and report inappropriate content instead of escalating a dispute.</span></li>
        </ul>
        <Link :href="route('community.rules')" class="mt-4 inline-flex text-xs font-semibold text-orange-700 hover:text-orange-800 hover:underline">
          Read the full community rules
        </Link>
      </section>

      <!-- Links -->
      <div class="mt-6 text-center space-y-2">
        <p class="text-sm text-slate-600">
          Don't have an account?
          <Link :href="route('register')" class="font-medium text-orange-600 hover:text-orange-700 hover:underline">
            Create one here
          </Link>
        </p>
        <p class="text-sm">
          <Link :href="route('dashboard')" class="text-orange-600 hover:text-orange-700 hover:underline">
            Back to home
          </Link>
        </p>
      </div>
    </div>
  </GuestLayout>
</template>

<script setup>
import { ref } from 'vue';
import GuestLayout from '@/Layouts/GuestLayout.vue';
import TextInput from '@/Components/TextInput.vue';
import PasswordInput from '@/Components/PasswordInput.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';
import Alert from '@/Components/Alert.vue';
import { Link, useForm } from '@inertiajs/vue3';

const form = useForm({
  email: '',
  password: '',
  remember: false
});

const submit = () => {
  form.post(route('login'), {
    onFinish: () => form.reset('password'),
  });
};
</script>
