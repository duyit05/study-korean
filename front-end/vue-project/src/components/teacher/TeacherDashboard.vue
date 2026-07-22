<template>
  <div class="teacher-dashboard animate-fade">
    <div class="welcome-banner">
      <div class="banner-content">
        <h2>안녕하세요, 애리 선생님! 👋</h2>
        <p>Hôm nay cô muốn làm gì?</p>
      </div>
      <div class="banner-badge">
        <span class="kr-char">한</span>
      </div>
    </div>

    <!-- Quick Stats -->
    <div class="stats-grid">
      <div class="stat-card" @click="emit('navigate', 'teacher-classes')">
        <div class="stat-icon purple">
          <AppIcon name="user" size="24" />
        </div>
        <div class="stat-info">
          <h3>{{ classesCount }}</h3>
          <p>Lớp đang phụ trách</p>
        </div>
      </div>

      <div class="stat-card" @click="emit('navigate', 'teacher-classes')">
        <div class="stat-icon blue">
          <AppIcon name="profile" size="24" />
        </div>
        <div class="stat-info">
          <h3>{{ activeStudentsCount }}</h3>
          <p>Học viên hoạt động</p>
        </div>
      </div>

      <div class="stat-card" @click="emit('navigate', 'teacher-vocabulary')">
        <div class="stat-icon green">
          <AppIcon name="book" size="24" />
        </div>
        <div class="stat-info">
          <h3>{{ studySets.length }}</h3>
          <p>Bộ từ vựng đã soạn</p>
        </div>
      </div>

      <div class="stat-card" @click="emit('navigate', 'teacher-grading')">
        <div class="stat-icon orange">
          <AppIcon name="edit" size="24" />
        </div>
        <div class="stat-info">
          <h3>{{ pendingEssaysCount }}</h3>
          <p>Bài tự luận cần chấm</p>
        </div>
      </div>
    </div>

    <!-- Visual Charts Section -->
    <div class="charts-section-grid" style="margin-bottom: 1.5rem;">
      <div class="panel-card chart-card">
        <div class="panel-header">
          <h3>Điểm trung bình thi & bài tập (%)</h3>
        </div>
        <div class="chart-wrapper">
          <Bar :data="barChartData" :options="barChartOptions" />
        </div>
      </div>

      <div class="panel-card chart-card">
        <div class="panel-header">
          <h3>Số lượng bài nộp (6 tháng qua)</h3>
        </div>
        <div class="chart-wrapper">
          <Line :data="lineChartData" :options="lineChartOptions" />
        </div>
      </div>
    </div>

    <!-- Main Content Area: Shortcuts & Recent Activity -->
    <div class="dashboard-grid">
      <!-- Shortcuts Section -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Lối tắt thao tác nhanh</h3>
        </div>
        <div class="shortcuts-list">
          <button class="shortcut-item" @click="emit('navigate', 'teacher-classes')">
            <div class="shortcut-icon">
              <AppIcon name="user" size="18" />
            </div>
            <div class="shortcut-text">
              <h4>Tạo Lớp học mới</h4>
              <p>Mở lớp học, cấu hình lịch và link học trực tuyến</p>
            </div>
            <AppIcon name="chevron-right" size="16" class="arrow" />
          </button>

          <button class="shortcut-item" @click="emit('navigate', 'teacher-vocabulary')">
            <div class="shortcut-icon">
              <AppIcon name="book" size="18" />
            </div>
            <div class="shortcut-text">
              <h4>Soạn Bộ từ vựng</h4>
              <p>Thêm bộ flashcards mới để giao cho học viên tự học</p>
            </div>
            <AppIcon name="chevron-right" size="16" class="arrow" />
          </button>

          <button class="shortcut-item" @click="emit('navigate', 'teacher-quizzes')">
            <div class="shortcut-icon">
              <AppIcon name="quiz" size="18" />
            </div>
            <div class="shortcut-text">
              <h4>Thiết kế Đề thi / Bài tập</h4>
              <p>Soạn đề trắc nghiệm nghe, đọc và tự luận theo chuẩn TOPIK</p>
            </div>
            <AppIcon name="chevron-right" size="16" class="arrow" />
          </button>

          <button class="shortcut-item" @click="emit('navigate', 'teacher-grading')">
            <div class="shortcut-icon">
              <AppIcon name="edit" size="18" />
            </div>
            <div class="shortcut-text">
              <h4>Vào cổng chấm bài</h4>
              <p>Đọc bài viết luận của học sinh, nhận xét và cho điểm lẻ</p>
            </div>
            <AppIcon name="chevron-right" size="16" class="arrow" />
          </button>
        </div>
      </div>

      <!-- Recent Submissions & Class updates -->
      <div class="panel-card">
        <div class="panel-header">
          <h3>Nộp bài gần đây của lớp</h3>
        </div>
        <div class="activity-feed">
          <div v-for="act in recentActivities" :key="act.id" class="feed-item">
            <div class="feed-badge unread"></div>
            <div class="feed-content">
              <p><strong>{{ act.studentName }}</strong> đã nộp bài <em>{{ act.quizTitle }}</em></p>
              <span class="time">{{ act.submittedAt }} - Đang chờ cô chấm điểm</span>
            </div>
            <button class="action-btn" @click="emit('navigate', 'teacher-grading')">Chấm điểm</button>
          </div>
          <div v-if="recentActivities.length === 0" class="feed-item" style="text-align: center; color: var(--text-muted); justify-content: center; padding: 2rem 0;">
            Hiện không có hoạt động nộp bài tự luận mới nào gần đây.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import { useQuizStore } from '../../stores/quiz'
