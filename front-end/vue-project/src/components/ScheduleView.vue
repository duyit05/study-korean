<template>
  <div class="schedule-container animate-fade">
    <!-- Header -->
    <div class="schedule-header-section">
      <div>
        <h2>Lịch Học Cá Nhân 📅</h2>
        <p>Theo dõi thời gian học, chủ đề bài học và phòng học trực tuyến/trực tiếp.</p>
      </div>

      <div class="schedule-filters">
        <button 
          class="filter-btn"
          :class="{ active: filterMode === 'all' }"
          @click="filterMode = 'all'"
        >
          Tất cả lịch
        </button>
        <button 
          class="filter-btn"
          :class="{ active: filterMode === 'upcoming' }"
          @click="filterMode = 'upcoming'"
        >
          Sắp diễn ra
        </button>
      </div>
    </div>

    <!-- Calendar Timeline list -->
    <div class="schedule-timeline animate-scale">
      <div 
        v-for="item in filteredSchedule" 
        :key="item.id" 
        class="timeline-item"
        :class="{ completed: item.status === 'completed', today: isToday(item.date) }"
      >
        <!-- Date Badge column -->
        <div class="date-column">
          <div class="date-circle">
            <span class="day-num">{{ getDayNumber(item.date) }}</span>
            <span class="month-name">T{{ getMonthNumber(item.date) }}</span>
          </div>
          <span class="day-of-week">{{ item.dayOfWeek }}</span>
        </div>

        <!-- Timeline Connector -->
        <div class="timeline-indicator-connector">
          <div class="node">
            <AppIcon v-if="item.status === 'completed'" name="check" size="12" />
          </div>
          <div class="line"></div>
        </div>

        <!-- Lesson Card details -->
        <div class="lesson-card">
          <div class="lesson-header-row">
            <div class="class-subject-badge">{{ item.className }}</div>
            <div class="lesson-time">
              <AppIcon name="clock" size="14" />
              <span>{{ item.time }}</span>
            </div>
          </div>

          <h3 class="lesson-topic">{{ item.topic }}</h3>
          
          <div class="lesson-details-row">
            <span class="teacher-name"><AppIcon name="profile" size="14" /> {{ item.teacher }}</span>
            
            <span class="location" :class="{ online: isOnline(item.location) }">
              <AppIcon name="dashboard" size="14" /> {{ item.location }}
            </span>
          </div>

          <!-- Quick Action -->
          <div class="lesson-actions" v-if="item.status === 'upcoming'">
            <button 
              v-if="isOnline(item.location)" 
              class="join-btn online"
              @click="mockJoinZoom(item)"
            >
              <AppIcon name="play" size="14" /> Vào lớp học Zoom
            </button>
            <span v-else class="offline-badge">Vui lòng đến đúng giờ tại phòng học</span>
          </div>
          <div class="lesson-actions" v-else>
            <span class="completed-badge">✓ Đã kết thúc lớp học</span>
          </div>
        </div>
      </div>

      <div v-if="filteredSchedule.length === 0" class="empty-state">
        <AppIcon name="calendar" size="36" />
        <p>Không có lịch học nào khớp với bộ lọc.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppIcon from './icons/AppIcon.vue'

const props = defineProps({
  schedule: {
    type: Array,
    required: true
  }
})

const filterMode = ref('all') // all, upcoming

// Filters list
const filteredSchedule = computed(() => {
  if (filterMode.value === 'upcoming') {
    return props.schedule.filter(s => s.status === 'upcoming')
  }
  return props.schedule
})

// Date parse helpers
const getDayNumber = (dateStr) => {
  return new Date(dateStr).getDate()
}

const getMonthNumber = (dateStr) => {
  return new Date(dateStr).getMonth() + 1
}

const isToday = (dateStr) => {
  const today = new Date().toDateString()
  const classDate = new Date(dateStr).toDateString()
  return today === classDate
}

const isOnline = (locationStr) => {
  return locationStr.toLowerCase().includes('zoom') || locationStr.toLowerCase().includes('online')
}

// Join Class Room simulation
const mockJoinZoom = (item) => {
  alert(`Đang mở liên kết tham gia lớp học: ${item.className}\nChủ đề: ${item.topic}\nĐịa điểm: ${item.location}`)
}
</script>

<style scoped>
.schedule-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Header */
.schedule-header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1.25rem;
}

.schedule-header-section h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.schedule-header-section p {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.schedule-filters {
  display: flex;
  background-color: var(--bg-badge);
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.filter-btn {
  padding: 0.4rem 1rem;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  color: var(--text-body);
}

.filter-btn.active {
  background-color: var(--bg-card);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

/* Timeline Layout */
.schedule-timeline {
  display: flex;
  flex-direction: column;
  margin-top: 1rem;
}

.timeline-item {
  display: grid;
  grid-template-columns: 100px 30px 1fr;
  gap: 1rem;
}

/* Date Column */
.date-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 0.5rem;
}

.date-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: var(--shadow-sm);
}

.timeline-item.today .date-circle {
  border-color: var(--primary);
  background: linear-gradient(135deg, var(--primary-light), var(--bg-card));
}

.day-num {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-title);
  line-height: 1;
}

.month-name {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-weight: 600;
}

.day-of-week {
  font-size: 0.8rem;
  color: var(--text-body);
  font-weight: 600;
  margin-top: 0.35rem;
}

/* Timeline vertical line */
.timeline-indicator-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.node {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  background-color: var(--bg-card);
  z-index: 2;
  margin-top: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--success);
}

.timeline-item.today .node {
  border-color: var(--primary);
  background-color: var(--primary);
  box-shadow: 0 0 0 4px var(--primary-glow);
}

.timeline-item.completed .node {
  border-color: var(--success);
  background-color: var(--success-light);
}

.line {
  width: 2px;
  background-color: var(--border-color);
  flex-grow: 1;
  position: absolute;
  top: 1.5rem;
  bottom: -1.5rem;
  z-index: 1;
}

.timeline-item:last-child .line {
  display: none;
}

/* Lesson card details */
.lesson-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
  margin-bottom: 2rem;
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.lesson-card:hover {
  transform: translateY(-2px);
  border-color: var(--border-color-hover);
}

.timeline-item.today .lesson-card {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
}

.lesson-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.class-subject-badge {
  background-color: var(--bg-badge);
  color: var(--text-title);
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm);
}

.timeline-item.today .class-subject-badge {
  background-color: var(--primary);
  color: #fff;
}

.lesson-time {
  font-size: 0.85rem;
  color: var(--text-title);
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.lesson-topic {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.75rem;
}

.lesson-details-row {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
}

.lesson-details-row span {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.location.online {
  color: var(--primary);
  font-weight: 600;
}

.lesson-actions {
  border-top: 1px solid var(--border-color);
  padding-top: 0.85rem;
  display: flex;
}

.join-btn {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.join-btn.online {
  background-color: var(--primary);
  color: #fff;
  box-shadow: 0 4px 10px var(--primary-glow);
}
.join-btn.online:hover { background-color: var(--primary-hover); }

.offline-badge {
  font-size: 0.8rem;
  color: var(--warning);
  font-weight: 600;
}

.completed-badge {
  font-size: 0.8rem;
  color: var(--success);
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

@media (max-width: 600px) {
  .timeline-item {
    grid-template-columns: 70px 20px 1fr;
    gap: 0.5rem;
  }
  .date-circle {
    width: 48px;
    height: 48px;
  }
  .day-num {
    font-size: 1.1rem;
  }
  .lesson-header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
