<template>
  <div class="quiz-container animate-fade">
    <!-- Header -->
    <div class="quiz-header-section" v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz">
      <div>
        <h2>Bài Tập & Kiểm Tra 📝</h2>
        <p>Danh mục bài tập được phân loại theo kỹ năng giúp bạn dễ dàng ôn tập và học tập tốt hơn.</p>
      </div>
      
      <div class="quiz-status-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'reading' }"
          @click="activeTab = 'reading'"
        >
          📖 Đọc & Từ Vựng ({{ readingQuizzesCount }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'listening' }"
          @click="activeTab = 'listening'"
        >
          🎧 Kỹ Năng Nghe ({{ listeningQuizzesCount }})
        </button>
        <button 
          class="tab-btn completed-tab" 
          :class="{ active: activeTab === 'completed' }"
          @click="activeTab = 'completed'"
        >
          ✅ Lịch Sử Làm Bài ({{ completedQuizzes.length }})
        </button>
      </div>
    </div>

    <!-- LIST OF QUIZZES -->
    <div v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz" class="quizzes-grid animate-scale">
      <!-- 1. Reading & Vocab Tab -->
      <template v-if="activeTab === 'reading'">
        <!-- Create Shortcut Box inside Reading tab -->
        <div class="quiz-card create-trigger-card reading-btn" @click="openCreator('reading')">
          <div class="trigger-content">
            <div class="trigger-plus-icon">📖</div>
            <h4>Tạo Đề Đọc/Từ Vựng</h4>
            <p>Tự thiết kế đề thi trắc nghiệm đọc hiểu, từ vựng và ngữ pháp tiếng Hàn.</p>
          </div>
        </div>

        <!-- Quizzes under Reading category -->
        <div 
          v-for="quiz in readingQuizzes" 
          :key="quiz.id" 
          class="quiz-card pending"
        >
          <div class="quiz-badge-row">
            <span class="badge type-badge reading">📖 Đọc & Từ Vựng</span>
            <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit || 10 }} Phút</span>
            <span v-if="quiz.score !== null && quiz.score !== undefined" class="badge score-badge practice-score">Điểm gần nhất: {{ quiz.score }} / 10</span>
          </div>
          <h3>{{ quiz.title || 'Đề ôn tập đọc' }}</h3>
          <p class="due-date" v-if="quiz.dueDate && quiz.status !== 'completed'">Hạn nộp: {{ formatDate(quiz.dueDate) }}</p>
          <p class="summary-q" v-else>{{ quiz.questions ? quiz.questions.length : 0 }} câu hỏi ôn tập</p>

          <div class="quiz-action-bar">
            <button class="start-btn" @click="startQuiz(quiz)">
              <AppIcon name="play" size="16" /> {{ (quiz.score !== null && quiz.score !== undefined) ? 'Làm lại ôn tập' : 'Làm bài ngay' }}
            </button>
            <button v-if="quiz.id && quiz.id.toString().startsWith('practice-')" class="delete-practice-btn" @click.stop="deletePracticeQuiz(quiz.id)" title="Xóa đề ôn tập này">
              <AppIcon name="x" size="16" />
            </button>
          </div>
        </div>

        <div v-if="readingQuizzes.length === 0" class="empty-state">
          <p>Không có bài tập đọc nào.</p>
        </div>
      </template>

      <!-- 2. Listening Skills Tab -->
      <template v-if="activeTab === 'listening'">
        <!-- Create Shortcut Box inside Listening tab -->
        <div class="quiz-card create-trigger-card listening-btn" @click="openCreator('listening')">
          <div class="trigger-content">
            <div class="trigger-plus-icon">🎧</div>
            <h4>Tạo Bài Thi Nghe</h4>
            <p>Soạn bài thi nghe có chèn audio của riêng bạn hoặc phát giọng AI và chọn đáp án ABCD.</p>
          </div>
        </div>

        <!-- Quizzes under Listening category -->
        <div 
          v-for="quiz in listeningQuizzes" 
          :key="quiz.id" 
          class="quiz-card pending"
        >
          <div class="quiz-badge-row">
            <span class="badge type-badge listening">🎧 Kỹ Năng Nghe</span>
            <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit || 10 }} Phút</span>
            <span v-if="quiz.score !== null && quiz.score !== undefined" class="badge score-badge practice-score">Điểm gần nhất: {{ quiz.score }} / 10</span>
          </div>
          <h3>{{ quiz.title || 'Đề ôn tập nghe' }}</h3>
          <p class="due-date" v-if="quiz.dueDate && quiz.status !== 'completed'">Hạn nộp: {{ formatDate(quiz.dueDate) }}</p>
          <p class="summary-q" v-else>{{ quiz.questions ? quiz.questions.length : 0 }} câu hỏi luyện nghe</p>

          <div class="quiz-action-bar">
            <button class="start-btn" @click="startQuiz(quiz)">
              <AppIcon name="play" size="16" /> {{ (quiz.score !== null && quiz.score !== undefined) ? 'Làm lại đề nghe' : 'Làm bài ngay' }}
            </button>
            <button v-if="quiz.id && quiz.id.toString().startsWith('practice-')" class="delete-practice-btn" @click.stop="deletePracticeQuiz(quiz.id)" title="Xóa đề ôn tập này">
              <AppIcon name="x" size="16" />
            </button>
          </div>
        </div>

        <div v-if="listeningQuizzes.length === 0" class="empty-state">
          <p>Không có bài tập nghe nào.</p>
        </div>
      </template>

      <!-- 3. Completed History Tab -->
      <template v-if="activeTab === 'completed'">
        <div 
          v-for="quiz in completedQuizzes" 
          :key="quiz.id" 
          class="quiz-card completed"
        >
          <div class="quiz-badge-row">
            <span class="badge type-badge" :class="quiz.quizType === 'listening' ? 'listening' : 'reading'">
              {{ quiz.quizType === 'listening' ? '🎧 Bài Nghe' : '📖 Bài Đọc' }}
            </span>
            <span class="badge score-badge">Điểm: {{ quiz.score }} / 10</span>
            <span class="badge date-completed" v-if="quiz.completedAt"><AppIcon name="check" size="14" /> {{ formatDateShort(quiz.completedAt) }}</span>
          </div>
          <h3>{{ quiz.title || 'Đề hoàn thành' }}</h3>
          <p class="summary-q">{{ quiz.questions ? quiz.questions.length : 0 }} câu hỏi đã nộp</p>

          <div class="quiz-action-bar">
            <button class="review-btn" @click="viewResults(quiz)">
              <AppIcon name="quiz" size="16" /> Xem chi tiết bài làm
            </button>
          </div>
        </div>

        <div v-if="completedQuizzes.length === 0" class="empty-state">
          <AppIcon name="alert" size="32" />
          <p>Bạn chưa hoàn thành bài tập nào.</p>
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
        <p>
          {{ creatorQuizType === 'listening' 
            ? 'Cấu trúc bài nghe bao gồm đoạn audio tự chọn (tải lên file .mp3 hoặc dán link) và 4 đáp án trắc nghiệm A B C D.' 
            : 'Cấu trúc trắc nghiệm chữ kiểm tra mặt chữ, ngữ pháp và nghĩa từ vựng.' }}
        </p>
      </div>

      <div class="creator-form-card">
        <div class="form-group">
          <label for="new-quiz-title" class="label-bold">Tên đề thi tự luyện</label>
          <input 
            type="text" 
            id="new-quiz-title" 
            v-model="newQuizTitle" 
            placeholder="Ví dụ: Đề thi thử nghe tiếng Hàn sơ cấp"
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

            <!-- Custom Audio selection inside Listening Quiz Creator -->
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

              <!-- Option 1: AI voice text -->
              <div class="form-group animate-fade" v-if="q.audioSource === 'tts'" style="margin-bottom: 1rem;">
                <label>Từ hoặc câu tiếng Hàn để AI phát âm</label>
                <input 
                  type="text" 
                  v-model="q.koreanText" 
                  placeholder="Ví dụ: 안녕하세요 hoặc 봄"
                  class="creator-input-field"
                >
              </div>

              <!-- Option 2: MP3 file / Link url -->
              <div class="form-group animate-fade" v-else style="margin-bottom: 1rem;">
                <label>Liên kết âm thanh (.mp3) hoặc chọn file từ máy tính</label>
                <input 
                  type="text" 
                  v-model="q.audioUrl" 
                  placeholder="Dán link audio: https://example.com/audio.mp3"
                  class="creator-input-field"
                  style="margin-bottom: 0.5rem;"
                >
                <div class="file-uploader-box">
                  <input 
                    type="file" 
                    accept="audio/*" 
                    @change="handleAudioUpload($event, qIdx)"
                    :id="'file-upload-' + qIdx"
                    class="hidden-file-input"
                  >
                  <label :for="'file-upload-' + qIdx" class="file-upload-btn">
                    📁 {{ q.audioUrl ? 'Chọn file khác...' : 'Tải file MP3 từ thiết bị...' }}
                  </label>
                  <span class="file-name-hint" v-if="q.audioUrl && q.audioUrl.toString().startsWith('data:')">
                    ⚡ Đã đính kèm file âm thanh gốc (dạng offline)
                  </span>
                </div>
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
          
          <button class="save-quiz-btn" @click="saveCustomQuiz">
            Lưu đề thi
          </button>
        </div>
      </div>
    </div>

    <!-- ACTIVE QUIZ SCREEN -->
    <div v-else-if="activeQuiz" class="active-quiz-container animate-scale">
      <div class="quiz-run-header">
        <div class="quiz-title-info">
          <h3>{{ activeQuiz.title || 'Đang làm bài' }}</h3>
          <span class="question-tracker">Câu hỏi {{ currentQuestionIndex + 1 }} / {{ activeQuiz.questions ? activeQuiz.questions.length : 0 }}</span>
        </div>
        
        <div class="timer-box" :class="{ warning: timerMinutes < 2 }">
          <AppIcon name="clock" size="18" />
          <span>{{ padZero(timerMinutes) }}:{{ padZero(timerSeconds) }}</span>
        </div>
      </div>

      <!-- Question Progress Bar -->
      <div class="run-progress-bar">
        <div class="fill" :style="{ width: (((currentQuestionIndex + 1) / (activeQuiz.questions ? activeQuiz.questions.length : 1)) * 100) + '%' }"></div>
      </div>

      <!-- Question Box -->
      <div class="question-view-card">
        <p class="question-text">{{ currentQuestionIndex + 1 }}. {{ currentQuestion.question || '' }}</p>

        <!-- Standard Option Choice Question -->
        <div v-if="currentQuestion.type === 'choice' || currentQuestion.type === 'match'" class="options-grid">
          <button 
            v-for="opt in (currentQuestion.options || [])" 
            :key="opt"
            class="option-row"
            :class="{ selected: userAnswers[currentQuestion.id] === opt }"
            @click="selectOption(opt)"
          >
            <div class="radio-indicator">
              <span v-if="userAnswers[currentQuestion.id] === opt" class="dot"></span>
            </div>
            <span class="option-text">{{ opt }}</span>
          </button>
        </div>

        <!-- Listening (Audio Player + Option Choice) Question -->
        <div v-else-if="currentQuestion.type === 'listening'" class="listening-block">
          <div class="audio-player-card">
            <!-- Custom MP3 / URL playback -->
            <button 
              v-if="currentQuestion.audioUrl"
              class="audio-play-btn" 
              @click="playCustomAudio(currentQuestion.audioUrl)"
              title="Phát file âm thanh đính kèm"
            >
              <AppIcon name="play" size="20" />
              <span>Phát Audio Đính Kèm 🎧</span>
            </button>
            
            <!-- Fallback to TTS -->
            <button 
              v-else
              class="audio-play-btn" 
              @click="playSpeech(currentQuestion.koreanText)"
              title="Phát giọng đọc mẫu bằng AI"
            >
              <AppIcon name="play" size="20" />
              <span>Phát Giọng AI (TTS) 🔊</span>
            </button>
            
            <div class="audio-visualizer" :class="{ playing: isSpeaking && (playingKoreanText === currentQuestion.koreanText || playingKoreanText === currentQuestion.audioUrl) }">
              <span class="bar bar-1"></span>
              <span class="bar bar-2"></span>
              <span class="bar bar-3"></span>
              <span class="bar bar-4"></span>
              <span class="bar bar-5"></span>
            </div>
          </div>

          <div class="options-grid">
            <button 
              v-for="opt in (currentQuestion.options || [])" 
              :key="opt"
              class="option-row"
              :class="{ selected: userAnswers[currentQuestion.id] === opt }"
              @click="selectOption(opt)"
            >
              <div class="radio-indicator">
                <span v-if="userAnswers[currentQuestion.id] === opt" class="dot"></span>
              </div>
              <span class="option-text">{{ opt }}</span>
            </button>
          </div>
        </div>

        <!-- Text Fill Question -->
        <div v-else-if="currentQuestion.type === 'fill'" class="fill-input-box">
          <input 
            type="text" 
            v-model="userAnswers[currentQuestion.id]" 
            placeholder="Nhập câu trả lời của bạn..."
            class="fill-text-field"
          >
          <p class="input-hint">* Điền từ hoặc cụm từ tiếng Hàn chính xác.</p>
        </div>
      </div>

      <!-- Footer Buttons -->
      <div class="quiz-run-footer">
        <button 
          class="nav-btn-run text" 
          :disabled="currentQuestionIndex === 0"
          @click="prevQuestion"
        >
          Quay lại
        </button>

        <button 
          v-if="currentQuestionIndex < (activeQuiz.questions ? activeQuiz.questions.length - 1 : 0)"
          class="nav-btn-run primary" 
          @click="nextQuestion"
        >
          Câu tiếp theo
        </button>

        <button 
          v-else
          class="nav-btn-run submit" 
          @click="submitQuiz"
        >
          Nộp Bài
        </button>
      </div>
    </div>

    <!-- DETAILED QUIZ RESULTS REVIEW VIEW -->
    <div v-else-if="viewingResultQuiz" class="results-review-container animate-scale">
      <button class="back-link-btn" @click="viewingResultQuiz = null">
        <AppIcon name="chevron-left" size="16" /> Quay lại danh sách bài tập
      </button>

      <div class="result-score-banner">
        <div class="score-radial">
          <span class="score-num">{{ viewingResultQuiz.score || 0 }}</span>
          <span class="total-num">/ 10</span>
        </div>
        <div class="score-text-details">
          <h3>Kết quả: {{ viewingResultQuiz.title || 'Đề ôn tập' }}</h3>
          <p>Nộp ngày: {{ formatDate(viewingResultQuiz.completedAt) }}</p>
          <div class="points-earned">
            <AppIcon name="award" class="gold-award" size="20" />
            <span>Tích lũy: <strong>+{{ Math.round((viewingResultQuiz.score || 0) * 10) }} XP</strong></span>
          </div>
        </div>
      </div>

      <h3 class="review-heading">Xem lại chi tiết câu hỏi</h3>

      <div class="questions-review-list">
        <div 
          v-for="(q, idx) in (viewingResultQuiz.questions || [])" 
          :key="q.id" 
          class="review-question-card"
          :class="isUserCorrect(q) ? 'correct' : 'incorrect'"
        >
          <div class="review-q-header">
            <span class="q-index">Câu {{ idx + 1 }}</span>
            <span class="correct-badge" :class="isUserCorrect(q) ? 'correct' : 'incorrect'">
              <AppIcon :name="isUserCorrect(q) ? 'check' : 'x'" size="14" />
              {{ isUserCorrect(q) ? 'Đúng' : 'Sai' }}
            </span>
          </div>

          <p class="q-text">{{ q.question || '' }}</p>

          <!-- Replay buttons in Review for Listening -->
          <div class="review-listening-replay" v-if="q.type === 'listening'">
            <button v-if="q.audioUrl" class="replay-btn" @click="playCustomAudio(q.audioUrl)">
              <AppIcon name="play" size="14" /> Phát lại Audio 🎧
            </button>
            <button v-else class="replay-btn" @click="playSpeech(q.koreanText)">
              <AppIcon name="play" size="14" /> Phát lại giọng đọc AI: <strong>{{ q.koreanText }}</strong>
            </button>
          </div>

          <div class="review-answers-grid">
            <div class="ans-row">
              <span class="label">Câu trả lời của bạn:</span>
              <span class="ans-val student-ans" :class="{ err: !isUserCorrect(q) }">
                {{ viewingResultQuiz.userAnswers ? (viewingResultQuiz.userAnswers[q.id] || '(Không trả lời)') : '(Không trả lời)' }}
              </span>
            </div>
            
            <div class="ans-row" v-if="!isUserCorrect(q)">
              <span class="label">Đáp án đúng:</span>
              <span class="ans-val correct-ans">{{ q.correctAnswer || '' }}</span>
            </div>
          </div>

          <!-- Explanation box -->
          <div class="explanation-box" v-if="q.explanation">
            <span class="label"><AppIcon name="alert" size="14" /> Hướng dẫn giải:</span>
            <p>{{ q.explanation }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, onMounted } from 'vue'
