<template>
  <div class="dashboard-container animate-fade">
    <!-- Header Greeting -->
    <div class="welcome-banner">
      <div class="banner-text">
        <h2>{{ greetingMessage }}, {{ user.name }}! 👋</h2>
        <p>Hôm nay là một ngày tuyệt vời để nâng cao tiếng Hàn của bạn. Hãy duy trì chuỗi học tập nhé!</p>
      </div>
      <div class="streak-badge">
        <AppIcon name="streak" class="streak-icon animate-pulse" size="28" />
        <div class="streak-info">
          <span class="count">{{ user.streak }} Ngày</span>
          <span class="label">Chuỗi học tập</span>
        </div>
      </div>
    </div>

    <!-- Quick Stats Grid -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon xp">
          <AppIcon name="award" size="24" />
        </div>
        <div class="stat-content">
          <span class="value">{{ user.xp }} XP</span>
          <span class="label">Điểm kinh nghiệm</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon vocab">
          <AppIcon name="book" size="24" />
        </div>
        <div class="stat-content">
          <span class="value">{{ vocabStats.learned }} / {{ vocabStats.total }}</span>
          <span class="label">Từ vựng đã thuộc</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon quiz">
          <AppIcon name="quiz" size="24" />
        </div>
        <div class="stat-content">
          <span class="value">{{ quizStats.completed }} / {{ quizStats.total }}</span>
          <span class="label">Bài tập hoàn thành</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon schedule">
          <AppIcon name="calendar" size="24" />
        </div>
        <div class="stat-content">
          <span class="value">{{ upcomingLessons }} Buổi</span>
          <span class="label">Lịch học sắp tới tuần này</span>
        </div>
      </div>
    </div>

    <!-- Two Column Layout -->
    <div class="dashboard-grid">
      <!-- Left Column: Classes, Shortcuts -->
      <div class="left-column">
        <!-- Classes Section -->
        <div class="card-section">
          <h3 class="section-title">Lớp học của tôi</h3>
          <div class="classes-list">
            <div 
              v-for="cls in classes" 
              :key="cls.id" 
              class="class-item-card clickable-card"
              @click="openClassDetails(cls)"
            >
              <div class="class-info">
                <h4>{{ cls.name }}</h4>
                <div class="class-meta">
                  <span class="teacher"><AppIcon name="profile" size="14" /> {{ cls.teacherName || 'Chưa phân công' }}</span>
                  <span class="schedule"><AppIcon name="clock" size="14" /> {{ cls.schedule }}</span>
                </div>
              </div>
            </div>
            <div v-if="!classes || classes.length === 0" class="empty-state">
              <AppIcon name="alert" size="32" />
              <p>Bạn chưa tham gia lớp học nào.</p>
            </div>
          </div>
        </div>

        <!-- Shortcut Action Items -->
        <div class="card-section">
          <h3 class="section-title">Nhiệm vụ cần thực hiện</h3>
          <div class="shortcuts-list">
            <!-- Vocabulary Shortcut -->
            <div v-if="nextVocabSet" class="shortcut-action-card">
              <div class="action-details">
                <div class="action-icon vocab">
                  <AppIcon name="book" size="20" />
                </div>
                <div>
                  <h5>Học Từ Vựng: {{ nextVocabSet.title }}</h5>
                  <p>{{ nextVocabSet.description }}</p>
                </div>
              </div>
              <button class="action-btn primary-btn" @click="$emit('navigate', 'vocabulary')">
                Học Ngay <AppIcon name="chevron-right" size="14" />
              </button>
            </div>

            <!-- Quiz Shortcut -->
            <div v-for="quiz in pendingQuizzes" :key="quiz.id" class="shortcut-action-card">
              <div class="action-details">
                <div class="action-icon quiz">
                  <AppIcon name="quiz" size="20" />
                </div>
                <div>
                  <h5>Làm Bài Tập: {{ quiz.title }}</h5>
                  <p class="due">Hạn nộp: {{ formatDate(quiz.dueDate) }}</p>
                </div>
              </div>
              <button class="action-btn warning-btn" @click="$emit('navigate', quiz.examType === 'PRACTICE' ? 'translation' : 'quizzes')">
                Làm Quiz <AppIcon name="chevron-right" size="14" />
              </button>
            </div>

            <div v-if="!nextVocabSet && pendingQuizzes.length === 0" class="empty-state">
              <AppIcon name="check" size="32" class="success-text" />
              <p>Tuyệt vời! Bạn đã hoàn thành tất cả nhiệm vụ hôm nay.</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Notifications & Progress -->
      <div class="right-column">
        <!-- Progress Tracking -->
        <div class="card-section progress-section">
          <h3 class="section-title">Tiến độ học tập tổng quan</h3>
          <div class="progress-box">
            <div class="progress-header">
              <span>Học tập & Bài tập</span>
              <span class="percentage">{{ totalProgress }}%</span>
            </div>
            <div class="progress-bar-container">
              <div class="progress-bar-fill" :style="{ width: totalProgress + '%' }"></div>
            </div>
            <div class="progress-legend">
              <div class="legend-item">
                <span class="dot learned"></span>
                <span>Từ vựng: {{ vocabStats.learned }} / {{ vocabStats.total }}</span>
              </div>
              <div class="legend-item">
                <span class="dot done"></span>
                <span>Quiz: {{ quizStats.completed }} / {{ quizStats.total }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Notifications Drawer -->
        <div class="card-section">
          <div class="section-title-row">
            <h3 class="section-title">Thông báo từ giáo viên</h3>
            <span v-if="unreadCount > 0" class="badge-unread">{{ unreadCount }} mới</span>
          </div>
          
          <div class="notifications-list">
            <div 
              v-for="notif in user.notifications" 
              :key="notif.id" 
              class="notif-item" 
              :class="{ unread: !notif.isRead }"
            >
              <div class="notif-header">
                <div class="notif-sender-info">
                  <span class="sender-name">{{ notif.sender }}</span>
                  <span class="notif-date">{{ formatDateShort(notif.date) }}</span>
                </div>
                <button 
                  v-if="!notif.isRead"
                  class="mark-read-btn" 
                  title="Đánh dấu đã đọc"
                  @click="$emit('mark-read', notif.id)"
                >
                  <AppIcon name="check" size="16" />
                </button>
              </div>
              <h4 class="notif-subject">{{ notif.title }}</h4>
              <p class="notif-body">{{ notif.content }}</p>
            </div>

            <div v-if="user.notifications.length === 0" class="empty-state">
              <AppIcon name="bell" size="32" />
              <p>Không có thông báo nào từ giáo viên.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Class Details Modal -->
  <div v-if="selectedClassDetails" class="modal-overlay" @click.self="closeClassDetails">
    <div class="modal-wrapper animate-scale" style="max-width: 700px; width: 100%;">
      <div class="modal-header">
        <h3>Chi tiết lớp học: {{ selectedClassDetails.name }}</h3>
        <button class="close-btn" @click="closeClassDetails">✕</button>
      </div>
      
      <div class="modal-body">
        <div class="class-summary-section">
          <div class="summary-item horizontal">
            <span class="label">Mã lớp:</span>
            <span class="value room-badge">{{ selectedClassDetails.code }}</span>
          </div>
          <div class="summary-item horizontal">
            <span class="label">Giáo viên:</span>
            <span class="value">{{ selectedClassDetails.teacherName || 'Chưa phân công' }}</span>
          </div>
          <div class="summary-item horizontal">
            <span class="label">Lịch học:</span>
            <span class="value">{{ selectedClassDetails.schedule }}</span>
          </div>
          <div class="summary-item horizontal">
            <span class="label">Phòng học:</span>
            <a 
              v-if="getRoomLink(selectedClassDetails.room)" 
              :href="getRoomLink(selectedClassDetails.room)" 
              target="_blank" 
              rel="noopener noreferrer" 
              class="value room-link"
              title="Nhấn để mở liên kết lớp học"
            >
              {{ selectedClassDetails.room }}
            </a>
            <span v-else class="value">
              {{ selectedClassDetails.room || 'Chưa xác định' }}
            </span>
          </div>
          <div v-if="selectedClassDetails.notes" class="summary-item full-width">
            <span class="label">Ghi chú từ giáo viên:</span>
            <p class="notes-text">{{ selectedClassDetails.notes }}</p>
          </div>
        </div>
        
        <div class="materials-section">
          <h4 class="section-subtitle">Tài liệu & Giáo trình</h4>
          
          <div v-if="loadingMaterials" class="loading-state">
            <span class="spinner"></span>
            <p>Đang tải danh sách tài liệu...</p>
          </div>
          
          <div v-else-if="classMaterials.length === 0" class="empty-state" style="padding: 2rem 0;">
            <AppIcon name="alert" size="24" />
            <p>Lớp học chưa có tài liệu nào được tải lên.</p>
          </div>
          
          <div v-else class="materials-list">
            <div v-for="mat in classMaterials" :key="mat.id" class="material-card-item">
              <div class="material-left">
                <div class="material-icon-box" :class="getFileIconClass(mat.contentType)">
                  📄
                </div>
                <div class="material-meta-info">
                  <a :href="mat.viewUrl" target="_blank" class="material-filename-link" :title="mat.title">
                    {{ mat.title }}
                  </a>
                  <span class="material-filesize">{{ formatBytes(mat.fileSize) }} • Đăng lúc {{ formatMaterialDate(mat.createdAt) }}</span>
                </div>
              </div>
              <a :href="mat.downloadUrl" target="_blank" class="download-link-btn" title="Tải tài liệu">
                Tải xuống
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppIcon from './icons/AppIcon.vue'
import api from '../services/axios'

const getRoomLink = (str) => {
  if (!str) return ''
  const trimmed = str.trim()
  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed
  }
  if (/^www\./i.test(trimmed)) {
    return 'https://' + trimmed
  }
  return ''
}

