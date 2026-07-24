<template>
    <GuestLayout>
        <div>
            <div class="mb-6">
                <p class="text-xs font-bold uppercase tracking-widest text-orange-600">Join SmartForum</p>
                <h2 class="mt-1 text-2xl font-bold text-slate-900">Create your account</h2>
                <p class="mt-2 text-sm text-slate-500">Set up your profile to join academic discussions and groups.</p>
            </div>

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
                <label class="flex cursor-pointer items-start gap-3 rounded-xl border border-orange-100 bg-orange-50/60 p-4">
                    <input v-model="form.agreed_to_rules" type="checkbox" class="mt-0.5 h-4 w-4 rounded border-gray-300 text-orange-600 focus:ring-orange-500" />
                    <span class="text-sm leading-5 text-slate-700">
                        I have read and agree to the SmartForum community rules.
                        <Link :href="route('community.rules')" class="font-semibold text-orange-700 hover:text-orange-800 hover:underline" @click.stop>
                            Read the full rules
                        </Link>
                    </span>
                </label>

                <p v-if="form.errors.agreed_to_rules" class="text-sm text-rose-600">
                    {{ form.errors.agreed_to_rules }}
                </p>

                <!-- Submit Button -->
                <PrimaryButton
                    type="submit"
                    :disabled="form.processing"
                    class="w-full"
                >
                    {{
                        form.processing
                            ? "Creating account..."
                            : "Create Account"
                    }}
                </PrimaryButton>
            </form>

            <!-- Links -->
            <div class="mt-6 text-center text-sm text-slate-600">
                <p>
                    Already have an account?
                    <Link
                        :href="route('login')"
                        class="font-medium text-orange-600 hover:text-orange-700 hover:underline"
                    >
                        Sign in here
                    </Link>
                </p>
            </div>
        </div>
    </GuestLayout>
</template>

<script setup>
import GuestLayout from "@/Layouts/GuestLayout.vue";
import TextInput from "@/Components/TextInput.vue";
import PasswordInput from "@/Components/PasswordInput.vue";
import PrimaryButton from "@/Components/PrimaryButton.vue";
import Alert from "@/Components/Alert.vue";
import { Link, useForm } from "@inertiajs/vue3";

const form = useForm({
    name: "",
    email: "",
    username: "",
    password: "",
    password_confirmation: "",
    agreed_to_rules: false,
});

const submit = () => {
    form.post(route("register"), {
        onFinish: () => form.reset("password", "password_confirmation"),
    });
};
</script>
