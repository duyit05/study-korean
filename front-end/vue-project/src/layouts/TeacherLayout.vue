<template>
  <div class="teacher-layout">
    <!-- Overlay when sidebar is open on mobile -->
    <div 
      v-if="isSidebarOpen" 
      class="sidebar-overlay" 
      @click="isSidebarOpen = false"
    ></div>

    <!-- Sidebar Navigation -->
    <aside class="sidebar" :class="{ 'open': isSidebarOpen }">
      <div class="sidebar-brand">
        <div class="brand-header-mobile">
          <div class="logo-box">한</div>
          <button class="close-sidebar-btn" @click="isSidebarOpen = false" title="Đóng menu">
            &times;
          </button>
        </div>
        <h2>Study Korea</h2>
        <span class="admin-badge">TEACHER PORTAL</span>
      </div>

      <!-- Navigation links -->
      <nav class="sidebar-nav">
        <router-link to="/teacher/dashboard" class="sidebar-link" active-class="active">
          <AppIcon name="dashboard" size="18" />
          <span>Tổng quan</span>
        </router-link>
        
        <router-link to="/teacher/classes" class="sidebar-link" active-class="active">
          <AppIcon name="user" size="18" />
          <span>Quản lý Lớp học</span>
        </router-link>

        <router-link to="/teacher/courses" class="sidebar-link" active-class="active">
          <AppIcon name="award" size="18" />
          <span>Quản lý Khóa học</span>
        </router-link>

        <router-link to="/teacher/vocabulary" class="sidebar-link" active-class="active">
          <AppIcon name="book" size="18" />
          <span>Kho từ vựng</span>
        </router-link>

        <router-link to="/teacher/quizzes" class="sidebar-link" active-class="active">
          <AppIcon name="quiz" size="18" />
          <span>Đề thi & Bài tập</span>
        </router-link>

        <router-link to="/teacher/grading" class="sidebar-link" active-class="active">
          <AppIcon name="edit" size="18" />
          <span>Chấm điểm Viết</span>
        </router-link>

        <router-link to="/teacher/topik-levels" class="sidebar-link" active-class="active">
          <AppIcon name="settings" size="18" />
          <span>Quản lý level</span>
        </router-link>

        <router-link to="/teacher/students" class="sidebar-link" active-class="active">
          <AppIcon name="profile" size="18" />
          <span>Quản lý học viên</span>
        </router-link>
      </nav>

      <!-- Sidebar Footer / Teacher Profile -->
      <div class="sidebar-footer">
        <div class="profile-info" @click="navigateToProfile" style="cursor: pointer;" title="Xem hồ sơ cá nhân">
          <img 
            :src="user.avatar || 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=64'" 
            alt="Teacher Avatar" 
            class="avatar"
          >
          <div class="profile-text">
            <strong>{{ user.name }}</strong>
            <span>{{ user.level || 'Giáo viên' }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout" title="Đăng xuất">
          <AppIcon name="logout" size="18" />
        </button>
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="main-container">
      <header class="top-header">
        <div class="header-left-mobile" style="display: flex; align-items: center; gap: 0.75rem;">
          <button class="menu-toggle-btn" @click="isSidebarOpen = !isSidebarOpen" title="Mở menu">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="4" x2="20" y1="12" y2="12"></line>
              <line x1="4" x2="20" y1="6" y2="6"></line>
              <line x1="4" x2="20" y1="18" y2="18"></line>
            </svg>
          </button>
          <div class="view-title">
            <h3>Hệ thống quản lý</h3>
          </div>
        </div>
        <div class="header-actions">
          <button class="theme-btn" @click="toggleTheme" title="Chuyển chế độ Sáng/Tối">
            <AppIcon :name="isDark ? 'sun' : 'moon'" size="20" />
          </button>
        </div>
      </header>

      <main class="content-view">
        <router-view 
          :user="user"
          :study-sets="studySets"
          :quizzes="quizzes"
          @save-profile="$emit('save-profile', $event)"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AppIcon from '../components/icons/AppIcon.vue'

const props = defineProps({
  studySets: Array,
  quizzes: Array
})

defineEmits(['save-profile'])

const router = useRouter()
const authStore = useAuthStore()
const user = computed(() => authStore.user)

const isDark = ref(false)
const isSidebarOpen = ref(false)

router.afterEach(() => {
  isSidebarOpen.value = false
})

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

const handleLogout = async () => {
  if (confirm("Bạn có chắc chắn muốn đăng xuất khỏi trang giáo viên?")) {
    await authStore.logout()
    router.push('/login')
  }
}

const navigateToProfile = () => {
  router.push('/teacher/profile')
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
.teacher-layout {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-body);
  color: var(--text-title);
}

/* Sidebar Styling */
.sidebar {
  width: 260px;
  background-color: var(--bg-card);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  box-shadow: var(--shadow-sm);
  z-index: 10;
  transition: transform var(--transition-normal, 0.3s) ease;
}

.brand-header-mobile {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.sidebar-overlay {
  display: none;
}

.close-sidebar-btn {
  display: none;
}

.menu-toggle-btn {
  display: none;
}

@media (max-width: 768px) {
  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 99;
    backdrop-filter: blur(2px);
  }

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    height: 100vh;
    z-index: 100;
    transform: translateX(-100%);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .close-sidebar-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background: none;
    border: none;
    color: var(--text-muted);
    font-size: 1.8rem;
    cursor: pointer;
    line-height: 1;
    padding: 0.25rem;
  }

  .close-sidebar-btn:hover {
    color: var(--danger);
  }

  .menu-toggle-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--bg-card);
    border: 1px solid var(--border-color);
    color: var(--text-title);
    width: 36px;
    height: 36px;
    border-radius: var(--radius-sm, 6px);
    cursor: pointer;
    transition: all var(--transition-fast, 0.2s);
  }

  .menu-toggle-btn:hover {
    background-color: var(--bg-body);
    border-color: var(--primary);
    color: var(--primary);
  }
}

.sidebar-brand {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.sidebar-brand .logo-box {
  width: 32px;
  height: 32px;
  background-color: var(--primary);
  color: #fff;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.sidebar-brand h2 {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--text-title);
  letter-spacing: -0.5px;
}

.admin-badge {
  font-size: 0.65rem;
  font-weight: 700;
  color: var(--primary);
  background-color: var(--primary-glow);
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  align-self: flex-start;
  letter-spacing: 0.5px;
}

.sidebar-nav {
  flex-grow: 1;
  padding: 1.25rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 0.9rem;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.sidebar-link:hover {
  background-color: var(--bg-body);
  color: var(--text-title);
}

.sidebar-link.active {
  background-color: var(--primary-glow);
  color: var(--primary);
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.profile-info .avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--border-color);
}

.profile-text {
  display: flex;
  flex-direction: column;
}

.profile-text strong {
  font-size: 0.85rem;
  color: var(--text-title);
}

.profile-text span {
  font-size: 0.7rem;
  color: var(--text-muted);
}

.logout-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 0.35rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  background-color: var(--bg-body);
  color: var(--danger);
}

/* Main Container Area */
.main-container {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  overflow-x: hidden;
}

.top-header {
  height: 60px;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  padding: 0 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 5;
}

.view-title h3 {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.theme-btn {
  width: 36px;
  height: 36px;
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

.theme-btn:hover {
  background-color: var(--bg-body);
  color: var(--primary);
  border-color: var(--primary);
}

.content-view {
  flex-grow: 1;
  padding: 0.5rem;
  background-color: var(--bg-body);
}
</style>
