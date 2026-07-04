<template>
  <div class="teacher-students animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Quản lý Học viên</h2>
        <p>Quản lý thông tin tài khoản, tiến trình học tập và mục tiêu của học viên.</p>
      </div>
      <button class="primary-btn" @click="openCreateModal">
        <AppIcon name="plus" size="16" />
        Thêm học viên mới
      </button>
    </div>

    <!-- Filter & Search Section -->
    <div class="filter-card">
      <div class="search-box">
        <AppIcon name="search" size="18" class="search-icon" />
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Tìm kiếm học viên theo tên, tài khoản hoặc email..."
        >
      </div>
      <div class="filter-options">
        <div class="filter-group">
          <label for="levelFilter">Cấp độ</label>
          <select id="levelFilter" v-model="selectedLevelFilter">
            <option value="">Tất cả cấp độ</option>
            <option v-for="lvl in topikLevels" :key="lvl.id" :value="lvl.name">
              {{ lvl.name }}
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label for="statusFilter">Trạng thái</label>
          <select id="statusFilter" v-model="selectedStatusFilter">
            <option value="">Tất cả</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Đã khóa</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Students Table Section -->
    <div class="table-card">
      <div v-if="loading" class="table-loading">
        <div class="spinner"></div>
        <span>Đang tải danh sách học viên...</span>
      </div>
      
      <div v-else-if="filteredStudents.length === 0" class="table-empty">
        <AppIcon name="profile" size="48" style="opacity: 0.3; margin-bottom: 1rem;" />
        <p>Không tìm thấy học viên nào phù hợp.</p>
      </div>

      <div v-else class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Học viên</th>
              <th>Email / SĐT</th>
              <th>Mục tiêu học</th>
              <th>Trình độ hiện tại</th>
              <th>Kinh nghiệm (XP)</th>
              <th>Trạng thái</th>
              <th style="text-align: center;">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in filteredStudents" :key="student.id">
              <td>
                <div class="student-profile-info">
                  <div class="avatar-circle">
                    {{ student.fullName ? student.fullName[0].toUpperCase() : student.username[0].toUpperCase() }}
                  </div>
                  <div class="student-names">
                    <strong>{{ student.fullName }}</strong>
                    <span>@{{ student.username }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="contact-info">
                  <span class="email">{{ student.email }}</span>
                  <span class="phone" v-if="student.phone">{{ student.phone }}</span>
                  <span class="phone empty" v-else>Chưa có SĐT</span>
                </div>
              </td>
              <td>
                <span class="goal-text" :title="student.goal">
                  {{ student.goal || 'Chưa thiết lập' }}
                </span>
              </td>
              <td>
                <span class="level-badge" v-if="student.currentLevel">
                  {{ student.currentLevel }}
                </span>
                <span class="level-badge empty" v-else>Chưa phân lớp</span>
              </td>
              <td>
                <div class="xp-info">
                  <strong>{{ student.xp || 0 }} XP</strong>
                  <span>Cấp độ học: {{ student.level || 1 }}</span>
                </div>
              </td>
              <td>
                <span class="status-badge" :class="student.isActive ? 'active' : 'inactive'">
                  {{ student.isActive ? 'Hoạt động' : 'Đã khóa' }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button class="action-btn edit" @click="openEditModal(student)" title="Sửa thông tin">
                    <AppIcon name="edit" size="16" />
                  </button>
                  <button class="action-btn delete" @click="triggerDelete(student)" title="Xóa học viên">
                    <AppIcon name="trash" size="16" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create Student Modal -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Thêm học viên mới</h3>
          <button class="close-btn" @click="showCreateModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleCreateStudent" class="modal-form">
          <div class="form-grid">
            <div class="form-group">
              <label for="newUsername">Tên tài khoản (Username) <span class="required">*</span></label>
              <input type="text" id="newUsername" v-model="newStudent.username" required placeholder="Ví dụ: nguyenvana">
            </div>
            <div class="form-group">
              <label for="newEmail">Địa chỉ Email <span class="required">*</span></label>
              <input type="email" id="newEmail" v-model="newStudent.email" required placeholder="nguyenvana@gmail.com">
            </div>
            <div class="form-group">
              <label for="newPassword">Mật khẩu ban đầu <span class="required">*</span></label>
              <input type="password" id="newPassword" v-model="newStudent.password" required placeholder="Tối thiểu 6 ký tự">
            </div>
            <div class="form-group">
              <label for="newFullName">Họ và tên học viên <span class="required">*</span></label>
              <input type="text" id="newFullName" v-model="newStudent.fullName" required placeholder="Ví dụ: Nguyễn Văn A">
            </div>
            <div class="form-group">
              <label for="newPhone">Số điện thoại</label>
              <input type="text" id="newPhone" v-model="newStudent.phone" placeholder="Số điện thoại liên hệ">
            </div>
            <div class="form-group">
              <label for="newCurrentLevel">Trình độ hiện tại</label>
              <select id="newCurrentLevel" v-model="newStudent.currentLevel">
                <option value="">Chưa phân lớp</option>
                <option v-for="lvl in topikLevels" :key="lvl.id" :value="lvl.name">
                  {{ lvl.name }}
                </option>
              </select>
            </div>
            <div class="form-group full-width">
              <label for="newGoal">Mục tiêu học tập</label>
              <textarea id="newGoal" v-model="newStudent.goal" placeholder="Ví dụ: Thi đạt TOPIK II cấp 3 trong 6 tháng..." rows="2"></textarea>
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showCreateModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ submitting ? 'Đang tạo...' : 'Tạo tài khoản' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit Student Modal -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Sửa thông tin học viên</h3>
          <button class="close-btn" @click="showEditModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleUpdateStudent" class="modal-form">
          <div class="form-grid">
            <div class="form-group">
              <label>Tên tài khoản (Username)</label>
              <input type="text" :value="editingStudent.username" disabled class="disabled-input">
            </div>
            <div class="form-group">
              <label for="editEmail">Địa chỉ Email <span class="required">*</span></label>
              <input type="email" id="editEmail" v-model="editingStudent.email" required>
            </div>
            <div class="form-group">
              <label for="editFullName">Họ và tên học viên <span class="required">*</span></label>
              <input type="text" id="editFullName" v-model="editingStudent.fullName" required>
            </div>
            <div class="form-group">
              <label for="editPhone">Số điện thoại</label>
              <input type="text" id="editPhone" v-model="editingStudent.phone">
            </div>
            <div class="form-group">
              <label for="editCurrentLevel">Trình độ hiện tại</label>
              <select id="editCurrentLevel" v-model="editingStudent.currentLevel">
                <option value="">Chưa phân lớp</option>
                <option v-for="lvl in topikLevels" :key="lvl.id" :value="lvl.name">
                  {{ lvl.name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label for="editStatus">Trạng thái tài khoản</label>
              <select id="editStatus" v-model="editingStudent.isActive">
                <option :value="true">Hoạt động</option>
                <option :value="false">Đã khóa</option>
              </select>
            </div>
            <div class="form-group full-width">
              <label for="editGoal">Mục tiêu học tập</label>
              <textarea id="editGoal" v-model="editingStudent.goal" rows="2"></textarea>
            </div>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showEditModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ submitting ? 'Đang lưu...' : 'Lưu thay đổi' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal-content delete-confirm animate-scale">
        <div class="modal-header">
          <h3>Xác nhận xóa tài khoản</h3>
          <button class="close-btn" @click="showDeleteConfirm = false">&times;</button>
        </div>
        <div class="modal-body" style="padding-top: 1rem;">
          <div class="warning-alert">
            <AppIcon name="alert" size="20" class="warning-icon" />
            <div>
              <strong>Chú ý quan trọng!</strong>
              <p>Hành động này sẽ xóa vĩnh viễn tài khoản học viên <strong>{{ studentToDelete?.fullName }}</strong> (@{{ studentToDelete?.username }}). Tất cả tiến độ học từ vựng, kết quả thi và thông tin học tập liên quan cũng sẽ bị xóa vĩnh viễn và không thể khôi phục.</p>
            </div>
          </div>
          <p class="confirm-question">Bạn có chắc chắn muốn tiếp tục xóa?</p>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showDeleteConfirm = false">Hủy bỏ</button>
            <button type="button" class="danger-btn" @click="confirmDelete" :disabled="submitting">
              {{ submitting ? 'Đang xóa...' : 'Xóa tài khoản' }}
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
import { useStudentStore } from '../../stores/student'
import { useTopikLevelStore } from '../../stores/topikLevel'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const studentStore = useStudentStore()
const topikLevelStore = useTopikLevelStore()

const loading = computed(() => studentStore.loading)
const students = computed(() => studentStore.students)
const topikLevels = computed(() => topikLevelStore.levels)

// Search & Filter state
const searchQuery = ref('')
const selectedLevelFilter = ref('')
const selectedStatusFilter = ref('')

// Modal state
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteConfirm = ref(false)
const submitting = ref(false)

// Student Data forms
const newStudent = ref({
  username: '',
  email: '',
  password: '',
  fullName: '',
  phone: '',
  goal: '',
  currentLevel: ''
})

const editingStudent = ref({
  id: null,
  email: '',
  fullName: '',
  phone: '',
  goal: '',
  currentLevel: '',
  isActive: true
})

const studentToDelete = ref(null)

onMounted(async () => {
  try {
    await studentStore.fetchStudents()
    await topikLevelStore.fetchLevels()
  } catch (err) {
    console.error("Error initial load:", err)
    toast.error("Không thể tải danh sách dữ liệu ban đầu.")
  }
})

// Filter computation
const filteredStudents = computed(() => {
  let list = students.value || []
  
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    list = list.filter(s => 
      (s.fullName || '').toLowerCase().includes(q) ||
      (s.username || '').toLowerCase().includes(q) ||
      (s.email || '').toLowerCase().includes(q)
    )
  }

  if (selectedLevelFilter.value) {
    list = list.filter(s => s.currentLevel === selectedLevelFilter.value)
  }

  if (selectedStatusFilter.value) {
    const isActiveVal = selectedStatusFilter.value === 'active'
    list = list.filter(s => s.isActive === isActiveVal)
  }

  return list
})

// Action triggers
const openCreateModal = () => {
  newStudent.value = {
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
    goal: '',
    currentLevel: ''
  }
  showCreateModal.value = true
}

const openEditModal = (student) => {
  editingStudent.value = {
    id: student.id,
    username: student.username,
    email: student.email,
    fullName: student.fullName,
    phone: student.phone,
    goal: student.goal,
    currentLevel: student.currentLevel || '',
    isActive: student.isActive
  }
  showEditModal.value = true
}

const triggerDelete = (student) => {
  studentToDelete.value = student
  showDeleteConfirm.value = true
}

// Handlers
const handleCreateStudent = async () => {
  if (newStudent.value.password.length < 6) {
    toast.warning("Mật khẩu phải tối thiểu có 6 ký tự.")
    return
  }
  submitting.value = true
  try {
    await studentStore.createStudent(newStudent.value)
    toast.success("Tạo tài khoản học viên mới thành công!")
    showCreateModal.value = false
  } catch (err) {
    console.error(err)
    toast.error(err.message || "Tạo tài khoản thất bại. Username hoặc Email có thể đã tồn tại.")
  } finally {
    submitting.value = false
  }
}

const handleUpdateStudent = async () => {
  submitting.value = true
  try {
    await studentStore.updateStudent(editingStudent.value.id, {
      email: editingStudent.value.email,
      fullName: editingStudent.value.fullName,
      phone: editingStudent.value.phone,
      goal: editingStudent.value.goal,
      currentLevel: editingStudent.value.currentLevel,
      isActive: editingStudent.value.isActive
    })
    toast.success("Cập nhật thông tin học viên thành công!")
    showEditModal.value = false
  } catch (err) {
    console.error(err)
    toast.error(err.message || "Cập nhật thất bại. Email có thể đã được sử dụng.")
  } finally {
    submitting.value = false
  }
}

const confirmDelete = async () => {
  if (!studentToDelete.value) return
  submitting.value = true
  try {
    await studentStore.deleteStudent(studentToDelete.value.id)
    toast.success("Xóa tài khoản học viên thành công!")
    showDeleteConfirm.value = false
    studentToDelete.value = null
  } catch (err) {
    console.error(err)
    toast.error("Xóa tài khoản học viên thất bại.")
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.teacher-students {
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
  padding: 0.6rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s;
}

.primary-btn:hover {
  background-color: var(--primary-hover);
}

/* Filter Card */
.filter-card {
  background-color: var(--bg-card);
  padding: 1.25rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 280px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
}

.search-box input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.9rem;
}

.search-box input:focus {
  border-color: var(--primary);
  outline: none;
}

.filter-options {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
}

.filter-group select {
  padding: 0.6rem 2rem 0.6rem 1rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.85rem;
  cursor: pointer;
}

.filter-group select:focus {
  border-color: var(--primary);
  outline: none;
}

/* Table Card */
.table-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.table-loading, .table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 5rem 1rem;
  color: var(--text-muted);
}

.table-loading .spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background-color: var(--bg-body);
  padding: 1rem 1.25rem;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--border-color);
}

.data-table td {
  padding: 1.25rem;
  border-bottom: 1px solid var(--border-color);
  vertical-align: middle;
}

.data-table tr:last-child td {
  border-bottom: none;
}

/* Student Profile Info cell */
.student-profile-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(219, 142, 113, 0.15);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
}

