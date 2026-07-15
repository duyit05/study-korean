<template>
  <div class="teacher-students animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2 style="margin: 0; font-size: 1.35rem; font-weight: 700; color: var(--text-title);">Quản lý Học viên</h2>
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
          <AppSelect
            id="levelFilter"
            v-model="selectedLevelFilter"
            :options="levelFilterOptions"
            placeholder="Tất cả cấp độ"
            style="min-width: 180px;"
          />
        </div>
        <div class="filter-group">
          <label for="statusFilter">Trạng thái</label>
          <AppSelect
            id="statusFilter"
            v-model="selectedStatusFilter"
            :options="statusFilterOptions"
            placeholder="Tất cả"
            style="min-width: 120px;"
          />
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
            <tr v-for="student in paginatedStudents" :key="student.id">
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
                  <button class="action-btn view" @click="openProgressModal(student)" title="Xem tiến trình học">
                    <AppIcon name="eye" size="16" />
                  </button>
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

      <!-- Pagination Section -->
      <div v-if="totalElements > 0" class="pagination-bar">
        <div class="pagination-info">
          Đang hiển thị <strong>{{ startIndex }} - {{ endIndex }}</strong> trong số <strong>{{ totalElements }}</strong> học viên
        </div>
        <div class="pagination-controls">
          <div class="page-buttons">
            <button 
              class="page-nav-btn" 
              :disabled="currentPage === 1" 
              @click="goToPage(1)" 
              title="Trang đầu"
            >
              <span style="display: inline-flex; align-items: center; margin-right: -4px;">
                <AppIcon name="chevron-left" size="12" />
                <AppIcon name="chevron-left" size="12" style="margin-left: -6px;" />
              </span>
            </button>
            <button 
              class="page-nav-btn" 
              :disabled="currentPage === 1" 
              @click="prevPage" 
              title="Trang trước"
            >
              <AppIcon name="chevron-left" size="12" />
            </button>
            
            <button 
              v-for="page in pageNumbers" 
              :key="page" 
              class="page-num-btn" 
              :class="{ active: currentPage === page, dots: page === '...' }"
              :disabled="page === '...'"
              @click="typeof page === 'number' && goToPage(page)"
            >
              {{ page }}
            </button>

            <button 
              class="page-nav-btn" 
              :disabled="currentPage === totalPages" 
              @click="nextPage" 
              title="Trang sau"
            >
              <AppIcon name="chevron-right" size="12" />
            </button>
            <button 
              class="page-nav-btn" 
              :disabled="currentPage === totalPages" 
              @click="goToPage(totalPages)" 
              title="Trang cuối"
            >
              <span style="display: inline-flex; align-items: center; margin-left: -4px;">
                <AppIcon name="chevron-right" size="12" style="margin-right: -6px;" />
                <AppIcon name="chevron-right" size="12" />
              </span>
            </button>
          </div>
        </div>
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
              <AppSelect
                id="newCurrentLevel"
                v-model="newStudent.currentLevel"
                :options="levelFormOptions"
                placeholder="Chưa phân lớp"
              />
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
              <AppSelect
                id="editCurrentLevel"
                v-model="editingStudent.currentLevel"
                :options="levelFormOptions"
                placeholder="Chưa phân lớp"
              />
            </div>
            <div class="form-group">
              <label for="editStatus">Trạng thái tài khoản</label>
              <AppSelect
                id="editStatus"
                v-model="editingStudent.isActive"
                :options="statusFormOptions"
              />
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

    <!-- Student Progress Detail Modal -->
    <div v-if="showProgressModal" class="modal-overlay">
      <div class="modal-content large animate-scale" style="max-width: 680px;">
        <div class="modal-header">
          <h3>Tiến trình học tập: {{ selectedStudent?.fullName }}</h3>
          <button class="close-btn" @click="showProgressModal = false">&times;</button>
        </div>
        <div class="modal-body" style="padding-top: 1rem; max-height: 520px; overflow-y: auto;">
          <!-- Profile Quick Summary -->
          <div class="progress-student-summary" style="display: flex; gap: 1rem; align-items: center; background-color: var(--bg-body); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1.25rem; border: 1px solid var(--border-color);">
            <div class="avatar-circle" style="width: 50px; height: 50px; font-size: 1.5rem; font-weight: 700;">
              {{ selectedStudent?.fullName ? selectedStudent.fullName[0].toUpperCase() : 'S' }}
            </div>
            <div>
              <h4 style="margin: 0; font-size: 1.1rem; font-weight: 700; color: var(--text-title);">{{ selectedStudent?.fullName }}</h4>
              <p style="margin: 0.15rem 0 0 0; font-size: 0.85rem; color: var(--text-muted);">
                Username: @{{ selectedStudent?.username }} | Trình độ: {{ selectedStudent?.currentLevel || 'Chưa phân lớp' }}
              </p>
            </div>
          </div>

          <!-- Tabs inside Modal -->
          <div class="modal-tabs" style="display: flex; border-bottom: 1px solid var(--border-color); margin-bottom: 1rem;">
            <button 
              type="button" 
              class="tab-btn" 
              :class="{ active: progressActiveTab === 'classes' }"
              @click="progressActiveTab = 'classes'"
              style="padding: 0.5rem 1rem; font-weight: 600; font-size: 0.85rem; background: none; border: none; border-bottom: 2px solid transparent; cursor: pointer; color: var(--text-muted);"
            >
              Lớp học
            </button>
            <button 
              type="button" 
              class="tab-btn" 
              :class="{ active: progressActiveTab === 'quizzes' }"
              @click="progressActiveTab = 'quizzes'"
              style="padding: 0.5rem 1rem; font-weight: 600; font-size: 0.85rem; background: none; border: none; border-bottom: 2px solid transparent; cursor: pointer; color: var(--text-muted);"
            >
              Lịch sử thi & bài tập
            </button>
            <button 
              type="button" 
              class="tab-btn" 
              :class="{ active: progressActiveTab === 'vocab' }"
              @click="progressActiveTab = 'vocab'"
              style="padding: 0.5rem 1rem; font-weight: 600; font-size: 0.85rem; background: none; border: none; border-bottom: 2px solid transparent; cursor: pointer; color: var(--text-muted);"
            >
              Tiến trình từ vựng
            </button>
          </div>

          <!-- Tab Content: Classes -->
          <div v-show="progressActiveTab === 'classes'" class="progress-tab-content">
            <div v-if="loadingProgress" class="select-loading-spinner" style="padding: 2rem 0; text-align: center; color: var(--text-muted);">
              Đang tải thông tin...
            </div>
            <div v-else-if="!studentProgress.classes || studentProgress.classes.length === 0" class="empty-state" style="padding: 2rem 0; text-align: center; color: var(--text-muted);">
              Chưa tham gia lớp học nào.
            </div>
            <div v-else class="progress-classes-list" style="display: flex; flex-direction: column; gap: 0.75rem;">
              <div v-for="cls in studentProgress.classes" :key="cls.id" style="background-color: var(--bg-body); padding: 0.75rem 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center;">
                <div>
                  <strong style="color: var(--text-title); font-size: 0.9rem;">Lớp: {{ cls.name }}</strong>
                  <span style="display: block; font-size: 0.75rem; color: var(--text-muted); margin-top: 0.15rem;">Khóa: {{ cls.courseTitle }}</span>
                </div>
                <span class="status-badge active" style="font-size: 0.75rem; padding: 0.15rem 0.5rem; border-radius: 10px; font-weight: 600; background-color: rgba(16, 185, 129, 0.1); color: #10b981;">Đang học</span>
              </div>
            </div>
          </div>

          <!-- Tab Content: Quiz attempts -->
          <div v-show="progressActiveTab === 'quizzes'" class="progress-tab-content">
            <div v-if="loadingProgress" class="select-loading-spinner" style="padding: 2rem 0; text-align: center; color: var(--text-muted);">
              Đang tải thông tin...
            </div>
            <div v-else-if="!studentProgress.quizAttempts || studentProgress.quizAttempts.length === 0" class="empty-state" style="padding: 2rem 0; text-align: center; color: var(--text-muted);">
              Chưa có lịch sử làm bài thi hay bài tập.
            </div>
            <div v-else class="progress-attempts-table" style="border: 1px solid var(--border-color); border-radius: var(--radius-sm); overflow: hidden;">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left;">
                <thead>
                  <tr style="background-color: var(--bg-body); border-bottom: 1px solid var(--border-color);">
                    <th style="padding: 0.65rem 1rem;">Đề thi / Bài tập</th>
                    <th style="padding: 0.65rem 1rem;">Điểm đạt</th>
                    <th style="padding: 0.65rem 1rem;">Trạng thái</th>
                    <th style="padding: 0.65rem 1rem;">Ngày nộp</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="att in studentProgress.quizAttempts" :key="att.id" style="border-bottom: 1px solid var(--border-color);">
                    <td style="padding: 0.65rem 1rem; color: var(--text-title); font-weight: 600;">{{ att.quizTitle }}</td>
                    <td style="padding: 0.65rem 1rem; color: var(--primary); font-weight: 700;">{{ att.score }} điểm</td>
                    <td style="padding: 0.65rem 1rem;">
                      <span style="font-size: 0.75rem; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 600;" :class="att.status === 'COMPLETED' ? 'status-badge active' : 'status-badge-sm orange'">
                        {{ att.status === 'COMPLETED' ? 'Hoàn thành' : 'Chờ chấm' }}
                      </span>
                    </td>
                    <td style="padding: 0.65rem 1rem; color: var(--text-muted); font-size: 0.8rem;">
                      {{ att.submittedAt ? att.submittedAt.replace('T', ' ').substring(0, 16) : 'N/A' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Tab Content: Vocabulary progress -->
          <div v-show="progressActiveTab === 'vocab'" class="progress-tab-content">
            <div v-if="loadingProgress" class="select-loading-spinner" style="padding: 2rem 0; text-align: center; color: var(--text-muted);">
              Đang tải thông tin...
            </div>
            <div v-else class="progress-vocab-summary" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem;">
              <div style="background-color: var(--bg-body); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); text-align: center;">
                <div style="font-size: 1.75rem; font-weight: 800; color: #10b981;">{{ studentProgress.vocabProgress?.totalLearnedCards || 0 }}</div>
                <span style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Từ vựng đã thuộc</span>
              </div>
              <div style="background-color: var(--bg-body); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); text-align: center;">
                <div style="font-size: 1.75rem; font-weight: 800; color: #f59e0b;">{{ studentProgress.vocabProgress?.totalReviewCards || 0 }}</div>
                <span style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Từ vựng cần ôn tập</span>
              </div>
              <div style="background-color: var(--bg-body); padding: 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); text-align: center;">
                <div style="font-size: 1.75rem; font-weight: 800; color: var(--primary);">{{ studentProgress.vocabProgress?.totalStudySets || 0 }}</div>
                <span style="font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">Bộ từ vựng đã học</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer" style="padding: 1rem 1.5rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end;">
          <button type="button" class="cancel-btn" @click="showProgressModal = false" style="margin: 0;">Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
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

const levelFilterOptions = computed(() => [
  { value: '', label: 'Tất cả cấp độ' },
  ...topikLevels.value.map(lvl => ({ value: lvl.name, label: lvl.name }))
])

const statusFilterOptions = [
  { value: '', label: 'Tất cả' },
  { value: 'active', label: 'Hoạt động' },
  { value: 'inactive', label: 'Đã khóa' }
]

const levelFormOptions = computed(() => [
  { value: '', label: 'Chưa phân lớp' },
  ...topikLevels.value.map(lvl => ({ value: lvl.name, label: lvl.name }))
])

const statusFormOptions = [
  { value: true, label: 'Hoạt động' },
  { value: false, label: 'Đã khóa' }
]

// Modal state
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteConfirm = ref(false)
const showProgressModal = ref(false)
const progressActiveTab = ref('classes')
const selectedStudent = ref(null)
const loadingProgress = ref(false)
const studentProgress = ref({
  classes: [],
  quizAttempts: [],
  vocabProgress: { totalLearnedCards: 0, totalReviewCards: 0, totalStudySets: 0 }
})

const openProgressModal = async (student) => {
  selectedStudent.value = student
  showProgressModal.value = true
  progressActiveTab.value = 'classes'
  loadingProgress.value = true
  try {
    const data = await studentStore.fetchStudentProgress(student.id)
    studentProgress.value = data || {
      classes: [],
      quizAttempts: [],
      vocabProgress: { totalLearnedCards: 0, totalReviewCards: 0, totalStudySets: 0 }
    }
  } catch (err) {
    console.error("Failed to load student progress details:", err)
    toast.error("Không thể tải thông tin tiến trình của học viên.")
  } finally {
    loadingProgress.value = false
  }
}
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

const loadStudentsFromServer = async () => {
  try {
    const params = {
      unpaged: false,
      page: currentPage.value,
      size: itemsPerPage.value
    }
    if (searchQuery.value.trim()) {
      params.search = searchQuery.value.trim()
    }
    if (selectedLevelFilter.value) {
      params.level = selectedLevelFilter.value
    }
    if (selectedStatusFilter.value) {
      params.isActive = selectedStatusFilter.value === 'active'
    }
    const pageData = await studentStore.fetchStudents(params)
    totalPages.value = pageData.totalPage || 1
    totalElements.value = pageData.totalElements || 0
  } catch (err) {
    console.error("Failed to load students from server:", err)
    toast.error("Không thể tải danh sách học viên từ máy chủ.")
  }
}

onMounted(async () => {
  try {
    await loadStudentsFromServer()
    await topikLevelStore.fetchLevels()
  } catch (err) {
    console.error("Error initial load:", err)
    toast.error("Không thể tải danh sách dữ liệu ban đầu.")
  }
})

// Filter computation
const filteredStudents = computed(() => {
  return students.value || []
})

// Pagination logic
const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalPages = ref(1)
const totalElements = ref(0)

const startIndex = computed(() => {
  if (totalElements.value === 0) return 0
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endIndex = computed(() => {
  return Math.min(currentPage.value * itemsPerPage.value, totalElements.value)
})

const paginatedStudents = computed(() => {
  return students.value || []
})

const pageNumbers = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  const maxVisible = 5
  
  if (total <= maxVisible) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, 4, '...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...', total - 3, total - 2, total - 1, total)
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total)
    }
  }
  return pages
})

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

