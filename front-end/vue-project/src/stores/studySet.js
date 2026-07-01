import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useStudySetStore = defineStore('studySet', () => {
  const studySets = ref([]);
  const classes = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchStudySets = async () => {
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

  const fetchClasses = async (role) => {
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

  return {
    studySets,
    classes,
    loading,
    errorMessage,
    fetchStudySets,
    createStudySet,
    addCard,
    deleteCard,
    fetchClasses,
    createClass,
    deleteClass
  };
});
