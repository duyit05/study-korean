<template>
  <div class="practice-container animate-fade">
    <!-- Header Section -->
    <div class="practice-header-section" v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz">
      <div class="header-left">
        <h2>Góc Tự Ôn Tập & Luyện Đề 🎯</h2>
        <p>Tự tạo bài tập đọc từ vựng, bài thi nghe và import từ vựng hàng loạt để ôn tập cá nhân.</p>
      </div>
      <div class="practice-status-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'reading' }" 
          @click="activeTab = 'reading'"
        >
          📖 Đọc & Từ Vựng ({{ readingQuizzes.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'listening' }" 
          @click="activeTab = 'listening'"
        >
          🎧 Kỹ Năng Nghe ({{ listeningQuizzes.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'completed' }" 
          @click="activeTab = 'completed'"
        >
          ✅ Lịch Sử Làm Bài ({{ completedQuizzes.length }})
        </button>
      </div>
    </div>

    <!-- MAIN DASHBOARD CONTENT -->
    <div v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz">
      <!-- 1. Reading / Vocab Tab -->
      <template v-if="activeTab === 'reading'">
        <div class="quizzes-grid animate-scale">
          <!-- Create Shortcut Box -->
          <div class="quiz-card create-trigger-card reading-btn" @click="openCreator('reading')">
            <div class="trigger-content">
              <div class="trigger-plus-icon">📖</div>
              <h4>Tạo Đề Đọc/Từ Vựng</h4>
              <p>Tự thiết kế đề thi trắc nghiệm đọc hiểu, từ vựng và ngữ pháp tiếng Hàn.</p>
            </div>
          </div>

          <!-- Self Created Quizzes -->
          <div 
            v-for="quiz in readingQuizzes" 
            :key="quiz.id" 
            class="quiz-card pending"
          >
            <div class="quiz-badge-row">
              <span class="badge type-badge reading">📖 Đọc & Từ Vựng</span>
              <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit || 10 }} Phút</span>
              <span v-if="quiz.score !== null && quiz.score !== undefined" class="badge score-badge practice-score">Điểm gần nhất: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
            </div>
            <h3>{{ quiz.title || 'Đề ôn tập đọc' }}</h3>
            <p class="summary-q">{{ quiz.questionCount || (quiz.questions ? quiz.questions.length : 0) }} câu hỏi ôn tập</p>

            <div class="quiz-action-bar">
              <button class="start-btn" @click="startQuiz(quiz)">
                <AppIcon name="play" size="16" /> {{ (quiz.score !== null && quiz.score !== undefined) ? 'Làm lại ôn tập' : 'Làm bài ngay' }}
              </button>
              <button class="edit-practice-btn" @click.stop="editPracticeQuiz(quiz)" title="Chỉnh sửa / Thêm từ vựng">
                <AppIcon name="edit" size="16" />
              </button>
              <button class="delete-practice-btn" @click.stop="deletePracticeQuiz(quiz.id)" title="Xóa đề ôn tập này">
                <AppIcon name="x" size="16" />
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- 2. Listening Tab -->
      <template v-if="activeTab === 'listening'">
        <div class="quizzes-grid animate-scale">
          <!-- Create Shortcut Box -->
          <div class="quiz-card create-trigger-card listening-btn" @click="openCreator('listening')">
            <div class="trigger-content">
              <div class="trigger-plus-icon">🎧</div>
              <h4>Tạo Bài Thi Nghe</h4>
              <p>Soạn bài thi nghe có chèn audio của riêng bạn hoặc phát giọng AI và chọn đáp án ABCD.</p>
            </div>
          </div>

          <div 
            v-for="quiz in listeningQuizzes" 
            :key="quiz.id" 
            class="quiz-card pending"
          >
            <div class="quiz-badge-row">
              <span class="badge type-badge listening">🎧 Kỹ Năng Nghe</span>
              <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit || 10 }} Phút</span>
              <span v-if="quiz.score !== null && quiz.score !== undefined" class="badge score-badge practice-score">Điểm gần nhất: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
            </div>
            <h3>{{ quiz.title || 'Đề ôn tập nghe' }}</h3>
            <p class="summary-q">{{ quiz.questionCount || (quiz.questions ? quiz.questions.length : 0) }} câu hỏi luyện nghe</p>

            <div class="quiz-action-bar">
              <button class="start-btn" @click="startQuiz(quiz)">
                <AppIcon name="play" size="16" /> {{ (quiz.score !== null && quiz.score !== undefined) ? 'Làm lại đề nghe' : 'Làm bài ngay' }}
              </button>
              <button class="edit-practice-btn" @click.stop="editPracticeQuiz(quiz)" title="Chỉnh sửa / Thêm từ vựng">
                <AppIcon name="edit" size="16" />
              </button>
              <button class="delete-practice-btn" @click.stop="deletePracticeQuiz(quiz.id)" title="Xóa đề ôn tập này">
                <AppIcon name="x" size="16" />
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- 3. Completed Tab -->
      <template v-if="activeTab === 'completed'">
        <div class="quizzes-grid animate-scale">
          <div 
            v-for="quiz in completedQuizzes" 
            :key="quiz.id" 
            class="quiz-card completed"
          >
            <div class="quiz-badge-row">
              <span class="badge type-badge" :class="quiz.quizType === 'listening' ? 'listening' : 'reading'">
                {{ quiz.quizType === 'listening' ? '🎧 Bài Nghe' : '📖 Bài Đọc' }}
              </span>
              <span class="badge score-badge">Điểm: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
              <span v-if="quiz.topikLevelResult" class="badge type-badge" style="background-color: var(--primary-light); color: var(--primary); font-weight: 700;">
                🏆 {{ quiz.topikLevelResult }}
              </span>
            </div>
            <h3>{{ quiz.title || 'Đề hoàn thành' }}</h3>

            <div class="quiz-action-bar">
              <button class="review-btn" @click="viewResults(quiz)">
                Xem lại kết quả
              </button>
              <button class="start-btn retake" @click="startQuiz(quiz)">
                <AppIcon name="play" size="16" /> Làm lại
              </button>
            </div>
          </div>

          <div v-if="completedQuizzes.length === 0" class="empty-state">
            <p>Chưa có bài ôn tập nào hoàn thành.</p>
          </div>
        </div>
      </template>
    </div>

    <!-- QUIZ CREATOR FORM VIEW -->
    <div v-else-if="isCreatingQuiz" class="quiz-creator-container animate-scale">
      <button class="back-link-btn" @click="isCreatingQuiz = false">
        <AppIcon name="chevron-left" size="16" /> Hủy và quay lại danh sách
      </button>

      <div class="creator-header">
        <h3>{{ creatorQuizType === 'listening' ? 'Thiết Kế Đề Thi Nghe (Listening) 🎧' : 'Thiết Kế Đề Đọc / Từ Vựng 📖' }}</h3>
        <p>Cấu trúc trắc nghiệm chữ kiểm tra mặt chữ, ngữ pháp và nghĩa từ vựng.</p>
      </div>

      <div class="creator-body-grid">
        <!-- LEFT COLUMN: Form -->
        <div class="creator-form-card">
          <div class="form-group">
            <label for="new-quiz-title" class="label-bold">Tên đề thi tự luyện</label>
            <input 
              type="text" 
              id="new-quiz-title" 
              v-model="newQuizTitle" 
              placeholder="Ví dụ: Đề trắc nghiệm đọc từ vựng tự luyện"
              class="creator-input-field"
            >
          </div>

          <div class="questions-builder-list">
            <div 
              v-for="(q, qIdx) in newQuizQuestions" 
              :key="qIdx" 
              class="question-builder-item"
            >
              <div class="builder-item-header">
                <h4>Câu hỏi {{ qIdx + 1 }}</h4>
                <button 
                  v-if="newQuizQuestions.length > 1" 
                  class="remove-q-btn" 
                  @click="removeQuestionFromBuilder(qIdx)"
                >
                  <AppIcon name="x" size="14" /> Xóa câu hỏi
                </button>
              </div>

              <!-- Audio option for Listening -->
              <template v-if="creatorQuizType === 'listening'">
                <div class="form-group" style="margin-bottom: 1rem;">
                  <label class="label-accent">Nguồn file âm thanh (Audio Source)</label>
                  <div class="source-toggle-group">
                    <label class="radio-label">
                      <input type="radio" value="tts" v-model="q.audioSource">
                      <span>AI phát âm mẫu (TTS ko-KR)</span>
                    </label>
                    <label class="radio-label">
                      <input type="radio" value="file" v-model="q.audioSource">
                      <span>Tải file MP3 / Dán liên kết</span>
                    </label>
                  </div>
                </div>

                <div class="form-group animate-fade" v-if="q.audioSource === 'tts'" style="margin-bottom: 1rem;">
                  <label>Từ hoặc câu tiếng Hàn để AI phát âm</label>
                  <input 
                    type="text" 
                    v-model="q.koreanText" 
                    placeholder="Ví dụ: 안녕하세요 hoặc 봄"
                    class="creator-input-field"
                  >
                </div>

                <div class="form-group animate-fade" v-else style="margin-bottom: 1rem;">
                  <label>Liên kết âm thanh (.mp3) hoặc chọn file từ máy tính</label>
                  <input 
                    type="text" 
                    v-model="q.audioUrl" 
                    placeholder="Dán link audio: https://example.com/audio.mp3"
                    class="creator-input-field"
                    style="margin-bottom: 0.5rem;"
                  >
                </div>
              </template>

              <div class="form-group">
                <label>Câu hỏi phụ / Gợi ý bằng tiếng Việt</label>
                <input 
                  type="text" 
                  v-model="q.question" 
                  :placeholder="creatorQuizType === 'listening' ? 'Ví dụ: Lắng nghe đoạn audio hội thoại sau và chọn nghĩa đúng:' : 'Ví dụ: Từ nào sau đây có nghĩa là Mùa Đông?'"
                  class="creator-input-field"
                >
              </div>

              <div class="form-row-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 0.75rem; margin-bottom: 0.75rem;">
                <div class="form-group">
                  <label>Loại từ (Từ loại)</label>
                  <select v-model="q.wordType" class="creator-input-field">
                    <option value="Động từ">Động từ</option>
                    <option value="Tính từ">Tính từ</option>
                    <option value="Danh từ">Danh từ</option>
                    <option value="Trạng từ">Trạng từ</option>
                    <option value="Cụm từ / Ngữ pháp">Cụm từ / Ngữ pháp</option>
                    <option value="Khác">Khác</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Phiên âm (tùy chọn)</label>
                  <input 
                    type="text" 
                    v-model="q.pronunciation" 
                    placeholder="Ví dụ: [ka-da]"
                    class="creator-input-field"
                  >
                </div>
              </div>

              <div class="options-builder-grid">
                <div class="option-builder-row" v-for="(opt, optIdx) in 4" :key="optIdx">
                  <div class="opt-label-wrapper">
                    <input 
                      type="radio" 
                      :name="'correct-opt-' + qIdx" 
                      :value="optIdx" 
                      v-model="q.correctOptionIndex"
                      :id="'radio-' + qIdx + '-' + optIdx"
                    >
                    <label :for="'radio-' + qIdx + '-' + optIdx">Đáp án {{ String.fromCharCode(65 + optIdx) }}</label>
                  </div>
                  <input 
                    type="text" 
                    v-model="q.options[optIdx]" 
                    :placeholder="'Nhập câu trả lời ' + String.fromCharCode(65 + optIdx)"
                    class="creator-input-field-opt"
                  >
                </div>
              </div>
              
              <p class="correct-hint">* Nhấp chọn nút tròn bên cạnh đáp án đúng của câu hỏi này.</p>
            </div>
          </div>

          <div class="creator-actions">
            <button class="add-q-btn" @click="addQuestionToBuilder">
              + Thêm câu hỏi mới
            </button>
            <button class="import-q-btn" @click="openStudentImportModal">
              📁 Import từ file / text
            </button>
            <button class="save-quiz-btn" @click="saveCustomQuiz">
              Lưu từ vựng
            </button>
          </div>
        </div>

        <!-- RIGHT COLUMN: Created Items Table -->
        <div class="created-items-panel">
          <div class="panel-header-row">
            <h4>Từ vựng đã tạo</h4>
            <span class="count-pill">{{ newQuizQuestions.length }} câu hỏi</span>
          </div>

          <div class="table-container-side">
            <table class="created-items-table">
              <thead>
                <tr>
                  <th>CÂU HỎI</th>
                  <th>TỪ LOẠI</th>
                  <th>NGHĨA ĐÚNG CỦA TỪ</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(q, idx) in newQuizQuestions" :key="idx">
                  <td>
                    <div class="q-cell">
                      <span class="question-icon">❓</span>
                      <div class="q-text-group">
                        <strong class="q-title">{{ q.question || q.koreanText || ('Câu ' + (idx + 1)) }}</strong>
                        <span v-if="q.pronunciation" class="q-pron">/{{ q.pronunciation }}/</span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span class="type-pill">{{ q.wordType || 'Động từ' }}</span>
                  </td>
                  <td>
                    <span class="correct-badge-pill">✓ {{ q.options[q.correctOptionIndex] || '(Chưa chọn)' }}</span>
                  </td>
                  <td class="action-cell">
                    <button v-if="newQuizQuestions.length > 1" class="delete-mini-btn" @click="removeQuestionFromBuilder(idx)" title="Xóa câu này">&times;</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- STUDENT IMPORT MODAL -->
    <div v-if="showStudentImportModal" class="modal-overlay" @click.self="showStudentImportModal = false">
      <div class="modal-content animate-scale" style="max-width: 650px;">
        <div class="modal-header">
          <h3>Import Từ Vựng / Câu Hỏi Hàng Loạt 📁</h3>
          <button class="close-btn" @click="showStudentImportModal = false">&times;</button>
        </div>
        <div class="modal-body" style="padding: 1.25rem;">
          <div class="import-tabs" style="display: flex; gap: 0.5rem; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem;">
            <button class="tab-btn" :class="{ active: studentImportTab === 'paste' }" @click="studentImportTab = 'paste'">Dán văn bản trực tiếp</button>
            <button class="tab-btn" :class="{ active: studentImportTab === 'file' }" @click="studentImportTab = 'file'">Tải file .txt / .csv</button>
          </div>

          <div v-if="studentImportTab === 'paste'" class="paste-section">
            <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem;">
              Nhập mỗi câu hỏi/từ vựng trên một dòng theo định dạng:<br>
              <code>Từ tiếng Hàn | Từ loại | Phiên âm | Nghĩa tiếng Việt</code><br>
              <em>(Hoặc đơn giản: <code>Từ tiếng Hàn - Nghĩa tiếng Việt</code>)</em>
            </p>
            <textarea 
              v-model="studentImportText" 
              placeholder="Ví dụ:&#10;가다 | Động từ | ka-da | Đi&#10;오다 | Động từ | o-da | Đến&#10;예쁘다 | Tính từ | ye-ppeu-da | Đẹp"
              rows="8"
              class="creator-input-field"
              style="width: 100%; font-family: monospace; font-size: 0.9rem;"
            ></textarea>
          </div>

          <div v-else class="file-section">
            <input type="file" accept=".txt,.csv" id="studentImportFile" class="hidden-file-input" @change="handleStudentFileSelected">
            <label for="studentImportFile" class="upload-drag-zone" style="display: flex; flex-direction: column; align-items: center; padding: 2rem; border: 2px dashed var(--border-color); border-radius: var(--radius-md); cursor: pointer;">
              <AppIcon name="upload" size="32" />
              <span style="margin-top: 0.5rem;">{{ studentImportFileName || 'Nhấp chọn file .txt hoặc .csv từ máy tính' }}</span>
            </label>
          </div>
        </div>
        <div class="modal-actions" style="padding: 1rem 1.25rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end; gap: 0.75rem;">
          <button type="button" class="cancel-btn" @click="showStudentImportModal = false">Hủy bỏ</button>
          <button type="button" class="submit-btn" @click="confirmStudentImport">Import vào đề thi</button>
        </div>
      </div>
    </div>

    <!-- ACTIVE QUIZ SCREEN -->
    <div v-else-if="activeQuiz" class="active-quiz-container animate-scale">
      <div class="quiz-run-header">
        <div class="quiz-title-info">
          <h3>{{ activeQuiz.title || 'Đang làm bài ôn tập' }}</h3>
          <span class="question-tracker">Câu hỏi {{ currentQuestionIndex + 1 }} / {{ activeQuiz.questions ? activeQuiz.questions.length : 0 }}</span>
        </div>
        <button class="exit-quiz-btn" @click="confirmExitQuiz">
          Thoát bài làm
        </button>
      </div>

      <div class="quiz-main-card">
        <div class="question-display-area">
          <h4 class="q-number">Câu {{ currentQuestionIndex + 1 }}</h4>
          
          <template v-if="currentQuestion.type === 'listening'">
            <div class="audio-player-box" v-if="currentQuestion.audioSource === 'tts' && currentQuestion.koreanText">
              <button class="play-audio-btn" @click="playTTS(currentQuestion.koreanText)">
                🔊 Phát âm thanh AI (TTS)
              </button>
            </div>
            <div class="audio-player-box" v-else-if="currentQuestion.audioUrl">
              <audio controls :src="currentQuestion.audioUrl" style="width: 100%;"></audio>
            </div>
          </template>

          <p class="q-text">{{ currentQuestion.question || 'Nội dung câu hỏi ôn tập:' }}</p>

          <div class="options-choice-list">
            <button 
              v-for="(opt, oIdx) in currentQuestion.options" 
              :key="oIdx"
              class="option-choice-btn"
              :class="{ selected: userAnswers[currentQuestion.id] === opt }"
              @click="selectAnswer(currentQuestion.id, opt)"
            >
              <span class="opt-key">{{ String.fromCharCode(65 + oIdx) }}</span>
              <span class="opt-label">{{ opt }}</span>
            </button>
          </div>
        </div>

        <div class="quiz-nav-footer">
          <button class="nav-step-btn" :disabled="currentQuestionIndex === 0" @click="currentQuestionIndex--">
            Quay lại
          </button>
          <button v-if="currentQuestionIndex < (activeQuiz.questions ? activeQuiz.questions.length - 1 : 0)" class="nav-step-btn primary" @click="currentQuestionIndex++">
            Câu tiếp
          </button>
          <button v-else class="submit-quiz-btn" @click="submitQuiz">
            Nộp bài
          </button>
        </div>
      </div>
    </div>

    <!-- RESULTS REVIEW SCREEN -->
    <div v-else-if="viewingResultQuiz" class="results-review-container animate-scale">
      <button class="back-link-btn" @click="viewingResultQuiz = null">
        <AppIcon name="chevron-left" size="16" /> Quay lại danh sách ôn tập
      </button>

      <div class="result-score-banner">
        <div class="score-radial">
          <span class="score-num">{{ viewingResultQuiz.score || 0 }}</span>
          <span class="total-num">/ {{ viewingResultQuiz.totalScore || 10 }}</span>
        </div>
        <div class="score-text-details">
          <h3>Kết quả: {{ viewingResultQuiz.title || 'Đề ôn tập' }}</h3>
          <p>Nộp ngày: {{ formatDate(viewingResultQuiz.completedAt) }}</p>
        </div>
      </div>

      <h3 class="review-heading">Xem lại chi tiết bài làm</h3>
      <div v-for="(q, qIdx) in viewingResultQuiz.questions" :key="qIdx" class="review-q-item">
        <div class="q-review-header">
          <h4>Câu {{ qIdx + 1 }}: {{ q.question }}</h4>
        </div>
        <div class="options-review-list">
          <div 
            v-for="(opt, oIdx) in q.options" 
            :key="oIdx"
            class="review-opt-item"
            :class="{
              correct: opt === q.correctAnswer,
              wrong: viewingResultQuiz.userAnswers && viewingResultQuiz.userAnswers[q.id] === opt && opt !== q.correctAnswer
            }"
          >
            <span>{{ opt }}</span>
            <span v-if="opt === q.correctAnswer" class="tag-correct">✓ Đáp án đúng</span>
            <span v-else-if="viewingResultQuiz.userAnswers && viewingResultQuiz.userAnswers[q.id] === opt" class="tag-wrong">✕ Đã chọn</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppIcon from './icons/AppIcon.vue'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const emit = defineEmits(['submit-quiz'])

