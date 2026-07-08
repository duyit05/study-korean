<template>
  <div class="teacher-courses animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Quản lý Khóa học</h2>
        <p>Soạn thảo và quản lý các khóa học tiếng Hàn. Mỗi lớp học sẽ thuộc về một khóa học cụ thể.</p>
      </div>
      <button class="primary-btn" @click="openCreateModal">
        <AppIcon name="plus" size="18" />
        <span>Thêm khóa học mới</span>
      </button>
    </div>

    <!-- Stats Summary Row -->
    <div class="courses-stats border-box">
      <div class="stat-item">
        <span class="count">{{ courses.length }}</span>
        <span class="lbl">Tổng khóa học</span>
      </div>
      <div class="stat-item">
        <span class="count">{{ publishedCount }}</span>
        <span class="lbl">Đã xuất bản</span>
      </div>
      <div class="stat-item">
        <span class="count">{{ draftCount }}</span>
        <span class="lbl">Bản nháp</span>
      </div>
    </div>

    <!-- Course Grid -->
    <div v-if="courseStore.loading" class="loading-state">
      <AppIcon name="refresh" class="spin" size="32" />
      <p>Đang tải danh sách khóa học...</p>
    </div>

    <div v-else-if="courses.length === 0" class="empty-state border-box">
      <AppIcon name="alert" size="48" />
      <p>Chưa có khóa học nào được tạo. Hãy bấm vào "Thêm khóa học mới" để bắt đầu!</p>
    </div>

    <div v-else class="courses-grid">
      <div 
        v-for="course in courses" 
        :key="course.id" 
        class="course-card"
        :class="{ draft: !course.isPublished }"
      >
        <div class="course-thumb">
          <img :src="course.thumbnailUrl || defaultThumbnail" alt="Thumbnail Course">
          <span class="status-badge" :class="course.isPublished ? 'published' : 'draft'">
            {{ course.isPublished ? 'Đã xuất bản' : 'Bản nháp' }}
          </span>
        </div>
        <div class="course-body">
          <div class="level-tag">{{ course.level }}</div>
          <h3>{{ course.title }}</h3>
          <p class="desc">{{ course.description || 'Chưa có mô tả cho khóa học này.' }}</p>
          <div class="meta-row">
            <span><strong>{{ course.totalSessions }}</strong> Buổi học</span>
            <span class="price">{{ formatPrice(course.price) }}</span>
          </div>
        </div>
        <div class="course-footer">
          <button class="action-btn edit-btn" @click="openEditModal(course)" title="Chỉnh sửa khóa học">
            <AppIcon name="edit" size="16" /> Sửa
          </button>
          <button class="action-btn delete-btn" @click="triggerDelete(course)" title="Xóa khóa học">
            <AppIcon name="trash" size="16" /> Xóa
          </button>
        </div>
      </div>
    </div>

    <!-- Create / Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>{{ isEditMode ? 'Cập nhật khóa học' : 'Tạo khóa học mới' }}</h3>
          <button class="close-btn" @click="showModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label for="courseTitle">Tiêu đề khóa học <span class="required">*</span></label>
            <input 
              type="text" 
              id="courseTitle" 
              v-model="form.title" 
              placeholder="Ví dụ: Tiếng Hàn Sơ Cấp 1A" 
              required
            >
          </div>

          <div class="form-group">
            <label for="courseDesc">Mô tả khóa học</label>
            <textarea 
              id="courseDesc" 
              v-model="form.description" 
              placeholder="Nhập mô tả chi tiết khóa học..."
              rows="3"
            ></textarea>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label for="courseLevel">Cấp độ học tập <span class="required">*</span></label>
              <AppSelect id="courseLevel" v-model="form.level" :options="levelOptions" placeholder="-- Cấp độ học tập --" />
            </div>

            <div class="form-group">
              <label for="courseSessions">Tổng số buổi học <span class="required">*</span></label>
              <input 
                type="number" 
                id="courseSessions" 
                v-model.number="form.totalSessions" 
                min="1" 
                required
              >
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label for="coursePrice">Giá khóa học (VNĐ)</label>
              <input 
                type="number" 
                id="coursePrice" 
                v-model.number="form.price" 
                min="0" 
                placeholder="Để trống nếu miễn phí"
              >
            </div>

            <div class="form-group">
              <label for="courseThumb">Thumbnail URL</label>
              <input 
                type="text" 
                id="courseThumb" 
                v-model="form.thumbnailUrl" 
                placeholder="Nhập link ảnh nền..."
              >
            </div>
          </div>

          <div class="form-group checkbox-group">
            <label class="checkbox-container">
              <input type="checkbox" v-model="form.isPublished">
              <span class="checkmark"></span>
              Xuất bản khóa học ngay lập tức (Học viên có thể xem thấy)
            </label>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ isEditMode ? 'Lưu thay đổi' : 'Tạo khóa học' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <div class="modal-content delete-confirm animate-scale">
        <div class="modal-header">
          <h3>Xác nhận xóa khóa học</h3>
          <button class="close-btn" @click="showDeleteConfirm = false">&times;</button>
        </div>
        <div class="modal-body text-center">
          <div class="alert-icon-wrapper">
            <AppIcon name="alert" size="48" class="warning-color" />
          </div>
          <p class="warning-text">
            Bạn có chắc chắn muốn xóa khóa học <strong>{{ courseToDelete?.title }}</strong>?
          </p>
          <p class="sub-text">
            Hành động này không thể hoàn tác. Lớp học đang liên kết với khóa học này sẽ bị mất tham chiếu.
          </p>
        </div>
        <div class="modal-actions">
          <button type="button" class="cancel-btn" @click="showDeleteConfirm = false">Hủy bỏ</button>
          <button type="button" class="submit-btn danger-btn" @click="confirmDelete" :disabled="submitting">
            Xác nhận xóa
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCourseStore } from '../../stores/course'
import { useTopikLevelStore } from '../../stores/topikLevel'
import AppIcon from '../icons/AppIcon.vue'
import AppSelect from '../AppSelect.vue'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const courseStore = useCourseStore()
const topikLevelStore = useTopikLevelStore()

