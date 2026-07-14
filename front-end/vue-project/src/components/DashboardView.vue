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
              v-for="cls in user.joinedClasses" 
              :key="cls.id" 
              class="class-item-card"
            >
              <div class="class-info">
                <h4>{{ cls.name }}</h4>
                <div class="class-meta">
                  <span class="teacher"><AppIcon name="profile" size="14" /> {{ cls.teacher }}</span>
                  <span class="schedule"><AppIcon name="clock" size="14" /> {{ cls.schedule }}</span>
                </div>
              </div>
              <div class="class-action">
                <span class="room-badge">{{ cls.room }}</span>
              </div>
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
                  <h5>Học Từ Vựng: {{ nextVocabSet.name }}</h5>
                  <p>{{ nextVocabSet.description }}</p>
                </div>
              </div>
              <button class="action-btn primary-btn" @click="$emit('navigate', 'vocabulary')">
                Học Ngay <AppIcon name="chevron-right" size="14" />
              </button>
            </div>

            <!-- Quiz Shortcut -->
            <div v-if="pendingQuiz" class="shortcut-action-card">
              <div class="action-details">
                <div class="action-icon quiz">
                  <AppIcon name="quiz" size="20" />
                </div>
                <div>
                  <h5>Làm Bài Tập: {{ pendingQuiz.title }}</h5>
                  <p class="due">Hạn nộp: {{ formatDate(pendingQuiz.dueDate) }}</p>
                </div>
              </div>
              <button class="action-btn warning-btn" @click="$emit('navigate', 'quizzes')">
                Làm Quiz <AppIcon name="chevron-right" size="14" />
              </button>
            </div>

            <div v-if="!nextVocabSet && !pendingQuiz" class="empty-state">
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
</template>

<script setup>
import { computed } from 'vue'
import AppIcon from './icons/AppIcon.vue'

const props = defineProps({
  user: {
    type: Object,
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

const pendingQuiz = computed(() => {
  // Find quiz with 'not_started' or 'in_progress' status
  return props.quizzes.find(q => q.status !== 'completed') || null
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
  }
  .streak-badge {
    align-self: flex-start;
  }
}
</style>