const props = defineProps({
  user: {
    type: Object,
    required: true
  },
  classes: {
    type: Array,
    required: true
  },
  studySets: {
    type: Array,
    required: true
  },
  quizzes: {
    type: Array,
    required: true
  },
  schedule: {
    type: Array,
    required: true
  }
})

defineEmits(['navigate', 'mark-read'])

// Greeting by hour
const greetingMessage = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Chào buổi sáng'
  if (hour < 18) return 'Chào buổi chiều'
  return 'Chào buổi tối'
})

// Statistics calculations
const vocabStats = computed(() => {
  let total = 0
  let learned = 0
  props.studySets.forEach(set => {
    total += (set.wordCount || 0)
    learned += (set.learnedCount || 0)
  })
  return { total, learned }
})

const quizStats = computed(() => {
  const total = props.quizzes.length
  const completed = props.quizzes.filter(q => q.status === 'completed').length
  return { total, completed }
})

const upcomingLessons = computed(() => {
  return props.schedule.filter(s => s.status === 'upcoming').length
})

// Total learning progress percentage
const totalProgress = computed(() => {
  const vocabWeight = 0.5
  const quizWeight = 0.5
  
  const vocabRatio = vocabStats.value.total > 0 ? (vocabStats.value.learned / vocabStats.value.total) : 0
  const quizRatio = quizStats.value.total > 0 ? (quizStats.value.completed / quizStats.value.total) : 0
  
  return Math.round((vocabRatio * vocabWeight + quizRatio * quizWeight) * 100)
})

