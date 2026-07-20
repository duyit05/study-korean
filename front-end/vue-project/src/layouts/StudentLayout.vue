<template>
  <div class="app-layout student-theme">
    <!-- Navbar / Header -->
    <header class="app-header">
      <div class="header-logo" @click="navigateTo('/student/dashboard')">
        <div class="logo-box">한</div>
        <h1>Study Korea</h1>
      </div>

      <!-- Navigation tabs for Student -->
      <nav class="nav-links">
        <router-link 
          to="/student/dashboard" 
          class="nav-tab-btn" 
          active-class="active"
        >
          <AppIcon name="dashboard" size="18" />
          <span>Tổng quan</span>
        </router-link>
        
        <router-link 
          to="/student/vocabulary" 
          class="nav-tab-btn" 
          active-class="active"
        >
          <AppIcon name="book" size="18" />
          <span>Từ vựng</span>
        </router-link>

        <router-link 
          to="/student/quizzes" 
          class="nav-tab-btn" 
          active-class="active"
        >
          <AppIcon name="quiz" size="18" />
          <span>Bài tập</span>
        </router-link>

        <router-link 
          to="/student/practice" 
          class="nav-tab-btn" 
          active-class="active"
        >
          <AppIcon name="award" size="18" />
          <span>Ôn tập</span>
        </router-link>

        <router-link 
          to="/student/schedule" 
          class="nav-tab-btn" 
          active-class="active"
        >
          <AppIcon name="schedule" size="18" />
          <span>Lịch học</span>
        </router-link>
      </nav>

      <!-- Right Toolbar Actions -->
      <div class="header-actions">
        <!-- Light / Dark toggle -->
        <button class="action-btn theme" @click="toggleTheme" title="Chuyển chế độ Sáng/Tối">
          <AppIcon :name="isDark ? 'sun' : 'moon'" size="20" />
        </button>

        <!-- Notification Drawer Toggle -->
        <div class="notification-trigger-wrapper">
          <button 
            class="action-btn bell" 
            :class="{ active: unreadNotifCount() > 0 }"
            @click="showNotificationDropdown = !showNotificationDropdown"
            title="Xem thông báo"
          >
            <AppIcon name="bell" size="20" />
            <span v-if="unreadNotifCount() > 0" class="notif-badge"></span>
          </button>
          
          <!-- Dropdown -->
          <div v-if="showNotificationDropdown" class="notif-dropdown animate-scale">
            <div class="dropdown-header">
              <h4>Thông báo</h4>
              <span class="count">{{ unreadNotifCount() }} mới</span>
            </div>
            <div class="dropdown-body">
              <div 
                v-for="notif in user.notifications.slice(0, 3)" 
                :key="notif.id" 
                class="dropdown-item"
                :class="{ unread: !notif.isRead }"
                @click="navigateTo('/student/dashboard')"
              >
                <h5>{{ notif.title }}</h5>
                <p>{{ notif.content.substring(0, 50) }}...</p>
              </div>
              <div v-if="user.notifications.length === 0" class="dropdown-empty">
                Không có thông báo.
              </div>
            </div>
            <button class="dropdown-footer" @click="navigateTo('/student/dashboard')">
              Xem tất cả thông báo
            </button>
          </div>
        </div>

        <!-- Student profile shortcut / Logout -->
        <div class="user-profile-menu">
          <div class="avatar" @click="navigateTo('/student/profile')" title="Xem hồ sơ cá nhân">
            <img :src="user.avatar || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=64'" alt="User Profile">
          </div>
          <div class="user-details">
            <span class="name" @click="navigateTo('/student/profile')">{{ user.name }}</span>
            <span class="level">{{ user.level }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout" title="Đăng xuất">
            <AppIcon name="logout" size="18" />
          </button>
        </div>
      </div>
    </header>

    <!-- Main View Content Area -->
    <main class="app-main-content">
      <router-view 
        :user="user"
        :classes="classes"
        :study-sets="studySets"
        :quizzes="quizzes"
        :schedule="schedule"
        @navigate="navigateTo"
        @mark-read="$emit('mark-read', $event)"
        @update-vocab-status="$emit('update-vocab-status', $event)"
        @submit-quiz="$emit('submit-quiz', $event)"
        @save-profile="$emit('save-profile', $event)"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppIcon from '../components/icons/AppIcon.vue'

const props = defineProps({
  classes: Array,
  studySets: Array,
  quizzes: Array,
  schedule: Array
})

defineEmits(['mark-read', 'update-vocab-status', 'submit-quiz', 'save-profile'])

const router = useRouter()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const isDark = ref(false)
const showNotificationDropdown = ref(false)

const unreadNotifCount = () => {
  if (!user.value || !user.value.notifications) return 0
  return user.value.notifications.filter(n => !n.isRead).length
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  try {
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  } catch (e) {}
  updateThemeClass()
}

const updateThemeClass = () => {
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

const navigateTo = (path) => {
  router.push(path)
  showNotificationDropdown.value = false
}

const handleLogout = () => {
  if (confirm("Bạn có chắc chắn muốn đăng xuất?")) {
    authStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  try {
    const savedTheme = localStorage.getItem('theme')
    isDark.value = savedTheme === 'dark'
    updateThemeClass()
  } catch (e) {}
})
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* Header style rules */
.app-header {
  height: 72px;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
  transition: background-color var(--transition-normal), border-color var(--transition-normal);
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.logo-box {
  width: 36px;
  height: 36px;
  background-color: var(--primary);
  color: #fff;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 1.25rem;
  font-weight: 700;
}

.header-logo h1 {
  font-size: 1.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--text-title), var(--primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

.nav-links {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.nav-tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 0.9rem;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.nav-tab-btn:hover {
  background-color: var(--bg-body);
  color: var(--text-title);
}

.nav-tab-btn.active {
  background-color: var(--primary-glow);
  color: var(--primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-title);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background-color: var(--bg-body);
  border-color: var(--primary);
  color: var(--primary);
}

.notification-trigger-wrapper {
  position: relative;
}

.action-btn.bell.active {
  color: var(--primary);
  border-color: var(--primary-glow);
  background-color: var(--primary-glow);
}

.notif-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 8px;
  height: 8px;
  background-color: var(--danger);
  border-radius: 50%;
  border: 2px solid var(--bg-card);
}

.notif-dropdown {
  position: absolute;
  top: 50px;
  right: 0;
  width: 320px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 200;
}

.dropdown-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-header h4 {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--text-title);
}

.dropdown-header .count {
  font-size: 0.75rem;
  background-color: var(--primary-glow);
  color: var(--primary);
  padding: 0.15rem 0.5rem;
  border-radius: 10px;
  font-weight: 600;
}

.dropdown-body {
  max-height: 280px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.dropdown-item:hover {
  background-color: var(--bg-body);
}

.dropdown-item.unread {
  background-color: rgba(var(--primary-rgb), 0.03);
}

.dropdown-item h5 {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.dropdown-item p {
  font-size: 0.75rem;
  color: var(--text-muted);
  line-height: 1.4;
}

.dropdown-empty {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.85rem;
}

.dropdown-footer {
  width: 100%;
  padding: 0.75rem;
  background: transparent;
  border: none;
  border-top: 1px solid var(--border-color);
  color: var(--primary);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
}

.dropdown-footer:hover {
  background-color: var(--bg-body);
  text-decoration: underline;
}

.user-profile-menu {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-left: 1px solid var(--border-color);
  padding-left: 1.25rem;
}

.user-profile-menu .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border-color);
  transition: border-color var(--transition-fast);
}

.user-profile-menu .avatar:hover {
  border-color: var(--primary);
}

.user-profile-menu .avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-details .name {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-title);
  cursor: pointer;
}

.user-details .name:hover {
  color: var(--primary);
}

.user-details .level {
  font-size: 0.7rem;
  color: var(--text-muted);
}

.logout-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0.25rem;
  margin-left: 0.25rem;
  display: flex;
  align-items: center;
  transition: color var(--transition-fast);
}

.logout-btn:hover {
  color: var(--danger);
}

.app-main-content {
  flex-grow: 1;
  padding: 2rem;
  background-color: var(--bg-body);
  transition: background-color var(--transition-normal);
}

@media (max-width: 768px) {
  .app-main-content {
    padding: 1rem;
  }
}
</style>