const activeTab = ref('reading')
const isCreatingQuiz = ref(false)
const creatorQuizType = ref('reading')
const newQuizTitle = ref('')
const newQuizQuestions = ref([])

const activeQuiz = ref(null)
const currentQuestionIndex = ref(0)
const userAnswers = ref({})
const viewingResultQuiz = ref(null)

// Practice Quizzes array stored locally
const practiceQuizzes = ref([
  {
    id: "practice-seed-reading",
    title: "Đề tự luyện Đọc: Từ vựng Động từ cơ bản 📖",
    quizType: "reading",
    status: "not_started",
    points: 10,
    score: null,
    totalScore: 10,
    timeLimit: 10,
    completedAt: null,
    questions: [
      {
        id: "pq-reading-1",
        type: "choice",
        question: "Từ '일어나다' trong tiếng Hàn có nghĩa là gì?",
        wordType: "Động từ",
        pronunciation: "i-reo-na-da",
        options: ["A. Đi làm", "B. Thức dậy", "C. Ăn cơm", "D. Thể dục"],
        correctAnswer: "B. Thức dậy"
      },
      {
        id: "pq-reading-2",
        type: "choice",
        question: "Từ nào có nghĩa là 'Học tập'?",
        wordType: "Động từ",
        pronunciation: "gong-bu-ha-da",
        options: ["A. 공부하다", "B. 일하다", "C. 자다", "D. 쉬다"],
        correctAnswer: "A. 공부하다"
      }
    ]
  }
])