const courses = computed(() => courseStore.courses)
const topikLevels = computed(() => topikLevelStore.levels)
const levelOptions = computed(() => {
  return topikLevels.value.map(lvl => ({ label: lvl.name, value: lvl.name }))
})
const publishedCount = computed(() => courses.value.filter(c => c.isPublished).length)
const draftCount = computed(() => courses.value.filter(c => !c.isPublished).length)

const defaultThumbnail = 'https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?auto=format&fit=crop&q=80&w=400'

const showModal = ref(false)
const isEditMode = ref(false)
const selectedCourseId = ref(null)
const submitting = ref(false)
const showDeleteConfirm = ref(false)
const courseToDelete = ref(null)

const form = ref({
  title: '',
  description: '',
  level: '',
  totalSessions: 20,
  price: 0,
  thumbnailUrl: '',
  isPublished: false
})

onMounted(async () => {
  try {
    await courseStore.fetchCourses()
    await topikLevelStore.fetchLevels()
  } catch (error) {
    toast.error('Lỗi khi tải danh sách khóa học hoặc cấp độ: ' + error.message)
  }
})

const openCreateModal = () => {
  isEditMode.value = false
  selectedCourseId.value = null
  form.value = {
    title: '',
    description: '',
    level: '',
    totalSessions: 20,
    price: 0,
    thumbnailUrl: '',
    isPublished: false
  }
  showModal.value = true
}

const openEditModal = (course) => {
  isEditMode.value = true
  selectedCourseId.value = course.id
  form.value = {
    title: course.title,
    description: course.description || '',
    level: course.level,
    totalSessions: course.totalSessions,
    price: course.price || 0,
    thumbnailUrl: course.thumbnailUrl || '',
    isPublished: course.isPublished
  }
  showModal.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      level: form.value.level,
      totalSessions: form.value.totalSessions,
      price: form.value.price || 0,
      thumbnailUrl: form.value.thumbnailUrl,
      isPublished: form.value.isPublished
    }

    if (isEditMode.value) {
      await courseStore.updateCourse(selectedCourseId.value, payload)
      toast.success('Cập nhật khóa học thành công!')
    } else {
      await courseStore.createCourse(payload)
      toast.success('Tạo khóa học mới thành công!')
    }
    showModal.value = false
  } catch (error) {
    toast.error('Lỗi: ' + error.message)
  } finally {
    submitting.value = false
  }
}