// Shortcut targets
const nextVocabSet = computed(() => {
  // Find set with unlearned/review words
  return props.studySets[0] || null
})

const pendingQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  return list.filter(q => q.status !== 'completed')
})

// Unread announcements
const unreadCount = computed(() => {
  return props.user.notifications.filter(n => !n.isRead).length
})

// Date helpers
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit', 
    day: '2-digit', 
    month: '2-digit',
    year: 'numeric' 
  })
}

const formatDateShort = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('vi-VN', { 
    day: '2-digit', 
    month: '2-digit' 
  }) + ' ' + date.toLocaleTimeString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// Class details and materials logic
const selectedClassDetails = ref(null)
const classMaterials = ref([])
const loadingMaterials = ref(false)

const openClassDetails = async (cls) => {
  selectedClassDetails.value = cls
  loadingMaterials.value = true
  classMaterials.value = []
  try {
    const res = await api.get(`/classes/${cls.id}/materials`)
    if (res && res.data) {
      classMaterials.value = res.data
    }
  } catch (e) {
    console.error("Failed to fetch class materials:", e)
  } finally {
    loadingMaterials.value = false
  }
}

const closeClassDetails = () => {
  selectedClassDetails.value = null
  classMaterials.value = []
}

const getFileIconClass = (contentType) => {
  if (!contentType) return 'other'
  const ct = contentType.toLowerCase()
  if (ct.includes('pdf')) return 'pdf'
  if (ct.includes('word') || ct.includes('document') || ct.includes('docx')) return 'word'
  if (ct.includes('image')) return 'image'
  return 'other'
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatMaterialDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

/* Banner greeting */
.welcome-banner {
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  border-radius: var(--radius-lg);
  padding: 2rem;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  box-shadow: var(--shadow-md);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '한글';
  position: absolute;
  right: 15%;
  bottom: -20px;
  font-size: 8rem;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.05);
  font-family: 'Noto Sans KR', sans-serif;
  pointer-events: none;
}

