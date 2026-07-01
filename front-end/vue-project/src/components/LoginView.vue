<template>
  <div class="login-container animate-fade">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-badge">
          <span class="kr-char">애리</span>
        </div>
        <h1>Learn Korean with Aeri</h1>
        <p class="subtitle">5 phút mỗi ngày để chinh phục Hanguko</p>
      </div>

      <!-- Login Form -->
      <form v-if="!isRegisterMode" @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Tài khoản</label>
          <div class="input-wrapper">
            <AppIcon name="username" class="input-icon" size="18" />
            <input 
              type="text" 
              id="username" 
              v-model="username" 
              placeholder="Hãy nhập tài khoản" 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="password">Mật khẩu</label>
          <div class="input-wrapper">
            <AppIcon name="lock" class="input-icon" size="18" />
            <input 
              :type="showPassword ? 'text' : 'password'" 
              id="password" 
              v-model="password" 
              placeholder="Hãy nhập mật khẩu" 
              required
            >
            <button 
              type="button" 
              class="eye-btn" 
              @click="showPassword = !showPassword"
              tabindex="-1"
            >
              <AppIcon :name="showPassword ? 'eye-off' : 'eye'" size="16" />
            </button>
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

        <div class="toggle-mode">
          Chưa có tài khoản? <a href="#" @click.prevent="toggleMode">Đăng ký ngay</a>
        </div>
      </form>

      <!-- Register Form -->
      <form v-else @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label for="reg-fullName">Họ và tên</label>
          <div class="input-wrapper">
            <AppIcon name="user" class="input-icon" size="18" />
            <input 
              type="text" 
              id="reg-fullName" 
              v-model="regFullName" 
              placeholder="Ví dụ: Nguyễn Văn An" 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="reg-username">Tên đăng nhập</label>
          <div class="input-wrapper">
            <AppIcon name="username" class="input-icon" size="18" />
            <input 
              type="text" 
              id="reg-username" 
              v-model="regUsername" 
              placeholder="Ví dụ: vanan123" 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="reg-email">Email</label>
          <div class="input-wrapper">
            <AppIcon name="email" class="input-icon" size="18" />
            <input 
              type="email" 
              id="reg-email" 
              v-model="regEmail" 
              placeholder="email@example.com" 
              required
            >
          </div>
        </div>

        <div class="form-group">
          <label for="reg-phone">Số điện thoại</label>
          <div class="input-wrapper">
            <AppIcon name="phone" class="input-icon" size="18" />
            <input 
              type="tel" 
              id="reg-phone" 
              v-model="regPhone" 
              placeholder="Nhập số điện thoại (tùy chọn)"
            >
          </div>
        </div>


        <div class="form-group">
          <label for="reg-password">Mật khẩu</label>
          <div class="input-wrapper">
            <AppIcon name="lock" class="input-icon" size="18" />
            <input 
              :type="showRegPassword ? 'text' : 'password'" 
              id="reg-password" 
              v-model="regPassword" 
              placeholder="Tối thiểu 6 ký tự" 
              required
            >
            <button 
              type="button" 
              class="eye-btn" 
              @click="showRegPassword = !showRegPassword"
              tabindex="-1"
            >
              <AppIcon :name="showRegPassword ? 'eye-off' : 'eye'" size="16" />
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="reg-confirm-password">Nhập lại mật khẩu</label>
          <div class="input-wrapper">
            <AppIcon name="lock" class="input-icon" size="18" />
            <input 
              :type="showRegConfirmPassword ? 'text' : 'password'" 
              id="reg-confirm-password" 
              v-model="regConfirmPassword" 
              placeholder="Xác nhận mật khẩu của bạn" 
              required
            >
            <button 
              type="button" 
              class="eye-btn" 
              @click="showRegConfirmPassword = !showRegConfirmPassword"
              tabindex="-1"
            >
              <AppIcon :name="showRegConfirmPassword ? 'eye-off' : 'eye'" size="16" />
            </button>
          </div>
        </div>

        <div v-if="errorMessage" class="error-box animate-scale">
          <AppIcon name="alert" size="16" />
          <span>{{ errorMessage }}</span>
        </div>

        <div v-if="successMessage" class="success-box animate-scale">
          <AppIcon name="check" size="16" />
          <span>{{ successMessage }}</span>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="!loading">Đăng Ký</span>
          <span v-else class="loader"></span>
        </button>

        <div class="toggle-mode">
          Đã có tài khoản? <a href="#" @click.prevent="toggleMode">Đăng nhập</a>
        </div>
      </form>

      <div class="login-footer">
        <p>© 2026 Learn Korean with Aeri.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppIcon from './icons/AppIcon.vue'
