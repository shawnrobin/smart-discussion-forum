<template>
  <AuthenticatedLayout page-title="Quizzes" :user-name="auth.user.name">
    <div class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Active Quizzes -->
        <div class="bg-green-50 border border-green-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-green-900">Active</h3>
          <p class="text-3xl font-bold text-green-600 mt-2">{{ activeQuizzes.length }}</p>
        </div>

        <!-- Upcoming -->
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900">Upcoming</h3>
          <p class="text-3xl font-bold text-blue-600 mt-2">{{ upcomingQuizzes.length }}</p>
        </div>

        <!-- Completed -->
        <div class="bg-gray-50 border border-gray-200 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-gray-900">Completed</h3>
          <p class="text-3xl font-bold text-gray-600 mt-2">{{ completedQuizzes.length }}</p>
        </div>
      </div>

      <!-- Active Quizzes Section -->
      <Card title="Active Quizzes">
        <div class="space-y-4">
          <div
            v-for="quiz in activeQuizzes"
            :key="quiz.id"
            class="p-6 border border-green-200 bg-green-50 rounded-lg"
          >
            <div class="flex justify-between items-start">
              <div>
                <h3 class="text-lg font-semibold text-gray-900">{{ quiz.title }}</h3>
                <p class="text-sm text-gray-600 mt-1">by {{ quiz.instructor }}</p>
                <p class="text-sm text-gray-600 mt-2">
                  ⏱️ {{ quiz.duration }} minutes | 📋 {{ quiz.questions }} questions
                </p>
                <p class="text-xs text-red-600 mt-2">
                  ⏰ Closes: {{ quiz.closesAt }}
                </p>
              </div>
              <PrimaryButton @click="startQuiz(quiz.id)">
                Take Quiz
              </PrimaryButton>
            </div>
          </div>
        </div>
      </Card>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import Card from '@/Components/Card.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';

const auth = usePage().props.auth;

const activeQuizzes = ref([
  {
    id: 1,
    title: 'Database Design Basics',
    instructor: 'Dr. Smith',
    duration: 60,
    questions: 20,
    closesAt: 'Jan 15, 3:00 PM'
  }
]);

const upcomingQuizzes = ref([
  {
    id: 2,
    title: 'OOP Concepts',
    instructor: 'Prof. Johnson',
    startsAt: 'Jan 20, 10:00 AM'
  }
]);

const completedQuizzes = ref([
  {
    id: 3,
    title: 'SQL Fundamentals',
    score: '85/100',
    grade: 'A',
    completedAt: '2 days ago'
  }
]);

const startQuiz = (quizId) => {
  router.visit(`/quizzes/${quizId}/attempt`);
};
</script>