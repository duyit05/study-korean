import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
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
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors cleanly
api.interceptors.response.use(
  (response) => {
    return response.data; // Return backend response directly (which contains code, message, data)
  },
  (error) => {
    let message = 'Có lỗi xảy ra kết nối đến máy chủ.';
    if (error.response && error.response.data) {
      message = error.response.data.message || message;
    }
    return Promise.reject(new Error(message));
  }
);

export default api;