onMounted(() => {
  const saved = localStorage.getItem('sk_practice_quizzes')
  if (saved) {
    try {
      practiceQuizzes.value = JSON.parse(saved)
    } catch (e) {
      console.warn("Could not parse saved practice quizzes")
    }
  }
})

const readingQuizzes = computed(() => {
  return practiceQuizzes.value.filter(q => q.quizType === 'reading' && q.status !== 'completed')
})

const listeningQuizzes = computed(() => {
  return practiceQuizzes.value.filter(q => q.quizType === 'listening' && q.status !== 'completed')
})

const completedQuizzes = computed(() => {
  return practiceQuizzes.value.filter(q => q.score !== null && q.score !== undefined)
})

const currentQuestion = computed(() => {
  if (!activeQuiz.value || !activeQuiz.value.questions) return {}
  return activeQuiz.value.questions[currentQuestionIndex.value] || {}
})

// Student Import modal state
const showStudentImportModal = ref(false)
const studentImportTab = ref('paste')
const studentImportText = ref('')
const studentImportFileName = ref('')

const openStudentImportModal = () => {
  studentImportText.value = ''
  studentImportFileName.value = ''
  studentImportTab.value = 'paste'
  showStudentImportModal.value = true
}

const handleStudentFileSelected = (e) => {
  const file = e.target.files[0]
  if (!file) return
  studentImportFileName.value = file.name
  const reader = new FileReader()
  reader.onload = (event) => {
    studentImportText.value = event.target.result
  }
  reader.readAsText(file)
}

