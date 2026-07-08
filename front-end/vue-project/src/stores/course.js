import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useCourseStore = defineStore('course', () => {
  const courses = ref([]);
  const currentCourse = ref(null);
  const loading = ref(false);
  const errorMessage = ref('');

  const fetchCourses = async () => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get('/courses');
      courses.value = response.data || [];
      return courses.value;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const fetchCourseDetails = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.get(`/courses/${id}`);
      currentCourse.value = response.data;
      return response.data;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const createCourse = async (payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.post('/courses', payload);
      const newCourse = response.data;
      courses.value.push(newCourse);
      return newCourse;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const updateCourse = async (id, payload) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const response = await api.put(`/courses/${id}`, payload);
      const updatedCourse = response.data;
      const idx = courses.value.findIndex(c => c.id === id);
      if (idx !== -1) {
        courses.value[idx] = updatedCourse;
      }
      if (currentCourse.value && currentCourse.value.id === id) {
        currentCourse.value = updatedCourse;
      }
      return updatedCourse;
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const deleteCourse = async (id) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      await api.delete(`/courses/${id}`);
      courses.value = courses.value.filter(c => c.id !== id);
      if (currentCourse.value && currentCourse.value.id === id) {
        currentCourse.value = null;
      }
    } catch (error) {
      errorMessage.value = error.message;
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    courses,
    currentCourse,
    loading,
    errorMessage,
    fetchCourses,
    fetchCourseDetails,
    createCourse,
    updateCourse,
    deleteCourse
  };
});
