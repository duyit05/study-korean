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
              <span v-if="set.level" class="level-badge">{{ set.level }}</span>
            </div>
          </div>
        </div>
        <div class="card-actions-row">
          <button class="action-btn text" @click.stop="openEditCards(set)">Sửa thẻ từ</button>
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
            <select id="setRange" v-model="newSetLevel">
              <option value="Sơ cấp 1A">Sơ cấp 1A</option>
              <option value="Sơ cấp 1B">Sơ cấp 1B</option>
              <option value="Trung cấp (3-4)">Trung cấp (3-4)</option>
              <option value="Cao cấp (5-6)">Cao cấp (5-6)</option>
            </select>
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

const props = defineProps({
  studySets: {
    type: Array,
    required: true
  }
})

const selectedSet = ref(null)
const showCreateModal = ref(false)
const showAddCardModal = ref(false)

// New Set Fields
const newSetTitle = ref('')
const newSetDescription = ref('')
const newSetLevel = ref('Sơ cấp 1A')

// New Card Fields
const newCardKorean = ref('')
const newCardMeaning = ref('')
const newCardExample = ref('')

const handleCreateSet = () => {
  if (!newSetTitle.value || !newSetDescription.value) return

  props.studySets.push({
    id: 'set-' + (props.studySets.length + 1),
    title: newSetTitle.value,
    description: newSetDescription.value,
    level: newSetLevel.value,
    words: []
  })

  newSetTitle.value = ''
  newSetDescription.value = ''
  newSetLevel.value = 'Sơ cấp 1A'
  showCreateModal.value = false
}

const openAddCardForm = () => {
  newCardKorean.value = ''
  newCardMeaning.value = ''
  newCardExample.value = ''
  showAddCardModal.value = true
}

const handleAddCard = () => {
  if (!selectedSet.value || !newCardKorean.value || !newCardMeaning.value) return

  const targetSet = props.studySets.find(s => s.id === selectedSet.value.id)
  if (targetSet) {
    if (!targetSet.words) targetSet.words = []
    
    targetSet.words.push({
      id: 'word-' + (targetSet.words.length + 1),
      word: newCardKorean.value,
      meaning: newCardMeaning.value,
      example: newCardExample.value || ''
    })
  }

  showAddCardModal.value = false
}

const deleteCard = (set, wordId) => {
  const targetSet = props.studySets.find(s => s.id === set.id)
  if (targetSet && targetSet.words) {
    const index = targetSet.words.findIndex(w => w.id === wordId)
    if (index !== -1) {
      targetSet.words.splice(index, 1)
    }
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
  padding-top: 0.5rem;
  text-align: right;
}

.action-btn.text {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
}

.action-btn.text:hover {
  text-decoration: underline;
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
  flex-grow: 1;
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
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
</style>
