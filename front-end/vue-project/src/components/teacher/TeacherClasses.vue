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
        <div class="tab-actions-row">
          <button class="primary-btn-sm" @click="openEnrollModal">
            <AppIcon name="plus" size="14" />
            Thêm học viên vào lớp
          </button>
        </div>
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
            <tr v-for="student in (selectedClass.students || [])" :key="student.id">
              <td>
                <div class="student-profile">
                  <div class="avatar-circle">{{ student.name ? student.name[0] : 'U' }}</div>
                  <strong>{{ student.name }}</strong>
                </div>
              </td>
              <td>{{ student.email }}</td>
              <td>
                <div class="progress-bar-wrapper">
                  <div class="progress-bar" :style="{ width: (student.vocabProgress || 0) + '%' }"></div>
                  <span class="progress-text">{{ student.vocabProgress || 0 }}%</span>
                </div>
              </td>
              <td><span class="score-badge">{{ student.avgScore || 0 }} / 100</span></td>
              <td>
                <button class="text-btn">Gửi nhắc nhở</button>
              </td>
            </tr>
            <tr v-if="!selectedClass.students || selectedClass.students.length === 0">
              <td colspan="5" class="empty-row" style="text-align: center; color: var(--text-muted); padding: 3rem;">
                Lớp học này chưa có học viên nào tham gia. Hãy cung cấp Mã lớp cho học viên!
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
            <select id="className" v-model="newClassLevelId" required style="padding: 0.75rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background-color: var(--bg-body); color: var(--text-title); font-size: 0.9rem;">
              <option value="" disabled>-- Chọn lớp học từ Cấp độ TOPIK --</option>
              <option v-for="lvl in topikLevels" :key="lvl.id" :value="lvl.id">
                {{ lvl.name }}
              </option>
            </select>
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

    <!-- Add Student Modal -->
    <div v-if="showEnrollModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Thêm học viên vào lớp</h3>
          <button class="close-btn" @click="showEnrollModal = false">&times;</button>
        </div>
        <div class="modal-body" style="padding: 1.5rem 0 0 0;">
          <div class="search-box-group">
            <label for="studentSearch">Tìm kiếm học viên</label>
            <input 
              type="text" 
              id="studentSearch" 
              v-model="studentSearchQuery" 
              placeholder="Nhập tên hoặc email..." 
              class="search-input"
            >
          </div>
          
          <div class="students-select-list">
            <div v-if="loadingStudents" class="select-loading-spinner">
              <div class="spinner"></div>
              <span>Đang tải danh sách học viên...</span>
            </div>
            <template v-else>
              <div 
                v-for="st in filteredStudents" 
                :key="st.id" 
                class="student-select-item"
                :class="{ selected: selectedStudentId === st.id }"
                @click="selectedStudentId = st.id"
              >
                <div class="student-select-info">
                  <strong>{{ st.fullName || st.username }}</strong>
                  <span>{{ st.email }}</span>
                </div>
                <div class="select-indicator">
                  <span v-if="selectedStudentId === st.id" class="check-icon">&#10004;</span>
                </div>
              </div>
              <div v-if="filteredStudents.length === 0" class="empty-select-list">
                Không tìm thấy học viên phù hợp hoặc tất cả đã tham gia lớp.
              </div>
            </template>
          </div>
          
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showEnrollModal = false">Hủy bỏ</button>
            <button 
              type="button" 
              class="submit-btn" 
              :disabled="!selectedStudentId || enrolling" 
              @click="handleEnrollStudent"
              style="display: flex; align-items: center; justify-content: center; gap: 0.5rem;"
            >
              <div v-if="enrolling" class="spinner-sm"></div>
              {{ enrolling ? 'Đang thêm...' : 'Thêm vào lớp' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

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
import { useTopikLevelStore } from '../../stores/topikLevel'

const studySetStore = useStudySetStore()
const quizStore = useQuizStore()
const topikLevelStore = useTopikLevelStore()

const topikLevels = computed(() => topikLevelStore.levels)

onMounted(async () => {
  try {
    await topikLevelStore.fetchLevels()
  } catch (e) {
    console.error("Failed to fetch topik levels:", e)
  }
})

const classes = computed(() => {
  const dbClasses = studySetStore.classes || []
  return dbClasses.map(cls => ({
    ...cls,
    students: cls.students || [],
    studentsCount: cls.studentsCount || 0
  }))
})

const selectedClass = ref(null)
const activeTab = ref('students')
const showCreateModal = ref(false)

// New Class Fields
const newClassLevelId = ref('')
const newClassSchedule = ref('')
const newClassRoom = ref('')

const assignedQuizzes = computed(() => {
  if (!selectedClass.value) return []
  return quizStore.quizzes.filter(q => q.classId === selectedClass.value.id || q.clazzId === selectedClass.value.id)
})

const handleCreateClass = async () => {
  if (!newClassLevelId.value || !newClassSchedule.value || !newClassRoom.value) return

  try {
    await studySetStore.createClass({
      topikLevelId: newClassLevelId.value,
      schedule: newClassSchedule.value,
      room: newClassRoom.value,
      notes: ''
    })
    toast.success("Tạo lớp học mới thành công!")
  } catch (e) {
    console.error("Failed to create class via API:", e)
    toast.error("Tạo lớp học mới thất bại.")
  }

  // Reset fields
  newClassLevelId.value = ''
  newClassSchedule.value = ''
  newClassRoom.value = ''
  showCreateModal.value = false
}

const handleDeleteClass = async (classId) => {
  if (!confirm("Bạn có chắc chắn muốn xóa lớp học này?")) return
  try {
    await studySetStore.deleteClass(classId)
    selectedClass.value = null
    toast.success("Xóa lớp học thành công!")
  } catch (e) {
    console.error("Failed to delete class:", e)
    toast.error("Có lỗi xảy ra khi xóa lớp học.")
  }
}

// Add Student Enrollment state and actions
const showEnrollModal = ref(false)
const studentSearchQuery = ref('')
const selectedStudentId = ref(null)
const enrolling = ref(false)
const loadingStudents = ref(false)

const openEnrollModal = async () => {
  loadingStudents.value = true
  showEnrollModal.value = true
  try {
    await studySetStore.fetchStudents()
    studentSearchQuery.value = ''
    selectedStudentId.value = null
  } catch (err) {
    console.error(err)
    toast.error("Không thể tải danh sách học viên")
  } finally {
    loadingStudents.value = false
  }
}

const filteredStudents = computed(() => {
  const list = studySetStore.studentsList || []
  const enrolledIds = (selectedClass.value?.students || []).map(s => s.id)
  return list.filter(st => {
    if (enrolledIds.includes(st.id)) return false
    const q = studentSearchQuery.value.toLowerCase()
    return (st.fullName || '').toLowerCase().includes(q) || 
           (st.username || '').toLowerCase().includes(q) || 
           (st.email || '').toLowerCase().includes(q)
  })
})

const handleEnrollStudent = async () => {
  if (!selectedStudentId.value || !selectedClass.value) return
  enrolling.value = true
  try {
    const updated = await studySetStore.enrollStudent(selectedClass.value.id, selectedStudentId.value)
    selectedClass.value = {
      ...selectedClass.value,
      students: updated.students || [],
      studentsCount: updated.studentsCount || 0
    }
    toast.success("Thêm học viên vào lớp thành công!")
    showEnrollModal.value = false
  } catch (err) {
    console.error(err)
    toast.error("Thêm học viên vào lớp thất bại.")
  } finally {
    enrolling.value = false
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
  justify-content: center;
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

.tab-actions-row {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1rem;
}

.primary-btn-sm {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.45rem 0.9rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  transition: all 0.2s;
}

.primary-btn-sm:hover {
  background-color: var(--primary-hover);
}

.search-box-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1rem;
}

.search-box-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.search-input {
  padding: 0.6rem 0.8rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.students-select-list {
  max-height: 220px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 1.5rem;
}

.student-select-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color 0.2s;
}

.student-select-item:last-child {
  border-bottom: none;
}

.student-select-item:hover {
  background-color: var(--bg-card);
}

.student-select-item.selected {
  background-color: rgba(219, 142, 113, 0.1);
}

.student-select-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.student-select-info strong {
  color: var(--text-title);
  font-size: 0.9rem;
}

.student-select-info span {
  color: var(--text-muted);
  font-size: 0.8rem;
}

.select-indicator {
  color: var(--primary);
  font-size: 1rem;
}

.empty-select-list {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* Spinner Animations */
.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-sm {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.select-loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}
</style>
