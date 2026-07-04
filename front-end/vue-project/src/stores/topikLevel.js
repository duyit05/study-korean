import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useTopikLevelStore = defineStore('topikLevel', () => {
  const levels = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchLevels = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/topik-levels');
      levels.value = response.data || [];
      return levels.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createLevel = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/topik-levels', payload);
      const newLevel = response.data;
      levels.value.push(newLevel);
      return newLevel;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const updateLevel = async (id, payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/topik-levels/${id}`, payload);
      const updatedLevel = response.data;
      const idx = levels.value.findIndex(l => l.id === id);
      if (idx !== -1) {
        levels.value[idx] = updatedLevel;
      }
      return updatedLevel;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteLevel = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/topik-levels/${id}`);
      levels.value = levels.value.filter(l => l.id !== id);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    levels,
    loading,
    errorMessage,
    fetchLevels,
    createLevel,
    updateLevel,
    deleteLevel
  };
});
