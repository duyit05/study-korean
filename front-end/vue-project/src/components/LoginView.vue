<template>
  <div class="login-page animate-fade">
    <!-- Left decorative panel -->
    <div class="deco-panel">
      <div class="deco-overlay"></div>
      <div class="deco-content">
        <div class="deco-badge">애리</div>
        <h2 class="deco-title">Learn Korean<br>with Aeri</h2>
        <p class="deco-subtitle">5 phút mỗi ngày để chinh phục Hangugo</p>

        <div class="deco-features">
          <div class="deco-feature">
            <span class="deco-feat-icon">📚</span>
            <span>Bộ từ vựng TOPIK chuyên sâu</span>
          </div>
          <div class="deco-feature">
            <span class="deco-feat-icon">🎯</span>
            <span>Luyện thi TOPIK I &amp; II</span>
          </div>
          <div class="deco-feature">
            <span class="deco-feat-icon">✨</span>
            <span>Theo dõi tiến độ mỗi ngày</span>
          </div>
        </div>

        <!-- Floating Korean chars -->
        <div class="kr-float kr-float-1">한</div>
        <div class="kr-float kr-float-2">국</div>
        <div class="kr-float kr-float-3">어</div>
        <div class="kr-float kr-float-4">배</div>
        <div class="kr-float kr-float-5">움</div>
      </div>
    </div>

    <!-- Right form panel -->
    <div class="form-panel">
      <div class="form-card" :class="{ 'register-card': isRegisterMode }">
        <!-- Header -->
        <div class="form-header">
          <div class="logo-badge">
            <span class="kr-char">애리</span>
          </div>
          <h1 v-if="!isRegisterMode">Đăng Nhập</h1>
          <h1 v-else>Tạo Tài Khoản</h1>
          <p class="form-subtitle" v-if="!isRegisterMode">Chào mừng trở lại! Hãy tiếp tục hành trình của bạn.</p>
          <p class="form-subtitle" v-else>Bắt đầu hành trình học tiếng Hàn ngay hôm nay.</p>
        </div>

        <!-- LOGIN FORM -->
        <form v-show="!isRegisterMode" @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username">Tài khoản</label>
            <div class="input-wrapper">
              <AppIcon name="username" class="input-icon" size="18" />
              <input
                type="text"
                id="username"
                v-model="username"
                placeholder="Nhập tên đăng nhập"
                required
                autocomplete="username"
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
                placeholder="Nhập mật khẩu"
                required
                autocomplete="current-password"
              >
              <button type="button" class="eye-btn" @click="showPassword = !showPassword" tabindex="-1">
                <AppIcon :name="showPassword ? 'eye-off' : 'eye'" size="16" />
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox-container">
              <input type="checkbox" v-model="rememberMe">
              <span class="checkmark"></span>
              Ghi nhớ đăng nhập
            </label>
          </div>

          <div v-if="errorMessage" class="error-box animate-scale">
            <AppIcon name="alert" size="16" />
            <span>{{ errorMessage }}</span>
          </div>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="!loading">Đăng Nhập</span>
            <span v-else class="loader"></span>
          </button>

          <div class="divider-or">
            <span>Hoặc</span>
          </div>

          <div class="social-login">
            <div id="google-signin-btn"></div>
          </div>

          <div class="toggle-mode">
            Chưa có tài khoản?
            <a href="#" @click.prevent="toggleMode">Đăng ký ngay</a>
          </div>
        </form>

        <!-- REGISTER FORM -->
        <form v-show="isRegisterMode" @submit.prevent="handleRegister" class="login-form">
          <div class="form-row">
            <div class="form-group">
              <label for="reg-fullName">Họ và tên</label>
              <div class="input-wrapper">
                <AppIcon name="user" class="input-icon" size="18" />
                <input type="text" id="reg-fullName" v-model="regFullName" placeholder="Nguyễn Văn An" required>
              </div>
            </div>
            <div class="form-group">
              <label for="reg-username">Tên đăng nhập</label>
              <div class="input-wrapper">
                <AppIcon name="username" class="input-icon" size="18" />
                <input type="text" id="reg-username" v-model="regUsername" placeholder="vanan123" required>
              </div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="reg-email">Email</label>
              <div class="input-wrapper">
                <AppIcon name="email" class="input-icon" size="18" />
                <input type="email" id="reg-email" v-model="regEmail" placeholder="email@example.com" required>
              </div>
            </div>
            <div class="form-group">
              <label for="reg-phone">Số điện thoại</label>
              <div class="input-wrapper">
                <AppIcon name="phone" class="input-icon" size="18" />
                <input type="tel" id="reg-phone" v-model="regPhone" placeholder="Tùy chọn">
              </div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="reg-password">Mật khẩu</label>
              <div class="input-wrapper">
                <AppIcon name="lock" class="input-icon" size="18" />
                <input :type="showRegPassword ? 'text' : 'password'" id="reg-password" v-model="regPassword" placeholder="Tối thiểu 6 ký tự" required>
                <button type="button" class="eye-btn" @click="showRegPassword = !showRegPassword" tabindex="-1">
                  <AppIcon :name="showRegPassword ? 'eye-off' : 'eye'" size="16" />
                </button>
              </div>
            </div>
            <div class="form-group">
              <label for="reg-confirm-password">Nhập lại mật khẩu</label>
              <div class="input-wrapper">
                <AppIcon name="lock" class="input-icon" size="18" />
                <input :type="showRegConfirmPassword ? 'text' : 'password'" id="reg-confirm-password" v-model="regConfirmPassword" placeholder="Xác nhận mật khẩu" required>
                <button type="button" class="eye-btn" @click="showRegConfirmPassword = !showRegConfirmPassword" tabindex="-1">
                  <AppIcon :name="showRegConfirmPassword ? 'eye-off' : 'eye'" size="16" />
                </button>
              </div>
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
            <span v-if="!loading">Tạo Tài Khoản</span>
            <span v-else class="loader"></span>
          </button>

          <div class="toggle-mode">
            Đã có tài khoản?
            <a href="#" @click.prevent="toggleMode">Đăng nhập</a>
          </div>
        </form>

        <div class="form-footer">© 2026 Learn Korean with Aeri.</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from './icons/AppIcon.vue'