import AppIcon from './icons/AppIcon.vue'

const props = defineProps({
  quizzes: {
    type: Array,
    required: true,
    default: () => []
  }
})

const emit = defineEmits(['submit-quiz'])

const activeTab = ref('reading') // reading, listening, completed

// Practice / Custom Quiz States
const practiceQuizzes = ref([
  {
    id: "practice-seed-reading",
    title: "Đề tự luyện Đọc: Từ vựng Động từ cơ bản 📖",
    quizType: "reading",
    status: "not_started",
    points: 10,
    score: null,
    timeLimit: 10,
    completedAt: null,
    questions: [
      {
        id: "pq-reading-1",
        type: "choice",
        question: "Từ '일어나다' trong tiếng Hàn có nghĩa là gì?",
        options: ["A. Đi làm", "B. Thức dậy", "C. Ăn cơm", "D. Thể dục"],
        correctAnswer: "B. Thức dậy",
        explanation: "일어나다 nghĩa là Thức dậy."
      },
      {
        id: "pq-reading-2",
        type: "choice",
        question: "Từ nào có nghĩa là 'Học tập'?",
        options: ["A. 운동하다", "B. 공부하다", "C. 세수하다", "D. 일하다"],
        correctAnswer: "B. 공부하다",
        explanation: "공부하다 nghĩa là Học tập."
      }
    ]
  },
  {
    id: "practice-seed-listening",
    title: "Đề thi thử Nghe: TOEIC Hàn ngữ Part 1 🎧",
    quizType: "listening",
    status: "not_started",
    points: 10,
    score: null,
    timeLimit: 10,
    completedAt: null,
    questions: [
      {
        id: "pq-listening-1",
        type: "listening",
        question: "Hãy nghe đoạn âm thanh tiếng Hàn sau và chọn ý nghĩa chính xác:",
        koreanText: "오늘 날씨가 아주 따뜻합니다",
        audioSource: "tts",
        audioUrl: "",
        options: ["A. Thời tiết hôm nay rất lạnh.", "B. Thời tiết hôm nay rất ấm áp.", "C. Thời tiết hôm nay mát mẻ.", "D. Hôm nay trời mưa lớn."],
        correctAnswer: "B. Thời tiết hôm nay rất ấm áp.",
        explanation: "Âm thanh tiếng Hàn nói: '오늘 날씨가 아주 따뜻합니다' (Thời tiết hôm nay rất ấm áp)."
      },
      {
        id: "pq-listening-2",
        type: "listening",
        question: "Hãy nghe phát âm và chọn động từ mô tả hành động tương ứng:",
        koreanText: "세수하다",
        audioSource: "tts",
        audioUrl: "",
        options: ["A. Rửa mặt", "B. Thức dậy", "C. Đi ngủ", "D. Ăn cơm"],
        correctAnswer: "A. Rửa mặt",
        explanation: "Từ nghe được là '세수하다', nghĩa là Rửa mặt."
      }
    ]
  }
])
const isCreatingQuiz = ref(false)
const creatorQuizType = ref('reading') // reading or listening
const newQuizTitle = ref('')
const newQuizQuestions = ref([
  { type: 'choice', question: '', koreanText: '', audioSource: 'tts', audioUrl: '', options: ['', '', '', ''], correctOptionIndex: 0 }
])