.banner-text h2 {
  font-size: 1.75rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  letter-spacing: -0.5px;
}

.banner-text p {
  font-size: 1rem;
  opacity: 0.9;
  max-width: 600px;
}

.streak-badge {
  background-color: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.streak-icon {
  color: #ff9f43;
}

.streak-info {
  display: flex;
  flex-direction: column;
}

.streak-info .count {
  font-weight: 800;
  font-size: 1.15rem;
  line-height: 1.2;
}

.streak-info .label {
  font-size: 0.75rem;
  opacity: 0.8;
}

/* Stats Row */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.25rem;
}

.stat-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-3px);
  border-color: var(--primary-hover);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.xp { background-color: var(--primary-light); color: var(--primary); }
.stat-icon.vocab { background-color: var(--success-light); color: var(--success); }
.stat-icon.quiz { background-color: var(--warning-light); color: var(--warning); }
.stat-icon.schedule { background-color: var(--danger-light); color: var(--danger); }

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-content .value {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-title);
  line-height: 1.2;
}

.stat-content .label {
  font-size: 0.8rem;
  color: var(--text-muted);
}

/* Grid Layout */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 1.5rem;
}

.left-column, .right-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.card-section {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.section-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 1.25rem;
}

.section-title-row .section-title {
  margin-bottom: 0;
}

.badge-unread {
  background-color: var(--danger);
  color: #fff;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: 50px;
}

/* Class Cards */
.classes-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.class-item-card {
  padding: 1.25rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-card);
  transition: background-color var(--transition-fast);
}

.class-item-card:hover {
  background-color: var(--bg-hover);
}

.class-info h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.5rem;
}

.class-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.class-meta span {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.room-badge {
  background-color: var(--bg-badge);
  color: var(--text-title);
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  white-space: nowrap;
}

/* Shortcuts list */
.shortcuts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.shortcut-action-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  padding: 1.25rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--bg-card);
}

.action-details {
  display: flex;
  gap: 1rem;
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon.vocab { background-color: var(--success-light); color: var(--success); }
.action-icon.quiz { background-color: var(--warning-light); color: var(--warning); }

.action-details h5 {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.action-details p {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.action-details p.due {
  color: var(--warning);
  font-weight: 600;
}

.action-btn {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  white-space: nowrap;
}

.action-btn.primary-btn {
  background-color: var(--primary);
  color: #fff;
}
.action-btn.primary-btn:hover { background-color: var(--primary-hover); }

.action-btn.warning-btn {
  background-color: var(--warning);
  color: #fff;
}
.action-btn.warning-btn:hover {
  background-color: hsl(38, 92%, 42%);
}

/* Progress Box */
.progress-box {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-title);
}

.progress-bar-container {
  height: 10px;
  background-color: var(--bg-badge);
  border-radius: 50px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--success));
  border-radius: 50px;
  transition: width 0.5s ease-out;
}