import { useAuthStore } from '../stores/auth'

const emit = defineEmits(['login-success'])

const router = useRouter()
const authStore = useAuthStore()

const isRegisterMode = ref(false)
const successMessage = ref('')

const showPassword = ref(false)
const showRegPassword = ref(false)
const showRegConfirmPassword = ref(false)

const username = ref('')
const password = ref('')
const rememberMe = ref(false)

onMounted(() => {
  const savedUsername = localStorage.getItem('sk_remembered_username')
  if (savedUsername) {
    username.value = savedUsername
    rememberMe.value = true
  }
  initGoogleSignIn()
})

const initGoogleSignIn = () => {
  if (window.google) {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'your-google-client-id-here.apps.googleusercontent.com'
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: handleGoogleCredentialResponse,
      auto_select: false,
      cancel_on_tap_outside: true
    })

    const btnContainer = document.getElementById('google-signin-btn')
    if (btnContainer) {
      window.google.accounts.id.renderButton(btnContainer, {
        theme: 'outline',
        size: 'large',
        width: 320,
        text: 'signin_with',
        shape: 'rectangular'
      })
    }
  } else {
    setTimeout(initGoogleSignIn, 500)
  }
}

const handleGoogleCredentialResponse = async (response) => {
  const idToken = response.credential
  authStore.errorMessage = ''
  try {
    const userData = await authStore.loginWithGoogle(idToken)
    emit('login-success', userData)
    if (userData.role === 'TEACHER') {
      router.push({ name: 'TeacherDashboard' })
    } else {
      router.push({ name: 'StudentDashboard' })
    }
  } catch (error) {
    console.error("Google Login failed", error)
    authStore.errorMessage = error.message || 'Đăng nhập Google thất bại.'
  }
}


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
  username.value = ''
  password.value = ''
  regFullName.value = ''
  regUsername.value = ''
  regEmail.value = ''
  regPhone.value = ''
  regRole.value = 'STUDENT'
  regPassword.value = ''
  regConfirmPassword.value = ''
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
    const userData = await authStore.login(username.value, password.value, rememberMe.value)
    emit('login-success', userData)
    router.push(userData.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard')
  } catch (error) {
    // handled by store
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
    successMessage.value = 'Đăng ký thành công! Đang chuyển về trang Đăng nhập...'
    authStore.errorMessage = ''
    setTimeout(() => { toggleMode() }, 2000)
  } catch (error) {
    console.error('Đăng ký thất bại:', error)
  }
}
</script>

