<script setup>
import { useForm } from '@inertiajs/vue3';
import { ref } from 'vue';

const props = defineProps({
    group: Object
});

const form = useForm({
    title: '',
    description: '',
    start_time: '',
    end_time: '',
    duration_minutes: 30,
    shuffle_questions: true,
    show_results_after: 'immediately',
    questions: [
        {
            question_text: '',
            question_type: 'multiple_choice',
            options: ['', '', '', ''],
            correct_answer: '',
            points: 5
        }
    ]
});

const addQuestion = () => {
    form.questions.push({
        question_text: '',
        question_type: 'multiple_choice',
        options: ['', '', '', ''],
        correct_answer: '',
        points: 5
    });
};

const removeQuestion = (index) => {
    form.questions.splice(index, 1);
};

const submit = () => {
    form.post(route('quizzes.store', props.group.id));
};
</script>

<template>
    <div class="max-w-4xl mx-auto p-6 bg-white shadow-md rounded-lg mt-10">
        <h1 class="text-2xl font-bold mb-6">Create New Quiz for {{ group.name }}</h1>

        <form @submit.prevent="submit">
            <div class="grid grid-cols-1 gap-4 mb-8">
                <div>
                    <label class="block font-medium">Quiz Title</label>
                    <input v-model="form.title" type="text" class="w-full border rounded p-2" required>
                </div>
                <div>
                    <label class="block font-medium">Description</label>
                    <textarea v-model="form.description" class="w-full border rounded p-2"></textarea>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <div>
                        <label class="block font-medium">Start Time</label>
                        <input v-model="form.start_time" type="datetime-local" class="w-full border rounded p-2" required>
                    </div>
                    <div>
                        <label class="block font-medium">End Time</label>
                        <input v-model="form.end_time" type="datetime-local" class="w-full border rounded p-2" required>
                    </div>
                </div>
                <div>
                    <label class="block font-medium">Duration (Minutes)</label>
                    <input v-model="form.duration_minutes" type="number" class="w-1/4 border rounded p-2" required>
                </div>
            </div>

            <hr class="my-8">

            <h2 class="text-xl font-semibold mb-4">Questions</h2>
            <div v-for="(question, index) in form.questions" :key="index" class="p-4 border rounded mb-6 bg-gray-50 relative">
                <button @click.prevent="removeQuestion(index)" class="absolute top-2 right-2 text-red-500 font-bold">X</button>
                
                <div class="mb-4">
                    <label class="block font-medium">Question {{ index + 1 }}</label>
                    <input v-model="question.question_text" type="text" class="w-full border rounded p-2" placeholder="Enter question text" required>
                </div>

                <div class="grid grid-cols-2 gap-4 mb-4">
                    <div>
                        <label class="block font-medium">Type</label>
                        <select v-model="question.question_type" class="w-full border rounded p-2">
                            <option value="multiple_choice">Multiple Choice</option>
                            <option value="true_false">True/False</option>
                            <option value="short_answer">Short Answer</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium">Points</label>
                        <input v-model="question.points" type="number" class="w-full border rounded p-2">
                    </div>
                </div>

                <div v-if="question.question_type === 'multiple_choice'" class="mb-4 grid grid-cols-2 gap-2">
                    <div v-for="(opt, oIdx) in 4" :key="oIdx">
                        <label class="text-sm">Option {{ oIdx + 1 }}</label>
                        <input v-model="question.options[oIdx]" type="text" class="w-full border rounded p-1 text-sm" placeholder="Option text">
                    </div>
                </div>

                <div>
                    <label class="block font-medium">Correct Answer</label>
                    <input v-model="question.correct_answer" type="text" class="w-full border rounded p-2 bg-green-50" placeholder="Must match one of the options exactly" required>
                </div>
            </div>

            <div class="flex justify-between items-center mt-8">
                <button @click.prevent="addQuestion" type="button" class="bg-blue-500 text-white px-4 py-2 rounded">
                    + Add Another Question
                </button>
                <button type="submit" :disabled="form.processing" class="bg-green-600 text-white px-8 py-3 rounded font-bold">
                    Create and Publish Quiz
                </button>
            </div>
        </form>
    </div>
</template>