import axios from 'axios';
import { ref } from 'vue';

export const globalLoading = ref(false);
let activeRequestsCount = 0;
let loadingTimer = null;

function updateLoadingState() {
  if (activeRequestsCount > 0) {
    if (!globalLoading.value && !loadingTimer) {
      loadingTimer = setTimeout(() => {
        globalLoading.value = true;
        loadingTimer = null;
      }, 250);
    }
  } else {
    if (loadingTimer) {
      clearTimeout(loadingTimer);
      loadingTimer = null;
    }
    globalLoading.value = false;
  }
}

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token and add a 5-second delay for testing
api.interceptors.request.use(
  (config) => {
    activeRequestsCount++;
    updateLoadingState();
    try {
      const savedUser = localStorage.getItem('sk_user');
      if (savedUser) {
        const user = JSON.parse(savedUser);
        if (user && user.token) {
          config.headers['Authorization'] = `Bearer ${user.token}`;
        }
      }
    } catch (e) {
      console.warn("Could not load token from storage:", e);
    }
    return config;
  },
  (error) => {
    activeRequestsCount = Math.max(0, activeRequestsCount - 1);
    updateLoadingState();
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors cleanly
api.interceptors.response.use(
  (response) => {
    activeRequestsCount = Math.max(0, activeRequestsCount - 1);
    updateLoadingState();
    return response.data; // Return backend response directly (which contains code, message, data)
  },
  (error) => {
    activeRequestsCount = Math.max(0, activeRequestsCount - 1);
    updateLoadingState();
    let message = 'Có lỗi xảy ra kết nối đến máy chủ.';
    if (error.response && error.response.data) {
      message = error.response.data.message || message;
    }
    return Promise.reject(new Error(message));
  }
);

export default api;