<style scoped>
/* ─── Page wrapper ─── */
.login-page {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-app);
}

/* ─── Left decorative panel ─── */
.deco-panel {
  position: relative;
  flex: 0 0 40%;
  background: linear-gradient(145deg, #2A1F17 0%, #3E3026 50%, #56453A 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.deco-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(234,219,200,0.08) 0%, transparent 55%),
    radial-gradient(ellipse at 80% 80%, rgba(108,88,76,0.4) 0%, transparent 55%);
}

.deco-content {
  position: relative;
  z-index: 1;
  padding: 3rem 2.5rem;
  text-align: center;
  color: #EADBC8;
}

.deco-badge {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(234,219,200,0.25), rgba(234,219,200,0.08));
  border: 1.5px solid rgba(234,219,200,0.3);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.75rem;
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 1.6rem;
  font-weight: 700;
  color: #FAF6F0;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}

.deco-title {
  font-size: 2.4rem;
  font-weight: 800;
  color: #FAF6F0;
  line-height: 1.2;
  letter-spacing: -0.5px;
  margin-bottom: 1rem;
}

.deco-subtitle {
  font-size: 1rem;
  color: rgba(234,219,200,0.75);
  margin-bottom: 2.5rem;
  font-style: italic;
}

.deco-features {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  text-align: left;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(234,219,200,0.12);
  border-radius: 16px;
  padding: 1.25rem 1.5rem;
}

.deco-feature {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.9rem;
  color: rgba(234,219,200,0.85);
  font-weight: 500;
}

.deco-feat-icon {
  font-size: 1.1rem;
  flex-shrink: 0;
}

/* Floating Korean characters */
.kr-float {
  position: absolute;
  font-family: 'Noto Sans KR', sans-serif;
  font-weight: 900;
  color: rgba(234,219,200,0.06);
  pointer-events: none;
  user-select: none;
  animation: floatAnim 8s ease-in-out infinite;
}
.kr-float-1 { font-size: 9rem;  top: -2rem;  left: -1.5rem;  animation-delay: 0s; }
.kr-float-2 { font-size: 7rem;  top: 10%;    right: -1rem;   animation-delay: 1.5s; }
.kr-float-3 { font-size: 11rem; bottom: -3rem; left: 5%;     animation-delay: 3s; }
.kr-float-4 { font-size: 6rem;  bottom: 12%; right: 5%;      animation-delay: 2s; }
.kr-float-5 { font-size: 8rem;  top: 42%;    left: 3%;       animation-delay: 4s; }

@keyframes floatAnim {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50%       { transform: translateY(-18px) rotate(3deg); }
}

/* ─── Right form panel ─── */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 1.5rem;
  overflow-y: auto;
}

.form-card {
  width: 100%;
  max-width: 480px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2.75rem 2.5rem;
  box-shadow: var(--shadow-lg);
  transition: max-width var(--transition-normal, 0.3s) ease;
}

.form-card.register-card {
  max-width: 640px;
}

/* ─── Form header ─── */
.form-header {
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
  margin: 0 auto 1.1rem;
  box-shadow: 0 8px 20px var(--primary-glow);
}

.kr-char {
  color: #fff;
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
}