const confirmStudentImport = () => {
  if (!studentImportText.value.trim()) {
    toast.warning("Vui lòng dán văn bản hoặc chọn file để import.")
    return
  }
  const parsed = parseTextToQuizQuestions(studentImportText.value)
  if (parsed.length === 0) {
    toast.error("Không thể phân tích dữ liệu từ vựng. Vui lòng kiểm tra định dạng!")
    return
  }
  
  if (newQuizQuestions.value.length === 1 && !newQuizQuestions.value[0].question.trim() && !newQuizQuestions.value[0].koreanText.trim()) {
    newQuizQuestions.value = parsed
  } else {
    newQuizQuestions.value.push(...parsed)
  }
  
  toast.success(`Import thành công ${parsed.length} câu hỏi/từ vựng!`)
  showStudentImportModal.value = false
}

const parseTextToQuizQuestions = (rawText) => {
  if (!rawText) return []
  const lines = rawText.split('\n').map(l => l.trim()).filter(l => l.length > 0)
  const items = []
  
  for (const line of lines) {
    let parts = []
    if (line.includes('|')) parts = line.split('|')
    else if (line.includes('\t')) parts = line.split('\t')
    else if (line.includes(';')) parts = line.split(';')
    else if (line.includes(',')) parts = line.split(',')
    else parts = line.split('-')
    
    parts = parts.map(p => p.trim())
    if (parts.length < 2) continue
    
    const korText = parts[0]
    let wordType = 'Động từ'
    let pron = ''
    let meaning = ''
    
    if (parts.length >= 4) {
      wordType = parts[1] || 'Động từ'
      pron = parts[2] || ''
      meaning = parts[3]
    } else if (parts.length === 3) {
      if (['Động từ', 'Tính từ', 'Danh từ', 'Trạng từ', 'Cụm từ', 'Khác'].some(wt => parts[1].toLowerCase().includes(wt.toLowerCase()))) {
        wordType = parts[1]
        meaning = parts[2]
      } else {
        pron = parts[1]
        meaning = parts[2]
      }
    } else {
      meaning = parts[1]
    }
    
    const opts = [
      meaning,
      meaning + ' (khác 1)',
      meaning + ' (khác 2)',
      meaning + ' (khác 3)'
    ]
    
    items.push({
      type: creatorQuizType.value === 'listening' ? 'listening' : 'choice',
      audioSource: 'tts',
      koreanText: korText,
      audioUrl: '',
      question: `Từ "${korText}" có nghĩa là gì?`,
      wordType: wordType,
      pronunciation: pron,
      options: opts,
      correctOptionIndex: 0
    })
  }
  return items
}

