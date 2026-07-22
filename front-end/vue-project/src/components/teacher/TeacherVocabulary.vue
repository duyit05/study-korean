<template>
  <div class="teacher-vocabulary animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2 style="margin: 0; font-size: 1.35rem; font-weight: 700; color: var(--text-title);">Kho từ vựng (Flashcards)</h2>
      </div>
      <button class="primary-btn" @click="showCreateModal = true">
        <AppIcon name="book" size="16" />
        <span>Tạo bộ từ vựng mới</span>
      </button>
    </div>

    <!-- Filter & Search -->
    <div class="filter-card">
      <div class="search-box">
        <AppIcon name="search" size="16" class="search-icon" />
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Tìm kiếm bộ từ vựng theo tên hoặc mô tả..."
        />
      </div>
      <div class="filter-options">
        <div class="filter-group">
          <label>Cấp độ</label>
          <AppSelect
            id="levelFilter"
            v-model="selectedLevelFilter"
            :options="levelFilterOptions"
            placeholder="Tất cả cấp độ"
            style="min-width: 150px;"
          />
        </div>
      </div>
    </div>

    <!-- Content Card wrapping grid + pagination -->
    <div class="content-card">
      <!-- Study Sets List -->
      <div class="sets-grid">
        <div
          v-if="paginatedSets.length === 0"
          class="empty-grid"
        >
          <AppIcon name="book" size="40" />
          <p>Không tìm thấy bộ từ vựng nào.</p>
        </div>
        <div 
          v-for="set in paginatedSets" 
          :key="set.id" 
          class="set-card"
          :class="{ active: selectedSet && selectedSet.id === set.id }"
          @click="selectSet(set)"
        >
          <div class="card-inner">
            <span class="kr-badge">한</span>
            <div class="card-content">
              <h3>{{ set.title }}</h3>
              <p class="desc">{{ set.description }}</p>
              <div class="card-meta">
                <span class="count">{{ set.wordCount || set.words?.length || 0 }} Từ vựng</span>
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

      <!-- Pagination -->
      <div v-if="totalElements > 0" class="pagination-bar">
        <div class="pagination-info">
          Đang hiển thị <strong>{{ startIndex }} - {{ endIndex }}</strong> trong số <strong>{{ totalElements }}</strong> bộ từ vựng
        </div>
        <div class="pagination-controls">
          <div class="page-buttons">
            <button class="page-nav-btn" :disabled="currentPage === 1" @click="currentPage = 1" title="Trang đầu">
              <span style="display: inline-flex; align-items: center; margin-right: -4px;">
                <AppIcon name="chevron-left" size="12" />
                <AppIcon name="chevron-left" size="12" style="margin-left: -6px;" />
              </span>
            </button>
            <button class="page-nav-btn" :disabled="currentPage === 1" @click="currentPage--" title="Trang trước">
              <AppIcon name="chevron-left" size="12" />
            </button>
            <button
              v-for="page in pageNumbers"
              :key="page"
              class="page-num-btn"
              :class="{ active: currentPage === page, dots: page === '...' }"
              :disabled="page === '...'"
              @click="typeof page === 'number' && (currentPage = page)"
            >{{ page }}</button>
            <button class="page-nav-btn" :disabled="currentPage === totalPages" @click="currentPage++" title="Trang sau">
              <AppIcon name="chevron-right" size="12" />
            </button>
            <button class="page-nav-btn" :disabled="currentPage === totalPages" @click="currentPage = totalPages" title="Trang cuối">
              <span style="display: inline-flex; align-items: center; margin-left: -4px;">
                <AppIcon name="chevron-right" size="12" style="margin-right: -6px;" />
                <AppIcon name="chevron-right" size="12" />
              </span>
            </button>
          </div>
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
          <div class="header-action-group" style="display: flex; gap: 0.5rem;">
            <button class="secondary-btn" @click="openImportModal" style="background-color: var(--primary-glow); border-color: var(--primary); display: flex; align-items: center; gap: 0.25rem;">
              <AppIcon name="upload" size="14" />
              <span>Import từ file / text</span>
            </button>
            <button class="secondary-btn" @click="openAddCardForm">
              <span>+ Thêm từ mới</span>
            </button>
          </div>
        </div>

        <div class="cards-list">
          <div v-for="(word, index) in selectedSet.words" :key="word.id" class="word-row">
            <span class="index">#{{ index + 1 }}</span>
            <div class="word-details">
              <strong class="korean">{{ word.word || word.korean }}</strong>
              <span v-if="word.pronunciation || word.romanization" class="pronounce">/ {{ word.pronunciation || word.romanization }} /</span>
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
            <label for="cardPronunciation">Phiên âm (tùy chọn)</label>
            <input type="text" id="cardPronunciation" v-model="newCardPronunciation" placeholder="Ví dụ: nalssi / annyeonghaseyo">
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
            <AppSelect id="editRange" v-model="editSetLevelId" :options="levelOptions" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showEditModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Lưu thay đổi</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Import Cards Modal -->
    <div v-if="showImportModal" class="modal-overlay">
      <div class="modal-content large animate-scale" style="max-width: 600px;">
        <div class="modal-header">
          <h3>Nhập từ vựng hàng loạt (Import)</h3>
          <button class="close-btn" @click="showImportModal = false">&times;</button>
        </div>
        <div class="modal-body" style="padding: 1rem 0;">
          <div class="import-tabs">
            <button 
              type="button" 
              class="tab-btn" 
              :class="{ active: importTab === 'paste' }"
              @click="importTab = 'paste'"
            >
              Copy & Paste văn bản
            </button>
            <button 
              type="button" 
              class="tab-btn" 
              :class="{ active: importTab === 'file' }"
              @click="importTab = 'file'"
            >
              Chọn file từ máy tính (.txt, .csv)
            </button>
          </div>

          <!-- Format selector -->
          <div class="format-selector-row">
            <span class="format-label">Định dạng nhập liệu:</span>
            <div class="format-options">
              <label class="format-option-card" :class="{ active: importFormat === 'auto' }">
                <input type="radio" value="auto" v-model="importFormat" @change="previewParsedData">
                <span>Tự động nhận diện</span>
              </label>
              <label class="format-option-card" :class="{ active: importFormat === 'vertical' }">
                <input type="radio" value="vertical" v-model="importFormat" @change="previewParsedData">
                <span>Hàng dọc (dòng trống)</span>
              </label>
              <label class="format-option-card" :class="{ active: importFormat === 'horizontal' }">
                <input type="radio" value="horizontal" v-model="importFormat" @change="previewParsedData">
                <span>Hàng ngang (Tab/|/,/;)</span>
              </label>
            </div>
          </div>

          <!-- Paste Tab Content -->
          <div v-if="importTab === 'paste'" class="tab-content">
            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">
              Cách dùng: Nhập thông tin từ vựng theo dạng hàng dọc (mỗi từ chiếm 2-3 dòng liên tiếp, cách nhau bởi dòng trống) hoặc dán các cột từ Excel sang hàng ngang.
            </p>
            <textarea 
              v-model="importText" 
              placeholder="DÁN DỮ LIỆU VÀO ĐÂY&#10;&#10;Định dạng hàng dọc ví dụ (Mỗi từ 2-3 dòng, cách nhau dòng trống):&#10;날씨&#10;Thời tiết&#10;오늘 날씨가 참 좋네요&#10;&#10;사과&#10;Quả táo&#10;사과를 먹어요&#10;&#10;----------------------------------------&#10;&#10;Định dạng hàng ngang ví dụ:&#10;날씨 | Thời tiết | 오늘 날씨가 참 좋네요&#10;사과 | Quả táo | 사과를 먹어요"
              rows="8"
              style="width: 100%; padding: 0.75rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background-color: var(--bg-body); color: var(--text-title); font-family: monospace; font-size: 0.85rem;"
              @input="previewParsedData"
            ></textarea>
          </div>

          <!-- File Upload Tab Content -->
          <div v-else-if="importTab === 'file'" class="tab-content">
            <p style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.5rem;">
              Chọn file văn bản hoặc file csv (.txt, .csv) được mã hóa UTF-8. Các từ phân cách bằng dòng trống (nếu để hàng dọc) hoặc ngăn cách bằng dấu Tab/|/,/; (nếu để hàng ngang).
            </p>
            <div class="upload-container" style="margin-bottom: 1rem;">
              <input 
                type="file" 
                @change="handleImportFileSelected" 
                accept=".txt,.csv" 
                class="hidden-file-input" 
                id="importFileInput"
                style="display: none;"
              >
              <label for="importFileInput" class="upload-drag-zone" style="display: flex; flex-direction: column; align-items: center; justify-content: center; border: 2px dashed var(--border-color); border-radius: var(--radius-md); padding: 1.5rem; background-color: var(--bg-body); cursor: pointer; text-align: center;">
                <AppIcon name="upload" size="24" class="upload-icon" style="color: var(--text-muted); margin-bottom: 0.5rem;" />
                <span v-if="!importedFileName">Nhấp để chọn file từ máy tính</span>
                <span v-else class="file-name-display" style="color: var(--primary); font-weight: 600;">{{ importedFileName }}</span>
              </label>
            </div>
          </div>

          <!-- Preview Table -->
          <div v-if="parsedCards.length > 0" class="parsed-preview" style="margin-top: 1rem;">
            <h5 style="margin-bottom: 0.5rem; font-weight: 700; font-size: 0.85rem; color: var(--text-title);">Xem trước kết quả phân tích (Tìm thấy {{ parsedCards.length }} từ):</h5>
            <div style="max-height: 200px; overflow-y: auto; border: 1px solid var(--border-color); border-radius: var(--radius-sm);">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.8rem; text-align: left;">
                <thead>
                  <tr style="background-color: var(--bg-body); border-bottom: 1px solid var(--border-color);">
                    <th style="padding: 0.5rem;">Từ tiếng Hàn</th>
                    <th style="padding: 0.5rem;">Nghĩa tiếng Việt</th>
                    <th style="padding: 0.5rem;">Phiên âm (Tùy chọn)</th>
                    <th style="padding: 0.5rem;">Ví dụ (Tùy chọn)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(card, cIdx) in parsedCards" :key="cIdx" style="border-bottom: 1px solid var(--border-color);">
                    <td style="padding: 0.5rem; font-weight: 600;">{{ card.frontText }}</td>
                    <td style="padding: 0.5rem;">{{ card.backText }}</td>
                    <td style="padding: 0.5rem; color: var(--text-muted); font-style: italic;">{{ card.pronunciation }}</td>
                    <td style="padding: 0.5rem; color: var(--text-muted);">{{ card.exampleSentence }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-actions" style="margin-top: 1rem;">
          <button type="button" class="cancel-btn" @click="showImportModal = false">Hủy bỏ</button>
          <button 
            type="button" 
            class="submit-btn" 
            :disabled="parsedCards.length === 0" 
            @click="handleImportCards"
          >
            Nhập vào bộ từ vựng
          </button>
        </div>
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
import { ref, onMounted, computed, watch } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

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
const showImportModal = ref(false)

const importTab = ref('paste')
const importFormat = ref('auto')
const importText = ref('')
const importedFileName = ref('')
const parsedCards = ref([])

const openImportModal = () => {
  importText.value = ''
  importedFileName.value = ''
  parsedCards.value = []
  importTab.value = 'paste'
  importFormat.value = 'auto'
  showImportModal.value = true
}

const parseTextToCards = (text) => {
  if (!text) return []
  const cleanText = text.replace(/\r\n/g, '\n')
  
  let isVertical = importFormat.value === 'vertical'
  if (importFormat.value === 'auto') {
    const hasHorizontalDelimiters = cleanText.includes('\t') || cleanText.includes('|') || cleanText.includes(';')
    isVertical = !hasHorizontalDelimiters && cleanText.split('\n').length > 2
  }
  
  if (isVertical) {
    const blocks = cleanText.split(/\n\s*\n/)
    const results = []
    for (const block of blocks) {
      const lines = block.split('\n').map(l => l.trim()).filter(l => l.length > 0)
      if (lines.length >= 2) {
        let pronunciation = ''
        let exampleSentence = ''
        if (lines.length >= 4) {
          pronunciation = lines[2]
          exampleSentence = lines[3]
        } else if (lines.length === 3) {
          pronunciation = lines[2]
        }
        results.push({
          frontText: lines[0],
          backText: lines[1],
          pronunciation: pronunciation,
          exampleSentence: exampleSentence
        })
      }
    }
    return results
  } else {
    const lines = cleanText.split('\n')
    const results = []
    for (let line of lines) {
      line = line.trim()
      if (!line) continue
      
      let separator = ','
      if (line.includes('\t')) {
        separator = '\t'
      } else if (line.includes('|')) {
        separator = '|'
      } else if (line.includes(';')) {
        separator = ';'
      }
      
      const parts = line.split(separator).map(p => p.trim())
      if (parts.length >= 2 && parts[0] && parts[1]) {
        let pronunciation = ''
        let exampleSentence = ''
        if (parts.length >= 4) {
          pronunciation = parts[2]
          exampleSentence = parts[3]
        } else if (parts.length === 3) {
          pronunciation = parts[2]
        }
        results.push({
          frontText: parts[0],
          backText: parts[1],
          pronunciation: pronunciation,
          exampleSentence: exampleSentence
        })
      }
    }
    return results
  }
}

const previewParsedData = () => {
  parsedCards.value = parseTextToCards(importText.value)
}

const handleImportFileSelected = (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  importedFileName.value = file.name
  const reader = new FileReader()
  reader.onload = (e) => {
    const text = e.target.result
    importText.value = text
    parsedCards.value = parseTextToCards(text)
  }
  reader.readAsText(file, 'UTF-8')
}

const handleImportCards = async () => {
  if (!selectedSet.value || parsedCards.value.length === 0) return
  
  try {
    const importedList = await studySetStore.addCardsBatch(selectedSet.value.id, parsedCards.value)
    
    if (selectedSet.value) {
      if (!selectedSet.value.words) selectedSet.value.words = []
      selectedSet.value.words.push(...importedList)
      selectedSet.value.wordCount = (selectedSet.value.wordCount || 0) + importedList.length
    }
    
    toast.success(`Import thành công ${importedList.length} từ vựng!`)
    showImportModal.value = false
  } catch (e) {
    console.error("Batch import failed:", e)
    toast.error("Nhập dữ liệu hàng loạt thất bại.")
  }
}

// TOPIK Levels loaded from Pinia Store
const topikLevels = computed(() => (topikLevelStore.levels || []).filter(l => l.groupType === 'VOCAB'))
const levelOptions = computed(() => {
  return topikLevels.value.map(lvl => ({ label: lvl.name, value: lvl.id }))
})
const levelFilterOptions = computed(() => [
  { value: '', label: 'Tất cả cấp độ' },
  ...topikLevels.value.map(lvl => ({ value: lvl.name, label: lvl.name }))
])
const newSetLevelId = ref(null)
const editSetLevelId = ref(null)

// Search & Filter
const searchQuery = ref('')
const selectedLevelFilter = ref('')

// Pagination
const currentPage = ref(1)
const itemsPerPage = 10

const filteredSets = computed(() => {
  let sets = props.studySets || []
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    sets = sets.filter(s =>
      (s.title || '').toLowerCase().includes(q) ||
      (s.description || '').toLowerCase().includes(q)
    )
  }
  if (selectedLevelFilter.value) {
    sets = sets.filter(s =>
      (s.level || s.category || '') === selectedLevelFilter.value
    )
  }
  return sets
})

