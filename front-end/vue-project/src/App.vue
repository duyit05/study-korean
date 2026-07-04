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
import { globalLoading } from './services/axios'



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
      await studySetStore.fetchStudySets();
      if (user.value.role === 'TEACHER') {
        await quizStore.fetchMyCreatedQuizzes();
        await studySetStore.fetchClasses('TEACHER');
      } else {
        await quizStore.fetchQuizzesByClass(1);
        await studySetStore.fetchClasses('STUDENT');
      }
    } catch (e) {
      console.warn("Error loading database resources:", e);
    }
  }
})

// Authentication Handlers
const handleLoginSuccess = async (userData) => {
  user.value = userData
  safeStorage.setItem('sk_user', JSON.stringify(userData))
  
  // Seed notifications list for new general logins if empty
  if (!userData.notifications) {
    user.value.notifications = [
      { id: 1, title: 'Chào mừng bạn!', message: 'Bắt đầu học tiếng Hàn ngay hôm nay nhé.', isRead: false }
    ]
    user.value.joinedClasses = []
    user.value.streak = 1
    user.value.xp = 0
    user.value.level = userData.role === 'TEACHER' ? 'Giáo viên' : 'Sơ cấp 1'
    safeStorage.setItem('sk_user', JSON.stringify(user.value))
  }

  // Fetch logged in user's resources
  try {
    await studySetStore.fetchStudySets();
    if (userData.role === 'TEACHER') {
      await quizStore.fetchMyCreatedQuizzes();
      await studySetStore.fetchClasses('TEACHER');
    } else {
      await quizStore.fetchQuizzesByClass(1);
      await studySetStore.fetchClasses('STUDENT');
    }
  } catch (e) {
    console.error("Error loading user resources after login:", e);
  }
  
  router.push(userData.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard')
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

// Global update Handlers
const handleUpdateVocabStatus = ({ setId, wordId, status }) => {
  const setIndex = studySets.value.findIndex(s => s.id === setId)
  if (setIndex !== -1) {
    const wordIndex = studySets.value[setIndex].words.findIndex(w => w.id === wordId)
    if (wordIndex !== -1) {
      studySets.value[setIndex].words[wordIndex].status = status
      safeStorage.setItem('sk_study_sets', JSON.stringify(studySets.value))
      
      // Update XP for marking as learned
      if (status === 'learned') {
        user.value.xp += 10
        safeStorage.setItem('sk_user', JSON.stringify(user.value))
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
        await quizStore.fetchQuizzesByClass(1);
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

const handleSaveProfile = ({ name, email, avatar }) => {
  if (user.value) {
    user.value.name = name
    user.value.email = email
    user.value.avatar = avatar
    safeStorage.setItem('sk_user', JSON.stringify(user.value))
  }
}
</script>

<template>
  <router-view 
    :study-sets="studySets"
    :quizzes="quizzes"
    :schedule="schedule"
    @update-vocab-status="handleUpdateVocabStatus"
    @submit-quiz="handleSubmitQuiz"
    @save-profile="handleSaveProfile"
    @mark-read="handleMarkNotificationRead"
    @login-success="handleLoginSuccess"
  />

  <!-- Global Duck Loading Overlay -->
  <div v-if="globalLoading" class="global-duck-loading">
    <div class="duck-wrapper">
      <img src="./assets/duck-loading.png" alt="Duck Loading" class="duck-img">
      <div class="shadow"></div>
      <p class="loading-text">Đang xử lý, vui lòng đợi...</p>
    </div>
  </div>
</template>

<style>
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
  font-family: 'Inter', sans-serif;
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
</style>