const editingQuizId = ref(null)

const openCreator = (type) => {
  editingQuizId.value = null
  isCreatingQuiz.value = true
  creatorQuizType.value = type
  newQuizTitle.value = type === 'listening' ? 'Bài ôn tập Nghe tiếng Hàn (Tự luyện)' : 'Đề trắc nghiệm đọc từ vựng tự luyện'
  newQuizQuestions.value = [
    { 
      type: type === 'listening' ? 'listening' : 'choice', 
      question: type === 'listening' ? 'Lắng nghe đoạn audio tiếng Hàn bên dưới và chọn đáp án chính xác:' : '', 
      koreanText: '', 
      audioSource: 'tts', 
      audioUrl: '', 
      wordType: 'Động từ',
      pronunciation: '',
      options: ['', '', '', ''], 
      correctOptionIndex: 0 
    }
  ]
}

const editPracticeQuiz = (quiz) => {
  if (!quiz) return
  editingQuizId.value = quiz.id
  creatorQuizType.value = quiz.quizType || 'reading'
  newQuizTitle.value = quiz.title || ''
  
  if (quiz.questions && Array.isArray(quiz.questions) && quiz.questions.length > 0) {
    newQuizQuestions.value = quiz.questions.map(q => {
      const rawOpts = (q.options || ['', '', '', '']).map(opt => {
        if (!opt) return ''
        return opt.replace(/^[A-D]\.\s*/, '')
      })
      
      let correctIdx = 0
      if (q.correctAnswer && q.options) {
        const found = q.options.findIndex(opt => opt === q.correctAnswer)
        if (found !== -1) correctIdx = found
      }
      
      return {
        type: q.type || (quiz.quizType === 'listening' ? 'listening' : 'choice'),
        question: q.question || '',
        koreanText: q.koreanText || '',
        audioSource: q.audioUrl ? 'file' : 'tts',
        audioUrl: q.audioUrl || '',
        wordType: q.wordType || 'Động từ',
        pronunciation: q.pronunciation || '',
        options: rawOpts,
        correctOptionIndex: correctIdx
      }
    })
  } else {
    newQuizQuestions.value = [
      {
        type: creatorQuizType.value === 'listening' ? 'listening' : 'choice',
        question: '',
        koreanText: '',
        audioSource: 'tts',
        audioUrl: '',
        wordType: 'Động từ',
        pronunciation: '',
        options: ['', '', '', ''],
        correctOptionIndex: 0
      }
    ]
  }
  
  isCreatingQuiz.value = true
}