.student-names {
  display: flex;
  flex-direction: column;
}

.student-names strong {
  color: var(--text-title);
  font-size: 0.95rem;
}

.student-names span {
  color: var(--text-muted);
  font-size: 0.8rem;
}

/* Contact Info cell */
.contact-info {
  display: flex;
  flex-direction: column;
}

.contact-info .email {
  color: var(--text-title);
  font-size: 0.9rem;
}

.contact-info .phone {
  color: var(--text-muted);
  font-size: 0.8rem;
}

.contact-info .phone.empty {
  font-style: italic;
  opacity: 0.7;
}

/* Goal cell */
.goal-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-width: 220px;
  font-size: 0.9rem;
  color: var(--text-title);
}

/* Level badge */
.level-badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  background-color: rgba(219, 142, 113, 0.1);
  color: var(--primary);
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 600;
}

.level-badge.empty {
  background-color: var(--bg-body);
  color: var(--text-muted);
  font-weight: normal;
  font-style: italic;
}

/* XP cell */
.xp-info {
  display: flex;
  flex-direction: column;
}

.xp-info strong {
  color: #10b981;
  font-size: 0.9rem;
}

.xp-info span {
  color: var(--text-muted);
  font-size: 0.8rem;
}

/* Status Badge */
.status-badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  font-size: 0.8rem;
  font-weight: 600;
  border-radius: 20px;
}

