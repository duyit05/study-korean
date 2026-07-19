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
      const savedUser = localStorage.getItem('sk_user') || sessionStorage.getItem('sk_user');
      if (savedUser) {
        user.value = JSON.parse(savedUser);
      }
    } catch (e) {
      console.warn("Storage access failed:", e);
    }
  };

  // Initial load
  loadUserFromStorage();

  const login = async (username, password, rememberMe = false) => {
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
          refreshToken: authData.refreshToken,
          avatar: authData.avatarUrl || "",
          level: authData.role === 'TEACHER' ? 'Giáo viên' : (authData.level ? `Sơ cấp ${authData.level}` : 'Sơ cấp 1A (Beginner)'),
          streak: authData.streak !== null && authData.streak !== undefined ? authData.streak : 0,
          xp: authData.xp !== null && authData.xp !== undefined ? authData.xp : 0,
          isGuest: false,
          role: authData.role,
          ipWarning: authData.ipWarning || false,
          warningMessage: authData.warningMessage || null,
        };

        user.value = userData;
        if (rememberMe) {
          localStorage.setItem('sk_user', JSON.stringify(userData));
          localStorage.setItem('sk_remembered_username', username);
          sessionStorage.removeItem('sk_user');
        } else {
          sessionStorage.setItem('sk_user', JSON.stringify(userData));
          localStorage.removeItem('sk_user');
          localStorage.removeItem('sk_remembered_username');
        }
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

  const logout = async () => {
    try {
      if (user.value && user.value.refreshToken) {
        await api.post('/auth/logout', { refreshToken: user.value.refreshToken });
      }
    } catch (e) {
      console.warn("Backend logout failed:", e);
    } finally {
      user.value = null;
      errorMessage.value = '';
      try {
        localStorage.removeItem('sk_user');
        sessionStorage.removeItem('sk_user');
      } catch (e) {
        console.warn("Storage access failed:", e);
      }
    }
  };

  const uploadAvatar = async (file) => {
    loading.value = true;
    errorMessage.value = '';
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('prefix', 'avatar');

      const response = await api.post('/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      if (response && response.key) {
        const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
        return `${baseUrl}/files/download/${response.key}`;
      } else {
        throw new Error("Không nhận được file key từ server.");
      }
    } catch (error) {
      console.error("Avatar upload failed:", error);
      errorMessage.value = error.message || 'Tải ảnh đại diện thất bại.';
      throw error;
    } finally {
      loading.value = false;
    }
  };

  return {
    user,
    loading,
    errorMessage,
    login,
    register,
    logout,
    uploadAvatar,
  };
});