const addQuestionToBuilder = () => {
  const type = creatorQuizType.value
  newQuizQuestions.value.push({
    type: type === 'listening' ? 'listening' : 'choice',
    question: type === 'listening' ? 'Lắng nghe đoạn audio tiếng Hàn bên dưới và chọn đáp án chính xác:' : '',
    koreanText: '',
    audioSource: 'tts',
    audioUrl: '',
    wordType: 'Động từ',
    pronunciation: '',
    options: ['', '', '', ''],
    correctOptionIndex: 0
  })
}

const removeQuestionFromBuilder = (idx) => {
  newQuizQuestions.value.splice(idx, 1)
}

const saveCustomQuiz = () => {
  if (!newQuizTitle.value.trim()) {
    toast.warning("Vui lòng nhập tên đề thi ôn tập.")
    return
  }

  const formattedQuestions = newQuizQuestions.value.map((q, idx) => {
    const opts = q.options.map((opt, optIdx) => `${String.fromCharCode(65 + optIdx)}. ${opt.trim()}`)
    const correctVal = opts[q.correctOptionIndex]
    
    return {
      id: `pq-${idx}-${Date.now()}`,
      type: q.type,
      question: q.question.trim(),
      wordType: q.wordType || 'Động từ',
      pronunciation: q.pronunciation ? q.pronunciation.trim() : '',
      koreanText: q.audioSource === 'tts' ? (q.koreanText ? q.koreanText.trim() : null) : null,
      audioUrl: q.audioSource === 'file' ? (q.audioUrl ? q.audioUrl.trim() : null) : null,
      options: opts,
      correctAnswer: correctVal
    }
  })

  if (editingQuizId.value) {
    const idx = practiceQuizzes.value.findIndex(pq => pq.id === editingQuizId.value)
    if (idx !== -1) {
      practiceQuizzes.value[idx].title = newQuizTitle.value.trim()
      practiceQuizzes.value[idx].quizType = creatorQuizType.value
      practiceQuizzes.value[idx].questions = formattedQuestions
      practiceQuizzes.value[idx].totalScore = formattedQuestions.length * 10
    }
    toast.success("Đã cập nhật bộ từ vựng thành công!")
    editingQuizId.value = null
  } else {
    const newQuiz = {
      id: `practice-${Date.now()}`,
      title: newQuizTitle.value.trim(),
      quizType: creatorQuizType.value,
      status: 'not_started',
      score: null,
      totalScore: formattedQuestions.length * 10,
      timeLimit: 10,
      completedAt: null,
      questions: formattedQuestions
    }

    practiceQuizzes.value.unshift(newQuiz)
    toast.success("Đã lưu bộ từ vựng mới thành công!")
  }

  localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
  isCreatingQuiz.value = false
}

