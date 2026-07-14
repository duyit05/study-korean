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
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
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

// Response interceptor to handle errors cleanly and automatically refresh tokens
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => {
    activeRequestsCount = Math.max(0, activeRequestsCount - 1);
    updateLoadingState();
    return response.data; // Return backend response directly (which contains code, message, data)
  },
  async (error) => {
    activeRequestsCount = Math.max(0, activeRequestsCount - 1);
    updateLoadingState();

    const originalRequest = error.config;

    // Handle 401 Unauthorized error (token expired or blacklisted)
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch(err => Promise.reject(err));
      }

      isRefreshing = true;

      try {
        const savedUserStr = localStorage.getItem('sk_user');
        if (!savedUserStr) throw new Error("No user session");

        const savedUser = JSON.parse(savedUserStr);
        if (!savedUser || !savedUser.refreshToken) throw new Error("No refresh token");

        // Request new tokens using standard axios to bypass interceptor headers
        const res = await axios.post(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/auth/refresh`, {
          refreshToken: savedUser.refreshToken
        });

        if (res.data && res.data.code === 200 && res.data.data) {
          const authData = res.data.data;

          // Update access and refresh tokens in user store
          savedUser.token = authData.token;
          savedUser.refreshToken = authData.refreshToken;
          if (authData.avatarUrl) {
            savedUser.avatar = authData.avatarUrl;
          }
          localStorage.setItem('sk_user', JSON.stringify(savedUser));

          processQueue(null, authData.token);
          isRefreshing = false;

          // Retry the original request
          originalRequest.headers['Authorization'] = `Bearer ${authData.token}`;
          return api(originalRequest);
        } else {
          throw new Error("Invalid refresh response data");
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        isRefreshing = false;

        // Clear local storage and redirect to login on refresh token expiration
        localStorage.removeItem('sk_user');
        window.location.href = '/login';
        return Promise.reject(new Error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."));
      }
    }

    let message = 'Có lỗi xảy ra kết nối đến máy chủ.';
    if (error.response && error.response.data) {
      message = error.response.data.message || message;
    }
    return Promise.reject(new Error(message));
  }
);

export default api;
