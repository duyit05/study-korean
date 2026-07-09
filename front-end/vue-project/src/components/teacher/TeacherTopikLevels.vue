<template>
  <div class="teacher-levels animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Quản lý mức độ học tập</h2>
        <p>Quản lý danh sách các cấp độ TOPIK dùng để phân loại Kho từ vựng và Đề thi.</p>
      </div>
      <button class="primary-btn" @click="openCreateModal">
        <AppIcon name="settings" size="18" />
        <span>Thêm cấp độ mới</span>
      </button>
    </div>

    <!-- Search and filter -->
    <div class="filter-bar border-box">
      <div class="search-box">
        <AppIcon name="search" size="16" class="search-icon" />
        <input 
          type="text" 
          v-model="searchQuery" 
          @keyup.enter="triggerSearch"
          placeholder="Tìm kiếm cấp độ (theo tên hoặc mã)..."
        >
      </div>
      <div class="filter-options">
        <label for="groupFilter">Lọc theo loại:</label>
        <AppSelect id="groupFilter" v-model="selectedGroupFilter" :options="groupFilterOptions" style="min-width: 220px;" />
      </div>
    </div>

    <!-- Levels Table -->
    <div class="table-container border-box">
      <div v-if="topikLevelStore.loading" class="loading-state">
        <AppIcon name="refresh" class="spin" size="32" />
        <p>Đang tải danh sách cấp độ...</p>
      </div>

      <div v-else-if="totalElements === 0" class="empty-state">
        <AppIcon name="alert" size="48" />
        <p>Không tìm thấy cấp độ nào phù hợp.</p>
      </div>

      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên cấp độ</th>
            <th>Mã định danh (Code)</th>
            <th>Nhóm áp dụng</th>
            <th class="text-right">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="level in topikLevelStore.levels" :key="level.id">
            <td><strong>#{{ level.id }}</strong></td>
            <td>
              <div class="level-name-cell">
                <span class="level-badge">한</span>
                <span class="name-text">{{ level.name }}</span>
              </div>
            </td>
            <td><code>{{ level.code }}</code></td>
            <td>
              <span class="group-badge" :class="level.groupType.toLowerCase()">
                {{ level.groupType }}
              </span>
            </td>
            <td class="text-right Actions">
              <button class="action-btnedit" @click="openEditModal(level)" title="Sửa">
                <AppIcon name="edit" size="16" />
              </button>
              <button class="action-btndelete" @click="triggerDelete(level)" title="Xóa">
                <AppIcon name="trash" size="16" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination Demo Section -->
      <div v-if="totalElements > 0" class="pagination-bar">
        <div class="pagination-info">
          Đang hiển thị <strong>{{ startIndex }} - {{ endIndex }}</strong> trong số <strong>{{ totalElements }}</strong> cấp độ
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

    <!-- Create / Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>{{ isEditMode ? 'Cập nhật cấp độ' : 'Thêm cấp độ mới' }}</h3>
          <button class="close-btn" @click="showModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label for="levelName">Tên cấp độ</label>
            <input 
              type="text" 
              id="levelName" 
              v-model="form.name" 
              placeholder="Ví dụ: Sơ cấp 1A" 
              required
            >
          </div>
          <div class="form-group">
            <label for="levelCode">Mã định danh (Code)</label>
            <input 
              type="text" 
              id="levelCode" 
              v-model="form.code" 
              placeholder="Ví dụ: SO_CAP_1A" 
              required
            >
          </div>
          <div class="form-group">
            <label for="levelGroup">Nhóm áp dụng</label>
            <AppSelect id="levelGroup" v-model="form.groupType" :options="groupTypeOptions" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ isEditMode ? 'Lưu thay đổi' : 'Tạo cấp độ' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <div class="modal-content delete-confirm animate-scale">
        <div class="modal-header">
          <h3>Xác nhận xóa cấp độ</h3>
          <button class="close-btn" @click="showDeleteConfirm = false">&times;</button>
        </div>
        <div class="modal-body text-center">
          <div class="alert-icon-wrapper">
            <AppIcon name="alert" size="48" class="warning-color" />
          </div>
          <p class="warning-text">
            Bạn có chắc chắn muốn xóa cấp độ <strong>{{ levelToDelete?.name }}</strong>?
          </p>
          <p class="sub-text">
            Hành động này không thể hoàn tác và có thể ảnh hưởng đến các đề thi hoặc bộ từ vựng đang liên kết.
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
import { ref, computed, onMounted, watch } from 'vue'
import { useTopikLevelStore } from '../../stores/topikLevel'
import AppIcon from '../icons/AppIcon.vue'
import AppSelect from '../AppSelect.vue'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const topikLevelStore = useTopikLevelStore()