import { useStudySetStore } from '../../stores/studySet'
import api from '../../services/axios'
import { Bar, Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler
} from 'chart.js'

ChartJS.register(
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler
)

defineProps({
  quizzes: {
    type: Array,
    default: () => []
  },
  studySets: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['navigate'])

const quizStore = useQuizStore()
const studySetStore = useStudySetStore()

const statsData = ref({
  monthlySubmissions: [],
  classAverageScores: []
})

onMounted(async () => {
  try {
    await studySetStore.fetchClasses('TEACHER')
    await quizStore.fetchPendingAttempts()
    
    const response = await api.get('/teacher/stats')
    if (response && response.data) {
      statsData.value = response.data
    }
  } catch (e) {
    console.warn("Failed to load dashboard statistics:", e)
  }
})

const classesCount = computed(() => studySetStore.classes.length)
const activeStudentsCount = computed(() => {
  return studySetStore.classes.reduce((sum, c) => sum + (c.studentsCount || 0), 0)
})
const pendingEssaysCount = computed(() => quizStore.pendingAttempts.length)

const recentActivities = computed(() => {
  const list = [];
  (quizStore.pendingAttempts || []).slice(0, 5).forEach(att => {
    list.push({
      id: `act-att-${att.id}`,
      studentName: att.studentName,
      quizTitle: att.quizTitle,
      submittedAt: att.submittedAt ? att.submittedAt.replace('T', ' ').substring(0, 16) : 'Mới đây'
    })
  })
  return list
})

// Bar Chart Data (Class Averages)
const barChartData = computed(() => {
  const labels = statsData.value.classAverageScores.map(c => c.className)
  const data = statsData.value.classAverageScores.map(c => c.averageScore)
  return {
    labels: labels.length > 0 ? labels : ['Chưa có lớp học'],
    datasets: [
      {
        label: 'Điểm trung bình (%)',
        backgroundColor: 'rgba(219, 142, 113, 0.75)',
        borderColor: 'rgb(219, 142, 113)',
        borderWidth: 1,
        borderRadius: 6,
        data: data.length > 0 ? data : [0]
      }
    ]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    y: {
      min: 0,
      max: 100,
      grid: {
        color: 'rgba(0, 0, 0, 0.05)'
      }
    },
    x: {
      grid: {
        display: false
      }
    }
  }
}

// Line Chart Data (Monthly Submissions)
const lineChartData = computed(() => {
  const labels = statsData.value.monthlySubmissions.map(m => m.month)
  const data = statsData.value.monthlySubmissions.map(m => m.count)
  return {
    labels: labels.length > 0 ? labels : ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6'],
    datasets: [
      {
        label: 'Số bài nộp',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        borderColor: 'rgb(59, 130, 246)',
        borderWidth: 2,
        tension: 0.35,
        pointBackgroundColor: 'rgb(59, 130, 246)',
        pointHoverRadius: 6,
        fill: true,
        data: data.length > 0 ? data : [0, 0, 0, 0, 0, 0]
      }
    ]
  }
})

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: 'rgba(0, 0, 0, 0.05)'
      }
    },
    x: {
      grid: {
        display: false
      }
    }
  }
}
</script>

