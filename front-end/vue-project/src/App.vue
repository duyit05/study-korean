<script setup>
import { ref, onMounted } from 'vue'
import AppIcon from './components/icons/AppIcon.vue'
import LoginView from './components/LoginView.vue'
import DashboardView from './components/DashboardView.vue'
import VocabularyView from './components/VocabularyView.vue'
import QuizView from './components/QuizView.vue'
import ScheduleView from './components/ScheduleView.vue'
import ProfileView from './components/ProfileView.vue'

import {
  defaultUserData,
  defaultStudySets,
  defaultQuizzes,
  defaultSchedule
} from './mockData.js'

// Core reactive states
const user = ref(null)
const studySets = ref([])
const quizzes = ref([])
const schedule = ref([])

const currentView = ref('dashboard') // dashboard, vocabulary, quizzes, schedule, profile
const isDark = ref(false)
const showNotificationDropdown = ref(false)

// Initialize states
onMounted(() => {
  // Load Theme
  const savedTheme = localStorage.getItem('theme')
  isDark.value = savedTheme === 'dark'
  updateThemeClass()

  // Load User
  const savedUser = localStorage.getItem('sk_user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
  }

  // Load Study Sets
  const savedSets = localStorage.getItem('sk_study_sets')
  if (savedSets) {
    studySets.value = JSON.parse(savedSets)
  } else {
    studySets.value = defaultStudySets
    localStorage.setItem('sk_study_sets', JSON.stringify(defaultStudySets))
  }

  // Load Quizzes
  const savedQuizzes = localStorage.getItem('sk_quizzes')
  if (savedQuizzes) {
    quizzes.value = JSON.parse(savedQuizzes)
  } else {
    quizzes.value = defaultQuizzes
    localStorage.setItem('sk_quizzes', JSON.stringify(defaultQuizzes))
  }

  // Load Schedule
  const savedSchedule = localStorage.getItem('sk_schedule')
  if (savedSchedule) {
    schedule.value = JSON.parse(savedSchedule)
  } else {
    schedule.value = defaultSchedule
    localStorage.setItem('sk_schedule', JSON.stringify(defaultSchedule))
  }
})

// Authentication Handlers
const handleLoginSuccess = (userData) => {
  user.value = userData
  localStorage.setItem('sk_user', JSON.stringify(userData))
  
  // Seed notifications list for new general logins if empty
  if (!userData.notifications) {
    user.value.notifications = [...defaultUserData.notifications]
    user.value.joinedClasses = [...defaultUserData.joinedClasses]
    user.value.streak = defaultUserData.streak
    user.value.xp = defaultUserData.xp
    user.value.level = defaultUserData.level
    localStorage.setItem('sk_user', JSON.stringify(user.value))
  }
  
  currentView.value = 'dashboard'
}

const handleLogout = () => {
  if (confirm("Bạn có chắc chắn muốn đăng xuất?")) {
    user.value = null
    localStorage.removeItem('sk_user')
    currentView.value = 'dashboard'
  }
}

// Global update Handlers
const handleUpdateVocabStatus = ({ setId, wordId, status }) => {
  const setIndex = studySets.value.findIndex(s => s.id === setId)
  if (setIndex !== -1) {
    const wordIndex = studySets.value[setIndex].words.findIndex(w => w.id === wordId)
    if (wordIndex !== -1) {
      studySets.value[setIndex].words[wordIndex].status = status
      localStorage.setItem('sk_study_sets', JSON.stringify(studySets.value))
      
      // Update XP for marking as learned
      if (status === 'learned') {
        user.value.xp += 10
        localStorage.setItem('sk_user', JSON.stringify(user.value))
      }
    }
  }
}