// Run states
const activeQuiz = ref(null)
const currentQuestionIndex = ref(0)
const userAnswers = ref({})
const timerMinutes = ref(0)
const timerSeconds = ref(0)
let timerInterval = null

// Audio playing TTS state
const isSpeaking = ref(false)
const playingKoreanText = ref('')
const currentAudio = ref(null)

// Result view states
const viewingResultQuiz = ref(null)

const currentQuestion = computed(() => {
  if (!activeQuiz.value || !activeQuiz.value.questions || !Array.isArray(activeQuiz.value.questions)) {
    return {}
  }
  return activeQuiz.value.questions[currentQuestionIndex.value] || {}
})

onMounted(() => {
  // Practice quizzes are initialized directly in memory. LocalStorage access is bypassed to prevent sandbox browser security blocks.
})

// Categories lists - heavily guarded
const readingQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  const teacherPending = list.filter(q => q.status !== 'completed' && q.quizType !== 'listening')
  const practiceAll = Array.isArray(practiceQuizzes.value) ? practiceQuizzes.value.filter(q => q.quizType === 'reading') : []
  return [...teacherPending, ...practiceAll]
})

const listeningQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  const teacherPending = list.filter(q => q.status !== 'completed' && q.quizType === 'listening')
  const practiceAll = Array.isArray(practiceQuizzes.value) ? practiceQuizzes.value.filter(q => q.quizType === 'listening') : []
  return [...teacherPending, ...practiceAll]
})

const completedQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  const teacherCompleted = list.filter(q => q.status === 'completed')
  const practiceCompleted = Array.isArray(practiceQuizzes.value) ? practiceQuizzes.value.filter(q => q.score !== null && q.score !== undefined) : []
  return [...teacherCompleted, ...practiceCompleted].sort((a, b) => {
    const dateA = a.completedAt ? new Date(a.completedAt).getTime() : 0
    const dateB = b.completedAt ? new Date(b.completedAt).getTime() : 0
    return dateB - dateA
  })
})

const readingQuizzesCount = computed(() => {
  return readingQuizzes.value.length
})

const listeningQuizzesCount = computed(() => {
  return listeningQuizzes.value.length
})

// Timer helpers
const padZero = (num) => {
  return num.toString().padStart(2, '0')
}

// Speak Korean TTS
const playSpeech = (text) => {
  if (!text) return
  stopAudio()
  
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'ko-KR'
    utterance.rate = 0.8
    
    utterance.onstart = () => {
      isSpeaking.value = true
      playingKoreanText.value = text
    }
    utterance.onend = () => {
      isSpeaking.value = false
      playingKoreanText.value = ''
    }
    utterance.onerror = () => {
      isSpeaking.value = false
      playingKoreanText.value = ''
    }
    
    window.speechSynthesis.speak(utterance)
  } else {
    alert("Trình duyệt không hỗ trợ phát âm thanh tự động.")
  }
}

