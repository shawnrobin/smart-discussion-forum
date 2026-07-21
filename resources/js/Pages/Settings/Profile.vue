<template>
  <AuthenticatedLayout page-title="Profile Settings">
    <div class="max-w-4xl mx-auto p-6 space-y-6">

      <!-- 1. Header Section -->
      <ProfileHeader
        :name="name"
        :email="email"
        :gamification="gamification"
      />

      <!-- 2. Academic Performance Section -->
      <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
        <h2 class="text-lg font-bold text-gray-900 mb-4">Academic & Quiz Performance</h2>
        <div class="overflow-hidden border border-gray-100 rounded-xl">
          <table class="min-w-full divide-y divide-gray-100 text-left text-sm">
            <thead class="bg-gray-50 font-semibold text-gray-700">
              <tr>
                <th class="px-4 py-3">Evaluation Element</th>
                <th class="px-4 py-3">Completed</th>
                <th class="px-4 py-3 text-right">Resulting Score</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100 text-gray-600">
              <tr v-for="exam in exams" :key="exam.id">
                <td class="px-4 py-3 font-medium text-gray-900">{{ exam.title }}</td>
                <td class="px-4 py-3 text-xs text-gray-400">{{ exam.date }}</td>
                <td class="px-4 py-3 text-right font-bold text-emerald-600">{{ exam.score }}%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 3. Settings Profile Form -->
      <div class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm">
        <UpdateProfileInformationForm />
      </div>

    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { usePage } from '@inertiajs/vue3';
import { computed, ref } from 'vue';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import ProfileHeader from '@/Components/ProfileHeader.vue';
import UpdateProfileInformationForm from '@/Pages/Profile/Partials/UpdateProfileInformationForm.vue';

// Data
const user = usePage().props.auth.user;
const name = computed(() => user.name);
const email = computed(() => user.email);
const gamification = ref({ level: 5, totalPoints: 1250, verifiedAnswers: 14, weeklyStreak: 6 });
const exams = ref([
  { id: 1, title: 'Introduction to Algorithms Quiz', date: 'July 01, 2026', score: 92 },
  { id: 2, title: 'Data Structures Mid-Term Assignment', date: 'June 24, 2026', score: 88 }
]);
</script>
