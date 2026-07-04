import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useMaterialStore = defineStore('material', () => {
  const materials = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchMaterials = async (classId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/classes/${classId}/materials`);
      materials.value = response.data || [];
      return materials.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const uploadMaterial = async (classId, file) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await api.post(`/classes/${classId}/materials/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      const newMaterial = response.data;
      materials.value.unshift(newMaterial);
      return newMaterial;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteMaterial = async (classId, materialId) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/classes/${classId}/materials/${materialId}`);
      materials.value = materials.value.filter(m => m.id !== materialId);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    materials,
    loading,
    errorMessage,
    fetchMaterials,
    uploadMaterial,
    deleteMaterial
  };
});
