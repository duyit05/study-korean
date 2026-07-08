import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useSessionStore = defineStore('session', () => {
  const sessions = ref([]);
  const currentSession = ref(null);
  const statuses = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchStatuses = async () => {
    try {
      const response = await api.get('/sessions/statuses');
      statuses.value = response.data || [];
      return statuses.value;
    } catch (error) {
      console.error("Failed to fetch session statuses:", error);
    }
  };

  const fetchSessionsByClass = async (classId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/sessions/class/${classId}`);
      sessions.value = response.data || [];
      return sessions.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchSessionDetails = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/sessions/${id}`);
      currentSession.value = response.data;
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createSession = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/sessions', payload);
      const newSession = response.data;
      sessions.value.push(newSession);
      // Sort sessions by sessionNumber Ascending
      sessions.value.sort((a, b) => a.sessionNumber - b.sessionNumber);
      return newSession;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const updateSession = async (id, payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/sessions/${id}`, payload);
      const updatedSession = response.data;
      const idx = sessions.value.findIndex(s => s.id === id);
      if (idx !== -1) {
        sessions.value[idx] = updatedSession;
      }
      if (currentSession.value && currentSession.value.id === id) {
        currentSession.value = updatedSession;
      }
      sessions.value.sort((a, b) => a.sessionNumber - b.sessionNumber);
      return updatedSession;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteSession = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/sessions/${id}`);
      sessions.value = sessions.value.filter(s => s.id !== id);
      if (currentSession.value && currentSession.value.id === id) {
        currentSession.value = null;
      }
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    sessions,
    currentSession,
    statuses,
    loading,
    errorMessage,
    fetchStatuses,
    fetchSessionsByClass,
    fetchSessionDetails,
    createSession,
    updateSession,
    deleteSession
  };
});
