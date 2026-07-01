<template>
  <div class="teacher-classes animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Quản lý lớp học</h2>
        <p>Xem danh sách lớp, tạo mã tham gia và theo dõi tiến độ của học viên.</p>
      </div>
      <button class="primary-btn" @click="showCreateModal = true">
        <AppIcon name="user" size="18" />
        <span>Tạo lớp học mới</span>
      </button>
    </div>

    <!-- Classes Grid -->
    <div class="classes-grid">
      <div 
        v-for="cls in classes" 
        :key="cls.id" 
        class="class-card"
        :class="{ active: selectedClass && selectedClass.id === cls.id }"
        @click="selectedClass = cls"
      >
        <div class="class-header">
          <div class="class-badge">한</div>
          <h3>{{ cls.name }}</h3>
        </div>
        <div class="class-body">
          <div class="info-row">
            <AppIcon name="calendar" size="16" class="icon" />
            <span>{{ cls.schedule }}</span>
          </div>
          <div class="info-row">
            <AppIcon name="search" size="16" class="icon" />
            <span>{{ cls.room }}</span>
          </div>
          <div class="info-row code">
            <strong>Mã lớp:</strong>
            <span class="code-badge">{{ cls.code }}</span>
          </div>
        </div>
        <div class="class-footer">
          <span>{{ cls.studentsCount }} Học viên</span>
          <span class="detail-link">Xem chi tiết &rarr;</span>
        </div>
      </div>
    </div>

    <!-- Selected Class Details Panel -->
    <div v-if="selectedClass" class="class-detail-panel animate-scale">
      <div class="panel-header">
        <div class="header-title">
          <h3>Chi tiết lớp: {{ selectedClass.name }}</h3>
          <p>{{ selectedClass.schedule }} | {{ selectedClass.room }}</p>
        </div>
        <div class="header-actions">
          <button class="delete-btn-sm" @click="handleDeleteClass(selectedClass.id)">Xóa lớp học</button>
          <button class="close-btn" @click="selectedClass = null">&times;</button>
        </div>
      </div>

      <div class="panel-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'students' }" 
          @click="activeTab = 'students'"
        >
          Danh sách Học viên ({{ selectedClass.students.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'assignments' }" 
          @click="activeTab = 'assignments'"
        >
          Bài tập & Đề thi đã giao ({{ assignedQuizzes.length }})
        </button>
      </div>

      <!-- Tab Content: Students -->
      <div v-if="activeTab === 'students'" class="tab-content students-tab">
        <table class="data-table">
          <thead>
            <tr>
              <th>Học viên</th>
              <th>Email</th>
              <th>Tiến độ từ vựng</th>
              <th>Điểm trung bình</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in selectedClass.students" :key="student.id">
              <td>
                <div class="student-profile">
                  <div class="avatar-circle">{{ student.name[0] }}</div>
                  <strong>{{ student.name }}</strong>
                </div>
              </td>
              <td>{{ student.email }}</td>
              <td>
                <div class="progress-bar-wrapper">
                  <div class="progress-bar" :style="{ width: student.vocabProgress + '%' }"></div>
                  <span class="progress-text">{{ student.vocabProgress }}%</span>
                </div>
              </td>
              <td><span class="score-badge">{{ student.avgScore }} / 100</span></td>
              <td>
                <button class="text-btn">Gửi nhắc nhở</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Tab Content: Assignments -->
      <div v-else-if="activeTab === 'assignments'" class="tab-content assignments-tab">
        <div class="assignments-list">
          <div v-for="quiz in assignedQuizzes" :key="quiz.id" class="assignment-item">
            <div class="assign-icon">
              <AppIcon name="quiz" size="20" />
            </div>
            <div class="assign-info">
              <h4>{{ quiz.title }}</h4>
              <p>Hạn nộp: {{ quiz.dueDate }} | Dạng: {{ quiz.type }}</p>
            </div>
            <div class="assign-status">
              <span class="status-badge">Đã nộp: 8/12</span>
            </div>
          </div>
          <div v-if="assignedQuizzes.length === 0" class="empty-state">
            Chưa có bài tập nào được giao cho lớp này.
          </div>
        </div>
      </div>
    </div>

    <!-- Create Class Modal -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Tạo lớp học mới</h3>
          <button class="close-btn" @click="showCreateModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleCreateClass" class="modal-form">
          <div class="form-group">
            <label for="className">Tên lớp học</label>
            <input type="text" id="className" v-model="newClassName" placeholder="Ví dụ: Tiếng Hàn Sơ cấp 1B" required>
          </div>
          <div class="form-group">
            <label for="classSchedule">Lịch học</label>
            <input type="text" id="classSchedule" v-model="newClassSchedule" placeholder="Ví dụ: Thứ 2, 4, 6 (18:00 - 19:30)" required>
          </div>
          <div class="form-group">
            <label for="classRoom">Phòng học / Link Zoom</label>
            <input type="text" id="classRoom" v-model="newClassRoom" placeholder="Ví dụ: Online Zoom A2 hoặc Phòng 401" required>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showCreateModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Tạo lớp</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

const props = defineProps({
  quizzes: {
    type: Array,
    default: () => []
  },
  studySets: {
    type: Array,
    default: () => []
  }
})

import { useStudySetStore } from '../../stores/studySet'
import { useQuizStore } from '../../stores/quiz'

const studySetStore = useStudySetStore()
const quizStore = useQuizStore()

const classes = computed(() => {
  return studySetStore.classes || []
})

const selectedClass = ref(null)
const activeTab = ref('students')
const showCreateModal = ref(false)

// New Class Fields
const newClassName = ref('')
const newClassSchedule = ref('')
const newClassRoom = ref('')

const assignedQuizzes = computed(() => {
  if (!selectedClass.value) return []
  return quizStore.quizzes.filter(q => q.classId === selectedClass.value.id || q.clazzId === selectedClass.value.id)
})

const handleCreateClass = async () => {
  if (!newClassName.value || !newClassSchedule.value || !newClassRoom.value) return

  try {
    await studySetStore.createClass({
      name: newClassName.value,
      schedule: newClassSchedule.value,
      room: newClassRoom.value,
      notes: ''
    })
  } catch (e) {
    console.error("Failed to create class via API:", e)
  }

  // Reset fields
  newClassName.value = ''
  newClassSchedule.value = ''
  newClassRoom.value = ''
  showCreateModal.value = false
}

const handleDeleteClass = async (classId) => {
  if (!confirm("Bạn có chắc chắn muốn xóa lớp học này?")) return
  try {
    await studySetStore.deleteClass(classId)
    selectedClass.value = null
    alert("Xóa lớp học thành công!")
  } catch (e) {
    console.error("Failed to delete class:", e)
    alert("Có lỗi xảy ra khi xóa lớp học.")
  }
}
</script>

<style scoped>
.teacher-classes {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-area h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.title-area p {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.primary-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  box-shadow: 0 4px 12px var(--primary-glow);
  transition: all var(--transition-fast);
}

.primary-btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.25rem;
}

.class-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.class-card:hover, .class-card.active {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.class-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.class-badge {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background-color: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.class-header h3 {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-title);
}

.class-body {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.info-row.code {
  font-size: 0.85rem;
  color: var(--text-title);
  margin-top: 0.25rem;
}

.code-badge {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.15rem 0.4rem;
  border-radius: var(--radius-sm);
  font-family: monospace;
  font-weight: 700;
  color: var(--primary);
}

.class-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 0.75rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.detail-link {
  color: var(--primary);
  font-weight: 600;
}

.class-detail-panel {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
}

.header-title h3 {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-title);
}

.header-title p {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
}

.close-btn:hover {
  color: var(--danger);
}

.panel-tabs {
  display: flex;
  gap: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.tab-btn {
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 0.5rem 0.25rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn.active, .tab-btn:hover {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th, .data-table td {
  padding: 0.75rem 1rem;
  font-size: 0.85rem;
  border-bottom: 1px solid var(--border-color);
}

.data-table th {
  color: var(--text-muted);
  font-weight: 600;
}

.student-profile {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: var(--primary-glow);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.8rem;
}

.progress-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-bar {
  height: 6px;
  background-color: var(--primary);
  border-radius: var(--radius-sm);
  min-width: 8px;
}

.progress-text {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.score-badge {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.text-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
}

.text-btn:hover {
  text-decoration: underline;
}

.assignments-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.assignment-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.assign-icon {
  width: 36px;
  height: 36px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.assign-info {
  flex-grow: 1;
}

.assign-info h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 0.15rem;
}

.assign-info p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.status-badge {
  background-color: var(--primary-glow);
  color: var(--primary);
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
}

/* Modal styling */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 450px;
  padding: 1.75rem;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
  margin-bottom: 1rem;
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-title);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.modal-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.modal-form label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.modal-form input {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.modal-form input:focus {
  border-color: var(--primary);
  outline: none;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.cancel-btn {
  background-color: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  padding: 0.6rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: var(--bg-body);
}

.submit-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.6rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: var(--primary-hover);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.delete-btn-sm {
  background-color: transparent;
  color: #ef4444;
  border: 1px solid #ef4444;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.delete-btn-sm:hover {
  background-color: #ef4444;
  color: #fff;
}
</style>