const totalElements = computed(() => filteredSets.value.length)
const totalPages = computed(() => Math.ceil(totalElements.value / itemsPerPage) || 1)
const startIndex = computed(() => totalElements.value === 0 ? 0 : (currentPage.value - 1) * itemsPerPage + 1)
const endIndex = computed(() => Math.min(currentPage.value * itemsPerPage, totalElements.value))

const paginatedSets = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  return filteredSets.value.slice(start, start + itemsPerPage)
})

const pageNumbers = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 5) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else if (current <= 3) {
    pages.push(1, 2, 3, 4, '...', total)
  } else if (current >= total - 2) {
    pages.push(1, '...', total - 3, total - 2, total - 1, total)
  } else {
    pages.push(1, '...', current - 1, current, current + 1, '...', total)
  }
  return pages
})

watch([searchQuery, selectedLevelFilter], () => { currentPage.value = 1 })

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
const newCardPronunciation = ref('')
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
  newCardPronunciation.value = ''
  newCardExample.value = ''
  showAddCardModal.value = true
}

const selectSet = async (set) => {
  selectedSet.value = set
  if (!set.words || set.words.length === 0) {
    try {
      const cards = await studySetStore.fetchCardsForSet(set.id)
      set.words = cards || []
    } catch (e) {
      console.error("Failed to load cards for set:", e)
      toast.error("Không thể tải danh sách thẻ từ.")
    }
  }
}

