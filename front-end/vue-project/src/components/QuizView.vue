<template>
  <div class="quiz-container animate-fade">
    <QuizList
      v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz"
    />
    <QuizCreator
      v-else-if="isCreatingQuiz"
    />
    <QuizRunner
      v-else-if="activeQuiz"
    />
    <QuizReview
      v-else-if="viewingResultQuiz"
    />
  </div>
</template>

<script setup>
import { provide } from 'vue'
import QuizList from './quiz/QuizList.vue'
import QuizRunner from './quiz/QuizRunner.vue'
import QuizReview from './quiz/QuizReview.vue'
import QuizCreator from './quiz/QuizCreator.vue'
import { useQuizState } from '../stores/useQuizState.js'

const props = defineProps({
  quizzes: {
    type: Array,
    required: true,
    default: () => []
  },
  mode: {
    type: String,
    default: 'quizzes'
  }
})

const emit = defineEmits(['submit-quiz'])

// Initialize composable ONCE at the parent level; sub-components inject it.
const quizState = useQuizState(props, emit)
provide('quizState', quizState)

const { activeQuiz, viewingResultQuiz, isCreatingQuiz } = quizState
</script>

<style scoped>
.quiz-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
</style>
