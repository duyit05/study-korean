<template>
  <div class="teacher-vocabulary animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Kho từ vựng (Flashcards)</h2>
        <p>Soạn thảo học liệu từ vựng theo chủ đề để giao cho học viên luyện tập.</p>
      </div>
      <button class="primary-btn" @click="showCreateModal = true">
        <AppIcon name="book" size="18" />
        <span>Tạo bộ từ vựng mới</span>
      </button>
    </div>

    <!-- Study Sets List -->
    <div class="sets-grid">
      <div 
        v-for="set in studySets" 
        :key="set.id" 
        class="set-card"
        :class="{ active: selectedSet && selectedSet.id === set.id }"
        @click="selectedSet = set"
      >
        <div class="card-inner">
          <span class="kr-badge">한</span>
          <div class="card-content">
            <h3>{{ set.title }}</h3>
            <p class="desc">{{ set.description }}</p>
            <div class="card-meta">
              <span class="count">{{ set.words?.length || 0 }} Từ vựng</span>
              <span v-if="set.level || set.category" class="level-badge">{{ set.level || set.category }}</span>
            </div>
          </div>
        </div>
        <div class="card-actions-row">
          <div class="action-btn-group">
            <button class="action-btn text" @click.stop="openEditSetModal(set)">
              <AppIcon name="edit" size="12" />
              <span>Sửa</span>
            </button>
            <button class="action-btn text danger" @click.stop="triggerDeleteSet(set)">
              <AppIcon name="trash" size="12" />
              <span>Xóa</span>
            </button>
          </div>
          <button class="action-btn text primary-action" @click.stop="openEditCards(set)">Sửa thẻ từ</button>
        </div>
      </div>
    </div>

    <!-- View & Edit Cards of Selected Set Panel -->
    <div v-if="selectedSet" class="set-detail-panel animate-scale">
      <div class="panel-header">
        <div class="header-title">
          <h3>Bộ từ vựng: {{ selectedSet.title }}</h3>
          <p>{{ selectedSet.description }}</p>
        </div>
        <button class="close-btn" @click="selectedSet = null">&times;</button>
      </div>

      <div class="panel-body">
        <div class="cards-section-header">
          <h4>Danh sách thẻ từ ({{ selectedSet.words?.length || 0 }})</h4>
          <button class="secondary-btn" @click="openAddCardForm">
            <span>+ Thêm từ mới</span>
          </button>
        </div>

        <div class="cards-list">
          <div v-for="(word, index) in selectedSet.words" :key="word.id" class="word-row">
            <span class="index">#{{ index + 1 }}</span>
            <div class="word-details">
              <strong class="korean">{{ word.word || word.korean }}</strong>
              <span class="divider">&rarr;</span>
              <span class="meaning">{{ word.meaning || word.vietnamese }}</span>
            </div>
            <span v-if="word.example" class="example">VD: {{ word.example }}</span>
            <div class="row-actions">
              <button class="delete-btn" @click="deleteCard(selectedSet, word.id)">Xóa</button>
            </div>
          </div>
          <div v-if="!selectedSet.words || selectedSet.words.length === 0" class="empty-state">
            Bộ từ vựng này chưa có thẻ từ nào. Hãy thêm từ vựng mới để hoàn thiện học liệu!
          </div>
        </div>
      </div>
    </div>

    <!-- Create Vocabulary Set Modal -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Tạo bộ từ vựng mới</h3>
          <button class="close-btn" @click="showCreateModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleCreateSet" class="modal-form">
          <div class="form-group">
            <label for="setTitle">Tên bộ từ vựng</label>
            <input type="text" id="setTitle" v-model="newSetTitle" placeholder="Ví dụ: Từ vựng Bài 5 - Thời tiết" required>
          </div>
          <div class="form-group">
            <label for="setDescription">Mô tả ngắn</label>
            <input type="text" id="setDescription" v-model="newSetDescription" placeholder="Ví dụ: Các từ vựng mô tả bốn mùa và thời tiết ở Hàn Quốc" required>
          </div>
          <div class="form-group">
            <label for="setRange">Cấp độ (TOPIK)</label>
            <AppSelect id="setRange" v-model="newSetLevelId" :options="levelOptions" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showCreateModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Tạo bộ từ</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Add Card Modal -->
    <div v-if="showAddCardModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Thêm từ vựng mới</h3>
          <button class="close-btn" @click="showAddCardModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleAddCard" class="modal-form">
          <div class="form-group">
            <label for="cardKorean">Từ tiếng Hàn</label>
            <input type="text" id="cardKorean" v-model="newCardKorean" placeholder="Ví dụ: 날씨" required>
          </div>
          <div class="form-group">
            <label for="cardMeaning">Nghĩa tiếng Việt</label>
            <input type="text" id="cardMeaning" v-model="newCardMeaning" placeholder="Ví dụ: Thời tiết" required>
          </div>
          <div class="form-group">
            <label for="cardExample">Ví dụ tiếng Hàn (tùy chọn)</label>
            <input type="text" id="cardExample" v-model="newCardExample" placeholder="Ví dụ: 오늘 날씨가 참 좋아요 (Thời tiết hôm nay thật đẹp)">
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showAddCardModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Thêm từ</button>
          </div>
        </form>
      </div>
    </div>
    <!-- Edit Vocabulary Set Modal -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Sửa thông tin bộ từ vựng</h3>
          <button class="close-btn" @click="showEditModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleUpdateSet" class="modal-form">
          <div class="form-group">
            <label for="editTitle">Tên bộ từ vựng</label>
            <input type="text" id="editTitle" v-model="editSetTitle" required>
          </div>
          <div class="form-group">
            <label for="editDescription">Mô tả ngắn</label>
            <input type="text" id="editDescription" v-model="editSetDescription" required>
          </div>
          <div class="form-group">
            <label for="editRange">Cấp độ (TOPIK)</label>
            <select id="editRange" v-model="editSetLevelId">
              <option v-for="level in topikLevels" :key="level.id" :value="level.id">
                {{ level.name }}
              </option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showEditModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Lưu thay đổi</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteConfirmModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3 style="color: var(--danger);">Xác nhận xóa bộ từ vựng</h3>
          <button class="close-btn" @click="showDeleteConfirmModal = false">&times;</button>
        </div>
        <div class="modal-body" style="margin-bottom: 1.5rem;">
          <p>Bạn có chắc chắn muốn xóa bộ từ vựng <strong>{{ setToDelete?.title }}</strong>?</p>
          <p style="color: var(--text-muted); font-size: 0.8rem; margin-top: 0.5rem;">
            Lưu ý: Hành động này sẽ xóa vĩnh viễn bộ từ vựng cùng với toàn bộ thẻ từ liên quan và không thể khôi phục.
          </p>
        </div>
        <div class="modal-actions">
          <button type="button" class="cancel-btn" @click="showDeleteConfirmModal = false">Hủy bỏ</button>
          <button type="button" class="submit-btn" style="background-color: var(--danger);" @click="confirmDeleteSet">Đồng ý xóa</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import AppSelect from '../AppSelect.vue'