const startQuiz = (quiz) => {
  activeQuiz.value = quiz
  currentQuestionIndex.value = 0
  userAnswers.value = {}
}

const selectAnswer = (qId, optionVal) => {
  userAnswers.value[qId] = optionVal
}

const submitQuiz = () => {
  if (!activeQuiz.value) return
  let correctCount = 0
  const questionsList = activeQuiz.value.questions || []
  
  questionsList.forEach(q => {
    if (userAnswers.value[q.id] === q.correctAnswer) {
      correctCount++
    }
  })

  const totalQuestions = questionsList.length || 1
  const score = Math.round((correctCount / totalQuestions) * 10 * 10) / 10
  const compAt = new Date().toISOString()

  const idx = practiceQuizzes.value.findIndex(pq => pq.id === activeQuiz.value.id)
  if (idx !== -1) {
    practiceQuizzes.value[idx].status = 'completed'
    practiceQuizzes.value[idx].score = score
    practiceQuizzes.value[idx].userAnswers = userAnswers.value
    practiceQuizzes.value[idx].completedAt = compAt
    localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
  }

  toast.success(`Đã hoàn thành bài ôn tập! Số điểm: ${score}/${activeQuiz.value.totalScore || 10}`)
  activeQuiz.value = null
  activeTab.value = 'completed'
}

const deletePracticeQuiz = (id) => {
  if (confirm("Bạn có chắc chắn muốn xóa bài ôn tập này?")) {
    practiceQuizzes.value = practiceQuizzes.value.filter(q => q.id !== id)
    localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
    toast.success("Đã xóa bài ôn tập!")
  }
}

const confirmExitQuiz = () => {
  if (confirm("Bạn có chắc muốn thoát bài làm ôn tập?")) {
    activeQuiz.value = null
  }
}

const viewResults = (quiz) => {
  viewingResultQuiz.value = quiz
}

const playTTS = (text) => {
  if (!text) return
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'ko-KR'
  window.speechSynthesis.speak(utterance)
}

const formatDate = (d) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('vi-VN')
}
</script>

<style scoped>
.practice-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.practice-header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1.25rem;
}

.header-left h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.header-left p {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.practice-status-tabs {
  display: flex;
  gap: 0.5rem;
  background-color: var(--bg-card);
  padding: 0.35rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.tab-btn {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
  background: none;
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn.active {
  background-color: var(--primary-light);
  color: var(--primary);
  font-weight: 700;
}

.quizzes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.25rem;
}

.quiz-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.quiz-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.create-trigger-card {
  border: 2px dashed var(--primary);
  background-color: var(--primary-glow);
  cursor: pointer;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 2rem 1.5rem;
}

.trigger-plus-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.trigger-content h4 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 0.25rem;
}

.trigger-content p {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.quiz-badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.badge {
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.type-badge.reading {
  background-color: #eff6ff;
  color: #3b82f6;
}

.type-badge.listening {
  background-color: #f0fdf4;
  color: #16a34a;
}

.score-badge {
  background-color: var(--bg-body);
  color: var(--text-body);
  border: 1px solid var(--border-color);
}

.summary-q {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.quiz-action-bar {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.start-btn {
  flex: 1;
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.65rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
}

.start-btn:hover {
  opacity: 0.9;
}

.delete-practice-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--danger);
  padding: 0.65rem;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.edit-practice-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--primary);
  padding: 0.65rem;
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
}

.edit-practice-btn:hover {
  background-color: var(--primary-light);
  border-color: var(--primary);
}

/* CREATOR FORM DESIGN */
.quiz-creator-container {
  width: 100%;
  margin: 0 auto;
}

.creator-header {
  margin-bottom: 1.5rem;
}

.creator-header h3 {
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.creator-header p {
  font-size: 0.9rem;
  color: var(--text-muted);
}

.creator-body-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 1.75rem;
  align-items: start;
  width: 100%;
}

@media (max-width: 1024px) {
  .creator-body-grid {
    grid-template-columns: 1fr;
  }
}

.creator-form-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow-md);
}