h1 {
  font-size: 1.75rem;
  color: var(--text-title);
  font-weight: 800;
  margin-bottom: 0.4rem;
  letter-spacing: -0.5px;
}

.form-subtitle {
  color: var(--text-muted);
  font-size: 0.88rem;
}

/* ─── Form layout ─── */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.15rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.form-group label {
  font-size: 0.83rem;
  font-weight: 600;
  color: var(--text-title);
  letter-spacing: 0.01em;
}

/* ─── Input ─── */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.input-wrapper input {
  width: 100%;
  padding: 0.72rem 2.6rem 0.72rem 2.6rem;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--border-color);
  background-color: var(--bg-app);
  color: var(--text-title);
  font-size: 0.9rem;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), background-color var(--transition-fast);
}

.input-wrapper input:hover {
  border-color: var(--border-color-hover);
}

.input-wrapper input:focus {
  border-color: var(--primary);
  background-color: var(--bg-card);
  box-shadow: 0 0 0 3.5px var(--primary-glow);
  outline: none;
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

.eye-btn:hover { color: var(--primary); }

/* ─── Alerts ─── */
.error-box,
.success-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  font-weight: 500;
}

.error-box {
  background-color: var(--danger-light);
  border: 1px solid var(--danger);
  color: var(--danger);
}

.success-box {
  background-color: var(--success-light);
  border: 1px solid var(--success);
  color: var(--success);
}

/* ─── Submit button ─── */
.submit-btn {
  width: 100%;
  padding: 0.88rem;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border-radius: var(--radius-md);
  font-weight: 700;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  box-shadow: 0 4px 16px var(--primary-glow);
  margin-top: 0.3rem;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  letter-spacing: 0.02em;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--primary-glow);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

/* ─── Toggle mode ─── */
.toggle-mode {
  text-align: center;
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.toggle-mode a {
  color: var(--primary);
  font-weight: 600;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.toggle-mode a:hover {
  color: var(--primary-hover);
  text-decoration: underline;
}

/* ─── Checkbox Options ─── */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: -0.25rem;
  margin-bottom: 0.25rem;
}

.checkbox-container {
  display: flex;
  align-items: center;
  position: relative;
  padding-left: 28px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-muted);
  user-select: none;
  transition: color var(--transition-fast);
}

.checkbox-container:hover {
  color: var(--text-title);
}

.checkbox-container input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.checkmark {
  position: absolute;
  left: 0;
  height: 18px;
  width: 18px;
  background-color: var(--bg-app);
  border: 1.5px solid var(--border-color);
  border-radius: 6px;
  transition: all var(--transition-fast);
}

.checkbox-container:hover input ~ .checkmark {
  border-color: var(--border-color-hover);
}

.checkbox-container input:checked ~ .checkmark {
  background-color: var(--primary);
  border-color: var(--primary);
}

.checkmark:after {
  content: "";
  position: absolute;
  display: none;
}

.checkbox-container input:checked ~ .checkmark:after {
  display: block;
}

.checkbox-container .checkmark:after {
  left: 5px;
  top: 2px;
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

/* ─── Footer ─── */
.form-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* ─── Loader ─── */
.loader {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* ─── Social Login ─── */
.divider-or {
  display: flex;
  align-items: center;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.8rem;
  margin: 1.25rem 0;
}

.divider-or::before,
.divider-or::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid var(--border-color);
}

.divider-or:not(:empty)::before {
  margin-right: .75em;
}

.divider-or:not(:empty)::after {
  margin-left: .75em;
}

.social-login {
  margin-bottom: 1.25rem;
  display: flex;
  justify-content: center;
  width: 100%;
}

/* ─── Responsive ─── */
@media (max-width: 900px) {
  .deco-panel { display: none; }
  .form-panel { padding: 2rem 1rem; }
}

@media (max-width: 540px) {
  .form-card { padding: 2rem 1.5rem; border-radius: var(--radius-md); }
  .form-row { grid-template-columns: 1fr; }
}
</style>
