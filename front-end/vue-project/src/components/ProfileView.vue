<template>
  <div class="profile-container animate-fade">
    <div class="profile-grid">
      <!-- Left Card: Personal info edit -->
      <div class="profile-card personal-info">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img :src="editableUser.avatar || defaultAvatar" alt="User Avatar" class="avatar-img">
            <button class="change-avatar-btn" @click="changeAvatar" title="Đổi ảnh đại diện">
              <AppIcon name="edit" size="14" />
            </button>
          </div>
          <h3>{{ user.name }}</h3>
          <p class="role-level">{{ user.level }}</p>
        </div>

        <form @submit.prevent="saveProfile" class="profile-form">
          <div class="form-group">
            <label for="prof-name">Họ và tên</label>
            <input 
              type="text" 
              id="prof-name" 
              v-model="editableUser.name" 
              required
            >
          </div>

          <div class="form-group">
            <label for="prof-email">Email liên hệ</label>
            <input 
              type="email" 
              id="prof-email" 
              v-model="editableUser.email" 
              required
            >
          </div>

          <div class="form-group">
            <label>Cấp độ học tập</label>
            <input 
              type="text" 
              :value="user.level" 
              disabled 
              class="disabled-input"
            >
          </div>

          <button type="submit" class="save-btn" :disabled="!isModified">
            Lưu thay đổi
          </button>
        </form>
      </div>

      <!-- Right Card: Stats & Preferences -->
      <div class="right-column-grid">
        <!-- Stats summary -->
        <div class="profile-card stats-overview">
          <h3 class="card-title">Thống kê học tập</h3>
          
          <div class="stats-meters-grid">
            <!-- XP Milestone -->
            <div class="meter-box">
              <div class="meter-header">
                <span>Cấp độ tiếp theo</span>
                <span class="val">{{ xpProgress }}% ({{ user.xp }} / 2000 XP)</span>
              </div>
              <div class="meter-bar">
                <div class="fill xp" :style="{ width: xpProgress + '%' }"></div>
              </div>
              <p class="meter-hint">Tích lũy thêm {{ 2000 - user.xp }} XP để thăng cấp trung cấp.</p>
            </div>

            <!-- Vocab breakdown -->
            <div class="meter-box">
              <div class="meter-header">
                <span>Trạng thái từ vựng</span>
                <span class="val">{{ vocabStats.learned }} / {{ vocabStats.total }} đã thuộc</span>
              </div>
              <div class="stacked-bar">
                <div class="fill learned" :style="{ width: vocabStats.learnedPct + '%' }" title="Đã thuộc"></div>
                <div class="fill review" :style="{ width: vocabStats.reviewPct + '%' }" title="Cần ôn tập"></div>
                <div class="fill unlearned" :style="{ width: vocabStats.unlearnedPct + '%' }" title="Chưa thuộc"></div>
              </div>
              <div class="bar-legend">
                <span class="leg-item"><span class="dot learned"></span> Đã thuộc ({{ vocabStats.learnedPct }}%)</span>
                <span class="leg-item"><span class="dot review"></span> Ôn tập ({{ vocabStats.reviewPct }}%)</span>
                <span class="leg-item"><span class="dot unlearned"></span> Chưa thuộc ({{ vocabStats.unlearnedPct }}%)</span>
              </div>
            </div>

            <!-- Quizzes stats -->
            <div class="hw-summary-panel">
              <div class="hw-stat-item">
                <span class="val">{{ quizStats.completed }}</span>
                <span class="lbl">Bài tập hoàn thành</span>
              </div>
              <div class="hw-stat-item">
                <span class="val">{{ quizStats.averageScore }}</span>
                <span class="lbl">Điểm trung bình</span>
              </div>
              <div class="hw-stat-item">
                <span class="val">{{ user.streak }} ngày</span>
                <span class="lbl">Chuỗi học hiện tại</span>
              </div>
            </div>
          </div>
        </div>

        <!-- System Preferences -->
        <div class="profile-card preferences-box">
          <h3 class="card-title">Cài đặt học tập</h3>
          
          <div class="prefs-list">
            <div class="pref-row">
              <div class="pref-text">
                <span class="pref-label">Tự động phát âm thanh khi lật thẻ</span>
                <span class="pref-desc">Phát âm từ vựng tiếng Hàn tự động trong chế độ Flashcard.</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="prefs.autoPronounce">
                <span class="slider"></span>
              </label>
            </div>

            <div class="pref-row">
              <div class="pref-text">
                <span class="pref-label">Nhận thông báo qua Email</span>
                <span class="pref-desc">Nhận tin nhắn nhắc nhở khi giáo viên giao bài tập mới hoặc thay đổi lịch.</span>
              </div>
              <label class="switch">
                <input type="checkbox" v-model="prefs.emailNotif">
                <span class="slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
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
  }
})

const emit = defineEmits(['save-profile'])

const defaultAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=256"

// Editable states
const editableUser = ref({
  name: props.user.name,
  email: props.user.email,
  avatar: props.user.avatar
})

// Preferences states
const prefs = ref({
  autoPronounce: true,
  emailNotif: true
})

// Watch for prop changes to update local model
watch(() => props.user, (newUser) => {
  editableUser.value.name = newUser.name
  editableUser.value.email = newUser.email
  editableUser.value.avatar = newUser.avatar
}, { deep: true })

// Check if changed
const isModified = computed(() => {
  return editableUser.value.name !== props.user.name || 
         editableUser.value.email !== props.user.email ||
         editableUser.value.avatar !== props.user.avatar
})

