<template>
  <GuestLayout>
    <div>
      <h2 class="text-2xl font-bold text-gray-900 mb-6">Create Account</h2>

      <!-- Error Alert -->
      <Alert
        v-if="form.errors.email"
        :message="form.errors.email"
        type="error"
      />

      <!-- Registration Form -->
      <form @submit.prevent="submit" class="space-y-4">
        <!-- Name Input -->
        <TextInput
          v-model="form.name"
          label="Full Name"
          placeholder="John Doe"
          :error="form.errors.name"
        />

        <!-- Email Input -->
        <TextInput
          v-model="form.email"
          label="Email Address"
          type="email"
          placeholder="your@email.com"
          :error="form.errors.email"
        />
        <TextInput
           v-model="form.username"
           label="Username"
           placeholder="Choose a username"
           :error="form.errors.username"
/>

        <!-- Password Input -->
        <PasswordInput
          v-model="form.password"
          label="Password (minimum 8 characters)"
          placeholder="Enter strong password"
          :error="form.errors.password"
        />

        <!-- Confirm Password -->
        <TextInput
          v-model="form.password_confirmation"
          label="Confirm Password"
          type="password"
          placeholder="Confirm your password"
          :error="form.errors.password_confirmation"
        />
        <!-- Agree to Rules Checkbox -->
        <div class="flex items-center">
          <input
            v-model="form.agreed_to_rules"
            type="checkbox"
            id="agree"
            class="mr-2"
          />
          <label for="agree" class="text-gray-700">
            I agree to the
            <Link :href="route('rules')" class="text-blue-600 hover:underline font-medium">
              rules and guidelines
            </Link>
          </label>
        </div>

        <!-- Submit Button -->
        <PrimaryButton
          type="submit"
          :disabled="form.processing"
          class="w-full"
        >
          {{ form.processing ? 'Creating account...' : 'Create Account' }}
        </PrimaryButton>
      </form>

      <!-- Links -->
      <div class="mt-6 text-center text-sm text-gray-600">
        <p>
          Already have an account?
          <Link :href="route('login')" class="text-blue-600 hover:underline font-medium">
            Sign in here
          </Link>
        </p>
      </div>
    </div>
  </GuestLayout>
</template>

<script setup>
import GuestLayout from '@/Layouts/GuestLayout.vue';
import TextInput from '@/Components/TextInput.vue';
import PasswordInput from '@/Components/PasswordInput.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';
import Alert from '@/Components/Alert.vue';
import { Link, useForm } from '@inertiajs/vue3';

const form = useForm({
  name: '',
  email: '',
  username: '',
  password: '',
  password_confirmation: '',
});

const submit = () => {
  form.post(route('register'), {
    onFinish: () => form.reset('password', 'password_confirmation'),
  });
};
</script>
