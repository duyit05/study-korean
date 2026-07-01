<template>
  <div class="teacher-quizzes animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Soạn đề thi & bài tập luyện tập</h2>
        <p>Soạn thảo đề ôn luyện TOPIK và trắc nghiệm từ vựng tự động chấm điểm.</p>
      </div>
      <button class="primary-btn" @click="openCreateQuizModal">
        <AppIcon name="quiz" size="18" />
        <span>Soạn đề thi mới</span>
      </button>
    </div>

    <!-- Quizzes Grid -->
    <div class="quizzes-grid">
      <div 
        v-for="quiz in quizzes" 
        :key="quiz.id" 
        class="quiz-card"
        :class="{ active: selectedQuiz && selectedQuiz.id === quiz.id }"
        @click="selectedQuiz = quiz"
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
            <span>{{ quiz.questions?.length || 0 }} câu hỏi</span>
          </div>
        </div>
        <div class="quiz-footer">
          <button class="action-btn text-link" @click.stop="openEditQuestions(quiz)">Quản lý câu hỏi &rarr;</button>
        </div>
      </div>
    </div>

    <!-- View & Edit Questions of Selected Quiz Panel -->
    <div v-if="selectedQuiz" class="quiz-detail-panel animate-scale">
      <div class="panel-header">
        <div class="header-title">
          <h3>Đề: {{ selectedQuiz.title }}</h3>
          <p>{{ selectedQuiz.topikLevel || 'Luyện tập thường' }} | Thời gian: {{ selectedQuiz.timeLimitMins }} phút</p>
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
                <div class="answer-item correct">
                  <span class="check-icon">&#10003;</span>
                  <span>{{ question.correctAnswer }}</span>
                </div>
                <div v-for="wrong in question.wrongAnswers" :key="wrong" class="answer-item wrong">
                  <span>{{ wrong }}</span>
                </div>
              </div>
              <div v-else class="essay-indicator">
                <span>[Tự luận] Học sinh điền câu trả lời ngắn hoặc viết bài luận.</span>
              </div>
            </div>
            <div class="q-actions-row">
              <button class="delete-btn" @click="deleteQuestion(selectedQuiz, question.id)">Xóa câu này</button>
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
            <select id="quizLevel" v-model="newQuizLevel">
              <option value="TOPIK_I">TOPIK I (Nghe & Đọc - 200đ)</option>
              <option value="TOPIK_II">TOPIK II (Nghe, Đọc & Viết - 300đ)</option>
              <option value="NORMAL">Luyện tập thường</option>
            </select>
          </div>
          <div class="form-group">
            <label for="quizTime">Thời gian làm bài (phút)</label>
            <input type="number" id="quizTime" v-model="newQuizTime" placeholder="Ví dụ: 100" required min="1">
          </div>
          <div class="form-group">
            <label for="quizDueDate">Hạn nộp bài</label>
            <input type="date" id="quizDueDate" v-model="newQuizDueDate" required>
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
          <h3>Soạn câu hỏi mới</h3>
          <button class="close-btn" @click="showAddQuestionModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleAddQuestion" class="modal-form">
          <div class="form-row-2">
            <div class="form-group">
              <label for="qSection">Kỹ năng / Phần thi</label>
              <select id="qSection" v-model="newQSection" required>
                <option value="LISTENING">Nghe (듣기)</option>
                <option value="READING">Đọc (읽기)</option>
                <option value="WRITING">Viết (쓰기 - Tự luận)</option>
              </select>
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
            <label for="qAudio">URL File âm thanh (.mp3)</label>
            <input type="text" id="qAudio" v-model="newQAudio" placeholder="https://example.com/audio.mp3" required>
          </div>

          <div class="form-group">
            <label for="qType">Dạng câu trả lời</label>
            <select id="qType" v-model="newQType" required>
              <option value="MULTIPLE_CHOICE">Trắc nghiệm chọn 1 đáp án</option>
              <option value="WRITING">Tự luận nhập văn bản (Viết)</option>
            </select>
          </div>

          <!-- Multiple choice fields -->
          <div v-if="newQType === 'MULTIPLE_CHOICE'" class="form-group border-box">
            <label>Thiết lập đáp án trắc nghiệm</label>
            <div class="ans-input-row">
              <span class="ans-label correct">&#10003; Đúng:</span>
              <input type="text" v-model="newQCorrectAnswer" placeholder="Nhập đáp án đúng" required>
            </div>
            <div class="ans-input-row">
              <span class="ans-label">Sai 1:</span>
              <input type="text" v-model="newQWrong1" placeholder="Đáp án sai số 1" required>
            </div>
            <div class="ans-input-row">
              <span class="ans-label">Sai 2:</span>
              <input type="text" v-model="newQWrong2" placeholder="Đáp án sai số 2" required>
            </div>
            <div class="ans-input-row">
              <span class="ans-label">Sai 3:</span>
              <input type="text" v-model="newQWrong3" placeholder="Đáp án sai số 3" required>
            </div>
          </div>

          <!-- Essay Fields -->
          <div v-else class="form-group border-box">
            <label for="qCorrectEssay">Gợi ý đáp án / Bài mẫu tự luận (tùy chọn)</label>
            <textarea id="qCorrectEssay" v-model="newQCorrectAnswer" placeholder="Nhập đáp án mẫu để tham khảo khi chấm bài..." rows="2"></textarea>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showAddQuestionModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Thêm vào đề thi</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import { useQuizStore } from '../../stores/quiz'

