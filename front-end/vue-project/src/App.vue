<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from './components/icons/AppIcon.vue'
import LoginView from './components/LoginView.vue'
import DashboardView from './components/DashboardView.vue'
import VocabularyView from './components/VocabularyView.vue'
import QuizView from './components/QuizView.vue'
import ScheduleView from './components/ScheduleView.vue'
import ProfileView from './components/ProfileView.vue'

// Teacher components
import TeacherDashboard from './components/teacher/TeacherDashboard.vue'
import TeacherClasses from './components/teacher/TeacherClasses.vue'
import TeacherVocabulary from './components/teacher/TeacherVocabulary.vue'
import TeacherQuizzes from './components/teacher/TeacherQuizzes.vue'
import TeacherGrading from './components/teacher/TeacherGrading.vue'

import { useAuthStore } from './stores/auth'
import { useQuizStore } from './stores/quiz'
import { useStudySetStore } from './stores/studySet'
import api, { globalLoading } from './services/axios'



// Core reactive states
const authStore = useAuthStore()
const quizStore = useQuizStore()
const studySetStore = useStudySetStore()
const router = useRouter()

const user = computed({
  get: () => authStore.user,
  set: (val) => { authStore.user = val }
})
const studySets = computed(() => studySetStore.studySets)
const quizzes = computed(() => quizStore.quizzes)
const classes = computed(() => studySetStore.classes)
const schedule = computed(() => {
  const list = []
  const dbClasses = studySetStore.classes || []
  dbClasses.forEach((cls, idx) => {
    const rawSched = cls.schedule || "Thứ 2, 4, 6 (18:00 - 19:30)"
    const parts = rawSched.split('(')
    const dayStr = parts[0].trim()
    const timeStr = parts.length > 1 ? parts[1].replace(')', '').trim() : '18:00 - 19:30'
    
    const days = dayStr.split(',').map(d => d.trim())
    days.forEach((day, dayIdx) => {
      const today = new Date()
      const currentDay = today.getDay()
      let targetDayNum = 1
      if (day.includes('2')) targetDayNum = 1
      else if (day.includes('3')) targetDayNum = 2
      else if (day.includes('4')) targetDayNum = 3
      else if (day.includes('5')) targetDayNum = 4
      else if (day.includes('6')) targetDayNum = 5
      else if (day.includes('7')) targetDayNum = 6
      else if (day.toLowerCase().includes('chủ nhật')) targetDayNum = 0
      
      const diff = targetDayNum - currentDay
      const date = new Date(today)
      date.setDate(today.getDate() + diff)
      
      list.push({
        id: `${cls.id}-${dayIdx}`,
        date: date.toISOString().substring(0, 10),
        dayOfWeek: day,
        className: cls.name,
        time: timeStr,
        topic: `Bài học ôn tập & từ vựng tiếng Hàn - Lớp ${cls.name}`,
        teacher: cls.teacherName || 'Giáo viên Bản xứ',
        location: cls.room || 'Phòng học trực tuyến Zoom',
        status: date >= today ? 'upcoming' : 'completed'
      })
    })
  })
  return list
})

const isDark = ref(false)
const showNotificationDropdown = ref(false)

// Security notification banners
const securityBanner = ref(null) // { type: 'warning'|'error', message: string }
const showSecurityBanner = ref(false)

function triggerSecurityBanner(type, message) {
  securityBanner.value = { type, message }
  showSecurityBanner.value = true
  if (type === 'warning') {
    // Auto-dismiss warning sau 10 giây
    setTimeout(() => { showSecurityBanner.value = false }, 10000)
  }
}

// Safe Storage Wrapper to prevent environment-specific security crashes (e.g. cookies blocked)
const safeStorage = {
  getItem(key) {
    try {
      return localStorage.getItem(key)
    } catch (e) {
      console.warn("Storage access denied:", e)
      return null
    }
  },
  setItem(key, value) {
    try {
      localStorage.setItem(key, value)
    } catch (e) {
      console.warn("Storage write denied:", e)
    }
  },
  removeItem(key) {
    try {
      localStorage.removeItem(key)
    } catch (e) {
      console.warn("Storage remove denied:", e)
    }
  }
}