import { useStudySetStore } from '../../stores/studySet'
import { useTopikLevelStore } from '../../stores/topikLevel'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const props = defineProps({
  studySets: {
    type: Array,
    required: true
  }
})

const studySetStore = useStudySetStore()
const topikLevelStore = useTopikLevelStore()

const selectedSet = ref(null)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteConfirmModal = ref(false)
const showAddCardModal = ref(false)

// TOPIK Levels loaded from Pinia Store
const topikLevels = computed(() => (topikLevelStore.levels || []).filter(l => l.groupType === 'VOCAB'))
const levelOptions = computed(() => {
  return topikLevels.value.map(lvl => ({ label: lvl.name, value: lvl.id }))
})
const newSetLevelId = ref(null)
const editSetLevelId = ref(null)

onMounted(async () => {
  try {
    await topikLevelStore.fetchLevels()
    if (topikLevels.value.length > 0) {
      newSetLevelId.value = topikLevels.value[0].id
      editSetLevelId.value = topikLevels.value[0].id
    }
  } catch (err) {
    console.error("Failed to load TOPIK levels:", err)
  }
})

// New Set Fields
const newSetTitle = ref('')
const newSetDescription = ref('')

// Edit Set Fields
const editSetId = ref(null)
const editSetTitle = ref('')
const editSetDescription = ref('')

// Delete Set Fields
const setToDelete = ref(null)

// New Card Fields
const newCardKorean = ref('')
const newCardMeaning = ref('')
const newCardExample = ref('')

const handleCreateSet = async () => {
  if (!newSetTitle.value || !newSetDescription.value) return

  try {
    await studySetStore.createStudySet({
      title: newSetTitle.value,
      description: newSetDescription.value,
      topikLevelId: newSetLevelId.value
    })
    toast.success("Tạo bộ từ vựng thành công!")
  } catch (e) {
    console.error("Failed to create study set via API:", e)
    toast.error("Tạo bộ từ vựng thất bại.")
  }

  newSetTitle.value = ''
  newSetDescription.value = ''
  newSetLevelId.value = topikLevels.value[0]?.id || null
  showCreateModal.value = false
}

const openEditSetModal = (set) => {
  editSetId.value = set.id
  editSetTitle.value = set.title
  editSetDescription.value = set.description
  
  const matched = topikLevels.value.find(l => l.name === set.category || l.code === set.category)
  editSetLevelId.value = matched ? matched.id : (topikLevels.value[0]?.id || null)
  
  showEditModal.value = true
}

