<template>
  <AuthenticatedLayout page-title="Quizzes" :user-name="auth.user.name">

    <div class="space-y-6">

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Active Quizzes -->
        <div class="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-green-900 dark:text-green-300">Active</h3>
          <p class="text-3xl font-bold text-green-600 dark:text-green-400 mt-2">{{ activeQuizzes.length }}</p>
        </div>


        <!-- Upcoming -->
        <div class="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-blue-900 dark:text-blue-300">Upcoming</h3>
          <p class="text-3xl font-bold text-blue-600 dark:text-blue-400 mt-2">{{ upcomingQuizzes.length }}</p>
        </div>

        <!-- Completed -->
        <div class="bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-lg p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Completed</h3>
          <p class="text-3xl font-bold text-gray-600 dark:text-gray-300 mt-2">{{ completedQuizzes.length }}</p>
        </div>

      </div>



      <!-- Active Quizzes -->

      <Card title="Active Quizzes">

        <div class="space-y-4">


          <div
            v-for="quiz in activeQuizzes"
            :key="quiz.id"
            class="p-6 border border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-900/20 rounded-lg"
          >

            <div class="flex justify-between items-start">


              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ quiz.title }}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-300 mt-1">by {{ quiz.instructor }}</p>
                <p class="text-sm text-gray-600 dark:text-gray-300 mt-2">
                  ⏱️ {{ quiz.duration }} minutes | 📋 {{ quiz.questions }} questions
                </p>
                <p class="text-xs text-red-600 dark:text-red-400 mt-2">
                  ⏰ Closes: {{ quiz.closesAt }}
                </p>

              </div>



              <PrimaryButton
                @click="startQuiz(quiz.id)"
              >
                Take Quiz
              </PrimaryButton>


            </div>


          </div>


          <p
            v-if="activeQuizzes.length === 0"
            class="text-gray-500"
          >
            No active quizzes available.
          </p>


        </div>


      </Card>


    </div>

  </AuthenticatedLayout>
</template>



<script setup>

import { computed } from 'vue';
import { usePage, router } from '@inertiajs/vue3';

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import Card from '@/Components/Card.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';



const page = usePage();


const auth = page.props.auth;



/*
|--------------------------------------------------------------------------
| Receive quizzes from Laravel controller
|--------------------------------------------------------------------------
*/

const props = defineProps({

    quizzes: {
        type: Array,
        default: () => []
    }

});



/*
|--------------------------------------------------------------------------
| Filter quizzes by status
|--------------------------------------------------------------------------
*/


const activeQuizzes = computed(() => {

    return props.quizzes.filter(
        quiz => quiz.status === 'active'
    );

});


const upcomingQuizzes = computed(() => {

    return props.quizzes.filter(
        quiz => quiz.status === 'upcoming'
    );

});


const completedQuizzes = computed(() => {

    return props.quizzes.filter(
        quiz => quiz.status === 'closed'
    );

});



/*
|--------------------------------------------------------------------------
| Start quiz
|--------------------------------------------------------------------------
*/

function startQuiz(quizId) {

    console.log("Opening quiz:", quizId);

    router.visit(
        route('quizzes.start', quizId)
    );

}




/*
|--------------------------------------------------------------------------
| Date formatter
|--------------------------------------------------------------------------
*/


const formatDate = (date) => {

    if (!date) {
        return '';
    }

    return new Date(date).toLocaleString();

};


</script>