// Initialize states
onMounted(async () => {

  // Load from database if user is logged in
  if (user.value) {
    try {
      if (user.value.role === 'TEACHER') {
        await studySetStore.fetchStudySets();
        await quizStore.fetchMyCreatedQuizzes();
        await studySetStore.fetchClasses('TEACHER');
      } else {
        await studySetStore.fetchClasses('STUDENT');
        await quizStore.fetchMyAttempts();
        const studentClasses = studySetStore.classes || [];
        
        // Filter and map only study sets assigned to student's classes with class metadata
        const assignedSetsMap = new Map();
        studentClasses.forEach(cls => {
          if (cls.assignedStudySets && Array.isArray(cls.assignedStudySets)) {
            cls.assignedStudySets.forEach(as => {
              const className = `${cls.name} (${cls.teacherName || 'Giáo viên'})`;
              const existing = assignedSetsMap.get(as.studySetId);
              if (existing) {
                if (!existing.classNames.includes(className)) {
                  existing.classNames.push(className);
                }
              } else {
                assignedSetsMap.set(as.studySetId, {
                  id: as.studySetId,
                  title: as.studySetTitle,
                  description: as.studySetDescription,
                  wordCount: as.wordCount,
                  learnedCount: as.learnedCount || 0,
                  classNames: [className],
                  words: []
                });
              }
            });
          }
        });
        studySetStore.studySets = Array.from(assignedSetsMap.values());

        let allQuizzes = [];
        if (studentClasses.length > 0) {
          for (const cls of studentClasses) {
            try {
              const res = await api.get(`/quizzes/class/${cls.id}`);
              if (res && res.data) {
                allQuizzes = allQuizzes.concat(res.data);
              }
            } catch (e) {
              console.warn(`Failed to fetch quizzes for class ${cls.id}:`, e);
            }
          }
          quizStore.quizzes = allQuizzes;
        } else {
          await quizStore.fetchQuizzesByClass(1);
        }
      }
    } catch (e) {
      console.warn("Error loading database resources:", e);
    }

    // Hiện IP warning nếu có từ lần login trước
    if (user.value.ipWarning && user.value.warningMessage) {
      triggerSecurityBanner('warning', user.value.warningMessage)
    }
  }

  // Lắng nghe session-kicked event (bị kick do login thiết bị khác)
  window.addEventListener('session-kicked', (e) => {
    triggerSecurityBanner('error', e.detail.message)
  })

  // Lắng nghe account-locked event (bị lock do nhiều IP)
  window.addEventListener('account-locked', (e) => {
    triggerSecurityBanner('error', e.detail.message)
  })
})