.status-badge.active {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.status-badge.inactive {
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.action-btn:hover {
  color: var(--primary);
  border-color: var(--primary);
}

.action-btn.delete:hover {
  color: #ef4444;
  border-color: #ef4444;
  background-color: rgba(239, 68, 68, 0.05);
}

/* Form Styles */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

.form-group.full-width {
  grid-column: span 2;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.form-group input, .form-group select, .form-group textarea {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.9rem;
}

.form-group input:focus, .form-group select:focus, .form-group textarea:focus {
  border-color: var(--primary);
  outline: none;
}

.form-group textarea {
  resize: vertical;
}

.disabled-input {
  background-color: var(--bg-card) !important;
  opacity: 0.6;
  cursor: not-allowed;
}

.required {
  color: #ef4444;
  margin-left: 2px;
}

/* Modals layout adjustment */
.modal-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

/* Warning alert */
.warning-alert {
  display: flex;
  gap: 0.75rem;
  background-color: rgba(239, 68, 68, 0.05);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--radius-md);
  padding: 1rem;
  color: var(--text-title);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  text-align: left;
}

.warning-icon {
  color: #ef4444;
  flex-shrink: 0;
  margin-top: 2px;
}

.warning-alert strong {
  color: #ef4444;
  display: block;
  margin-bottom: 0.25rem;
}

.confirm-question {
  text-align: center;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 1.5rem;
}

.danger-btn {
  background-color: #ef4444;
  color: #fff;
  border: none;
  padding: 0.6rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.danger-btn:hover {
  background-color: #dc2626;
}

/* Modals Overlay Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(2px);
}

.modal-content {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  width: 90%;
  max-width: 600px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.modal-content.delete-confirm {
  max-width: 480px;
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-title);
  margin: 0;
}

.close-btn {
  font-size: 1.5rem;
  color: var(--text-muted);
  line-height: 1;
  background: transparent;
  border: none;
  cursor: pointer;
}

.close-btn:hover {
  color: var(--text-title);
}

.modal-form {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.modal-body {
  padding: 1.5rem;
}

.cancel-btn {
  background-color: var(--bg-body);
  color: var(--text-muted);
  border: 1px solid var(--border-color);
  padding: 0.6rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background-color: var(--border-color);
  color: var(--text-title);
}

.submit-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.6rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover {
  background-color: var(--primary-hover);
}
</style>