<style scoped>
.teacher-dashboard {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1.5rem;
}

.welcome-banner {
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border-radius: var(--radius-lg);
  padding: 2.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-md);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.banner-content h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.banner-content p {
  opacity: 0.9;
  font-size: 0.95rem;
}

.banner-badge {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.banner-badge .kr-char {
  font-size: 2rem;
  font-weight: 800;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.stat-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.purple { background-color: rgba(139, 92, 246, 0.1); color: rgb(139, 92, 246); }
.stat-icon.blue { background-color: rgba(59, 130, 246, 0.1); color: rgb(59, 130, 246); }
.stat-icon.green { background-color: rgba(16, 185, 129, 0.1); color: rgb(16, 185, 129); }
.stat-icon.orange { background-color: rgba(249, 115, 22, 0.1); color: rgb(249, 115, 22); }

.stat-info h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.1rem;
}

.stat-info p {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

.panel-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.panel-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-title);
}

.shortcuts-list {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.85rem;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  text-align: left;
  transition: all var(--transition-fast);
}

.shortcut-item:hover {
  background-color: var(--bg-body);
  border-color: var(--border-color);
}

.shortcut-item:hover .shortcut-icon {
  background-color: var(--primary);
  color: #fff;
}

.shortcut-item:hover .arrow {
  transform: translateX(4px);
  color: var(--primary);
}

.shortcut-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background-color: var(--bg-body);
  color: var(--text-title);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.shortcut-text {
  flex-grow: 1;
}

.shortcut-text h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 0.15rem;
}

.shortcut-text p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.arrow {
  color: var(--text-muted);
  transition: transform var(--transition-fast), color var(--transition-fast);
}

.activity-feed {
  padding: 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.feed-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  position: relative;
}

.feed-badge {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: var(--border-color);
  margin-top: 6px;
  flex-shrink: 0;
}

.feed-badge.unread {
  background-color: var(--primary);
  box-shadow: 0 0 0 2px var(--primary-glow);
}

.feed-content {
  flex-grow: 1;
  font-size: 0.85rem;
  color: var(--text-title);
  line-height: 1.4;
}

.feed-content p {
  margin-bottom: 0.15rem;
}

.feed-content .time {
  font-size: 0.75rem;
  color: var(--text-muted);
  display: block;
}

.action-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast);
  white-space: nowrap;
}

.action-btn:hover {
  background-color: var(--primary-hover);
}

.charts-section-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

@media (max-width: 900px) {
  .charts-section-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  padding-bottom: 1.5rem;
}

.chart-wrapper {
  position: relative;
  height: 280px;
  padding: 0 1.5rem;
  margin-top: 1rem;
}

/* Mobile responsive styles */
@media (max-width: 768px) {
  .teacher-dashboard {
    padding: 1rem;
    gap: 1rem;
  }
}

@media (max-width: 600px) {
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    padding: 1.5rem;
  }

  .banner-content h2 {
    font-size: 1.35rem;
  }

  .banner-badge {
    display: none;
  }

  .chart-wrapper {
    height: 200px;
    padding: 0 0.5rem;
  }

  .panel-header {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 1rem;
  }

  .feed-item {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
    border-bottom: 1px solid var(--border-color);
    padding-bottom: 0.75rem;
  }

  .feed-item:last-child {
    border-bottom: none;
  }

  .feed-item .action-btn {
    align-self: flex-start;
  }
}

</style>
