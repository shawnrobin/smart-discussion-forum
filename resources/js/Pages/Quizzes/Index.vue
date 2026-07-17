<template>
  <AuthenticatedLayout page-title="Quizzes" :user-name="auth.user.name">

    <div class="space-y-6">

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">

        <!-- Active -->
        <div
    @click="selectedFilter = 'active'"
    class="bg-green-50 border border-green-200 rounded-lg p-6 cursor-pointer hover:bg-green-100"
>
          <h3 class="text-lg font-semibold text-green-900">
            Active
          </h3>

          <p class="text-3xl font-bold text-green-600 mt-2">
            {{ activeQuizzes.length }}
          </p>
        </div>


        <!-- Upcoming -->
        <div
    @click="selectedFilter = 'upcoming'"
    class="bg-blue-50 border border-blue-200 rounded-lg p-6 cursor-pointer hover:bg-blue-100"
>
          <h3 class="text-lg font-semibold text-blue-900">
            Upcoming
          </h3>

          <p class="text-3xl font-bold text-blue-600 mt-2">
            {{ upcomingQuizzes.length }}
          </p>
        </div>


        <!-- Closed -->
        <div
    @click="selectedFilter = 'completed'"
    class="bg-gray-50 border border-gray-200 rounded-lg p-6 cursor-pointer hover:bg-gray-100"
>
          <h3 class="text-lg font-semibold text-gray-900">
            Completed
          </h3>

          <p class="text-3xl font-bold text-gray-600 mt-2">
            {{ completedQuizzes.length }}
          </p>
        </div>

      </div>



      <!-- Active Quizzes -->

      <Card :title="`${selectedFilter.charAt(0).toUpperCase() + selectedFilter.slice(1)} Quizzes`">

        <div class="space-y-4">


          <div
    v-for="quiz in displayedQuizzes"
    :key="quiz.id"
    :class="[
        'p-6 border rounded-lg',
        selectedFilter === 'active'
            ? 'border-green-200 bg-green-50'
            : selectedFilter === 'upcoming'
            ? 'border-blue-200 bg-blue-50'
            : 'border-gray-200 bg-gray-50'
    ]"
>

            <div class="flex justify-between items-start">


              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ quiz.title }}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-300 mt-1">by {{ quiz.instructor }}</p>
                <p class="text-sm text-gray-600 dark:text-gray-300 mt-2">
                  ⏱️ {{ quiz.duration }} minutes | 📋 {{ quiz.questions }} questions
                </p>
                <!-- Active quizzes -->
<p
    v-if="selectedFilter === 'active'"
    class="text-xs text-red-600 mt-2"
>
    ⏰ Closes:
    {{ formatDate(quiz.end_time) }}
</p>

<!-- Upcoming quizzes -->
<p
    v-else-if="selectedFilter === 'upcoming'"
    class="text-xs text-blue-600 mt-2"
>
    📅 Starts:
    {{ formatDate(quiz.start_time) }}
</p>

<!-- Completed quizzes -->
<p
    v-else
    class="text-xs text-gray-600 mt-2"
>
    ✅ Closed:
    {{ formatDate(quiz.end_time) }}
</p>


                

              </div>


              <PrimaryButton
    v-if="selectedFilter === 'active'"
    @click="startQuiz(quiz.id)"
>
    Take Quiz
</PrimaryButton>

<span
    v-else-if="selectedFilter === 'completed'"
    class="text-green-600 font-semibold"
>
    ✓ Completed
</span>

<span
    v-else
    class="text-blue-600 font-semibold"
>
    📅 Upcoming
</span>



              

            </div>


          </div>


          <p
    v-if="displayedQuizzes.length === 0"
    class="text-gray-500"
>
    No {{ selectedFilter.charAt(0).toUpperCase() + selectedFilter.slice(1) }} quizzes available.
</p>


        </div>


      </Card>


    </div>

  </AuthenticatedLayout>
</template>



<script setup>

import { computed, ref } from 'vue';
import { usePage, router } from '@inertiajs/vue3';

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';
import Card from '@/Components/Card.vue';
import PrimaryButton from '@/Components/PrimaryButton.vue';


const page = usePage();


const auth = page.props.auth;

const selectedFilter = ref('active');


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
        quiz => quiz.status === 'completed'
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


const displayedQuizzes = computed(() => {

    if (selectedFilter.value === 'active') {
        return activeQuizzes.value;
    }

    if (selectedFilter.value === 'upcoming') {
        return upcomingQuizzes.value;
    }

    if (selectedFilter.value === 'completed') {
        return completedQuizzes.value;
    }

    return props.quizzes;

});

</script>