let searchTimeout = null
watch(searchQuery, () => {
  currentPage.value = 1
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    loadStudentsFromServer()
  }, 300)
})

watch([selectedLevelFilter, selectedStatusFilter], () => {
  currentPage.value = 1
  loadStudentsFromServer()
})

watch(currentPage, () => {
  loadStudentsFromServer()
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
    await loadStudentsFromServer()
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
    await loadStudentsFromServer()
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
    await loadStudentsFromServer()
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
  padding: 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
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
  padding: 0.45rem 1rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.primary-btn:hover {
  background-color: var(--primary-hover);
}

/* Filter Card */
.filter-card {
  background-color: var(--bg-card);
  padding: 0.75rem 1.25rem;
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
  padding: 0.45rem 1rem 0.45rem 2.25rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.85rem;
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
  white-space: nowrap;
}

.filter-group :deep(.select-trigger) {
  background-color: var(--bg-body);
  padding: 0.45rem 0.75rem;
}

.form-group :deep(.select-trigger) {
  background-color: var(--bg-body);
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
  padding: 0.75rem 1rem;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--border-color);
}

.data-table td {
  padding: 0.5rem 1rem;
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
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background-color: rgba(219, 142, 113, 0.15);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.95rem;
}

.student-names {
  display: flex;
  flex-direction: column;
}

.student-names strong {
  color: var(--text-title);
  font-size: 0.88rem;
}

.student-names span {
  color: var(--text-muted);
  font-size: 0.75rem;
}

/* Contact Info cell */
.contact-info {
  display: flex;
  flex-direction: column;
}

.contact-info .email {
  color: var(--text-title);
  font-size: 0.85rem;
}

.contact-info .phone {
  color: var(--text-muted);
  font-size: 0.75rem;
}

.contact-info .phone.empty {
  font-style: italic;
  opacity: 0.7;
}

/* Goal cell */
.goal-text {
  display: block;
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 0.85rem;
  color: var(--text-body);
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

.modal-tabs .tab-btn.active {
  border-bottom: 2px solid var(--primary) !important;
  color: var(--primary) !important;
}

.modal-tabs .tab-btn:hover {
  color: var(--text-title) !important;
}

/* Pagination Styling */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin: 1.5rem 1.25rem 1.25rem 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--border-color);
  min-height: 56px;
}

.pagination-info {
  font-size: 0.9rem;
  color: var(--text-body);
  position: absolute;
  left: 0;
}

.pagination-info strong {
  color: var(--text-title);
  font-weight: 700;
}

.pagination-controls {
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .pagination-bar {
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
    min-height: auto;
  }
  
  .pagination-info {
    position: static;
  }
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-body);
}

.page-buttons {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.page-nav-btn, .page-num-btn {
  height: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-body);
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  padding: 0 0.5rem;
}

.page-nav-btn:hover:not(:disabled), .page-num-btn:hover:not(:disabled):not(.dots) {
  background-color: var(--bg-hover);
  border-color: var(--border-color-hover);
  color: var(--text-title);
  transform: translateY(-1px);
}

.page-nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-num-btn.active {
  background-color: var(--primary);
  border-color: var(--primary);
  color: #fff;
  font-weight: 700;
  box-shadow: var(--shadow-sm);
}

.page-num-btn.dots {
  border-color: transparent;
  background-color: transparent;
  cursor: default;
}
</style>