const groupLabels = {
  'VOCAB': 'Kho từ vựng (VOCAB)',
  'QUIZ': 'Đề thi & Bài tập (QUIZ)'
}

const groupFilterOptions = computed(() => {
  const list = (topikLevelStore.groups || []).map(group => ({
    label: groupLabels[group] || group,
    value: group
  }))
  return [{ label: 'Tất cả các nhóm', value: 'ALL_GROUPS' }, ...list]
})

const groupTypeOptions = computed(() => {
  return (topikLevelStore.groups || []).map(group => ({
    label: groupLabels[group] || group,
    value: group
  }))
})

const searchQuery = ref('')
const selectedGroupFilter = ref('ALL_GROUPS')

const showModal = ref(false)
const isEditMode = ref(false)
const selectedLevelId = ref(null)
const submitting = ref(false)

const form = ref({
  name: '',
  code: '',
  groupType: 'VOCAB'
})

const showDeleteConfirm = ref(false)
const levelToDelete = ref(null)

const currentPage = ref(1)
const itemsPerPage = ref(10)
const totalPages = ref(1)
const totalElements = ref(0)

const loadLevels = async () => {
  try {
    const pageData = await topikLevelStore.fetchLevels({
      page: currentPage.value,
      size: itemsPerPage.value,
      search: searchQuery.value.trim() || null,
      group: selectedGroupFilter.value === 'ALL_GROUPS' ? null : selectedGroupFilter.value,
      unpaged: false
    })
    totalPages.value = pageData.totalPage || 1
    totalElements.value = pageData.totalElements || 0
  } catch (err) {
    toast.error("Không thể tải danh sách cấp độ từ máy chủ.")
  }
}

onMounted(async () => {
  try {
    await loadLevels()
    await topikLevelStore.fetchTopikGroups()
  } catch (err) {
    console.error(err)
  }
})

watch(currentPage, () => {
  loadLevels()
})

let debounceTimeout = null

watch(searchQuery, () => {
  currentPage.value = 1
  if (debounceTimeout) clearTimeout(debounceTimeout)
  debounceTimeout = setTimeout(() => {
    loadLevels()
  }, 300)
})

watch(selectedGroupFilter, () => {
  currentPage.value = 1
  loadLevels()
})

const triggerSearch = () => {
  if (debounceTimeout) clearTimeout(debounceTimeout)
  currentPage.value = 1
  loadLevels()
}

const startIndex = computed(() => {
  if (totalElements.value === 0) return 0
  return (currentPage.value - 1) * itemsPerPage.value + 1
})