const handleAddCard = async () => {
  if (!selectedSet.value || !newCardKorean.value || !newCardMeaning.value) return

  try {
    const newWord = await studySetStore.addCard(selectedSet.value.id, {
      frontText: newCardKorean.value,
      backText: newCardMeaning.value,
      pronunciation: newCardPronunciation.value || '',
      exampleSentence: newCardExample.value || ''
    })

    if (selectedSet.value) {
      if (!selectedSet.value.words) selectedSet.value.words = []
      selectedSet.value.words.push(newWord)
      selectedSet.value.wordCount = (selectedSet.value.wordCount || 0) + 1
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
        selectedSet.value.wordCount = Math.max(0, (selectedSet.value.wordCount || 0) - 1)
      }
    }
    toast.success("Xóa thẻ từ thành công!")
  } catch (e) {
    console.error("Failed to delete card via API:", e)
    toast.error("Xóa thẻ từ thất bại.")
  }
}

const openEditCards = (set) => {
  selectSet(set)
}
</script>

<style scoped>
.teacher-vocabulary {
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
  font-size: 0.85rem;
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
  min-width: 260px;
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
  padding: 0.45rem 0.75rem 0.45rem 2.25rem;
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
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-muted);
  white-space: nowrap;
}

.filter-group :deep(.select-trigger) {
  background-color: var(--bg-body);
  padding: 0.45rem 0.75rem;
  min-width: 150px;
}

