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

import {
  defaultUserData,
  defaultStudySets,
  defaultQuizzes,
  defaultSchedule
} from './mockData.js'

// Core reactive states
const authStore = useAuthStore()
const router = useRouter()
const user = computed({
  get: () => authStore.user,
  set: (val) => { authStore.user = val }
})
const studySets = ref([])
const quizzes = ref([])
const schedule = ref([])

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
onMounted(() => {
  // Load Study Sets
  const savedSets = safeStorage.getItem('sk_study_sets')
  if (savedSets) {
    studySets.value = JSON.parse(savedSets)
  } else {
    studySets.value = defaultStudySets
    safeStorage.setItem('sk_study_sets', JSON.stringify(defaultStudySets))
  }

  // Load Quizzes
  const savedQuizzes = safeStorage.getItem('sk_quizzes')
  if (savedQuizzes) {
    quizzes.value = JSON.parse(savedQuizzes)
  } else {
    quizzes.value = defaultQuizzes
    safeStorage.setItem('sk_quizzes', JSON.stringify(defaultQuizzes))
  }

  // Load Schedule
  const savedSchedule = safeStorage.getItem('sk_schedule')
  if (savedSchedule) {
    schedule.value = JSON.parse(savedSchedule)
  } else {
    schedule.value = defaultSchedule
    safeStorage.setItem('sk_schedule', JSON.stringify(defaultSchedule))
  }
})

// Authentication Handlers
const handleLoginSuccess = (userData) => {
  user.value = userData
  safeStorage.setItem('sk_user', JSON.stringify(userData))
  
  // Seed notifications list for new general logins if empty
  if (!userData.notifications) {
    user.value.notifications = [...defaultUserData.notifications]
    user.value.joinedClasses = [...defaultUserData.joinedClasses]
    user.value.streak = defaultUserData.streak
    user.value.xp = defaultUserData.xp
    user.value.level = defaultUserData.level
    safeStorage.setItem('sk_user', JSON.stringify(user.value))
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

const handleSubmitQuiz = ({ quizId, score, userAnswers, completedAt }) => {
  const quizIndex = quizzes.value.findIndex(q => q.id === quizId)
  if (quizIndex !== -1) {
    quizzes.value[quizIndex].status = 'completed'
    quizzes.value[quizIndex].score = score
    quizzes.value[quizIndex].userAnswers = userAnswers
    quizzes.value[quizIndex].completedAt = completedAt
    
    safeStorage.setItem('sk_quizzes', JSON.stringify(quizzes.value))
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
</style>
