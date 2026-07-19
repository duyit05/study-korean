<template>
  <div class="teacher-quizzes animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Soạn đề thi &amp; bài tập luyện tập</h2>
        <p>Soạn thảo đề ôn luyện TOPIK và trắc nghiệm từ vựng tự động chấm điểm.</p>
      </div>
      <button class="primary-btn" @click="openCreateQuizModal">
        <AppIcon name="quiz" size="18" />
        <span>Soạn đề thi mới</span>
      </button>
    </div>

    <!-- Filter & Search -->
    <div class="filter-card">
      <div class="search-box">
        <AppIcon name="search" size="16" class="search-icon" />
        <input
          type="text"
          v-model="quizSearchQuery"
          placeholder="Tìm kiếm đề thi theo tên..."
        />
      </div>
      <div class="filter-options">
        <div class="filter-group">
          <label>Cấp độ</label>
          <AppSelect
            id="quizLevelFilter"
            v-model="selectedQuizLevelFilter"
            :options="quizLevelFilterOptions"
            placeholder="Tất cả cấp độ"
            style="min-width: 150px;"
          />
        </div>
      </div>
    </div>

    <!-- Content Card wrapping grid + pagination -->
    <div class="content-card">
      <!-- Quizzes Grid -->
      <div class="quizzes-grid">
        <div
          v-if="paginatedQuizzes.length === 0"
          class="empty-grid"
          style="grid-column: 1 / -1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 1rem; color: var(--text-muted); gap: 0.75rem;"
        >
          <AppIcon name="quiz" size="40" />
          <p>Không tìm thấy đề thi nào.</p>
        </div>
        <div 
          v-for="quiz in paginatedQuizzes" 
          :key="quiz.id" 
          class="quiz-card"
          :class="{ active: selectedQuiz && selectedQuiz.id === quiz.id }"
          @click="selectQuiz(quiz)"
        >
          <div class="quiz-header">
            <div class="quiz-icon-badge">한</div>
            <div class="quiz-title-meta">
              <h3>{{ quiz.title }}</h3>
              <span class="type-badge">{{ quiz.topikLevel || 'Luyện tập' }}</span>
            </div>
          </div>
          <div class="quiz-body">
            <div class="meta-row">
              <AppIcon name="clock" size="16" class="icon" />
              <span>Thời gian: {{ quiz.timeLimitMins }} phút</span>
            </div>
            <div class="meta-row">
              <AppIcon name="calendar" size="16" class="icon" />
              <span>Hạn nộp: {{ quiz.dueDate ? quiz.dueDate.substring(0, 10) : 'Không có' }}</span>
            </div>
            <div class="meta-row">
              <AppIcon name="book" size="16" class="icon" />
              <span>{{ quiz.questionCount || 0 }} câu hỏi</span>
            </div>
            <div class="meta-row">
              <AppIcon name="award" size="16" class="icon" />
              <span>Tổng điểm: {{ quiz.totalScore || 100 }} điểm</span>
            </div>
          </div>
          <div class="quiz-footer">
            <div class="quiz-action-group">
              <button class="action-btn text-link" @click.stop="triggerEditQuiz(quiz)">Sửa</button>
              <button class="action-btn text-link danger-action" @click.stop="triggerDeleteQuiz(quiz)">Xóa</button>
            </div>
            <button class="action-btn text-link" @click.stop="selectQuiz(quiz)">Quản lý câu hỏi &rarr;</button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalQuizElements > 0" class="pagination-bar">
        <div class="pagination-info">
          Đang hiển thị <strong>{{ startQuizIndex }} - {{ endQuizIndex }}</strong> trong số <strong>{{ totalQuizElements }}</strong> đề thi
        </div>
        <div class="pagination-controls">
          <div class="page-buttons">
            <button class="page-nav-btn" :disabled="currentQuizPage === 1" @click="currentQuizPage = 1" title="Trang đầu">
              <span style="display: inline-flex; align-items: center; margin-right: -4px;">
                <AppIcon name="chevron-left" size="12" />
                <AppIcon name="chevron-left" size="12" style="margin-left: -6px;" />
              </span>
            </button>
            <button class="page-nav-btn" :disabled="currentQuizPage === 1" @click="currentQuizPage--" title="Trang trước">
              <AppIcon name="chevron-left" size="12" />
            </button>
            <button
              v-for="page in quizPageNumbers"
              :key="page"
              class="page-num-btn"
              :class="{ active: currentQuizPage === page, dots: page === '...' }"
              :disabled="page === '...'"
              @click="typeof page === 'number' && (currentQuizPage = page)"
            >{{ page }}</button>
            <button class="page-nav-btn" :disabled="currentQuizPage === totalQuizPages" @click="currentQuizPage++" title="Trang sau">
              <AppIcon name="chevron-right" size="12" />
            </button>
            <button class="page-nav-btn" :disabled="currentQuizPage === totalQuizPages" @click="currentQuizPage = totalQuizPages" title="Trang cuối">
              <span style="display: inline-flex; align-items: center; margin-left: -4px;">
                <AppIcon name="chevron-right" size="12" style="margin-right: -6px;" />
                <AppIcon name="chevron-right" size="12" />
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- View & Edit Questions of Selected Quiz Panel -->
    <div v-if="selectedQuiz" class="quiz-detail-panel animate-scale">
      <div class="panel-header">
        <div class="header-title">
          <h3>Đề: {{ selectedQuiz.title }}</h3>
          <p>{{ selectedQuiz.topikLevel || 'Luyện tập thường' }} | Thời gian: {{ selectedQuiz.timeLimitMins }} phút | Tổng điểm: {{ selectedQuiz.totalScore || 100 }} điểm</p>
        </div>
        <button class="close-btn" @click="selectedQuiz = null">&times;</button>
      </div>

      <div class="panel-body">
        <div class="section-actions">
          <h4>Danh sách câu hỏi trong đề ({{ selectedQuiz.questions?.length || 0 }})</h4>
          <button class="secondary-btn" @click="openAddQuestionModal">
            <span>+ Soạn câu hỏi mới</span>
          </button>
        </div>

        <div class="questions-list">
          <div v-for="(question, index) in selectedQuiz.questions" :key="question.id" class="question-row">
            <div class="q-header-row">
              <span class="q-index">Câu {{ index + 1 }}</span>
              <span class="section-tag" :class="question.section?.toLowerCase()">{{ getSectionLabel(question.section) }}</span>
              <span class="points-tag">{{ question.points }} điểm</span>
            </div>
            <div class="q-body-row">
              <p class="question-text">{{ question.questionText }}</p>
              <div v-if="question.audioUrl" class="audio-indicator">
                <AppIcon name="volume" size="14" />
                <span>Có file âm thanh Nghe ({{ question.audioSource || 'TTS' }})</span>
              </div>
              <div v-if="question.questionType === 'MULTIPLE_CHOICE'" class="answers-grid">
                <div 
                  v-for="(opt, oIdx) in getQuestionOptions(question)" 
                  :key="oIdx" 
                  class="answer-item"
                  :class="{ correct: opt.isCorrect, wrong: !opt.isCorrect }"
                >
                  <span v-if="opt.isCorrect" class="check-icon">&#10003;</span>
                  <span>{{ String.fromCharCode(65 + oIdx) }}. {{ opt.text }}</span>
                </div>
              </div>
              <div v-else class="essay-indicator">
                <span>[Tự luận] Học sinh điền câu trả lời ngắn hoặc viết bài luận.</span>
              </div>
            </div>
            <div class="q-actions-row" style="display: flex; justify-content: flex-end; gap: 1rem; align-items: center;">
              <button class="edit-question-btn" @click="triggerEditQuestion(question)">Sửa câu hỏi</button>
              <button class="delete-btn" @click="triggerDeleteQuestion(question)">Xóa câu này</button>
            </div>
          </div>
          
          <div v-if="!selectedQuiz.questions || selectedQuiz.questions.length === 0" class="empty-state">
            Đề thi này chưa có câu hỏi nào. Hãy soạn thảo các câu hỏi Nghe, Đọc, Viết để học viên ôn tập!
          </div>
        </div>
      </div>
    </div>

    <!-- Create Quiz Modal -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Soạn đề thi mới</h3>
          <button class="close-btn" @click="showCreateModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleCreateQuiz" class="modal-form">
          <div class="form-group">
            <label for="quizTitle">Tên đề thi / bài tập</label>
            <input type="text" id="quizTitle" v-model="newQuizTitle" placeholder="Ví dụ: Đề thi thử TOPIK I - Lần 90" required>
          </div>
          <div class="form-group">
            <label for="quizLevel">Dạng đề thi (TOPIK)</label>
            <AppSelect id="quizLevel" v-model="newQuizLevelId" :options="levelOptions" />
          </div>
          <div class="form-group">
            <label for="quizTime">Thời gian làm bài (phút)</label>
            <input type="number" id="quizTime" v-model="newQuizTime" placeholder="Ví dụ: 50" required min="1">
          </div>
          <div class="form-group">
            <label for="quizScore">Tổng điểm bài thi</label>
            <input type="number" id="quizScore" v-model="newQuizScore" placeholder="Ví dụ: 100" required min="1">
          </div>
          <div class="form-group">
            <label for="quizDueDate">Hạn nộp bài</label>
            <input type="date" id="quizDueDate" v-model="newQuizDueDate" required>
          </div>
          <div class="form-group">
            <label for="quizClass">Giao cho lớp học</label>
            <AppSelect id="quizClass" v-model="newQuizClassId" :options="classOptions" placeholder="-- Không giao cho lớp nào --" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showCreateModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Tạo đề thi</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Add Question Modal -->
    <div v-if="showAddQuestionModal" class="modal-overlay">
      <div class="modal-content large animate-scale">
        <div class="modal-header">
          <h3>{{ isEditQuestionMode ? 'Sửa thông tin câu hỏi' : 'Soạn câu hỏi mới' }}</h3>
          <button class="close-btn" @click="closeQuestionModal">&times;</button>
        </div>
        <form @submit.prevent="handleAddQuestion" class="modal-form">
          <div class="form-row-2">
            <div class="form-group">
              <label for="qSection">Kỹ năng / Phần thi</label>
              <AppSelect id="qSection" v-model="newQSection" :options="qSectionOptions" />
            </div>
            <div class="form-group">
              <label for="qPoints">Điểm số câu này</label>
              <input type="number" id="qPoints" v-model="newQPoints" required min="1" step="0.5">
            </div>
          </div>

          <div class="form-group">
            <label for="qText">Nội dung câu hỏi (Đề bài)</label>
            <textarea id="qText" v-model="newQText" placeholder="Nhập nội dung câu hỏi tiếng Hàn..." required rows="3"></textarea>
          </div>

          <!-- Listening Audio Options -->
          <div v-if="newQSection === 'LISTENING'" class="form-group border-box">
            <label style="margin-bottom: 0.5rem; display: block;">File âm thanh bài nghe (.mp3, .mp4)</label>
            
            <div class="upload-container">
              <input 
                type="file" 
                ref="audioFileRef" 
                @change="onAudioFileSelected" 
                accept="audio/*,video/mp4" 
                class="hidden-file-input" 
                id="audioFileInput"
              >
              <label for="audioFileInput" class="upload-drag-zone">
                <AppIcon name="upload" size="24" class="upload-icon" />
                <span v-if="!uploadingAudioFile && !newQAudio">Nhấp để chọn file âm thanh/video (.mp3, .mp4)</span>
                <span v-else-if="uploadingAudioFile" class="loading-text">Đang tải file lên...</span>
                <span v-else class="file-name-display">{{ getAudioFileName(newQAudio) }}</span>
              </label>
            </div>

            <!-- Global Audio Preview Player -->
            <div v-if="newQAudio" class="recording-preview" style="margin-top: 1rem;">
              <audio :key="newQAudio" :src="newQAudio" controls class="audio-player-preview"></audio>
            </div>
          </div>

          <div class="form-group">
            <label for="qType">Dạng câu trả lời</label>
            <AppSelect id="qType" v-model="newQType" :options="qTypeOptions" />
          </div>

          <!-- Multiple choice fields -->
          <div v-if="newQType === 'MULTIPLE_CHOICE'" class="form-group border-box">
            <label style="margin-bottom: 0.5rem; display: block;">Thiết lập đáp án trắc nghiệm (Chọn nút tròn để đặt làm đáp án đúng)</label>
            <div class="ans-input-row" v-for="(opt, optIdx) in 4" :key="optIdx">
              <input 
                type="radio" 
                name="correct-opt-teacher" 
                :value="optIdx" 
                v-model="newQCorrectOptionIndex"
                :id="'radio-teacher-' + optIdx"
              >
              <label 
                :for="'radio-teacher-' + optIdx" 
                class="ans-label"
                :class="{ correct: newQCorrectOptionIndex === optIdx }"
                style="cursor: pointer;"
              >
                {{ newQCorrectOptionIndex === optIdx ? '✓ Đúng:' : 'Sai:' }}
              </label>
              <input 
                type="text" 
                v-model="newQOptions[optIdx]" 
                :placeholder="'Đáp án ' + String.fromCharCode(65 + optIdx)"
                required
              >
            </div>
          </div>

          <!-- Essay Fields -->
          <div v-else class="form-group border-box">
            <label for="qCorrectEssay">Gợi ý đáp án / Bài mẫu tự luận (tùy chọn)</label>
            <textarea id="qCorrectEssay" v-model="newQCorrectAnswer" placeholder="Nhập đáp án mẫu để tham khảo khi chấm bài..." rows="2"></textarea>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeQuestionModal">Hủy bỏ</button>
            <button type="submit" class="submit-btn">{{ isEditQuestionMode ? 'Cập nhật câu hỏi' : 'Thêm vào đề thi' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit Quiz Modal -->
    <div v-if="showEditQuizModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Sửa thông tin đề thi / bài tập</h3>
          <button class="close-btn" @click="showEditQuizModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleUpdateQuiz" class="modal-form">
          <div class="form-group">
            <label for="editQuizTitle">Tên đề thi / bài tập</label>
            <input type="text" id="editQuizTitle" v-model="editQuizTitle" required>
          </div>
          <div class="form-group">
            <label for="editQuizLevel">Cấp độ (TOPIK)</label>
            <AppSelect id="editQuizLevel" v-model="editQuizLevelId" :options="levelOptions" />
          </div>
          <div class="form-group">
            <label for="editQuizTime">Thời gian làm bài (phút)</label>
            <input type="number" id="editQuizTime" v-model.number="editQuizTime" required min="5">
          </div>
          <div class="form-group">
            <label for="editQuizScore">Tổng điểm bài thi</label>
            <input type="number" id="editQuizScore" v-model="editQuizScore"  required min="1">
          </div>
          <div class="form-group">
            <label for="editQuizDueDate">Hạn nộp bài</label>
            <input type="date" id="editQuizDueDate" v-model="editQuizDueDate" required>
          </div>
          <div class="form-group">
            <label for="editQuizClass">Giao cho lớp học</label>
            <AppSelect id="editQuizClass" v-model="editQuizClassId" :options="classOptions" placeholder="-- Không giao cho lớp nào --" />
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showEditQuizModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Lưu thay đổi</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Quiz Confirmation Modal -->
    <div v-if="showDeleteQuizConfirmModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3 style="color: var(--danger);">Xác nhận xóa đề thi</h3>
          <button class="close-btn" @click="showDeleteQuizConfirmModal = false">&times;</button>
        </div>
        <div class="modal-body" style="margin-bottom: 1.5rem;">
          <p>Bạn có chắc chắn muốn xóa đề thi <strong>{{ quizToDelete?.title }}</strong>?</p>
          <p style="color: var(--text-muted); font-size: 0.8rem; margin-top: 0.5rem;">
            Lưu ý: Hành động này sẽ xóa vĩnh viễn đề thi cùng với toàn bộ câu hỏi và toàn bộ kết quả làm bài của học sinh liên quan. Hành động này không thể khôi phục.
          </p>
        </div>
        <div class="modal-actions">
          <button type="button" class="cancel-btn" @click="showDeleteQuizConfirmModal = false">Hủy bỏ</button>
          <button type="button" class="submit-btn" style="background-color: var(--danger);" @click="confirmDeleteQuiz">Đồng ý xóa</button>
        </div>
      </div>
    </div>

    <!-- Delete Question Confirmation Modal -->
    <div v-if="showDeleteQuestionConfirmModal" class="modal-overlay">
      <div class="modal-content delete-confirm animate-scale">
        <div class="modal-header">
          <h3>Xác nhận xóa câu hỏi</h3>
          <button class="close-btn" @click="showDeleteQuestionConfirmModal = false">&times;</button>
        </div>
        <div class="modal-body text-center">
          <div class="alert-icon-wrapper">
            <AppIcon name="alert" size="48" class="warning-color" />
          </div>
          <p class="warning-text">
            Bạn có chắc chắn muốn xóa câu hỏi này?
          </p>
          <p class="sub-text">
            Hành động này không thể hoàn tác và sẽ xóa vĩnh viễn câu hỏi khỏi đề thi.
          </p>
        </div>
        <div class="modal-actions">
          <button type="button" class="cancel-btn" @click="showDeleteQuestionConfirmModal = false">Hủy bỏ</button>
          <button type="button" class="danger-btn" @click="confirmDeleteQuestion">Xác nhận xóa</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import api from '../../services/axios'
import { useQuizStore } from '../../stores/quiz'
import { useStudySetStore } from '../../stores/studySet'
import { useTopikLevelStore } from '../../stores/topikLevel'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const quizStore = useQuizStore()
const studySetStore = useStudySetStore()
const topikLevelStore = useTopikLevelStore()

const classesList = computed(() => studySetStore.classes || [])

const selectedQuiz = ref(null)
const showCreateModal = ref(false)
const showAddQuestionModal = ref(false)

// TOPIK levels loaded from Pinia Store
const topikLevels = computed(() => (topikLevelStore.levels || []).filter(l => l.groupType === 'QUIZ'))
const newQuizLevelId = ref(null)
const editQuizLevelId = ref(null)

const levelOptions = computed(() => {
  return topikLevels.value.map(lvl => ({ label: lvl.name, value: lvl.id }))
})

// Search, Filter & Pagination
const quizSearchQuery = ref('')
const selectedQuizLevelFilter = ref('')
const currentQuizPage = ref(1)
const quizzesPerPage = 10
const totalQuizElements = computed(() => quizStore.totalQuizElements)
const totalQuizPages = computed(() => quizStore.totalQuizPages)

const quizLevelFilterOptions = computed(() => [
  { value: '', label: 'Tất cả cấp độ' },
  ...topikLevels.value.map(lvl => ({ value: lvl.name, label: lvl.name }))
])

const loadQuizzesFromServer = async () => {
  try {
    const params = {
      page: currentQuizPage.value,
      size: quizzesPerPage,
      unpaged: false
    }
    if (quizSearchQuery.value.trim()) {
      params.search = quizSearchQuery.value.trim()
    }
    if (selectedQuizLevelFilter.value) {
      params.level = selectedQuizLevelFilter.value
    }
    await quizStore.fetchMyCreatedQuizzes(true, params)
  } catch (err) {
    console.error('Failed to load quizzes from server:', err)
    toast.error('Không thể tải danh sách đề thi.')
  }
}

const paginatedQuizzes = computed(() => quizStore.quizzes || [])

const startQuizIndex = computed(() => totalQuizElements.value === 0 ? 0 : (currentQuizPage.value - 1) * quizzesPerPage + 1)
const endQuizIndex = computed(() => Math.min(currentQuizPage.value * quizzesPerPage, totalQuizElements.value))

const quizPageNumbers = computed(() => {
  const pages = []
  const total = totalQuizPages.value
  const current = currentQuizPage.value
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

watch([quizSearchQuery, selectedQuizLevelFilter], () => {
  currentQuizPage.value = 1
  loadQuizzesFromServer()
})

watch(currentQuizPage, () => {
  loadQuizzesFromServer()
})

const classOptions = computed(() => {
  const list = classesList.value.map(c => ({ label: `${c.name || 'Lớp học'} (${c.schedule || 'Lịch học'})`, value: c.id }))
  return [{ label: '-- Không giao cho lớp nào --', value: '' }, ...list]
})

const sectionLabels = {
  'LISTENING': 'Nghe (듣기)',
  'READING': 'Đọc (읽기)',
  'WRITING': 'Viết (쓰기 - Tự luận)'
}

const qSectionOptions = computed(() => {
  return (quizStore.questionSections || []).map(sec => ({
    label: sectionLabels[sec] || sec,
    value: sec
  }))
})

const typeLabels = {
  'MULTIPLE_CHOICE': 'Trắc nghiệm chọn 1 đáp án',
  'WRITING': 'Tự luận nhập văn bản (Viết)'
}

const qTypeOptions = computed(() => {
  return (quizStore.questionTypes || []).map(type => ({
    label: typeLabels[type] || type,
    value: type
  }))
})

onMounted(async () => {
  try {
    await topikLevelStore.fetchLevels()
    await quizStore.fetchQuestionTypes()
    await quizStore.fetchQuestionSections()
    await studySetStore.fetchClasses()
    await loadQuizzesFromServer()
    if (topikLevels.value.length > 0) {
      newQuizLevelId.value = topikLevels.value[0].id
      editQuizLevelId.value = topikLevels.value[0].id
    }
  } catch (err) {
    console.error("Failed to load TOPIK levels or enums:", err)
  }
})

// New Quiz Fields
const newQuizTitle = ref('')
const newQuizTime = ref(50)
const newQuizScore = ref(100)
const newQuizDueDate = ref('')
const newQuizClassId = ref('')

// New Question Fields
const newQSection = ref('LISTENING')
const newQPoints = ref(2)
const newQText = ref('')
const newQType = ref('MULTIPLE_CHOICE')
const newQAudio = ref('')
const newQCorrectAnswer = ref('')
const newQWrong1 = ref('')
const newQWrong2 = ref('')
const newQWrong3 = ref('')
const newQOptions = ref(['', '', '', ''])
const newQCorrectOptionIndex = ref(0)

const getSectionLabel = (sec) => {
  if (sec === 'LISTENING') return 'Nghe'
  if (sec === 'READING') return 'Đọc'
  if (sec === 'WRITING') return 'Viết'
  return sec
}

const openCreateQuizModal = () => {
  newQuizTitle.value = ''
  newQuizLevelId.value = topikLevels.value[0]?.id || null
  newQuizTime.value = 100
  newQuizDueDate.value = ''
  newQuizClassId.value = ''
  showCreateModal.value = true
  newQuizScore.value = 100
}

const handleCreateQuiz = async () => {
  if (!newQuizTitle.value || !newQuizTime.value || !newQuizDueDate.value || !newQuizScore.value) return

  try {
    await quizStore.createQuiz({
      title: newQuizTitle.value,
      topikLevelId: newQuizLevelId.value,
      timeLimitMins: parseInt(newQuizTime.value),
      dueDate: newQuizDueDate.value + 'T23:59:59',
      classId: newQuizClassId.value || null,
      totalScore: parseInt(newQuizScore.value)
    })
    toast.success("Tạo đề thi/bài tập mới thành công!")
  } catch (e) {
    console.error("Failed to create quiz via API:", e)
    toast.error("Tạo đề thi/bài tập thất bại.")
  }

  showCreateModal.value = false
}

const isEditQuestionMode = ref(false)
const editingQuestionId = ref(null)
const uploadingAudioFile = ref(false)
const audioFileRef = ref(null)

const openAddQuestionModal = () => {
  isEditQuestionMode.value = false
  editingQuestionId.value = null
  newQSection.value = 'LISTENING'
  newQPoints.value = 2
  newQText.value = ''
  newQType.value = 'MULTIPLE_CHOICE'
  newQAudio.value = ''
  newQCorrectAnswer.value = ''
  newQWrong1.value = ''
  newQWrong2.value = ''
  newQWrong3.value = ''
  newQOptions.value = ['', '', '', '']
  newQCorrectOptionIndex.value = 0
  showAddQuestionModal.value = true
}

const closeQuestionModal = () => {
  showAddQuestionModal.value = false
}

const triggerEditQuestion = (question) => {
  isEditQuestionMode.value = true
  editingQuestionId.value = question.id
  newQSection.value = question.section || 'LISTENING'
  newQPoints.value = question.points || 2
  newQText.value = question.questionText || ''
  newQType.value = question.questionType || 'MULTIPLE_CHOICE'
  newQAudio.value = question.audioUrl || ''
  
  if (question.questionType === 'MULTIPLE_CHOICE') {
    newQCorrectAnswer.value = ''
    if (question.wrongAnswers && question.wrongAnswers.length > 0) {
      newQOptions.value = [...question.wrongAnswers]
      const idx = question.wrongAnswers.indexOf(question.correctAnswer)
      newQCorrectOptionIndex.value = idx !== -1 ? idx : 0
    } else {
      newQOptions.value = ['', '', '', '']
      newQCorrectOptionIndex.value = 0
    }
  } else {
    newQCorrectAnswer.value = question.correctAnswer || ''
    newQOptions.value = ['', '', '', '']
    newQCorrectOptionIndex.value = 0
  }
  
  showAddQuestionModal.value = true
}

const onAudioFileSelected = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  uploadingAudioFile.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('prefix', 'audio')
    
    const res = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (res && res.key) {
      const baseURL = api.defaults.baseURL || 'http://localhost:8080/api'
      newQAudio.value = `${baseURL}/files/download/${res.key}`
      toast.success("Tải file lên thành công!")
    } else {
      toast.error("Tải file lên thất bại.")
    }
  } catch (e) {
    console.error("Upload failed:", e)
    toast.error("Lỗi khi tải file lên.")
  } finally {
    uploadingAudioFile.value = false
  }
}

const getAudioFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return decodeURIComponent(parts[parts.length - 1])
}

const handleAddQuestion = async () => {
  if (!selectedQuiz.value || !newQText.value) return

  try {
    let correctAnswerText = ''
    let wrongAnswers = []

    if (newQType.value === 'MULTIPLE_CHOICE') {
      correctAnswerText = newQOptions.value[newQCorrectOptionIndex.value] || ''
      wrongAnswers = newQOptions.value.map(w => w || '')
    } else {
      correctAnswerText = newQCorrectAnswer.value || ''
    }

    const questionPayload = {
      questionText: newQText.value,
      questionType: newQType.value,
      points: parseFloat(newQPoints.value),
      section: newQSection.value,
      correctAnswer: correctAnswerText,
      wrongAnswers: wrongAnswers,
      audioUrl: newQSection.value === 'LISTENING' ? newQAudio.value : null
    }

    if (isEditQuestionMode.value) {
      const updatedQuestion = await quizStore.updateQuestion(selectedQuiz.value.id, editingQuestionId.value, questionPayload)
      if (selectedQuiz.value && selectedQuiz.value.questions) {
        const idx = selectedQuiz.value.questions.findIndex(q => q.id === editingQuestionId.value)
        if (idx !== -1) {
          selectedQuiz.value.questions[idx] = updatedQuestion
        }
      }
      toast.success("Cập nhật câu hỏi thành công!")
    } else {
      const newQuestion = await quizStore.addQuestion(selectedQuiz.value.id, questionPayload)
      if (selectedQuiz.value) {
        if (!selectedQuiz.value.questions) selectedQuiz.value.questions = []
        selectedQuiz.value.questions.push(newQuestion)
      }
      toast.success("Thêm câu hỏi mới thành công!")
    }
  } catch (e) {
    console.error("Failed to save question:", e)
    toast.error(isEditQuestionMode.value ? "Cập nhật câu hỏi thất bại." : "Thêm câu hỏi mới thất bại.")
  }

  closeQuestionModal()
}

const showDeleteQuestionConfirmModal = ref(false)
const questionToDelete = ref(null)

const triggerDeleteQuestion = (question) => {
  questionToDelete.value = question
  showDeleteQuestionConfirmModal.value = true
}

const confirmDeleteQuestion = async () => {
  if (!questionToDelete.value || !selectedQuiz.value) return
  const qId = questionToDelete.value.id
  try {
    await quizStore.deleteQuestion(selectedQuiz.value.id, qId)
    if (selectedQuiz.value.questions) {
      const index = selectedQuiz.value.questions.findIndex(q => q.id === qId)
      if (index !== -1) {
        selectedQuiz.value.questions.splice(index, 1)
      }
    }
    toast.success("Xóa câu hỏi thành công!")
  } catch (e) {
    console.error("Failed to delete question via API:", e)
    toast.error("Xóa câu hỏi thất bại.")
  } finally {
    showDeleteQuestionConfirmModal.value = false
    questionToDelete.value = null
  }
}

const selectQuiz = async (quiz) => {
  try {
    const details = await quizStore.fetchQuizDetails(quiz.id)
    selectedQuiz.value = details
  } catch (e) {
    console.error("Failed to fetch quiz details:", e)
    toast.error("Không thể lấy chi tiết câu hỏi.")
  }
}

const getQuestionOptions = (question) => {
  const opts = [];
  
  if (question.wrongAnswers && question.wrongAnswers.length === 4) {
    question.wrongAnswers.forEach(opt => {
      opts.push({
        text: opt,
        isCorrect: opt === question.correctAnswer
      });
    });
  } else {
    if (question.correctAnswer) {
      opts.push({ text: question.correctAnswer, isCorrect: true });
    }
    if (question.wrongAnswers && Array.isArray(question.wrongAnswers)) {
      question.wrongAnswers.forEach(w => {
        opts.push({ text: w, isCorrect: false });
      });
    }
  }
  return opts;
}

// Edit Quiz state & handlers
const showEditQuizModal = ref(false)
const quizToEdit = ref(null)
const editQuizTitle = ref('')
const editQuizTime = ref(100)
const editQuizDueDate = ref('')
const editQuizClassId = ref('')
const editQuizScore = ref(100)
const showDeleteQuizConfirmModal = ref(false)
const quizToDelete = ref(null)

const triggerEditQuiz = (quiz) => {
  quizToEdit.value = quiz
  editQuizTitle.value = quiz.title
  
  const matched = topikLevels.value.find(l => l.name === quiz.topikLevel || l.code === quiz.topikLevel)
  editQuizLevelId.value = matched ? matched.id : (topikLevels.value[0]?.id || null)
  
  editQuizTime.value = quiz.timeLimitMins || 100
  editQuizDueDate.value = quiz.dueDate ? quiz.dueDate.substring(0, 10) : ''
  editQuizClassId.value = quiz.classId || quiz.clazzId || ''
  editQuizScore.value = quiz.totalScore || 100
  showEditQuizModal.value = true
}

const handleUpdateQuiz = async () => {
  if (!quizToEdit.value || !editQuizTitle.value || !editQuizTime.value || !editQuizDueDate.value || !editQuizScore.value) return

  try {
    await quizStore.updateQuiz(quizToEdit.value.id, {
      title: editQuizTitle.value,
      topikLevelId: editQuizLevelId.value,
      timeLimitMins: parseInt(editQuizTime.value),
      dueDate: editQuizDueDate.value + 'T23:59:59',
      classId: editQuizClassId.value || null,
      totalScore: parseInt(editQuizScore.value)
    })
    toast.success("Cập nhật đề thi thành công!")
    showEditQuizModal.value = false
    
    const matched = topikLevels.value.find(l => l.id === editQuizLevelId.value)
    const levelName = matched ? matched.name : ''
    
    // Update active selection details if selectedQuiz matches
    if (selectedQuiz.value && selectedQuiz.value.id === quizToEdit.value.id) {
      selectedQuiz.value.title = editQuizTitle.value
      selectedQuiz.value.topikLevel = levelName
      selectedQuiz.value.timeLimitMins = editQuizTime.value
      selectedQuiz.value.totalScore = parseInt(editQuizScore.value)
      selectedQuiz.value.classId = editQuizClassId.value || null
    }
  } catch (e) {
    console.error("Failed to update quiz:", e)
    toast.error("Cập nhật đề thi thất bại.")
  }
}

const triggerDeleteQuiz = (quiz) => {
  quizToDelete.value = quiz
  showDeleteQuizConfirmModal.value = true
}

const confirmDeleteQuiz = async () => {
  if (!quizToDelete.value) return
  try {
    await quizStore.deleteQuiz(quizToDelete.value.id)
    toast.success("Xóa đề thi thành công!")
    if (selectedQuiz.value && selectedQuiz.value.id === quizToDelete.value.id) {
      selectedQuiz.value = null
    }
  } catch (e) {
    console.error("Failed to delete quiz:", e)
    toast.error("Xóa đề thi thất bại.")
  } finally {
    showDeleteQuizConfirmModal.value = false
    quizToDelete.value = null
  }
}
</script>

<style scoped>
.teacher-quizzes {
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

.modal-body { padding: 2rem 1.5rem; }

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

.quizzes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}

/* Filter Card */
.filter-card {
  background-color: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  padding: 0.75rem 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
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

.quiz-card {
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

.quiz-card:hover, .quiz-card.active {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.quiz-header {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.quiz-icon-badge {
  width: 36px;
  height: 36px;
  background-color: var(--primary);
  color: #fff;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.quiz-title-meta h3 {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.15rem;
}

.type-badge {
  font-size: 0.7rem;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.1rem 0.4rem;
  border-radius: var(--radius-sm);
  color: var(--primary);
  font-weight: 600;
}

.quiz-body {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.quiz-footer {
  border-top: 1px solid var(--border-color);
  padding-top: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}

.quiz-action-group {
  display: flex;
  gap: 0.5rem;
}

.action-btn.text-link {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
}

.action-btn.text-link:hover {
  text-decoration: underline;
}

.action-btn.text-link.danger-action {
  color: var(--danger);
}

.quiz-detail-panel {
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

.section-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-actions h4 {
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

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.question-row {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.q-header-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.q-index {
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--text-title);
}

.section-tag {
  font-size: 0.7rem;
  padding: 0.15rem 0.45rem;
  border-radius: var(--radius-sm);
  font-weight: 700;
}

.section-tag.listening { background-color: rgba(139, 92, 246, 0.1); color: rgb(139, 92, 246); }
.section-tag.reading { background-color: rgba(59, 130, 246, 0.1); color: rgb(59, 130, 246); }
.section-tag.writing { background-color: rgba(249, 115, 22, 0.1); color: rgb(249, 115, 22); }

.points-tag {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-weight: 600;
  margin-left: auto;
}

.question-text {
  font-size: 0.9rem;
  color: var(--text-title);
  font-weight: 600;
  line-height: 1.4;
}

.audio-indicator {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.75rem;
  color: var(--primary);
  margin-top: 0.25rem;
}

.answers-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.answer-item {
  padding: 0.5rem 0.75rem;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.answer-item.correct {
  border-color: rgba(16, 185, 129, 0.4);
  background-color: rgba(16, 185, 129, 0.05);
  color: rgb(16, 185, 129);
  font-weight: 600;
}

.essay-indicator, .audio-indicator {
  font-size: 0.8rem;
  color: var(--text-muted);
  background-color: var(--bg-card);
  border: 1px dashed var(--border-color);
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-sm);
  margin-top: 0.25rem;
}

.q-actions-row {
  text-align: right;
  border-top: 1px dashed var(--border-color);
  padding-top: 0.5rem;
}

.delete-btn {
  background: none;
  border: none;
  color: var(--danger);
  font-weight: 600;
  font-size: 0.75rem;
  cursor: pointer;
}

.delete-btn:hover {
  text-decoration: underline;
}

.edit-question-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.75rem;
  cursor: pointer;
}

.edit-question-btn:hover {
  text-decoration: underline;
}

/* Modal layout variants */
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

.modal-content.large {
  max-width: 550px;
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
  gap: 1.1rem;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.border-box {
  border: 1px solid var(--border-color);
  padding: 0.75rem;
  border-radius: var(--radius-md);
  background-color: var(--bg-body);
}

label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-title);
}

input, select, textarea {
  padding: 0.65rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.85rem;
}

input:focus, select:focus, textarea:focus {
  border-color: var(--primary);
  outline: none;
}

.ans-input-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.4rem;
}

.ans-label {
  font-size: 0.75rem;
  font-weight: 600;
  width: 60px;
  color: var(--text-muted);
}

.ans-label.correct {
  color: rgb(16, 185, 129);
}

.ans-input-row input {
  flex-grow: 1;
  padding: 0.5rem;
  background-color: var(--bg-card);
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

.cancel-btn {
  background-color: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  padding: 0.55rem 1.25rem;
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
  padding: 0.55rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.submit-btn:hover {
  background-color: var(--primary-hover);
}

.delete-confirm {
  max-width: 440px;
}

.text-center {
  text-align: center;
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
  color: #fff;
  border: none;
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.danger-btn:hover:not(:disabled) {
  background-color: #83392c;
}

.audio-source-selector {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 0.75rem;
}

.audio-source-selector .radio-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
}

.upload-container {
  margin-top: 0.5rem;
}

.hidden-file-input {
  display: none;
}

.upload-drag-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  background-color: var(--bg-body);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: center;
}

.upload-drag-zone:hover {
  border-color: var(--primary);
  background-color: rgba(var(--primary-rgb), 0.05);
}

.upload-icon {
  color: var(--text-muted);
  margin-bottom: 0.5rem;
}

.file-name-display {
  font-size: 0.85rem;
  color: var(--primary);
  font-weight: 600;
  word-break: break-all;
}

.loading-text {
  font-size: 0.85rem;
  color: var(--text-muted);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.recorder-container {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.recorder-box {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1rem;
  background-color: var(--bg-body);
}

.recorder-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-sm);
  background-color: var(--bg-card);
}

.status-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--text-muted);
}

.recorder-status.recording .status-indicator {
  background-color: var(--danger);
  animation: blink 1s infinite;
}

.recorder-status.paused .status-indicator {
  background-color: #f59e0b;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.status-text {
  font-size: 0.85rem;
  font-weight: 500;
  flex-grow: 1;
  color: var(--text-title);
}

.timer-countdown {
  font-size: 1rem;
  font-family: monospace;
  font-weight: 700;
  color: var(--primary);
}

.recorder-controls {
  display: flex;
  justify-content: center;
}

.active-controls {
  display: flex;
  gap: 0.5rem;
  width: 100%;
}

.record-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.55rem 1rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  border: none;
  flex: 1;
  transition: all var(--transition-fast);
}

.record-btn.start {
  background-color: var(--primary);
  color: #fff;
  max-width: 200px;
}

.record-btn.start:hover {
  background-color: var(--primary-hover);
}

.record-btn.pause {
  background-color: #f59e0b;
  color: #fff;
}

.record-btn.pause:hover {
  background-color: #d97706;
}

.record-btn.resume {
  background-color: #10b981;
  color: #fff;
}

.record-btn.resume:hover {
  background-color: #059669;
}

.record-btn.stop {
  background-color: var(--danger);
  color: #fff;
}

.record-btn.stop:hover {
  background-color: #b91c1c;
}

.recording-preview {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.preview-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
}

.audio-player-preview {
  width: 100%;
  border-radius: var(--radius-sm);
}
</style>
