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
onMounted(async () => {
  // Load Schedule
  const savedSchedule = safeStorage.getItem('sk_schedule')
  if (savedSchedule) {
    schedule.value = JSON.parse(savedSchedule)
  } else {
    schedule.value = [
      { id: 1, day: 'Thứ 2', time: '18:00 - 19:30', activity: 'Lớp Sơ cấp 1A' },
      { id: 2, day: 'Thứ 4', time: '18:00 - 19:30', activity: 'Lớp Sơ cấp 1A' },
      { id: 3, day: 'Thứ 6', time: '18:00 - 19:30', activity: 'Lớp Sơ cấp 1A' }
    ]
    safeStorage.setItem('sk_schedule', JSON.stringify(schedule.value))
  }

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
