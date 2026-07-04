import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/axios';

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null);
  const loading = ref(false);
  const errorMessage = ref('');

  // Helper to safely load user on init
  const loadUserFromStorage = () => {
    try {
      const savedUser = localStorage.getItem('sk_user');
      if (savedUser) {
        user.value = JSON.parse(savedUser);
      }
    } catch (e) {
      console.warn("Storage access failed:", e);
    }
  };

  // Initial load
  loadUserFromStorage();

  const login = async (username, password) => {
    loading.value = true;
    errorMessage.value = '';
    
    try {
      const response = await api.post('/auth/login', { username, password });
      
      // Response contains: code, message, data (AuthResponse)
      if (response && response.data) {
        const authData = response.data;
        const userData = {
          name: authData.fullName || authData.email.split('@')[0],
          email: authData.email,
          token: authData.token,
          avatar: "",
          level: authData.role === 'TEACHER' ? 'Giáo viên' : 'Sơ cấp 1A (Beginner)',
          streak: 5,
          xp: 1250,
          isGuest: false,
          role: authData.role
        };

        user.value = userData;
        localStorage.setItem('sk_user', JSON.stringify(userData));
        return userData;
      } else {
        throw new Error("Không nhận được dữ liệu phản hồi hợp lệ.");
      }
    } catch (error) {
      // console.error("Login action error:", error);
      errorMessage.value = error.message || 'Đăng nhập không thành công. Vui lòng kiểm tra lại.';
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const register = async (userData) => {
    loading.value = true;
    errorMessage.value = '';
    
    try {
      const response = await api.post('/auth/register', userData);
      return response;
    } catch (error) {
      console.error("Register action error:", error);
      errorMessage.value = error.message || 'Đăng ký không thành công. Vui lòng kiểm tra lại.';
      throw error;
    } finally {
      loading.value = false;
    }
  };

  const logout = () => {
    user.value = null;
    errorMessage.value = '';
    try {
      localStorage.removeItem('sk_user');
    } catch (e) {
      console.warn("Storage access failed:", e);
    }
  };

  return {
    user,
    loading,
    errorMessage,
    login,
    register,
    logout,
  };
});