const triggerDelete = (course) => {
  courseToDelete.value = course
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  if (!courseToDelete.value) return
  submitting.value = true
  try {
    await courseStore.deleteCourse(courseToDelete.value.id)
    toast.success('Xóa khóa học thành công!')
    showDeleteConfirm.value = false
    courseToDelete.value = null
  } catch (error) {
    toast.error('Lỗi khi xóa khóa học: ' + error.message)
  } finally {
    submitting.value = false
  }
}

const formatPrice = (price) => {
  if (!price || price === 0) return 'Miễn phí'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}
</script>

<style scoped>
.teacher-courses {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 1.5rem;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1.25rem;
}

.header-section h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.header-section p {
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

/* Stats */
.courses-stats {
  display: flex;
  gap: 3rem;
  padding: 1.25rem 2rem;
  background-color: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-item .count {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--primary);
  line-height: 1.2;
}

.stat-item .lbl {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-weight: 600;
}

/* Grid */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.course-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary);
}

.course-card.draft {
  opacity: 0.85;
  border-style: dashed;
}

.course-thumb {
  position: relative;
  height: 160px;
  overflow: hidden;
  background-color: var(--bg-badge);
}

.course-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  padding: 0.25rem 0.6rem;
  font-size: 0.7rem;
  font-weight: 700;
  border-radius: var(--radius-sm);
  color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.15);
}

.status-badge.published {
  background-color: var(--success);
}

.status-badge.draft {
  background-color: var(--warning);
}

.course-body {
  padding: 1.25rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.level-tag {
  align-self: flex-start;
  padding: 0.2rem 0.5rem;
  background-color: var(--primary-light);
  color: var(--primary);
  font-size: 0.7rem;
  font-weight: 700;
  border-radius: var(--radius-sm);
  margin-bottom: 0.75rem;
}

.course-body h3 {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.5rem;
  line-height: 1.3;
}

.course-body .desc {
  font-size: 0.85rem;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 1.25rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-row {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
  color: var(--text-body);
  border-top: 1px solid var(--border-color);
  padding-top: 0.75rem;
}

.meta-row .price {
  font-weight: 800;
  color: var(--primary);
}

.course-footer {
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-top: 1px solid var(--border-color);
  background-color: var(--bg-badge);
}

.action-btn {
  padding: 0.75rem;
  border: none;
  background: transparent;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  transition: background-color 0.2s ease;
}

.edit-btn {
  color: var(--text-body);
  border-right: 1px solid var(--border-color);
}

.edit-btn:hover {
  background-color: var(--primary-light);
  color: var(--primary);
}

.delete-btn {
  color: var(--danger);
}

.delete-btn:hover {
  background-color: #fee2e2;
}

/* Modals Overlay */
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
  max-width: 500px;
  padding: 1.75rem;
  box-shadow: var(--shadow-lg);
  position: relative;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
  margin-bottom: 1.25rem;
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-title);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: var(--text-title);
}

/* Form layouts */
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

.modal-form input,
.modal-form textarea {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.9rem;
  width: 100%;
}

.modal-form input:focus,
.modal-form textarea:focus {
  border-color: var(--primary);
  outline: none;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.required {
  color: var(--danger);
}

.checkbox-group {
  margin-top: 0.5rem;
}

.checkbox-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-body);
  cursor: pointer;
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

.submit-btn:disabled, .cancel-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.danger-btn {
  background-color: var(--danger) !important;
}

.danger-btn:hover {
  background-color: #dc2626 !important;
}

.warning-color {
  color: var(--warning);
}

.text-center {
  text-align: center;
}

.alert-icon-wrapper {
  margin-bottom: 1rem;
}

.warning-text {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.5rem;
}

.sub-text {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 4rem 2rem;
  color: var(--text-muted);
  text-align: center;
}

.loading-state p,
.empty-state p {
  font-size: 0.95rem;
  font-weight: 600;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