// Play custom audio files (mp3, wav, local base64)
const playCustomAudio = (url) => {
  if (!url) return
  stopAudio()

  const audio = new Audio(url)
  currentAudio.value = audio
  
  isSpeaking.value = true
  playingKoreanText.value = url
  
  audio.play()
  
  audio.onended = () => {
    isSpeaking.value = false
    playingKoreanText.value = ''
    currentAudio.value = null
  }
  
  audio.onerror = () => {
    isSpeaking.value = false
    playingKoreanText.value = ''
    currentAudio.value = null
    alert("Không thể phát file âm thanh này. Hãy kiểm tra lại định dạng file hoặc liên kết đường dẫn.")
  }
}

// Clean stop audio
const stopAudio = () => {
  if (currentAudio.value) {
    currentAudio.value.pause()
    currentAudio.value = null
  }
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
  }
  isSpeaking.value = false
  playingKoreanText.value = ''
}

// Start a quiz - heavily guarded against undefined arrays
const startQuiz = (quiz) => {
  if (!quiz) return
  activeQuiz.value = quiz
  currentQuestionIndex.value = 0
  userAnswers.value = {}
  stopAudio()

  // Set default values for all inputs safely
  if (quiz.questions && Array.isArray(quiz.questions)) {
    quiz.questions.forEach(q => {
      if (q && q.id) {
        userAnswers.value[q.id] = ''
      }
    })
  }

  // Start countdown timer
  timerMinutes.value = quiz.timeLimit || 10
  timerSeconds.value = 0
  
  clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    if (timerSeconds.value > 0) {
      timerSeconds.value--
    } else if (timerMinutes.value > 0) {
      timerMinutes.value--
      timerSeconds.value = 59
    } else {
      submitQuiz()
    }
  }, 1000)
}

// Option selection
const selectOption = (opt) => {
  if (activeQuiz.value && currentQuestion.value && currentQuestion.value.id) {
    userAnswers.value[currentQuestion.value.id] = opt
  }
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
    stopAudio()
  }
}

const nextQuestion = () => {
  const maxIdx = activeQuiz.value && activeQuiz.value.questions ? activeQuiz.value.questions.length - 1 : 0
  if (currentQuestionIndex.value < maxIdx) {
    currentQuestionIndex.value++
    stopAudio()
  }
}

// Submit and grade
const submitQuiz = () => {
  clearInterval(timerInterval)
  stopAudio()
  if (!activeQuiz.value) return

  // Grader
  let correctCount = 0
  const questionsList = activeQuiz.value.questions || []

  questionsList.forEach(q => {
    if (!q) return
    const studentAns = (userAnswers.value[q.id] || '').toString().trim().toLowerCase()
    const correctAns = (q.correctAnswer || '').toString().trim().toLowerCase()
    
    if (q.type === 'choice' || q.type === 'match' || q.type === 'listening') {
      if (studentAns === correctAns) {
        correctCount++
      }
    } else if (q.type === 'fill') {
      let matchesAlternative = false
      if (q.alternativeAnswers) {
        matchesAlternative = q.alternativeAnswers.some(alt => (alt || '').toString().trim().toLowerCase() === studentAns)
      }
      if (studentAns === correctAns || matchesAlternative) {
        correctCount++
      }
    }
  })

  // Score out of 10
  const totalQuestions = questionsList.length || 1
  const score = Math.round((correctCount / totalQuestions) * 10 * 10) / 10
  const compAt = new Date().toISOString()

  // Handle saving if custom practice quiz
  if (activeQuiz.value.id && activeQuiz.value.id.toString().startsWith('practice-')) {
    const idx = practiceQuizzes.value.findIndex(pq => pq.id === activeQuiz.value.id)
    if (idx !== -1) {
      practiceQuizzes.value[idx].status = 'completed'
      practiceQuizzes.value[idx].score = score
      practiceQuizzes.value[idx].userAnswers = userAnswers.value
      practiceQuizzes.value[idx].completedAt = compAt
      localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
    }
    
    // Reward XP globally by emitting to parent
    emit('submit-quiz', {
      quizId: activeQuiz.value.id,
      score,
      userAnswers: userAnswers.value,
      completedAt: compAt
    })
  } else {
    emit('submit-quiz', {
      quizId: activeQuiz.value.id,
      score,
      userAnswers: userAnswers.value,
      completedAt: compAt
    })
  }

  // Show results view
  const completedRef = {
    ...activeQuiz.value,
    score,
    userAnswers: userAnswers.value,
    completedAt: compAt
  }
  
  activeQuiz.value = null
  viewingResultQuiz.value = completedRef
}

// View details of previous submission
const viewResults = (quiz) => {
  viewingResultQuiz.value = quiz
}

// Grader check helper for UI display - with fallback typeguards
const isUserCorrect = (question) => {
  if (!question || !viewingResultQuiz.value || !viewingResultQuiz.value.userAnswers) return false
  const studentAns = (viewingResultQuiz.value.userAnswers[question.id] || '').toString().trim().toLowerCase()
  const correctAns = (question.correctAnswer || '').toString().trim().toLowerCase()
  
  if (question.type === 'choice' || question.type === 'match' || question.type === 'listening') {
    return studentAns === correctAns
  }
  
  if (question.type === 'fill') {
    let matchesAlternative = false
    if (question.alternativeAnswers) {
      matchesAlternative = question.alternativeAnswers.some(alt => (alt || '').toString().trim().toLowerCase() === studentAns)
    }
    return studentAns === correctAns || matchesAlternative
  }

  return false
}

