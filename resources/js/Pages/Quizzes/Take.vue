<template>

<AuthenticatedLayout
    page-title="Take Quiz"
    :user-name="auth.user.name"
>


<div class="max-w-4xl mx-auto">

    <h1 class="text-3xl font-bold mb-2">
        {{ quiz.title }}
    </h1>


    <p class="text-gray-600 mb-6">
        {{ quiz.description }}
    </p>



    <!-- Questions -->

    <div
        v-for="(question,index) in quiz.questions"
        :key="question.id"
        class="border rounded-lg p-6 mb-6"
    >


        <h3 class="font-semibold mb-4">

            {{ index + 1 }}.
            {{ question.question_text }}

        </h3>



        <!-- Multiple choice -->

        <div
            v-if="question.question_type === 'multiple_choice'"
            class="space-y-3"
        >


            <label
                v-for="(option,index) in question.options"
                :key="index"
                class="block"
            >

                <input
                    type="radio"
                    :name="'question_'+question.id"
                    :value="option"
                    v-model="answers[question.id]"
                >

                {{ option }}

            </label>


        </div>



        <!-- True / False -->

        <div
            v-else-if="question.question_type === 'true_false'"
            class="space-y-3"
        >

            <label class="block">

                <input
                    type="radio"
                    :name="'question_'+question.id"
                    value="true"
                    v-model="answers[question.id]"
                >

                True

            </label>



            <label class="block">

                <input
                    type="radio"
                    :name="'question_'+question.id"
                    value="false"
                    v-model="answers[question.id]"
                >

                False

            </label>


        </div>



        <!-- Short answer -->

        <div v-else>

            <input
                type="text"
                class="border rounded p-2 w-full"
                v-model="answers[question.id]"
            >

        </div>



    </div>




    <button
    class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded"
    @click="checkBeforeSubmit"
>
    Submit Quiz
</button>
 <!-- Incomplete Quiz Warning -->

<div
    v-if="showIncomplete"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
>

    <div class="bg-white rounded-lg p-6 shadow-lg">

        <h2 class="text-xl font-bold text-red-600 mb-4">
            Cannot Submit Quiz
        </h2>


        <p class="mb-6">
            You must answer all questions before submitting.
        </p>


        <button
            class="bg-blue-600 text-white px-5 py-2 rounded"
            @click="showIncomplete=false"
        >
            OK
        </button>

    </div>

</div>

<!-- Confirmation Modal -->

<div
    v-if="showConfirm"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
>

    <div class="bg-white rounded-lg p-6 shadow-lg">

        <h2 class="text-xl font-bold mb-4">
            Submit Quiz?
        </h2>

        <p class="mb-6">
            Are you sure you want to submit your answers?
            You cannot change them after submission.
        </p>


        <div class="flex gap-4">

           <button
    class="bg-green-600 text-white px-5 py-2 rounded disabled:opacity-50"
    @click="confirmSubmit"
    :disabled="submitting"
>
    {{ submitting ? 'Submitting...' : 'Yes, Submit' }}
</button>



            <button
                class="bg-gray-500 text-white px-5 py-2 rounded"
                @click="showConfirm=false"
            >
                No, Go Back
            </button>

        </div>

    </div>

</div>


</div>


</AuthenticatedLayout>

</template>



<script setup>


import { reactive, ref } from 'vue'
import { router, usePage } from '@inertiajs/vue3'

import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue'

const showConfirm = ref(false)
const showIncomplete = ref(false)

const props = defineProps({

    quiz:Object,

    attempt:Object

})



const auth = usePage().props.auth

const answers = reactive({})
const submitting = ref(false)


function checkBeforeSubmit(){

   const unanswered = props.quiz.questions.some(question => {

        return !answers[question.id] || 
               answers[question.id].trim() === ''

    });


    if(unanswered){

        showIncomplete.value = true;

        return;
    }


    showConfirm.value = true;

}

function confirmSubmit(){

    submitting.value = true;

    showConfirm.value = false;


    const formattedAnswers = Object.keys(answers)
        .map(questionId => ({
            question_id: questionId,
            user_answer: answers[questionId]
        }));


    router.post(
    route(
        'quizzes.submit',
        props.attempt.id
    ),
    {
        answers: formattedAnswers
    },
    {
        onFinish: () => {
            submitting.value = false;
        },

        onSuccess: () => {
            router.visit('/quizzes');
        }
    }
);

}




</script>