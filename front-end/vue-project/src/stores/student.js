import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useStudentStore = defineStore('student', () => {
  const students = ref([]);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchStudents = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/students');
      students.value = response.data || [];
      return students.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createStudent = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/students', payload);
      const newStudent = response.data;
      students.value.push(newStudent);
      return newStudent;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const updateStudent = async (id, payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/students/${id}`, payload);
      const updated = response.data;
      const idx = students.value.findIndex(s => s.id === id);
      if (idx !== -1) {
        students.value[idx] = updated;
      }
      return updated;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteStudent = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/students/${id}`);
      students.value = students.value.filter(s => s.id !== id);
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    students,
    loading,
    errorMessage,
    fetchStudents,
    createStudent,
    updateStudent,
    deleteStudent
  };
});