// Practice Quiz Creator Handlers
const openCreator = (type) => {
  isCreatingQuiz.value = true
  creatorQuizType.value = type
  newQuizTitle.value = type === 'listening' ? 'Bài ôn tập Nghe tiếng Hàn (TOEIC)' : 'Đề trắc nghiệm đọc từ vựng tự luyện'
  newQuizQuestions.value = [
    { 
      type: type === 'listening' ? 'listening' : 'choice', 
      question: type === 'listening' ? 'Lắng nghe đoạn audio tiếng Hàn bên dưới và chọn đáp án chính xác:' : '', 
      koreanText: '', 
      audioSource: 'tts', 
      audioUrl: '', 
      options: ['', '', '', ''], 
      correctOptionIndex: 0 
    }
  ]
}

const addQuestionToBuilder = () => {
  const type = creatorQuizType.value
  newQuizQuestions.value.push({
    type: type === 'listening' ? 'listening' : 'choice',
    question: type === 'listening' ? 'Lắng nghe đoạn audio tiếng Hàn bên dưới và chọn đáp án chính xác:' : '',
    koreanText: '',
    audioSource: 'tts',
    audioUrl: '',
    options: ['', '', '', ''],
    correctOptionIndex: 0
  })
}

const removeQuestionFromBuilder = (idx) => {
  newQuizQuestions.value.splice(idx, 1)
}

const handleAudioUpload = (event, idx) => {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 1.2 * 1024 * 1024) {
      alert("Lưu ý: Khuyên dùng file MP3 nhỏ dưới 1.2MB để đảm bảo lưu trữ nhanh trong trình duyệt.")
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      newQuizQuestions.value[idx].audioUrl = e.target.result // base64 string
    }
    reader.readAsDataURL(file)
  }
}

const saveCustomQuiz = () => {
  if (!newQuizTitle.value.trim()) {
    alert("Vui lòng nhập tên đề thi ôn tập.");
    return
  }

  // Validate questions
  for (let i = 0; i < newQuizQuestions.value.length; i++) {
    const q = newQuizQuestions.value[i]
    if (!q.question.trim()) {
      alert(`Vui lòng nhập câu hỏi phụ cho câu hỏi thứ ${i + 1}.`);
      return
    }
    if (q.type === 'listening') {
      if (q.audioSource === 'tts' && (!q.koreanText || !q.koreanText.trim())) {
        alert(`Vui lòng nhập đoạn tiếng Hàn để AI phát âm cho câu hỏi ${i + 1}.`);
        return
      }
      if (q.audioSource === 'file' && (!q.audioUrl || !q.audioUrl.trim())) {
        alert(`Vui lòng tải lên file âm thanh MP3 hoặc chèn liên kết link cho câu hỏi ${i + 1}.`);
        return
      }
    }
    for (let j = 0; j < 4; j++) {
      if (!q.options[j] || !q.options[j].trim()) {
        alert(`Vui lòng điền đáp án ${String.fromCharCode(65 + j)} cho câu hỏi thứ ${i + 1}.`);
        return
      }
    }
  }

  // Format question array
  const formattedQuestions = newQuizQuestions.value.map((q, idx) => {
    const opts = q.options.map((opt, optIdx) => `${String.fromCharCode(65 + optIdx)}. ${opt.trim()}`)
    const correctVal = opts[q.correctOptionIndex]
    
    return {
      id: `pq-${idx}-${Date.now()}`,
      type: q.type,
      question: q.question.trim(),
      koreanText: q.audioSource === 'tts' ? q.koreanText.trim() : null,
      audioUrl: q.audioSource === 'file' ? q.audioUrl.trim() : null,
      options: opts,
      correctAnswer: correctVal,
      explanation: q.type === 'listening'
        ? (q.audioSource === 'tts' 
            ? `Đáp án đúng là: ${correctVal}. Từ tiếng Hàn được phát âm: '${q.koreanText.trim()}'.`
            : `Đáp án đúng là: ${correctVal}. Đoạn hội thoại nghe được từ file audio.`)
        : `Đáp án đúng là: ${correctVal}.`
    }
  })

  const newQuiz = {
    id: `practice-${Date.now()}`,
    title: newQuizTitle.value.trim(),
    quizType: creatorQuizType.value,
    status: 'not_started',
    points: 10,
    score: null,
    timeLimit: 10,
    completedAt: null,
    questions: formattedQuestions
  }

  practiceQuizzes.value.push(newQuiz)

  isCreatingQuiz.value = false
  activeTab.value = creatorQuizType.value
}

const deletePracticeQuiz = (quizId) => {
  if (confirm("Bạn có chắc chắn muốn xóa bài thi tự luyện này không?")) {
    practiceQuizzes.value = practiceQuizzes.value.filter(q => q.id !== quizId)
  }
}

// Clean up timer and TTS
onUnmounted(() => {
  clearInterval(timerInterval)
  stopAudio()
})

// Date formats (safe guarded against RangeError)
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr || ''
    return date.toLocaleString('vi-VN', { 
      hour: '2-digit', 
      minute: '2-digit', 
      day: '2-digit', 
      month: '2-digit'
    })
  } catch (e) {
    return dateStr || ''
  }
}

const formatDateShort = (dateStr) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr || ''
    return date.toLocaleDateString('vi-VN', { 
      day: '2-digit', 
      month: '2-digit',
      year: 'numeric'
    })
  } catch (e) {
    return dateStr || ''
  }
}
</script>

<style scoped>
.quiz-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Header section */
.quiz-header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1.25rem;
}

.quiz-header-section h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.quiz-header-section p {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.quiz-status-tabs {
  display: flex;
  background-color: var(--bg-badge);
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.tab-btn {
  padding: 0.4rem 1rem;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  color: var(--text-body);
}

.tab-btn.active {
  background-color: var(--bg-card);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

/* Back navigation Link */
.back-link-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.back-link-btn:hover {
  color: var(--primary-hover);
  transform: translateX(-2px);
}

/* Quiz list card */
.quizzes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}

.quiz-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.quiz-card:hover {
  transform: translateY(-4px);
  border-color: var(--primary);
}

/* Create Trigger Box */
.create-trigger-card {
  border: 2px dashed var(--border-color-hover);
  background-color: transparent;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  min-height: 180px;
}

.create-trigger-card:hover {
  border-color: var(--primary);
  background-color: var(--primary-glow);
}

.trigger-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-title);
}