.questions-builder-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin: 1.5rem 0;
}

.question-builder-item {
  padding: 1.25rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--bg-card);
  position: relative;
}

.builder-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.5rem;
}

.builder-item-header h4 {
  font-weight: 700;
  color: var(--primary);
  margin: 0;
}

.remove-q-btn {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--danger);
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
}

.remove-q-btn:hover {
  opacity: 0.8;
}

.options-builder-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-top: 1rem;
}

.option-builder-row {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.opt-label-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-title);
}

.creator-input-field-opt {
  width: 100%;
  padding: 0.6rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-body);
}

.creator-input-field-opt:focus {
  border-color: var(--primary);
  background-color: var(--bg-card);
}

.correct-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 0.75rem;
}

.created-items-panel {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 1.75rem;
  box-shadow: var(--shadow-sm);
  width: 100%;
}

.panel-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.85rem;
}

.panel-header-row h4 {
  font-size: 1.2rem;
  font-weight: 800;
  color: var(--text-title);
  margin: 0;
}

.count-pill {
  background-color: var(--primary-light, #eff6ff);
  color: var(--primary);
  font-weight: 700;
  font-size: 0.85rem;
  padding: 0.3rem 0.75rem;
  border-radius: var(--radius-sm);
}

.created-items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.created-items-table th {
  text-align: left;
  padding: 0.75rem 0.6rem;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-muted);
  border-bottom: 1px solid var(--border-color);
}

.created-items-table td {
  padding: 0.9rem 0.6rem;
  border-bottom: 1px solid var(--border-color);
  vertical-align: middle;
}

.q-cell {
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
}

.q-title {
  color: var(--text-title);
  font-weight: 700;
  font-size: 0.9rem;
}

.q-pron {
  color: var(--text-muted);
  font-size: 0.8rem;
}

.type-pill {
  display: inline-block;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--text-body);
  font-size: 0.8rem;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-sm);
}

.correct-badge-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  border: 1px solid var(--success, #10b981);
  color: var(--success, #10b981);
  background-color: rgba(16, 185, 129, 0.08);
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm);
}

.delete-mini-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 1.25rem;
  cursor: pointer;
}

.delete-mini-btn:hover {
  color: var(--danger);
}

.creator-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.add-q-btn {
  background-color: var(--primary-light);
  color: var(--primary);
  border: 1px solid var(--primary-glow);
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.import-q-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--text-title);
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.save-quiz-btn {
  background-color: var(--success);
  color: #fff;
  border: none;
  padding: 0.65rem 1.5rem;
  border-radius: var(--radius-md);
  font-weight: 700;
  cursor: pointer;
  margin-left: auto;
}

/* Modal Overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  padding: 1.5rem;
}

.modal-content {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.15);
  width: 100%;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.cancel-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.6rem 1.2rem;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.submit-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: var(--radius-md);
  cursor: pointer;
}

/* Active Quiz Run */
.active-quiz-container {
  max-width: 800px;
  margin: 0 auto;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2rem;
}

.quiz-run-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
}

.exit-quiz-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--danger);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.options-choice-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.option-choice-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--bg-body);
  cursor: pointer;
  text-align: left;
}

.option-choice-btn.selected {
  border-color: var(--primary);
  background-color: var(--primary-light);
}

.opt-key {
  font-weight: 700;
  color: var(--primary);
}

.quiz-nav-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
}

.nav-step-btn {
  padding: 0.65rem 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  cursor: pointer;
}

.nav-step-btn.primary {
  background-color: var(--primary);
  color: #fff;
  border: none;
}

.submit-quiz-btn {
  background-color: var(--success);
  color: #fff;
  border: none;
  padding: 0.65rem 1.5rem;
  border-radius: var(--radius-md);
  font-weight: 700;
  cursor: pointer;
}

/* Results Review */
.results-review-container {
  max-width: 800px;
  margin: 0 auto;
}

.result-score-banner {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-top: 1rem;
  margin-bottom: 2rem;
}

.score-radial {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background-color: var(--primary-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-num {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--primary);
}

.total-num {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.review-opt-item {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  margin-top: 0.5rem;
  display: flex;
  justify-content: space-between;
}

.review-opt-item.correct {
  border-color: var(--success);
  background-color: rgba(16, 185, 129, 0.08);
}

.review-opt-item.wrong {
  border-color: var(--danger);
  background-color: rgba(239, 68, 68, 0.08);
}

.creator-input-field {
  width: 100%;
  padding: 0.65rem 0.85rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--bg-body);
  color: var(--text-body);
}

.creator-input-field-opt {
  flex: 1;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--bg-body);
}

.form-group {
  margin-bottom: 1rem;
}

.hidden-file-input {
  display: none;
}

.back-link-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 1rem;
}
</style>
