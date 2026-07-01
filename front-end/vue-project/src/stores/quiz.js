import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useQuizStore = defineStore('quiz', () => {
  const quizzes = ref([]);
  const pendingAttempts = ref([]);
  const studentAttempts = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchQuizzesByClass = async (classId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/quizzes/class/${classId}`);
      quizzes.value = response.data || [];
      return quizzes.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchMyCreatedQuizzes = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/quizzes/creator');
      quizzes.value = response.data || [];
      return quizzes.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchQuizDetails = async (quizId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/quizzes/${quizId}`);
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createQuiz = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/quizzes', payload);
      const newQuiz = response.data;
      quizzes.value.push(newQuiz);
      return newQuiz;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const addQuestion = async (quizId, questionPayload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post(`/quizzes/${quizId}/questions`, questionPayload);
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteQuestion = async (quizId, questionId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/quizzes/${quizId}/questions/${questionId}`);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const submitQuizAttempt = async (quizId, answersPayload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post(`/quiz-attempts/${quizId}/submit`, { answers: answersPayload });
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchPendingAttempts = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/quiz-attempts/pending');
      pendingAttempts.value = response.data || [];
      return pendingAttempts.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const submitGrading = async (attemptId, gradesPayload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post(`/quiz-attempts/grade/${attemptId}`, { grades: gradesPayload });
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchMyAttempts = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/quiz-attempts/my-attempts');
      studentAttempts.value = response.data || [];
      return studentAttempts.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    quizzes,
    pendingAttempts,
    studentAttempts,
    loading,
    errorMessage,
    fetchQuizzesByClass,
    fetchMyCreatedQuizzes,
    fetchQuizDetails,
    createQuiz,
    addQuestion,
    deleteQuestion,
    submitQuizAttempt,
    fetchPendingAttempts,
    submitGrading,
    fetchMyAttempts
  };
});