.trigger-plus-icon {
  font-size: 2.2rem;
  margin-bottom: 0.25rem;
}

.trigger-content h4 {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--primary);
}

.trigger-content p {
  font-size: 0.8rem;
  color: var(--text-muted);
  max-width: 240px;
}

.quiz-badge-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.badge.time { background-color: var(--bg-badge); color: var(--text-title); }
.badge.points { background-color: var(--success-light); color: var(--success); }
.badge.score-badge { background-color: var(--primary-light); color: var(--primary); font-size: 0.8rem; }
.badge.date-completed { background-color: var(--bg-badge); color: var(--text-title); }

.badge.type-badge {
  background-color: var(--primary-light);
  color: var(--primary);
}
.badge.type-badge.listening {
  background-color: var(--warning-light);
  color: var(--warning);
}

.badge.score-badge.practice-score {
  background-color: var(--success-light);
  color: var(--success);
}

.quiz-card h3 {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.5rem;
  line-height: 1.3;
}

.due-date {
  font-size: 0.85rem;
  color: var(--warning);
  font-weight: 600;
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.summary-q {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 1.5rem;
  flex-grow: 1;
}

.quiz-action-bar {
  display: flex;
  gap: 0.5rem;
}

.start-btn {
  width: 100%;
  padding: 0.65rem;
  background-color: var(--primary);
  color: #fff;
  border-radius: var(--radius-sm);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
}
.start-btn:hover { background-color: var(--primary-hover); }

.review-btn {
  width: 100%;
  padding: 0.65rem;
  background-color: var(--bg-badge);
  color: var(--text-title);
  border-radius: var(--radius-sm);
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
}
.review-btn:hover { background-color: var(--border-color); }

.delete-practice-btn {
  padding: 0.65rem 0.85rem;
  border-radius: var(--radius-sm);
  background-color: var(--danger-light);
  color: var(--danger);
}

.delete-practice-btn:hover {
  background-color: var(--danger);
  color: #fff;
}

/* QUIZ CREATOR FORM DESIGN */
.quiz-creator-container {
  max-width: 680px;
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

.creator-form-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow-md);
}

.label-bold {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--text-title);
}

.label-accent {
  color: var(--primary);
  font-weight: 700;
  display: block;
  margin-bottom: 0.35rem;
}

.creator-input-field {
  width: 100%;
  padding: 0.75rem;
  border-radius: var(--radius-sm);
  background-color: var(--bg-app);
  margin-top: 0.35rem;
}

.creator-input-field:focus {
  border-color: var(--primary);
  background-color: var(--bg-card);
}

/* Creator file upload box */
.source-toggle-group {
  display: flex;
  gap: 1.5rem;
  margin-top: 0.35rem;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
}

.file-uploader-box {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.hidden-file-input {
  display: none;
}

.file-upload-btn {
  display: inline-block;
  align-self: flex-start;
  padding: 0.5rem 1rem;
  background-color: var(--bg-badge);
  border: 1px dashed var(--border-color-hover);
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-title);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.file-upload-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--primary);
}

.file-name-hint {
  font-size: 0.75rem;
  color: var(--success);
  font-weight: 600;
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
}

.remove-q-btn {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--danger);
  display: flex;
  align-items: center;
  gap: 0.25rem;
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
  background-color: var(--bg-app);
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

.creator-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 1.5rem;
  margin-top: 1.5rem;
}

.add-q-btn {
  padding: 0.7rem 1.25rem;
  border-radius: var(--radius-sm);
  background-color: var(--bg-badge);
  color: var(--text-title);
  font-weight: 700;
  font-size: 0.85rem;
  border: 1px solid var(--border-color);
}
.add-q-btn:hover { background-color: var(--border-color); }

.save-quiz-btn {
  padding: 0.7rem 1.5rem;
  border-radius: var(--radius-sm);
  background-color: var(--success);
  color: #fff;
  font-weight: 700;
  font-size: 0.85rem;
  box-shadow: 0 4px 10px rgba(46, 204, 113, 0.2);
}
.save-quiz-btn:hover { background-color: hsl(142, 72%, 24%); }

/* ACTIVE QUIZ SCREEN */
.active-quiz-container {
  width: 100%;
  max-width: 680px;
  margin: 0 auto;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2rem;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
}

.quiz-run-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 1rem;
}

.quiz-title-info {
  display: flex;
  flex-direction: column;
}

.quiz-title-info h3 {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-title);
  letter-spacing: -0.5px;
}

.question-tracker {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-weight: 600;
}

.timer-box {
  background-color: var(--bg-badge);
  border: 1px solid var(--border-color);
  color: var(--text-title);
  font-weight: 700;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.timer-box.warning {
  background-color: var(--danger-light);
  border-color: var(--danger);
  color: var(--danger);
  animation: pulse-glow 1.5s infinite;
}

.run-progress-bar {
  height: 6px;
  background-color: var(--bg-badge);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 2rem;
}

.run-progress-bar .fill {
  height: 100%;
  background-color: var(--primary);
  transition: width 0.3s ease;
}

.question-view-card {
  flex-grow: 1;
  overflow-y: auto;
  margin-bottom: 1.5rem;
  padding-right: 0.5rem;
}

.question-view-card::-webkit-scrollbar {
  width: 4px;
}

.question-view-card::-webkit-scrollbar-thumb {
  background: var(--border-color-hover);
  border-radius: 4px;
}

.question-text {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 1.5rem;
}

/* Listening Audio Box inside test */
.audio-player-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, var(--bg-hover), var(--bg-badge));
  border: 1px solid var(--border-color);
  padding: 1rem 1.5rem;
  border-radius: var(--radius-md);
  margin-bottom: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.audio-play-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--primary);
  color: #fff;
  padding: 0.6rem 1.25rem;
  border-radius: 50px;
  font-weight: 700;
  font-size: 0.85rem;
  box-shadow: 0 4px 10px var(--primary-glow);
}

