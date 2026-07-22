<template>
  <GuestLayout>
    <div>
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Sign In</h2>

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
            class="w-4 h-4 text-blue-600 rounded"
          />
          <label class="ml-2 text-sm text-gray-700">
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
        <p class="text-sm text-gray-600">
          Don't have an account?
          <Link :href="route('register')" class="text-blue-600 hover:underline font-medium">
            Create one here
          </Link>
        </p>
        <p class="text-sm">
          <Link :href="route('dashboard')" class="text-blue-600 hover:underline">
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