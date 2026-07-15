import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useStudySetStore = defineStore('studySet', () => {
  const studySets = ref([]);
  const classes = ref([]);
  const studentsList = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchStudySets = async (force = false) => {
    if (studySets.value.length > 0 && !force) return studySets.value;
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/study-sets');
      studySets.value = response.data || [];
      return studySets.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchCardsForSet = async (setId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/study-sets/${setId}/cards`);
      const idx = studySets.value.findIndex(s => s.id === setId);
      if (idx !== -1) {
        studySets.value[idx].words = response.data || [];
      }
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createStudySet = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/study-sets', payload);
      const newSet = response.data;
      studySets.value.push(newSet);
      return newSet;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const addCard = async (setId, cardPayload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post(`/study-sets/${setId}/cards`, cardPayload);
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const addCardsBatch = async (setId, cardsPayload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post(`/study-sets/${setId}/cards/batch`, cardsPayload);
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteCard = async (setId, cardId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/study-sets/${setId}/cards/${cardId}`);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const updateStudySet = async (id, payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/study-sets/${id}`, payload);
      const updatedSet = response.data;
      const idx = studySets.value.findIndex(s => s.id === id);
      if (idx !== -1) {
        studySets.value[idx] = updatedSet;
      }
      return updatedSet;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteStudySet = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/study-sets/${id}`);
      studySets.value = studySets.value.filter(s => s.id !== id);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchClasses = async (role, force = false) => {
    if (classes.value.length > 0 && !force) return classes.value;
    loading.value = true;
    errorMessage.value = '';
    try {
      const endpoint = role === 'TEACHER' ? '/classes/teacher' : '/classes/student';
      const response = await api.get(endpoint);
      classes.value = response.data || [];
      return classes.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createClass = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/classes', payload);
      const newClass = response.data;
      classes.value.push(newClass);
      return newClass;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteClass = async (classId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/classes/${classId}`);
      classes.value = classes.value.filter(c => c.id !== classId);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchStudents = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/users/students');
      studentsList.value = response.data || [];
      return studentsList.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const enrollStudent = async (classId, studentId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post(`/classes/${classId}/students/${studentId}`);
      const updatedClass = response.data;
      const idx = classes.value.findIndex(c => c.id === classId);
      if (idx !== -1) {
        classes.value[idx] = updatedClass;
      }
      return updatedClass;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const assignStudySet = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/study-sets/assignments', payload);
      const idx = classes.value.findIndex(c => c.id === payload.classId);
      if (idx !== -1) {
        if (!classes.value[idx].assignedStudySets) {
          classes.value[idx].assignedStudySets = [];
        }
        classes.value[idx].assignedStudySets.push(response.data);
      }
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const unassignStudySet = async (classId, assignmentId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/study-sets/assignments/${assignmentId}`);
      const idx = classes.value.findIndex(c => c.id === classId);
      if (idx !== -1 && classes.value[idx].assignedStudySets) {
        classes.value[idx].assignedStudySets = classes.value[idx].assignedStudySets.filter(
          a => a.id !== assignmentId
        );
      }
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    studySets,
    classes,
    loading,
    errorMessage,
    fetchStudySets,
    fetchCardsForSet,
    createStudySet,
    updateStudySet,
    deleteStudySet,
    addCard,
    addCardsBatch,
    deleteCard,
    fetchClasses,
    createClass,
    deleteClass,
    studentsList,
    fetchStudents,
    enrollStudent,
    assignStudySet,
    unassignStudySet
  };
});