.audio-play-btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.audio-visualizer {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 24px;
}

.audio-visualizer .bar {
  width: 4px;
  height: 4px;
  background-color: var(--primary);
  border-radius: 2px;
  transition: height 0.15s ease;
}

@keyframes wave {
  0%, 100% { height: 4px; }
  50% { height: 22px; }
}

.audio-visualizer.playing .bar {
  animation: wave 1s ease-in-out infinite;
}

.audio-visualizer.playing .bar-1 { animation-delay: 0.1s; }
.audio-visualizer.playing .bar-2 { animation-delay: 0.3s; }
.audio-visualizer.playing .bar-3 { animation-delay: 0.5s; }
.audio-visualizer.playing .bar-4 { animation-delay: 0.2s; }
.audio-visualizer.playing .bar-5 { animation-delay: 0.4s; }

.options-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  text-align: left;
  background-color: var(--bg-card);
  transition: all var(--transition-fast);
}

.option-row:hover {
  background-color: var(--bg-hover);
  border-color: var(--primary);
}

.option-row.selected {
  background-color: var(--primary-light);
  border-color: var(--primary);
}

.radio-indicator {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.option-row.selected .radio-indicator {
  border-color: var(--primary);
}

.radio-indicator .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--primary);
}

.option-text {
  font-size: 0.95rem;
  color: var(--text-title);
  font-weight: 500;
}

.fill-input-box {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.fill-text-field {
  padding: 0.85rem;
  border-radius: var(--radius-sm);
  background-color: var(--bg-app);
  width: 100%;
}

.fill-text-field:focus {
  border-color: var(--primary);
  background-color: var(--bg-card);
  box-shadow: 0 0 0 3px var(--primary-glow);
}

.input-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.quiz-run-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 1.5rem;
}

.nav-btn-run {
  padding: 0.7rem 1.5rem;
  border-radius: var(--radius-sm);
  font-weight: 700;
  font-size: 0.9rem;
}

.nav-btn-run.text {
  color: var(--text-title);
  background-color: var(--bg-badge);
}
.nav-btn-run.text:hover:not(:disabled) { background-color: var(--border-color); }
.nav-btn-run.text:disabled { opacity: 0.5; cursor: not-allowed; }

.nav-btn-run.primary {
  background-color: var(--primary);
  color: #fff;
}
.nav-btn-run.primary:hover { background-color: var(--primary-hover); }

.nav-btn-run.submit {
  background-color: var(--success);
  color: #fff;
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.2);
}
.nav-btn-run.submit:hover {
  background-color: hsl(142, 72%, 24%);
}

/* RESULTS BANNER */
.results-review-container {
  max-width: 680px;
  margin: 0 auto;
}

.result-score-banner {
  background: linear-gradient(135deg, var(--bg-card), var(--bg-hover));
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
  box-shadow: var(--shadow-md);
  margin-bottom: 2rem;
}

.score-radial {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--success));
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  box-shadow: 0 8px 20px var(--primary-glow);
  flex-shrink: 0;
}

.score-radial .score-num {
  font-size: 2rem;
  font-weight: 900;
  line-height: 1;
}

.score-radial .total-num {
  font-size: 0.8rem;
  opacity: 0.8;
}

.score-text-details h3 {
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.score-text-details p {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-bottom: 0.75rem;
}

.points-earned {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: var(--text-body);
}

.gold-award {
  color: #f1c40f;
}

.review-heading {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 1rem;
}

.questions-review-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.review-question-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.review-question-card.correct {
  border-left: 4px solid var(--success);
}
.review-question-card.incorrect {
  border-left: 4px solid var(--danger);
}

.review-q-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.q-index {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
}

.correct-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.correct-badge.correct { background-color: var(--success-light); color: var(--success); }
.correct-badge.incorrect { background-color: var(--danger-light); color: var(--danger); }

.q-text {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 1rem;
}

.review-listening-replay {
  margin-bottom: 1rem;
}

.replay-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--bg-badge);
  border: 1px solid var(--border-color);
  color: var(--text-title);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 600;
}

.replay-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--primary);
}

.review-answers-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background-color: var(--bg-app);
  padding: 1rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  margin-bottom: 1rem;
}

.ans-row {
  display: flex;
  gap: 0.5rem;
  font-size: 0.85rem;
}

.ans-row .label {
  color: var(--text-muted);
  width: 140px;
  font-weight: 600;
}

.ans-row .ans-val {
  font-weight: 700;
  color: var(--text-title);
}

.ans-row .ans-val.student-ans {
  color: var(--success);
}
.ans-row .ans-val.student-ans.err {
  color: var(--danger);
  text-decoration: line-through;
}

.ans-row .ans-val.correct-ans {
  color: var(--primary);
}

.explanation-box {
  background-color: var(--primary-light);
  border: 1px solid var(--primary-glow);
  padding: 0.85rem 1.25rem;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
}

.explanation-box .label {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 0.25rem;
}

.explanation-box p {
  color: var(--text-body);
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.success-text {
  color: var(--success);
}

@media (max-width: 600px) {
  .quiz-header-section {
    flex-direction: column;
    align-items: flex-start;
  }
  .quiz-status-tabs {
    width: 100%;
    margin-top: 1rem;
  }
  .tab-btn {
    flex-grow: 1;
    text-align: center;
  }
  .options-builder-grid {
    grid-template-columns: 1fr;
  }
  .result-score-banner {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 1rem;
  }
  .points-earned {
    justify-content: center;
  }
}
</style>
