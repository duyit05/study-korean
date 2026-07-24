import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useQuizStore = defineStore('quiz', () => {
  const quizzes = ref([]);
  const pendingAttempts = ref([]);
  const studentAttempts = ref([]);
  const teacherAttempts = ref([]);
  const totalTeacherAttemptElements = ref(0);
  const totalTeacherAttemptPages = ref(1);
  const questionTypes = ref([]);
  const questionSections = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');
  const totalQuizElements = ref(0);
  const totalQuizPages = ref(1);

  const fetchQuestionTypes = async () => {
    try {
      const response = await api.get('/quizzes/question-types');
      questionTypes.value = response.data || [];
      return questionTypes.value;
    } catch (error) {
      console.error("Failed to fetch question types:", error);
    }
  };

  const fetchQuestionSections = async () => {
    try {
      const response = await api.get('/quizzes/sections');
      questionSections.value = response.data || [];
      return questionSections.value;
    } catch (error) {
      console.error("Failed to fetch question sections:", error);
    }
  };

  const fetchQuizzesByClass = async (classId, force = false) => {
    if (quizzes.value.length > 0 && !force) return quizzes.value;
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

  const fetchMyCreatedQuizzes = async (force = false, params = {}) => {
    const hasParams = Object.keys(params).length > 0;
    if (quizzes.value.length > 0 && !force && !hasParams) return quizzes.value;
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/quizzes/creator', { params });
      // response = ApiResponse, response.data = PageResponse hoặc List
      if (response.data && response.data.items !== undefined) {
        // Paginated response
        quizzes.value = response.data.items;
        totalQuizElements.value = response.data.totalElements || 0;
        totalQuizPages.value = response.data.totalPage || 1;
        return response.data;
      } else {
        // Non-paginated (unpaged=true)
        quizzes.value = response.data || [];
        return quizzes.value;
      }
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

  const updateQuestion = async (quizId, questionId, questionPayload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/quizzes/${quizId}/questions/${questionId}`, questionPayload);
      return response.data;
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

  const fetchTeacherAttempts = async (params = {}) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/quiz-attempts/teacher', { params });
      if (response.data && response.data.items !== undefined) {
        teacherAttempts.value = response.data.items || [];
        totalTeacherAttemptElements.value = response.data.totalElements || 0;
        totalTeacherAttemptPages.value = response.data.totalPage || 1;
        return response.data;
      } else {
        teacherAttempts.value = response.data || [];
        return teacherAttempts.value;
      }
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

  const updateQuiz = async (quizId, payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/quizzes/${quizId}`, payload);
      const updatedQuiz = response.data;
      const idx = quizzes.value.findIndex(q => q.id === quizId);
      if (idx !== -1) {
        quizzes.value[idx] = updatedQuiz;
      }
      return updatedQuiz;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteQuiz = async (quizId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/quizzes/${quizId}`);
      quizzes.value = quizzes.value.filter(q => q.id !== quizId);
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
    teacherAttempts,
    totalTeacherAttemptElements,
    totalTeacherAttemptPages,
    questionTypes,
    questionSections,
    loading,
    errorMessage,
    totalQuizElements,
    totalQuizPages,
    fetchQuestionTypes,
    fetchQuestionSections,
    fetchQuizzesByClass,
    fetchMyCreatedQuizzes,
    fetchQuizDetails,
    createQuiz,
    updateQuiz,
    deleteQuiz,
    addQuestion,
    updateQuestion,
    deleteQuestion,
    submitQuizAttempt,
    fetchPendingAttempts,
    fetchTeacherAttempts,
    submitGrading,
    fetchMyAttempts
  };
});
