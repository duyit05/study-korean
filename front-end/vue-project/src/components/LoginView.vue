<template>
  <div class="login-container animate-fade">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-badge">
          <span class="kr-char">한</span>
        </div>
        <h1>Study Korea</h1>
        <p class="subtitle">Hệ thống học tiếng Hàn thông minh cho học viên</p>
      </div>

      <!-- Quick Profiles Selection -->
      <div class="profile-shortcuts">
        <p class="section-label">Đăng nhập nhanh với tài khoản mẫu:</p>
        <div class="shortcuts-grid">
          <button 
            type="button"
            class="shortcut-btn" 
            @click="selectQuickUser('student')"
          >
            <img src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=128" alt="Nguyen Van An">
            <div class="shortcut-info">
              <span class="name">Nguyễn Văn An</span>
              <span class="role">Học viên (Lớp 1A)</span>
            </div>
          </button>
          
          <button 
            type="button" 
            class="shortcut-btn"
            @click="selectQuickUser('guest')"
          >
            <div class="guest-avatar">G</div>
            <div class="shortcut-info">
              <span class="name">Học viên Khách</span>
              <span class="role">Tài khoản trải nghiệm</span>
            </div>
          </button>
        </div>
      </div>

      <div class="divider">
        <span>Hoặc nhập thông tin</span>
      </div>

      <!-- Credentials Form -->
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">Email học viên</label>
          <div class="input-wrapper">
            <AppIcon name="email" class="input-icon" size="18" />
            <input 
              type="email" 
              id="email" 
              v-model="email" 
              placeholder="tenhocvien@study-korea.edu.vn" 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="password">Mật khẩu</label>
          <div class="input-wrapper">
            <AppIcon name="lock" class="input-icon" size="18" />
            <input 
              type="password" 
              id="password" 
              v-model="password" 
              placeholder="••••••••" 
              required
            >
          </div>
        </div>

        <div v-if="errorMessage" class="error-box animate-scale">
          <AppIcon name="alert" size="16" />
          <span>{{ errorMessage }}</span>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="!loading">Đăng Nhập</span>
          <span v-else class="loader"></span>
        </button>
      </form>

      <div class="login-footer">
        <p>© 2026 Study Korea. Giao diện Cổng học viên.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppIcon from './icons/AppIcon.vue'

const emit = defineEmits(['login-success'])

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

const handleLogin = () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'Vui lòng điền đầy đủ email và mật khẩu.'
    return
  }

  loading.value = true
  errorMessage.value = ''

  // Simulate api response
  setTimeout(() => {
    loading.value = false
    if (email.value === 'vanan.student@study-korea.edu.vn') {
      emit('login-success', {
        name: "Nguyễn Văn An",
        email: "vanan.student@study-korea.edu.vn",
        avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=256",
        level: "Sơ cấp 1A (Beginner)",
        streak: 5,
        xp: 1250,
        isGuest: false
      })
    } else {
      // Allow general logins for simulation
      emit('login-success', {
        name: email.value.split('@')[0],
        email: email.value,
        avatar: "",
        level: "Tài khoản Khách",
        streak: 1,
        xp: 100,
        isGuest: true
      })
    }
  }, 1000)
}

const selectQuickUser = (role) => {
  if (role === 'student') {
    email.value = 'vanan.student@study-korea.edu.vn'
    password.value = '123456'
    handleLogin()
  } else {
    email.value = 'hocvien.khach@gmail.com'
    password.value = '123456'
    handleLogin()
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 1.5rem;
  background: radial-gradient(circle at 10% 20%, var(--primary-glow) 0%, transparent 40%),
              radial-gradient(circle at 90% 80%, var(--primary-glow) 0%, transparent 45%);
}

.login-card {
  width: 100%;
  max-width: 460px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2.5rem;
  box-shadow: var(--shadow-lg);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-badge {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  box-shadow: 0 8px 16px var(--primary-glow);
}

.kr-char {
  color: #fff;
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 1.7rem;
  font-weight: 700;
}

h1 {
  font-size: 1.8rem;
  color: var(--text-title);
  font-weight: 800;
  margin-bottom: 0.5rem;
  letter-spacing: -0.5px;
}

.subtitle {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.profile-shortcuts {
  margin-bottom: 1.5rem;
}

.section-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
  margin-bottom: 0.75rem;
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

.shortcut-btn {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  text-align: left;
  transition: all var(--transition-fast);
}

.shortcut-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--primary);
  transform: translateY(-2px);
}

.shortcut-btn img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.guest-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--text-muted);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
}

.shortcut-info {
  display: flex;
  flex-direction: column;
}

.shortcut-info .name {
  font-weight: 600;
  color: var(--text-title);
  font-size: 0.95rem;
}

.shortcut-info .role {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.8rem;
  margin: 1.5rem 0;
}

.divider::before, .divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid var(--border-color);
}

.divider span {
  padding: 0 10px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
}

.input-wrapper input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  transition: all var(--transition-fast);
}

.input-wrapper input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-glow);
}

.error-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background-color: var(--danger-light);
  border: 1px solid var(--danger);
  border-radius: var(--radius-md);
  color: var(--danger);
  font-size: 0.85rem;
}

.submit-btn {
  width: 100%;
  padding: 0.85rem;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px var(--primary-glow);
  margin-top: 0.5rem;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px var(--primary-glow);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.loader {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 480px) {
  .login-card {
    padding: 1.75rem;
  }
}
</style>