const handleUpdateSet = async () => {
  if (!editSetTitle.value || !editSetDescription.value || !editSetId.value) return

  try {
    await studySetStore.updateStudySet(editSetId.value, {
      title: editSetTitle.value,
      description: editSetDescription.value,
      topikLevelId: editSetLevelId.value
    })
    
    const matched = topikLevels.value.find(l => l.id === editSetLevelId.value)
    const levelName = matched ? matched.name : ''
    
    // Update selectedSet if it's currently selected
    if (selectedSet.value && selectedSet.value.id === editSetId.value) {
      selectedSet.value = {
        ...selectedSet.value,
        title: editSetTitle.value,
        description: editSetDescription.value,
        category: levelName,
        level: levelName
      }
    }
    toast.success("Cập nhật bộ từ vựng thành công!")
  } catch (e) {
    console.error("Failed to update study set via API:", e)
    toast.error("Cập nhật bộ từ vựng thất bại.")
  }

  showEditModal.value = false
}

const triggerDeleteSet = (set) => {
  setToDelete.value = set
  showDeleteConfirmModal.value = true
}

const confirmDeleteSet = async () => {
  if (!setToDelete.value) return

  try {
    await studySetStore.deleteStudySet(setToDelete.value.id)
    
    // If deleting the currently selected set, deselect it
    if (selectedSet.value && selectedSet.value.id === setToDelete.value.id) {
      selectedSet.value = null
    }
    toast.success("Xóa bộ từ vựng thành công!")
  } catch (e) {
    console.error("Failed to delete study set via API:", e)
    toast.error("Xóa bộ từ vựng thất bại.")
  }

  showDeleteConfirmModal.value = false
  setToDelete.value = null
}

const openAddCardForm = () => {
  newCardKorean.value = ''
  newCardMeaning.value = ''
  newCardExample.value = ''
  showAddCardModal.value = true
}

const handleAddCard = async () => {
  if (!selectedSet.value || !newCardKorean.value || !newCardMeaning.value) return

  try {
    const newWord = await studySetStore.addCard(selectedSet.value.id, {
      frontText: newCardKorean.value,
      backText: newCardMeaning.value,
      exampleSentence: newCardExample.value || ''
    })

    if (selectedSet.value) {
      if (!selectedSet.value.words) selectedSet.value.words = []
      selectedSet.value.words.push(newWord)
    }
    toast.success("Thêm thẻ từ mới thành công!")
  } catch (e) {
    console.error("Failed to add card via API:", e)
    toast.error("Thêm thẻ từ mới thất bại.")
  }

  showAddCardModal.value = false
}

const deleteCard = async (set, wordId) => {
  try {
    await studySetStore.deleteCard(set.id, wordId)
    if (selectedSet.value && selectedSet.value.words) {
      const index = selectedSet.value.words.findIndex(w => w.id === wordId)
      if (index !== -1) {
        selectedSet.value.words.splice(index, 1)
      }
    }
    toast.success("Xóa thẻ từ thành công!")
  } catch (e) {
    console.error("Failed to delete card via API:", e)
    toast.error("Xóa thẻ từ thất bại.")
  }
}

const openEditCards = (set) => {
  selectedSet.value = set
}
</script>

<style scoped>
.teacher-vocabulary {
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

.sets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.25rem;
}

.set-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.set-card:hover, .set-card.active {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-inner {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.kr-badge {
  width: 32px;
  height: 32px;
  background-color: var(--primary-glow);
  color: var(--primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.card-content h3 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.card-content .desc {
  font-size: 0.8rem;
  color: var(--text-muted);
  line-height: 1.4;
  margin-bottom: 0.5rem;
}

.card-meta {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.count {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--primary);
}

.level-badge {
  font-size: 0.7rem;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.1rem 0.4rem;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
}

.card-actions-row {
  border-top: 1px solid var(--border-color);
  padding-top: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.action-btn-group {
  display: flex;
  gap: 0.4rem;
}

.action-btn.text {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.action-btn.text:hover {
  background-color: var(--primary-glow);
}

.action-btn.text.danger {
  color: var(--danger);
}

.action-btn.text.danger:hover {
  background-color: rgba(239, 68, 68, 0.1);
}

.action-btn.text.primary-action {
  background-color: var(--primary);
  color: #fff;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.action-btn.text.primary-action:hover {
  background-color: var(--primary-hover);
  text-decoration: none;
}

.set-detail-panel {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 1.5rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
  margin-bottom: 1rem;
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

.cards-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.cards-section-header h4 {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
}

.secondary-btn {
  background-color: transparent;
  border: 1px solid var(--primary);
  color: var(--primary);
  padding: 0.4rem 0.85rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.secondary-btn:hover {
  background-color: var(--primary-glow);
}

.cards-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.word-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
}

.word-row .index {
  color: var(--text-muted);
  font-weight: 600;
  font-size: 0.75rem;
}

.word-details {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.korean {
  color: var(--text-title);
  font-weight: 700;
  font-size: 0.95rem;
}

.divider {
  color: var(--text-muted);
}

.meaning {
  color: var(--text-title);
}

.example {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-style: italic;
  flex: 1;
  text-align: right;
  padding: 0 0.5rem;
}

.delete-btn {
  background: none;
  border: none;
  color: var(--danger);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
}

.delete-btn:hover {
  text-decoration: underline;
}

/* Modal and Select styling */
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

.modal-form input, .modal-form select {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.modal-form input:focus, .modal-form select:focus {
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
</style>