.progress-legend {
  display: flex;
  gap: 1.5rem;
  margin-top: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot.learned { background-color: var(--success); }
.dot.done { background-color: var(--primary); }

/* Notifications list */
.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.notif-item {
  padding: 1rem 1.25rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.notif-item.unread {
  border-left: 4px solid var(--primary);
  background-color: var(--primary-glow);
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.notif-sender-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sender-name {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-title);
}

.notif-date {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.mark-read-btn {
  color: var(--text-muted);
  padding: 0.2rem;
  border-radius: 50%;
}

.mark-read-btn:hover {
  background-color: var(--bg-hover);
  color: var(--success);
}

.notif-subject {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.35rem;
}

.notif-body {
  font-size: 0.85rem;
  color: var(--text-body);
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.success-text {
  color: var(--success);
}

@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    padding: 1.5rem;
  }
  .streak-badge {
    align-self: flex-start;
  }
}

@media (max-width: 600px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .class-item-card {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  .class-item-card .primary-btn {
    align-self: flex-start;
  }
  .shortcut-action-card {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  .shortcut-action-card .primary-btn {
    align-self: flex-start;
  }
  .progress-legend {
    flex-direction: column;
    gap: 0.5rem;
  }
  .notif-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}

/* Modal overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 1.5rem;
  animation: fadeInOverlay 0.25s ease-out;
}

.modal-wrapper {
  background-color: var(--bg-card, #ffffff);
  border: 1px solid var(--border-color, #e2e8f0);
  border-radius: var(--radius-lg, 16px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  max-height: 85vh;
  overflow: hidden;
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color, #e2e8f0);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title, #1e293b);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.1rem;
  color: var(--text-muted, #64748b);
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
  width: 32px;
  height: 32px;
}

.close-btn:hover {
  background-color: var(--bg-hover, #f1f5f9);
  color: var(--text-title, #1e293b);
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Class summary section */
.class-summary-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  background-color: var(--bg-body, #f8fafc);
  padding: 1.25rem;
  border-radius: var(--radius-md, 12px);
  border: 1px dashed var(--border-color, #e2e8f0);
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.summary-item.horizontal {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
}

.summary-item.horizontal .value.room-badge {
  align-self: center;
}

.summary-item.full-width {
  grid-column: span 2;
}

.summary-item .label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted, #64748b);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.summary-item .value {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-body, #334155);
}

.summary-item .value.room-badge {
  align-self: flex-start;
  background-color: var(--primary-glow, rgba(99, 102, 241, 0.1));
  color: var(--primary, #6366f1);
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  font-size: 0.8rem;
}

.notes-text {
  font-size: 0.85rem;
  color: var(--text-body, #334155);
  margin-top: 0.25rem;
  line-height: 1.5;
  background-color: var(--bg-card, #ffffff);
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--border-color, #e2e8f0);
}

/* Materials section */
.materials-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-subtitle {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title, #1e293b);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
}

.materials-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.material-card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.85rem 1rem;
  border: 1px solid var(--border-color, #e2e8f0);
  border-radius: var(--radius-md, 12px);
  background-color: var(--bg-card, #ffffff);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.material-card-item:hover {
  border-color: var(--primary-glow, #cbd5e1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.material-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
}

.material-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.material-icon-box.pdf {
  background-color: rgba(239, 68, 68, 0.1);
  color: rgb(239, 68, 68);
}

.material-icon-box.word {
  background-color: rgba(59, 130, 246, 0.1);
  color: rgb(59, 130, 246);
}

.material-icon-box.image {
  background-color: rgba(16, 185, 129, 0.1);
  color: rgb(16, 185, 129);
}

.material-icon-box.other {
  background-color: rgba(107, 114, 128, 0.1);
  color: rgb(107, 114, 128);
}

.material-meta-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.material-filename-link {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title, #1e293b);
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  text-decoration: none;
  transition: color 0.2s;
}

.material-filename-link:hover {
  color: var(--primary, #6366f1);
  text-decoration: underline;
}

.material-filesize {
  font-size: 0.72rem;
  color: var(--text-muted, #64748b);
  margin-top: 0.1rem;
}

.download-link-btn {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--primary, #6366f1);
  background-color: var(--primary-glow, rgba(99, 102, 241, 0.08));
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm, 6px);
  text-decoration: none;
  transition: background-color 0.2s, color 0.2s;
  flex-shrink: 0;
}

.download-link-btn:hover {
  background-color: var(--primary, #6366f1);
  color: #ffffff;
}

/* Loading indicator */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
  gap: 0.75rem;
  color: var(--text-muted, #64748b);
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid var(--border-color, #e2e8f0);
  border-top-color: var(--primary, #6366f1);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.clickable-card {
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s, box-shadow 0.2s;
}

.clickable-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary, #6366f1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeInOverlay {
  from { opacity: 0; }
  to { opacity: 1; }
}

.room-link {
  color: var(--primary, #6366f1) !important;
  text-decoration: underline !important;
  cursor: pointer;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.room-link:hover {
  color: var(--primary-hover, #4f46e5) !important;
  opacity: 0.9;
}
  
</style>

