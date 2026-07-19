<script setup>
import { useForm, router } from '@inertiajs/vue3';
import { Plus, Trash2, ArrowLeft, Clock, Award, AlertCircle, CheckCircle, Save } from 'lucide-vue-next';

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
    if (form.questions.length > 1) {
        form.questions.splice(index, 1);
    }
};

const submit = () => {
    form.post(route('quizzes.store', props.group.id));
};

const questionTypeOptions = ['multiple_choice', 'true_false', 'short_answer'];

// Pre-fill a suggested end_time 1 hour after start
const suggestEndTime = () => {
    if (form.start_time && !form.end_time) {
        const start = new Date(form.start_time);
        start.setHours(start.getHours() + 1);
        form.end_time = start.toISOString().slice(0, 16);
    }
};
</script>

<template>
    <div class="min-h-screen bg-gray-50 py-8 px-4">
        <div class="max-w-4xl mx-auto space-y-6">

            <!-- Header with Back Button -->
            <div class="flex items-center justify-between">
                <div>
                    <button @click="router.visit('/dashboard')" class="inline-flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-orange-600 transition-colors mb-2">
                        <ArrowLeft class="w-4 h-4" />
                        Back to Dashboard
                    </button>
                    <h1 class="text-2xl font-bold text-gray-900">Create New Quiz</h1>
                    <p class="text-sm text-gray-500 mt-1">for group: <span class="font-semibold text-gray-700">{{ group?.name || 'Group' }}</span></p>
                </div>
            </div>

            <!-- Error Alert -->
            <div v-if="Object.keys(form.errors).length > 0" class="bg-red-50 border border-red-200 rounded-xl p-4 flex items-start gap-3">
                <AlertCircle class="w-5 h-5 text-red-500 shrink-0 mt-0.5" />
                <div>
                    <p class="font-semibold text-red-800 text-sm">Please fix the following errors:</p>
                    <ul class="text-sm text-red-600 mt-1 list-disc list-inside">
                        <li v-for="(msg, field) in form.errors" :key="field">{{ msg }}</li>
                    </ul>
                </div>
            </div>

            <form @submit.prevent="submit" class="space-y-8">

                <!-- SECTION 1: Quiz Settings -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 space-y-5">
                    <div class="flex items-center gap-3 pb-4 border-b border-gray-100">
                        <div class="w-8 h-8 rounded-lg bg-orange-100 flex items-center justify-center">
                            <Award class="w-4 h-4 text-orange-600" />
                        </div>
                        <h2 class="text-lg font-bold text-gray-900">Quiz Settings</h2>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1.5">Quiz Title</label>
                        <input v-model="form.title" type="text" placeholder="e.g. Mid-Term Assessment" 
                               class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                               :class="{ 'border-red-400': form.errors.title }" required>
                        <p v-if="form.errors.title" class="text-xs text-red-500 mt-1">{{ form.errors.title }}</p>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1.5">Description <span class="text-gray-400">(optional)</span></label>
                        <textarea v-model="form.description" rows="3" placeholder="Brief description of the quiz..." 
                                  class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition resize-none"></textarea>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1.5">Start Time</label>
                            <input v-model="form.start_time" type="datetime-local" @change="suggestEndTime"
                                   class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                                   :class="{ 'border-red-400': form.errors.start_time }" required>
                            <p v-if="form.errors.start_time" class="text-xs text-red-500 mt-1">{{ form.errors.start_time }}</p>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1.5">End Time</label>
                            <input v-model="form.end_time" type="datetime-local"
                                   class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                                   :class="{ 'border-red-400': form.errors.end_time }" required>
                            <p v-if="form.errors.end_time" class="text-xs text-red-500 mt-1">{{ form.errors.end_time }}</p>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1.5">
                                <Clock class="w-3.5 h-3.5 inline mr-1 text-gray-400" />
                                Duration (minutes)
                            </label>
                            <input v-model="form.duration_minutes" type="number" min="5" max="480"
                                   class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition"
                                   :class="{ 'border-red-400': form.errors.duration_minutes }" required>
                            <p v-if="form.errors.duration_minutes" class="text-xs text-red-500 mt-1">{{ form.errors.duration_minutes }}</p>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1.5">Shuffle Questions</label>
                            <select v-model="form.shuffle_questions"
                                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition bg-white">
                                <option :value="true">Yes — random order</option>
                                <option :value="false">No — fixed order</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1.5">Show Results</label>
                            <select v-model="form.show_results_after"
                                    class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition bg-white">
                                <option value="immediately">Right after submission</option>
                                <option value="after_end">After quiz closes</option>
                                <option value="manually">Manually by lecturer</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- SECTION 2: Questions -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 space-y-6">
                    <div class="flex items-center justify-between pb-4 border-b border-gray-100">
                        <div class="flex items-center gap-3">
                            <div class="w-8 h-8 rounded-lg bg-orange-100 flex items-center justify-center">
                                <CheckCircle class="w-4 h-4 text-orange-600" />
                            </div>
                            <h2 class="text-lg font-bold text-gray-900">Questions</h2>
                        </div>
                        <span class="text-sm text-gray-400 bg-gray-50 px-3 py-1 rounded-full">{{ form.questions.length }} question{{ form.questions.length !== 1 ? 's' : '' }}</span>
                    </div>

                    <div v-for="(question, index) in form.questions" :key="index"
                         class="border border-gray-200 rounded-xl p-5 bg-gray-50/50 relative transition hover:border-orange-200">

                        <!-- Question Header -->
                        <div class="flex items-center justify-between mb-4">
                            <div class="flex items-center gap-2">
                                <span class="w-7 h-7 rounded-full bg-orange-600 text-white text-xs font-bold flex items-center justify-center">{{ index + 1 }}</span>
                                <span class="font-semibold text-gray-900">Question {{ index + 1 }}</span>
                            </div>
                            <button @click.prevent="removeQuestion(index)"
                                    v-if="form.questions.length > 1"
                                    class="text-gray-400 hover:text-red-500 transition p-1.5 rounded-lg hover:bg-red-50"
                                    title="Remove question">
                                <Trash2 class="w-4 h-4" />
                            </button>
                        </div>

                        <!-- Question Text -->
                        <div class="mb-4">
                            <label class="block text-xs font-medium text-gray-500 mb-1">Question Text</label>
                            <input v-model="question.question_text" type="text" placeholder="Enter your question..." 
                                   class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition" required>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                            <div>
                                <label class="block text-xs font-medium text-gray-500 mb-1">Question Type</label>
                                <select v-model="question.question_type" @change="question.correct_answer = ''"
                                        class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition bg-white">
                                    <option value="multiple_choice">Multiple Choice</option>
                                    <option value="true_false">True / False</option>
                                    <option value="short_answer">Short Answer</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-medium text-gray-500 mb-1">Points</label>
                                <input v-model="question.points" type="number" min="1" 
                                       class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition">
                            </div>
                        </div>

                        <!-- Multiple Choice Options -->
                        <div v-if="question.question_type === 'multiple_choice'" class="mb-4 p-4 bg-white rounded-lg border border-gray-200">
                            <label class="block text-xs font-medium text-gray-500 mb-2">Answer Options</label>
                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                <div v-for="(_, oIdx) in 4" :key="oIdx">
                                    <div class="flex items-center gap-2">
                                        <span class="text-xs font-bold text-gray-400 w-5">{{ ['A', 'B', 'C', 'D'][oIdx] }}</span>
                                        <input v-model="question.options[oIdx]" type="text" :placeholder="`Option ${['A', 'B', 'C', 'D'][oIdx]}`"
                                               class="flex-1 px-3 py-2 text-sm border border-gray-200 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition">
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- True/False Options -->
                        <div v-if="question.question_type === 'true_false'" class="mb-4">
                            <label class="block text-xs font-medium text-gray-500 mb-2">Answer Options</label>
                            <div class="flex gap-3">
                                <label class="flex items-center gap-2 px-4 py-2.5 border rounded-lg cursor-pointer transition"
                                       :class="question.correct_answer === 'True' ? 'border-green-500 bg-green-50 text-green-700' : 'border-gray-200 hover:border-gray-300'">
                                    <input type="radio" :name="'tf-' + index" value="True" v-model="question.correct_answer" class="sr-only">
                                    <CheckCircle v-if="question.correct_answer === 'True'" class="w-4 h-4 text-green-600" />
                                    <span class="font-medium">True</span>
                                </label>
                                <label class="flex items-center gap-2 px-4 py-2.5 border rounded-lg cursor-pointer transition"
                                       :class="question.correct_answer === 'False' ? 'border-green-500 bg-green-50 text-green-700' : 'border-gray-200 hover:border-gray-300'">
                                    <input type="radio" :name="'tf-' + index" value="False" v-model="question.correct_answer" class="sr-only">
                                    <CheckCircle v-if="question.correct_answer === 'False'" class="w-4 h-4 text-green-600" />
                                    <span class="font-medium">False</span>
                                </label>
                            </div>
                        </div>

                        <!-- Correct Answer -->
                        <div v-if="question.question_type !== 'true_false'">
                            <label class="block text-xs font-medium text-gray-500 mb-1">
                                Correct Answer
                                <span v-if="question.question_type === 'multiple_choice'" class="text-gray-400 font-normal"> (must match an option exactly)</span>
                            </label>
                            <input v-model="question.correct_answer" type="text" placeholder="Enter the correct answer"
                                   class="w-full px-4 py-2.5 border-2 border-green-200 bg-green-50/50 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition"
                                   required>
                        </div>
                    </div>

                    <!-- Add Question Button -->
                    <button @click.prevent="addQuestion" type="button"
                            class="w-full py-3 border-2 border-dashed border-gray-300 rounded-xl text-sm font-medium text-gray-500 hover:border-orange-400 hover:text-orange-600 hover:bg-orange-50/50 transition flex items-center justify-center gap-2">
                        <Plus class="w-4 h-4" />
                        Add Another Question
                    </button>
                </div>

                <!-- SECTION 3: Submit -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
                    <div class="text-sm text-gray-500">
                        <p class="font-medium text-gray-700">Ready to publish?</p>
                        <p>The quiz will appear to students based on the scheduled times.</p>
                    </div>
                    <div class="flex gap-3 w-full sm:w-auto">
                        <button type="button" @click="router.visit('/dashboard')"
                                class="flex-1 sm:flex-none px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium text-sm">
                            Cancel
                        </button>
                        <button type="submit" :disabled="form.processing"
                                class="flex-1 sm:flex-none inline-flex items-center gap-2 px-8 py-2.5 bg-orange-600 text-white rounded-lg hover:bg-orange-700 disabled:opacity-50 transition font-semibold shadow-sm">
                            <Save class="w-4 h-4" />
                            {{ form.processing ? 'Publishing...' : 'Create & Publish Quiz' }}
                        </button>
                    </div>
                </div>

            </form>
        </div>
    </div>
</template>