/* Empty grid state */
.empty-grid {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 4rem 1rem;
  color: var(--text-muted);
}

/* Pagination */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin: 0.5rem 0 0 0;
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

/* Content Card */
.content-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.sets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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

.pronounce {
  color: var(--text-muted);
  font-size: 0.8rem;
  font-style: italic;
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

/* Import UI specific styles */
.import-tabs {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--border-color);
  padding-bottom: 0px;
}

.tab-btn {
  background: none;
  border: none;
  font-weight: 600;
  font-size: 0.95rem;
  padding: 0.75rem 0.5rem;
  cursor: pointer;
  color: var(--text-muted);
  position: relative;
  transition: all var(--transition-fast);
}

.tab-btn:hover {
  color: var(--primary);
}

.tab-btn.active {
  color: var(--primary);
  font-weight: 700;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--primary);
  border-radius: 2px;
}

.format-selector-row {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
}

.format-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.format-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.format-option-card {
  flex: 1;
  min-width: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.6rem 0.85rem;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: center;
}

.format-option-card input[type="radio"] {
  display: none;
}

.format-option-card:hover {
  border-color: var(--primary);
  color: var(--primary);
  background-color: var(--primary-glow);
}

.format-option-card.active {
  border-color: var(--primary);
  color: var(--primary);
  background-color: var(--primary-glow);
  box-shadow: 0 0 0 1px var(--primary);
}

@media (max-width: 768px) {
  .teacher-vocabulary {
    padding: 1rem;
  }

  .header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .header-section .primary-btn {
    align-self: flex-start;
  }

  .filter-card {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .search-box {
    width: 100%;
    min-width: 0;
  }

  .filter-options {
    width: 100%;
  }

  .filter-group {
    width: 100%;
    justify-content: space-between;
  }

  .pagination-bar {
    flex-direction: column;
    gap: 1rem;
    align-items: center;
    padding-top: 1rem;
    min-height: auto;
  }

  .pagination-info {
    position: static;
    text-align: center;
  }

  .modal-content {
    width: 95%;
    padding: 1.25rem;
    max-height: 90vh;
    overflow-y: auto;
  }

  .form-row-2 {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
</style>