import { useAuthStore } from '../stores/auth'

const emit = defineEmits(['login-success'])

const authStore = useAuthStore()

// Mode toggle
const isRegisterMode = ref(false)
const successMessage = ref('')

// Password eye toggles
const showPassword = ref(false)
const showRegPassword = ref(false)
const showRegConfirmPassword = ref(false)

// Login fields
const username = ref('')
const password = ref('')

// Register fields
const regFullName = ref('')
const regUsername = ref('')
const regEmail = ref('')
const regPhone = ref('')
const regRole = ref('STUDENT')
const regPassword = ref('')
const regConfirmPassword = ref('')

const errorMessage = computed(() => authStore.errorMessage)
const loading = computed(() => authStore.loading)

const toggleMode = () => {
  isRegisterMode.value = !isRegisterMode.value
  authStore.errorMessage = ''
  successMessage.value = ''
  // Clear fields
  username.value = ''
  password.value = ''
  regFullName.value = ''
  regUsername.value = ''
  regEmail.value = ''
  regPhone.value = ''
  regRole.value = 'STUDENT'
  regPassword.value = ''
  regConfirmPassword.value = ''
  
  // Clear eye states
  showPassword.value = false
  showRegPassword.value = false
  showRegConfirmPassword.value = false
}

const handleLogin = async () => {
  if (!username.value || !password.value) {
    authStore.errorMessage = 'Vui lòng điền đầy đủ tài khoản và mật khẩu.'
    return
  }

  try {
    const userData = await authStore.login(username.value, password.value)
    emit('login-success', userData)
  } catch (error) {
    console.error("Đăng nhập thất bại:", error)
  }
}

const handleRegister = async () => {
  if (!regFullName.value || !regUsername.value || !regEmail.value || !regPassword.value || !regConfirmPassword.value) {
    authStore.errorMessage = 'Vui lòng điền đầy đủ các thông tin bắt buộc.'
    return
  }

  if (regPassword.value.length < 6) {
    authStore.errorMessage = 'Mật khẩu phải có tối thiểu 6 ký tự.'
    return
  }

  if (regPassword.value !== regConfirmPassword.value) {
    authStore.errorMessage = 'Mật khẩu nhập lại không khớp.'
    return
  }

  try {
    const payload = {
      fullName: regFullName.value,
      username: regUsername.value,
      email: regEmail.value,
      phone: regPhone.value || null,
      password: regPassword.value,
      role: regRole.value
    }

    await authStore.register(payload)

    successMessage.value = 'Đăng ký tài khoản thành công! Tự động chuyển về trang Đăng nhập...'
    authStore.errorMessage = ''

    setTimeout(() => {
      toggleMode()
    }, 2000)
  } catch (error) {
    console.error("Đăng ký thất bại:", error)
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
  padding: 0.75rem 2.5rem 0.75rem 2.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-title);
  transition: all var(--transition-fast);
}

.role-select {
  width: 100%;
  padding: 0.75rem 2.5rem 0.75rem 2.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-title);
  appearance: none;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.role-select:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-glow);
}

.input-wrapper input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-glow);
}

.eye-btn {
  position: absolute;
  right: 4px;
  background: none;
  border: none;
  padding: 0 0.75rem;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  transition: color var(--transition-fast);
}

.eye-btn:hover {
  color: var(--primary);
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

.success-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background-color: rgba(16, 185, 129, 0.1);
  border: 1px solid rgb(16, 185, 129);
  border-radius: var(--radius-md);
  color: rgb(16, 185, 129);
  font-size: 0.85rem;
}

.toggle-mode {
  text-align: center;
  margin-top: 1.25rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.toggle-mode a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.toggle-mode a:hover {
  color: var(--primary-hover);
  text-decoration: underline;
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