const handleSubmitQuiz = ({ quizId, score, userAnswers, completedAt }) => {
  const quizIndex = quizzes.value.findIndex(q => q.id === quizId)
  if (quizIndex !== -1) {
    quizzes.value[quizIndex].status = 'completed'
    quizzes.value[quizIndex].score = score
    quizzes.value[quizIndex].userAnswers = userAnswers
    quizzes.value[quizIndex].completedAt = completedAt
    
    localStorage.setItem('sk_quizzes', JSON.stringify(quizzes.value))
    
    // Reward XP based on score
    const xpReward = Math.round(score * 10)
    user.value.xp += xpReward
    localStorage.setItem('sk_user', JSON.stringify(user.value))
  }
}

const handleMarkNotificationRead = (notifId) => {
  if (user.value && user.value.notifications) {
    const index = user.value.notifications.findIndex(n => n.id === notifId)
    if (index !== -1) {
      user.value.notifications[index].isRead = true
      localStorage.setItem('sk_user', JSON.stringify(user.value))
    }
  }
}

const handleSaveProfile = ({ name, email, avatar }) => {
  if (user.value) {
    user.value.name = name
    user.value.email = email
    user.value.avatar = avatar
    localStorage.setItem('sk_user', JSON.stringify(user.value))
  }
}

// Theme Handlers
const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  updateThemeClass()
}

const updateThemeClass = () => {
  if (isDark.value) {
    document.body.classList.add('dark-theme')
  } else {
    document.body.classList.remove('dark-theme')
  }
}

// Notification unread count helper
const unreadNotifCount = () => {
  if (!user.value || !user.value.notifications) return 0
  return user.value.notifications.filter(n => !n.isRead).length
}

const navigateTo = (viewName) => {
  currentView.value = viewName
  showNotificationDropdown.value = false
}
</script>

<template>
  <!-- LOGGED OUT SHELL (Login Screen) -->
  <LoginView v-if="!user" @login-success="handleLoginSuccess" />

  <!-- LOGGED IN SHELL -->
  <div v-else class="app-layout">
    <!-- Navbar / Header -->
    <header class="app-header">
      <div class="header-logo" @click="navigateTo('dashboard')">
        <div class="logo-box">한</div>
        <h1>Study Korea</h1>
      </div>

      <!-- Navigation tabs -->
      <nav class="nav-links">
        <button 
          class="nav-tab-btn" 
          :class="{ active: currentView === 'dashboard' }"
          @click="navigateTo('dashboard')"
        >
          <AppIcon name="dashboard" size="18" />
          <span>Tổng quan</span>
        </button>
        
        <button 
          class="nav-tab-btn" 
          :class="{ active: currentView === 'vocabulary' }"
          @click="navigateTo('vocabulary')"
        >
          <AppIcon name="book" size="18" />
          <span>Từ vựng</span>
        </button>

        <button 
          class="nav-tab-btn" 
          :class="{ active: currentView === 'quizzes' }"
          @click="navigateTo('quizzes')"
        >
          <AppIcon name="quiz" size="18" />
          <span>Bài tập</span>
        </button>

        <button 
          class="nav-tab-btn" 
          :class="{ active: currentView === 'schedule' }"
          @click="navigateTo('schedule')"
        >
          <AppIcon name="schedule" size="18" />
          <span>Lịch học</span>
        </button>
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
                @click="navigateTo('dashboard')"
              >
                <h5>{{ notif.title }}</h5>
                <p>{{ notif.content.substring(0, 50) }}...</p>
              </div>
              <div v-if="user.notifications.length === 0" class="dropdown-empty">
                Không có thông báo.
              </div>
            </div>
            <button class="dropdown-footer" @click="navigateTo('dashboard')">
              Xem tất cả thông báo
            </button>
          </div>
        </div>

        <!-- Student profile shortcut / Logout -->
        <div class="user-profile-menu">
          <div class="avatar" @click="navigateTo('profile')" title="Xem hồ sơ cá nhân">
            <img :src="user.avatar || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=64'" alt="User Profile">
          </div>
          <div class="user-details">
            <span class="name" @click="navigateTo('profile')">{{ user.name }}</span>
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
      <DashboardView 
        v-if="currentView === 'dashboard'" 
        :user="user"
        :study-sets="studySets"
        :quizzes="quizzes"
        :schedule="schedule"
        @navigate="navigateTo"
        @mark-read="handleMarkNotificationRead"
      />

      <VocabularyView 
        v-else-if="currentView === 'vocabulary'" 
        :study-sets="studySets"
        @update-vocab-status="handleUpdateVocabStatus"
      />

      <QuizView 
        v-else-if="currentView === 'quizzes'" 
        :quizzes="quizzes"
        @submit-quiz="handleSubmitQuiz"
      />

      <ScheduleView 
        v-else-if="currentView === 'schedule'" 
        :schedule="schedule"
      />

      <ProfileView 
        v-else-if="currentView === 'profile'" 
        :user="user"
        :study-sets="studySets"
        :quizzes="quizzes"
        @save-profile="handleSaveProfile"
      />
    </main>
  </div>
