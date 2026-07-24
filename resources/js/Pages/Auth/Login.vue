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