// Avatar url mock updater
const changeAvatar = () => {
  const newUrl = prompt("Nhập địa chỉ URL hình ảnh avatar mới của bạn:", editableUser.value.avatar)
  if (newUrl !== null) {
    editableUser.value.avatar = newUrl
  }
}

// Save form
const saveProfile = () => {
  emit('save-profile', {
    name: editableUser.value.name,
    email: editableUser.value.email,
    avatar: editableUser.value.avatar
  })
  alert("Cập nhật thông tin cá nhân thành công!")
}

// XP calculations
const xpProgress = computed(() => {
  // Assume next level needs 2000 XP
  return Math.min(Math.round((props.user.xp / 2000) * 100), 100)
})

// Vocab breakdown metrics
const vocabStats = computed(() => {
  let total = 0
  let learned = 0
  let review = 0
  let unlearned = 0

  props.studySets.forEach(set => {
    set.words.forEach(w => {
      total++
      if (w.status === 'learned') learned++
      else if (w.status === 'review') review++
      else unlearned++
    })
  })

  if (total === 0) return { total, learned: 0, review: 0, unlearned: 0, learnedPct: 0, reviewPct: 0, unlearnedPct: 0 }

  const learnedPct = Math.round((learned / total) * 100)
  const reviewPct = Math.round((review / total) * 100)
  const unlearnedPct = 100 - learnedPct - reviewPct

  return {
    total,
    learned,
    review,
    unlearned,
    learnedPct,
    reviewPct,
    unlearnedPct
  }
})

// Quiz details
const quizStats = computed(() => {
  const completedList = props.quizzes.filter(q => q.status === 'completed')
  const completed = completedList.length
  
  if (completed === 0) return { completed, averageScore: '0/10' }
  
  const sumScores = completedList.reduce((acc, q) => acc + (q.score || 0), 0)
  const avg = Math.round((sumScores / completed) * 10) / 10
  
  return {
    completed,
    averageScore: `${avg}/10`
  }
})
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1.8fr;
  gap: 1.5rem;
}

.right-column-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.profile-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.75rem;
  box-shadow: var(--shadow-sm);
}

.card-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
}

/* Personal info card layout */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1.5rem;
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  width: 110px;
  height: 110px;
  margin-bottom: 1rem;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--primary-light);
  box-shadow: var(--shadow-md);
}

.change-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: var(--primary);
  color: #fff;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--bg-card);
  box-shadow: var(--shadow-sm);
}

.change-avatar-btn:hover {
  background-color: var(--primary-hover);
  transform: scale(1.1);
}

.avatar-section h3 {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.role-level {
  font-size: 0.85rem;
  color: var(--primary);
  font-weight: 600;
  background-color: var(--primary-light);
  padding: 0.25rem 0.75rem;
  border-radius: 50px;
}

/* Form input styling */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-title);
}

.form-group input {
  padding: 0.75rem;
  border-radius: var(--radius-sm);
  background-color: var(--bg-app);
  width: 100%;
}

.form-group input:focus {
  border-color: var(--primary);
  background-color: var(--bg-card);
}

.form-group input.disabled-input {
  background-color: var(--bg-badge);
  color: var(--text-muted);
  cursor: not-allowed;
  border-color: var(--border-color);
}

.save-btn {
  padding: 0.75rem;
  background-color: var(--primary);
  color: #fff;
  border-radius: var(--radius-sm);
  font-weight: 700;
  margin-top: 0.5rem;
}

.save-btn:hover:not(:disabled) {
  background-color: var(--primary-hover);
}

.save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Stats view */
.stats-meters-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.meter-box {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.meter-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-title);
}

.meter-bar {
  height: 8px;
  background-color: var(--bg-badge);
  border-radius: 10px;
  overflow: hidden;
}

.meter-bar .fill.xp {
  height: 100%;
  background: linear-gradient(90deg, var(--primary), var(--primary-hover));
  border-radius: 10px;
}

.meter-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Stacked color progress bar */
.stacked-bar {
  height: 12px;
  background-color: var(--bg-badge);
  border-radius: 10px;
  overflow: hidden;
  display: flex;
}

.stacked-bar .fill {
  height: 100%;
  transition: width 0.4s ease;
}

.stacked-bar .fill.learned { background-color: var(--success); }
.stacked-bar .fill.review { background-color: var(--warning); }
.stacked-bar .fill.unlearned { background-color: var(--border-color-hover); }

.bar-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 0.25rem;
}

.leg-item {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.leg-item .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.leg-item .dot.learned { background-color: var(--success); }
.leg-item .dot.review { background-color: var(--warning); }
.leg-item .dot.unlearned { background-color: var(--border-color-hover); }

/* Score box panel grid */
.hw-summary-panel {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  background-color: var(--bg-app);
  padding: 1.25rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  text-align: center;
}

.hw-stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.hw-stat-item .val {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-title);
}

.hw-stat-item .lbl {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-weight: 600;
}

/* System preferences list */
.prefs-list {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.pref-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
}

.pref-text {
  display: flex;
  flex-direction: column;
}

.pref-label {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-title);
}

.pref-desc {
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Toggle Switch Styling */
.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  flex-shrink: 0;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--border-color-hover);
  transition: .3s;
  border-radius: 34px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .3s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: var(--primary);
}

input:checked + .slider:before {
  transform: translateX(20px);
}

@media (max-width: 800px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