const endIndex = computed(() => {
  return Math.min(currentPage.value * itemsPerPage.value, totalElements.value)
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

const openCreateModal = () => {
  isEditMode.value = false
  selectedLevelId.value = null
  form.value = {
    name: '',
    code: '',
    groupType: 'VOCAB'
  }
  showModal.value = true
}

const openEditModal = (level) => {
  isEditMode.value = true
  selectedLevelId.value = level.id
  form.value = {
    name: level.name,
    code: level.code,
    groupType: level.groupType
  }
  showModal.value = true
}

const handleSubmit = async () => {
  if (!form.value.name.trim() || !form.value.code.trim()) return
  
  submitting.value = true
  try {
    if (isEditMode.value) {
      await topikLevelStore.updateLevel(selectedLevelId.value, form.value)
      toast.success("Cập nhật cấp độ thành công!")
    } else {
      await topikLevelStore.createLevel(form.value)
      toast.success("Tạo cấp độ mới thành công!")
    }
    showModal.value = false
    await loadLevels()
  } catch (err) {
    console.error(err)
    toast.error("Đã xảy ra lỗi khi lưu thông tin cấp độ.")
  } finally {
    submitting.value = false
  }
}

const triggerDelete = (level) => {
  levelToDelete.value = level
  showDeleteConfirm.value = true
}

const confirmDelete = async () => {
  if (!levelToDelete.value) return
  submitting.value = true
  try {
    await topikLevelStore.deleteLevel(levelToDelete.value.id)
    toast.success("Xóa cấp độ thành công!")
    showDeleteConfirm.value = false
    levelToDelete.value = null
    await loadLevels()
  } catch (err) {
    console.error(err)
    toast.error("Không thể xóa cấp độ này. Vui lòng thử lại sau.")
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.teacher-levels {
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
  font-weight: 800;
  color: var(--text-title);
  letter-spacing: -0.5px;
}

.title-area p {
  font-size: 0.9rem;
  color: var(--text-muted);
}

.primary-btn {
  background-color: var(--primary);
  color: #fff;
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.primary-btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.border-box {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  box-shadow: var(--shadow-sm);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex-grow: 1;
  max-width: 480px;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
}

.search-box input {
  width: 100%;
  padding: 0.65rem 0.75rem 0.65rem 2.25rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.filter-options {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.filter-options label {
  font-weight: 600;
  color: var(--text-title);
  font-size: 0.9rem;
}

.table-container {
  overflow-x: auto;
  min-height: 250px;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 4rem 1rem;
  color: var(--text-muted);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th, .data-table td {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.data-table th {
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.data-table tbody tr:hover {
  background-color: var(--bg-hover);
}

.level-name-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.level-badge {
  width: 30px;
  height: 30px;
  background-color: var(--primary-glow);
  color: var(--primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 0.95rem;
}

.name-text {
  font-weight: 600;
  color: var(--text-title);
}

code {
  font-family: monospace;
  background-color: var(--bg-badge);
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  color: var(--primary);
  font-size: 0.85rem;
}

.group-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
}

.group-badge.vocab {
  background-color: var(--success-light);
  color: var(--success);
  border: 1px solid var(--success-border);
}

.group-badge.quiz {
  background-color: var(--primary-glow);
  color: var(--primary);
  border: 1px solid var(--border-color);
}

.text-right {
  text-align: right;
}

.action-btnedit, .action-btndelete {
  padding: 0.4rem;
  border-radius: 6px;
  margin-left: 0.5rem;
  transition: all var(--transition-fast);
}

.action-btnedit {
  color: var(--primary);
}

.action-btnedit:hover {
  background-color: var(--primary-glow);
}

.action-btndelete {
  color: var(--danger);
}

.action-btndelete:hover {
  background-color: var(--danger-light);
}

/* Modals */
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
  max-width: 500px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
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
}

.close-btn {
  font-size: 1.5rem;
  color: var(--text-muted);
  line-height: 1;
}

.close-btn:hover {
  color: var(--text-title);
}

.modal-form {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  height: 400px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-title);
}

.form-group input {
  padding: 0.65rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom:15px
}

.cancel-btn {
  background-color: var(--bg-body);
  color: var(--text-muted);
  border: 1px solid var(--border-color);
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.cancel-btn:hover {
  background-color: var(--bg-hover);
}

.submit-btn {
  background-color: var(--primary);
  color: #fff;
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  transition: all var(--transition-fast);
}

.submit-btn:hover:not(:disabled) {
  background-color: var(--primary-hover);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Delete Confirm Modal */
.delete-confirm {
  max-width: 440px;
}

.modal-body {
  padding: 2rem 1.5rem;
}

.alert-icon-wrapper {
  margin-bottom: 1rem;
}

.warning-color {
  color: var(--danger);
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
  line-height: 1.5;
}

.danger-btn {
  background-color: var(--danger);
}

.danger-btn:hover:not(:disabled) {
  background-color: #83392c;
}

/* Pagination Styling */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-top: 1.5rem;
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