</template>

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
  font-size: 1.2rem;
  font-weight: 800;
  color: var(--text-title);
  letter-spacing: -0.5px;
}

/* Tabs list */
.nav-links {
  display: flex;
  gap: 0.5rem;
  height: 100%;
}

.nav-tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0 1.25rem;
  color: var(--text-body);
  font-weight: 600;
  font-size: 0.9rem;
  height: 100%;
  border-bottom: 3px solid transparent;
  transition: all var(--transition-fast);
}

.nav-tab-btn:hover {
  color: var(--primary);
  background-color: var(--bg-hover);
}

.nav-tab-btn.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
  background-color: var(--primary-light);
}

/* Header toolbar actions */
.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--bg-badge);
  color: var(--text-title);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  border: 1px solid var(--border-color);
}

.action-btn:hover {
  background-color: var(--border-color-hover);
  color: var(--primary);
  transform: translateY(-1px);
}

/* Notifications drop overlay */
.notification-trigger-wrapper {
  position: relative;
}

.action-btn.bell.active {
  color: var(--primary);
}

.notif-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background-color: var(--danger);
  border: 2px solid var(--bg-card);
  border-radius: 50%;
}

.notif-dropdown {
  position: absolute;
  right: 0;
  top: 48px;
  width: 320px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  z-index: 1000;
}

.dropdown-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-header h4 {
  font-size: 0.9rem;
  font-weight: 700;
}

.dropdown-header .count {
  font-size: 0.75rem;
  color: var(--primary);
  font-weight: 600;
}

.dropdown-body {
  max-height: 240px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.dropdown-item:hover {
  background-color: var(--bg-hover);
}

.dropdown-item.unread {
  background-color: var(--primary-glow);
}

.dropdown-item h5 {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.15rem;
}

.dropdown-item p {
  font-size: 0.75rem;
  color: var(--text-body);
}

.dropdown-empty {
  padding: 1.5rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.85rem;
}

.dropdown-footer {
  width: 100%;
  padding: 0.6rem;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 600;
  background-color: var(--bg-badge);
  border-top: 1px solid var(--border-color);
  color: var(--primary);
}

/* User identity header widget */
.user-profile-menu {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border-left: 1px solid var(--border-color);
  padding-left: 1.25rem;
  height: 40px;
}

.user-profile-menu .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--primary-light);
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
  color: var(--text-muted);
  padding: 0.25rem;
}

.logout-btn:hover {
  color: var(--danger);
  transform: translateX(1px);
}

/* Page content space */
.app-main-content {
  flex-grow: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 2rem;
}

/* Responsive headers */
@media (max-width: 860px) {
  .app-header {
    height: auto;
    padding: 1rem;
    flex-direction: column;
    gap: 1rem;
  }
  .nav-links {
    width: 100%;
    justify-content: space-around;
    height: 48px;
  }
  .nav-tab-btn {
    padding: 0 0.5rem;
  }
  .user-profile-menu {
    border-left: none;
    padding-left: 0;
  }
}

@media (max-width: 520px) {
  .nav-tab-btn span {
    display: none; /* Hide labels on mobile text layout */
  }
  .user-details {
    display: none;
  }
}
</style>