// Authentication Handlers
const handleLoginSuccess = async (userData) => {
  user.value = userData
  safeStorage.setItem('sk_user', JSON.stringify(userData))
  
  // Seed notifications list for new general logins if empty
  if (!userData.notifications) {
    user.value.notifications = [
      { 
        id: 1, 
        title: 'Chào mừng bạn!', 
        content: 'Bắt đầu học tiếng Hàn ngay hôm nay nhé.', 
        sender: 'Hệ thống', 
        date: new Date().toISOString(), 
        isRead: false 
      }
    ]
    user.value.joinedClasses = []
    user.value.streak = 1
    user.value.xp = 0
    user.value.level = userData.role === 'TEACHER' ? 'Giáo viên' : 'Sơ cấp 1'
    safeStorage.setItem('sk_user', JSON.stringify(user.value))
  }

  // Fetch logged in user's resources
  try {
    if (userData.role === 'TEACHER') {
      await studySetStore.fetchStudySets();
      await quizStore.fetchMyCreatedQuizzes();
      await studySetStore.fetchClasses('TEACHER');
    } else {
      await studySetStore.fetchClasses('STUDENT');
      await quizStore.fetchMyAttempts();
      const studentClasses = studySetStore.classes || [];
      
      // Filter and map only study sets assigned to student's classes with class metadata
      const assignedSetsMap = new Map();
      studentClasses.forEach(cls => {
        if (cls.assignedStudySets && Array.isArray(cls.assignedStudySets)) {
          cls.assignedStudySets.forEach(as => {
            const className = `${cls.name} (${cls.teacherName || 'Giáo viên'})`;
            const existing = assignedSetsMap.get(as.studySetId);
            if (existing) {
              if (!existing.classNames.includes(className)) {
                existing.classNames.push(className);
              }
            } else {
              assignedSetsMap.set(as.studySetId, {
                id: as.studySetId,
                title: as.studySetTitle,
                description: as.studySetDescription,
                wordCount: as.wordCount,
                learnedCount: as.learnedCount || 0,
                classNames: [className],
                words: []
              });
            }
          });
        }
      });
      studySetStore.studySets = Array.from(assignedSetsMap.values());

      let allQuizzes = [];
      if (studentClasses.length > 0) {
        for (const cls of studentClasses) {
          try {
            const res = await api.get(`/quizzes/class/${cls.id}`);
            if (res && res.data) {
              allQuizzes = allQuizzes.concat(res.data);
            }
          } catch (e) {
            console.warn(`Failed to fetch quizzes for class ${cls.id}:`, e);
          }
        }
        quizStore.quizzes = allQuizzes;
      } else {
        await quizStore.fetchQuizzesByClass(1);
      }
    }
  } catch (e) {
    console.error("Error loading user resources after login:", e);
  }
  
  router.push(userData.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard')

  // Hiện IP warning ngay sau login nếu có
  if (userData.ipWarning && userData.warningMessage) {
    triggerSecurityBanner('warning', userData.warningMessage)
  }
}

const handleLogout = async () => {
  await authStore.logout()
  studySetStore.classes = []
  studySetStore.studySets = []
  quizStore.quizzes = []
  quizStore.pendingAttempts = []
  quizStore.studentAttempts = []
  router.push('/login')
}

// Global update Handlers
const handleUpdateVocabStatus = async ({ setId, wordId, status }) => {
  const setIndex = studySets.value.findIndex(s => s.id === setId)
  if (setIndex !== -1) {
    const wordIndex = studySets.value[setIndex].words.findIndex(w => w.id === wordId)
    if (wordIndex !== -1) {
      const oldStatus = studySets.value[setIndex].words[wordIndex].status
      studySets.value[setIndex].words[wordIndex].status = status
      safeStorage.setItem('sk_study_sets', JSON.stringify(studySets.value))
      
      try {
        await studySetStore.updateCardProgress(wordId, status)
        
        // Update XP for marking as learned
        if (status === 'learned' && oldStatus !== 'learned') {
          user.value.xp += 10
          user.value.level = `Sơ cấp ${Math.floor(user.value.xp / 1000) + 1}`
          safeStorage.setItem('sk_user', JSON.stringify(user.value))
        }
      } catch (e) {
        console.error("Failed to save card progress to server:", e)
        // Revert local status if failed
        studySets.value[setIndex].words[wordIndex].status = oldStatus
        safeStorage.setItem('sk_study_sets', JSON.stringify(studySets.value))
      }
    }
  }
}

const handleSubmitQuiz = async ({ quizId, score, userAnswers, completedAt }) => {
  if (!isNaN(Number(quizId))) {
    try {
      const answersPayload = userAnswers.map(ans => ({
        questionId: ans.questionId || ans.id,
        studentAnswer: ans.selectedAnswer || ans.writtenAnswer || '',
        timeTakenMs: ans.timeTakenMs || 5000
      }))
      
      await quizStore.submitQuizAttempt(quizId, answersPayload)
      if (user.value.role === 'TEACHER') {
        await quizStore.fetchMyCreatedQuizzes();
      } else {
        const studentClasses = studySetStore.classes || [];
        let allQuizzes = [];
        if (studentClasses.length > 0) {
          for (const cls of studentClasses) {
            try {
              const res = await api.get(`/quizzes/class/${cls.id}`);
              if (res && res.data) {
                allQuizzes = allQuizzes.concat(res.data);
              }
            } catch (e) {
              console.warn(`Failed to fetch quizzes for class ${cls.id}:`, e);
            }
          }
          quizStore.quizzes = allQuizzes;
        } else {
          await quizStore.fetchQuizzesByClass(1);
        }
        await quizStore.fetchMyAttempts();
      }
    } catch (e) {
      console.error("Failed to submit quiz attempt to API:", e)
    }
  } else {
    const quizIndex = quizStore.quizzes.findIndex(q => q.id === quizId)
    if (quizIndex !== -1) {
      quizStore.quizzes[quizIndex].status = 'completed'
      quizStore.quizzes[quizIndex].score = score
      quizStore.quizzes[quizIndex].userAnswers = userAnswers
      quizStore.quizzes[quizIndex].completedAt = completedAt
    }
  }
  
  // Reward XP based on score for both teacher assignments and self-practice quizzes
  if (user.value) {
    const xpReward = Math.round(score * 10)
    user.value.xp += xpReward
    user.value.level = `Sơ cấp ${Math.floor(user.value.xp / 1000) + 1}`
    safeStorage.setItem('sk_user', JSON.stringify(user.value))
  }
}

const handleMarkNotificationRead = (notifId) => {
  if (user.value && user.value.notifications) {
    const index = user.value.notifications.findIndex(n => n.id === notifId)
    if (index !== -1) {
      user.value.notifications[index].isRead = true
      safeStorage.setItem('sk_user', JSON.stringify(user.value))
    }
  }
}

const handleSaveProfile = async ({ name, email, avatar }) => {
  try {
    const response = await api.put('/users/profile', {
      fullName: name,
      email: email,
      avatarUrl: avatar
    })
    if (user.value && response.data) {
      const updated = response.data
      user.value.name = updated.fullName
      user.value.email = updated.email
      user.value.avatar = updated.avatarUrl
      safeStorage.setItem('sk_user', JSON.stringify(user.value))
    }
  } catch (error) {
    console.error("Failed to save profile:", error)
  }
}
</script>

<template>
  <router-view v-slot="{ Component }">
    <component
      :is="Component"
      :classes="classes"
      :study-sets="studySets"
      :quizzes="quizzes"
      :schedule="schedule"
      @update-vocab-status="handleUpdateVocabStatus"
      @submit-quiz="handleSubmitQuiz"
      @save-profile="handleSaveProfile"
      @mark-read="handleMarkNotificationRead"
      @login-success="handleLoginSuccess"
    />
  </router-view>

  <!-- Security Banner (IP Warning / Session Kicked) -->
  <Transition name="banner-slide">
    <div
      v-if="showSecurityBanner && securityBanner"
      :class="['security-banner', securityBanner.type === 'error' ? 'security-banner--error' : 'security-banner--warning']"
    >
      <span class="security-banner__icon">{{ securityBanner.type === 'error' ? '🔒' : '⚠️' }}</span>
      <span class="security-banner__msg">{{ securityBanner.message }}</span>
      <button class="security-banner__close" @click="showSecurityBanner = false">✕</button>
    </div>
  </Transition>

  <!-- Global Duck Loading Overlay -->
  <Transition name="duck-fade">
    <div v-if="globalLoading" class="global-duck-loading">
      <div class="duck-wrapper">
        <img src="./assets/duck-loading.png" alt="Duck Loading" class="duck-img">
        <div class="shadow"></div>
        <p class="loading-text">Đang xử lý, vui lòng đợi...</p>
      </div>
    </div>
  </Transition>
</template>

<style>
/* Duck Fade Transition */
.duck-fade-enter-active,
.duck-fade-leave-active {
  transition: opacity 0.25s ease;
}
.duck-fade-enter-from,
.duck-fade-leave-to {
  opacity: 0;
}
/* Global system transitions and fonts */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=Noto+Sans+KR:wght@300;400;500;700;900&display=swap');

.animate-fade {
  animation: fadeIn 0.4s ease-out;
}

.animate-scale {
  animation: scaleIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

/* Global Duck Loading Overlay Styles */
.global-duck-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  z-index: 99999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.duck-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.duck-img {
  width: 120px;
  height: 120px;
  animation: duckJump 0.8s infinite ease-in-out;
}

.shadow {
  width: 60px;
  height: 8px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  margin-top: 10px;
  animation: shadowScale 0.8s infinite ease-in-out;
}

.loading-text {
  margin-top: 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-title, #2d3748);
}

@keyframes duckJump {
  0%, 100% {
    transform: translateY(0) scaleY(1) scaleX(1);
  }
  40% {
    transform: translateY(-40px) scaleY(1.08) scaleX(0.95);
  }
  50% {
    transform: translateY(-40px) scaleY(1.08) scaleX(0.95);
  }
  85% {
    transform: translateY(0) scaleY(0.9) scaleX(1.1);
  }
}

@keyframes shadowScale {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  45%, 50% {
    transform: scale(0.4);
    opacity: 0.3;
  }
}

/* Security Banner */
.security-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.security-banner--warning {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
}

.security-banner--error {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
}

.security-banner__icon {
  font-size: 18px;
  flex-shrink: 0;
}

.security-banner__msg {
  flex: 1;
  line-height: 1.4;
}

.security-banner__close {
  background: rgba(255, 255, 255, 0.25);
  border: none;
  color: #fff;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}

.security-banner__close:hover {
  background: rgba(255, 255, 255, 0.4);
}

/* Banner slide animation */
.banner-slide-enter-active,
.banner-slide-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.banner-slide-enter-from,
.banner-slide-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}
</style>