const props = defineProps({
  quizzes: {
    type: Array,
    required: true
  }
})

const quizStore = useQuizStore()

const selectedQuiz = ref(null)
const showCreateModal = ref(false)
const showAddQuestionModal = ref(false)

// New Quiz Fields
const newQuizTitle = ref('')
const newQuizLevel = ref('TOPIK_I')
const newQuizTime = ref(100)
const newQuizDueDate = ref('')

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

const getSectionLabel = (sec) => {
  if (sec === 'LISTENING') return 'Nghe'
  if (sec === 'READING') return 'Đọc'
  if (sec === 'WRITING') return 'Viết'
  return sec
}

const openCreateQuizModal = () => {
  newQuizTitle.value = ''
  newQuizLevel.value = 'TOPIK_I'
  newQuizTime.value = 100
  newQuizDueDate.value = ''
  showCreateModal.value = true
}

const handleCreateQuiz = async () => {
  if (!newQuizTitle.value || !newQuizTime.value || !newQuizDueDate.value) return

  try {
    await quizStore.createQuiz({
      title: newQuizTitle.value,
      topikLevel: newQuizLevel.value,
      timeLimitMins: parseInt(newQuizTime.value),
      dueDate: newQuizDueDate.value + 'T23:59:59',
      classId: 1 // Default classId
    })
  } catch (e) {
    console.error("Failed to create quiz via API:", e)
  }

  showCreateModal.value = false
}

const openAddQuestionModal = () => {
  newQSection.value = 'LISTENING'
  newQPoints.value = 2
  newQText.value = ''
  newQType.value = 'MULTIPLE_CHOICE'
  newQAudio.value = ''
  newQCorrectAnswer.value = ''
  newQWrong1.value = ''
  newQWrong2.value = ''
  newQWrong3.value = ''
  showAddQuestionModal.value = true
}

const handleAddQuestion = async () => {
  if (!selectedQuiz.value || !newQText.value) return

  try {
    const wrongAnswers = [
      newQWrong1.value,
      newQWrong2.value,
      newQWrong3.value
    ].filter(Boolean)

    const questionPayload = {
      questionText: newQText.value,
      questionType: newQType.value,
      points: parseFloat(newQPoints.value),
      section: newQSection.value,
      correctAnswer: newQCorrectAnswer.value,
      wrongAnswers: newQType.value === 'MULTIPLE_CHOICE' ? wrongAnswers : [],
      audioUrl: newQSection.value === 'LISTENING' ? newQAudio.value : null
    }

    const newQuestion = await quizStore.addQuestion(selectedQuiz.value.id, questionPayload)
    
    if (selectedQuiz.value) {
      if (!selectedQuiz.value.questions) selectedQuiz.value.questions = []
      selectedQuiz.value.questions.push(newQuestion)
    }
  } catch (e) {
    console.error("Failed to add question via API:", e)
  }

  showAddQuestionModal.value = false
}

const deleteQuestion = async (quiz, questionId) => {
  try {
    await quizStore.deleteQuestion(quiz.id, questionId)
    if (selectedQuiz.value && selectedQuiz.value.questions) {
      const index = selectedQuiz.value.questions.findIndex(q => q.id === questionId)
      if (index !== -1) {
        selectedQuiz.value.questions.splice(index, 1)
      }
    }
  } catch (e) {
    console.error("Failed to delete question via API:", e)
  }
}

const openEditQuestions = (quiz) => {
  selectedQuiz.value = quiz
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
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.25rem;
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
  padding-top: 0.5rem;
  text-align: right;
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
  justify-content: flex-end;
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
</style>
