<template>
  <div class="quiz-container animate-fade">
    <!-- Header -->
    <div class="quiz-header-section" v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz">
      <div>
        <h2>Bài Tập & Kiểm Tra 📝</h2>
        <p>Hoàn thành bài tập giáo viên giao hoặc tự tạo đề trắc nghiệm để ôn tập kiến thức.</p>
      </div>
      
      <div class="quiz-status-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'pending' }"
          @click="activeTab = 'pending'"
        >
          Chưa làm ({{ pendingQuizzes.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'completed' }"
          @click="activeTab = 'completed'"
        >
          Đã hoàn thành ({{ completedQuizzes.length }})
        </button>
        <button 
          class="tab-btn practice-tab" 
          :class="{ active: activeTab === 'practice' }"
          @click="activeTab = 'practice'"
        >
          Tự ôn luyện ({{ practiceQuizzes.length }})
        </button>
      </div>
    </div>

    <!-- LIST OF QUIZZES -->
    <div v-if="!activeQuiz && !viewingResultQuiz && !isCreatingQuiz" class="quizzes-grid animate-scale">
      <!-- Pending Quizzes -->
      <template v-if="activeTab === 'pending'">
        <div 
          v-for="quiz in pendingQuizzes" 
          :key="quiz.id" 
          class="quiz-card pending"
        >
          <div class="quiz-badge-row">
            <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit }} Phút</span>
            <span class="badge points"><AppIcon name="award" size="14" /> {{ quiz.points }} Điểm</span>
          </div>
          <h3>{{ quiz.title }}</h3>
          <p class="due-date">Hạn nộp: {{ formatDate(quiz.dueDate) }}</p>

          <div class="quiz-action-bar">
            <button class="start-btn" @click="startQuiz(quiz)">
              <AppIcon name="play" size="16" /> Làm bài ngay
            </button>
          </div>
        </div>

        <div v-if="pendingQuizzes.length === 0" class="empty-state">
          <AppIcon name="check" size="32" class="success-text" />
          <p>Tốt lắm! Bạn không còn bài tập nào chưa làm.</p>
        </div>
      </template>

      <!-- Completed Quizzes -->
      <template v-if="activeTab === 'completed'">
        <div 
          v-for="quiz in completedQuizzes" 
          :key="quiz.id" 
          class="quiz-card completed"
        >
          <div class="quiz-badge-row">
            <span class="badge score-badge">Điểm: {{ quiz.score }} / 10</span>
            <span class="badge date-completed"><AppIcon name="check" size="14" /> {{ formatDateShort(quiz.completedAt) }}</span>
          </div>
          <h3>{{ quiz.title }}</h3>
          <p class="summary-q">{{ quiz.questions.length }} câu hỏi đã trả lời</p>

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

      <!-- Practice Quizzes (Self Created) -->
      <template v-if="activeTab === 'practice'">
        <!-- Create Shortcut Box -->
        <div class="quiz-card create-trigger-card" @click="openCreator">
          <div class="trigger-content">
            <div class="trigger-plus-icon">+</div>
            <h4>Tự Tạo Đề Trắc Nghiệm</h4>
            <p>Tự thêm câu hỏi tiếng Hàn và lựa chọn đáp án để ôn tập từ vựng.</p>
          </div>
        </div>

        <div 
          v-for="quiz in practiceQuizzes" 
          :key="quiz.id" 
          class="quiz-card practice"
        >
          <div class="quiz-badge-row">
            <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit }} Phút</span>
            <span v-if="quiz.score !== null" class="badge score-badge practice-score">Điểm: {{ quiz.score }} / 10</span>
            <span v-else class="badge score-badge pending-practice">Chưa làm</span>
          </div>
          <h3>{{ quiz.title }}</h3>
          <p class="summary-q">{{ quiz.questions.length }} câu hỏi trắc nghiệm</p>

          <div class="quiz-action-bar practice-actions">
            <button class="start-btn" @click="startQuiz(quiz)">
              <AppIcon name="play" size="16" /> Làm bài ôn tập
            </button>
            <button class="delete-practice-btn" @click.stop="deletePracticeQuiz(quiz.id)" title="Xóa đề ôn tập này">
              <AppIcon name="x" size="16" />
            </button>
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
        <h3>Tạo Đề Ôn Tập Trắc Nghiệm Mới 🛠️</h3>
        <p>Thiết kế các câu hỏi trắc nghiệm ôn tập từ vựng với 4 sự lựa chọn đáp án A, B, C, D.</p>
      </div>

      <div class="creator-form-card">
        <div class="form-group">
          <label for="new-quiz-title" class="label-bold">Tên đề ôn tập</label>
          <input 
            type="text" 
            id="new-quiz-title" 
            v-model="newQuizTitle" 
            placeholder="Ví dụ: Ôn tập từ vựng thời tiết & mùa"
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

            <div class="form-group">
              <label>Nội dung câu hỏi</label>
              <input 
                type="text" 
                v-model="q.question" 
                placeholder="Ví dụ: Từ nào có nghĩa là Mùa Đông?"
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
            
            <p class="correct-hint">* Nhấp vào nút tròn bên cạnh đáp án đúng để thiết lập đáp án cho câu hỏi.</p>
          </div>
        </div>

        <div class="creator-actions">
          <button class="add-q-btn" @click="addQuestionToBuilder">
            + Thêm câu hỏi mới
          </button>
          
          <button class="save-quiz-btn" @click="saveCustomQuiz">
            Lưu đề ôn tập
          </button>
        </div>
      </div>
    </div>

    <!-- ACTIVE QUIZ SCREEN -->
    <div v-else-if="activeQuiz" class="active-quiz-container animate-scale">
      <div class="quiz-run-header">
        <div class="quiz-title-info">
          <h3>{{ activeQuiz.title }}</h3>
          <span class="question-tracker">Câu hỏi {{ currentQuestionIndex + 1 }} / {{ activeQuiz.questions.length }}</span>
        </div>
        
        <div class="timer-box" :class="{ warning: timerMinutes < 2 }">
          <AppIcon name="clock" size="18" />
          <span>{{ padZero(timerMinutes) }}:{{ padZero(timerSeconds) }}</span>
        </div>
      </div>

      <!-- Question Progress Bar -->
      <div class="run-progress-bar">
        <div class="fill" :style="{ width: ((currentQuestionIndex + 1) / activeQuiz.questions.length) * 100 + '%' }"></div>
      </div>

      <!-- Question Box -->
      <div class="question-view-card">
        <p class="question-text">{{ currentQuestionIndex + 1 }}. {{ currentQuestion.question }}</p>

        <!-- Option Choice Question -->
        <div v-if="currentQuestion.type === 'choice' || currentQuestion.type === 'match'" class="options-grid">
          <button 
            v-for="opt in currentQuestion.options" 
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

        <!-- Text Fill Question -->
        <div v-else-if="currentQuestion.type === 'fill'" class="fill-input-box">
          <input 
            type="text" 
            v-model="userAnswers[currentQuestion.id]" 
            placeholder="Nhập câu trả lời của bạn tại đây..."
            class="fill-text-field"
          >
          <p class="input-hint">* Hãy viết tiếng Hàn chính xác và không thừa ký tự trắng.</p>
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
          v-if="currentQuestionIndex < activeQuiz.questions.length - 1"
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
          <span class="score-num">{{ viewingResultQuiz.score }}</span>
          <span class="total-num">/ 10</span>
        </div>
        <div class="score-text-details">
          <h3>Kết quả: {{ viewingResultQuiz.title }}</h3>
          <p>Nộp ngày: {{ formatDate(viewingResultQuiz.completedAt) }}</p>
          <div class="points-earned">
            <AppIcon name="award" class="gold-award" size="20" />
            <span>Bạn đã tích lũy thêm <strong>{{ Math.round(viewingResultQuiz.score * 10) }} XP</strong> vào tài khoản học viên!</span>
          </div>
        </div>
      </div>

      <h3 class="review-heading">Xem lại chi tiết câu hỏi</h3>

      <div class="questions-review-list">
        <div 
          v-for="(q, idx) in viewingResultQuiz.questions" 
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

          <p class="q-text">{{ q.question }}</p>

          <div class="review-answers-grid">
            <div class="ans-row">
              <span class="label">Câu trả lời của bạn:</span>
              <span class="ans-val student-ans" :class="{ err: !isUserCorrect(q) }">
                {{ viewingResultQuiz.userAnswers[q.id] || '(Không trả lời)' }}
              </span>
            </div>
            
            <div class="ans-row" v-if="!isUserCorrect(q)">
              <span class="label">Đáp án đúng:</span>
              <span class="ans-val correct-ans">{{ q.correctAnswer }}</span>
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
    required: true
  }
})

const emit = defineEmits(['submit-quiz'])

const activeTab = ref('pending') // pending, completed, practice

// Practice / Custom Quiz States
const practiceQuizzes = ref([])
const isCreatingQuiz = ref(false)
const newQuizTitle = ref('')
const newQuizQuestions = ref([
  { question: '', options: ['', '', '', ''], correctOptionIndex: 0 }
])

// Run states
const activeQuiz = ref(null)
const currentQuestionIndex = ref(0)
const userAnswers = ref({})
const timerMinutes = ref(0)
const timerSeconds = ref(0)
let timerInterval = null

// Result view states
const viewingResultQuiz = ref(null)

onMounted(() => {
  // Load custom quizzes from localStorage
  const savedPractice = localStorage.getItem('sk_practice_quizzes')
  if (savedPractice) {
    practiceQuizzes.value = JSON.parse(savedPractice)
  } else {
    // Seed default custom practice quiz
    const seedPractice = [
      {
        id: "practice-seed",
        title: "Đề tự luyện: Từ vựng Trái cây & Thức ăn 🍎",
        status: "not_started",
        points: 10,
        score: null,
        timeLimit: 10,
        completedAt: null,
        questions: [
          {
            id: "pq-seed-1",
            type: "choice",
            question: "Từ '사과' trong tiếng Hàn có nghĩa là gì?",
            options: ["A. Quả chuối", "B. Quả táo", "C. Quả cam", "D. Quả nho"],
            correctAnswer: "B. Quả táo",
            explanation: "사과 nghĩa là Quả táo."
          },
          {
            id: "pq-seed-2",
            type: "choice",
            question: "Từ '우유' trong tiếng Hàn nghĩa là gì?",
            options: ["A. Nước ngọt", "B. Cà phê", "C. Sữa", "D. Trà xanh"],
            correctAnswer: "C. Sữa",
            explanation: "우유 nghĩa là Sữa."
          }
        ]
      }
    ]
    practiceQuizzes.value = seedPractice
    localStorage.setItem('sk_practice_quizzes', JSON.stringify(seedPractice))
  }
})

// Split lists
const pendingQuizzes = computed(() => {
  return props.quizzes.filter(q => q.status !== 'completed')
})

const completedQuizzes = computed(() => {
  return props.quizzes.filter(q => q.status === 'completed')
})

const currentQuestion = computed(() => {
  return activeQuiz.value ? activeQuiz.value.questions[currentQuestionIndex.value] : {}
})

// Timer helpers
const padZero = (num) => {
  return num.toString().padStart(2, '0')
}

// Start a quiz
const startQuiz = (quiz) => {
  activeQuiz.value = quiz
  currentQuestionIndex.value = 0
  userAnswers.value = {}
  
  // Set default values for all inputs
  quiz.questions.forEach(q => {
    userAnswers.value[q.id] = ''
  })

  // Start countdown timer
  timerMinutes.value = quiz.timeLimit
  timerSeconds.value = 0
  
  clearInterval(timerInterval)
  timerInterval = setInterval(() => {
    if (timerSeconds.value > 0) {
      timerSeconds.value--
    } else if (timerMinutes.value > 0) {
      timerMinutes.value--
      timerSeconds.value = 59
    } else {
      // Out of time - auto submit
      submitQuiz()
    }
  }, 1000)
}

// Option selection
const selectOption = (opt) => {
  if (activeQuiz.value) {
    userAnswers.value[currentQuestion.value.id] = opt
  }
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < activeQuiz.value.questions.length - 1) {
    currentQuestionIndex.value++
  }
}

// Submit and grade
const submitQuiz = () => {
  clearInterval(timerInterval)
  if (!activeQuiz.value) return

  // Grader
  let correctCount = 0
  const questionsList = activeQuiz.value.questions

  questionsList.forEach(q => {
    const studentAns = (userAnswers.value[q.id] || '').trim().toLowerCase()
    
    if (q.type === 'choice' || q.type === 'match') {
      if (studentAns === q.correctAnswer.trim().toLowerCase()) {
        correctCount++
      }
    } else if (q.type === 'fill') {
      const correctAns = q.correctAnswer.trim().toLowerCase()
      let matchesAlternative = false
      if (q.alternativeAnswers) {
        matchesAlternative = q.alternativeAnswers.some(alt => alt.trim().toLowerCase() === studentAns)
      }
      if (studentAns === correctAns || matchesAlternative) {
        correctCount++
      }
    }
  })

  // Score out of 10
  const score = Math.round((correctCount / questionsList.length) * 10 * 10) / 10
  const compAt = new Date().toISOString()

  // Handle saving if custom practice quiz
  if (activeQuiz.value.id.startsWith('practice-')) {
    const idx = practiceQuizzes.value.findIndex(pq => pq.id === activeQuiz.value.id)
    if (idx !== -1) {
      practiceQuizzes.value[idx].status = 'completed'
      practiceQuizzes.value[idx].score = score
      practiceQuizzes.value[idx].userAnswers = userAnswers.value
      practiceQuizzes.value[idx].completedAt = compAt
      localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
    }
    
    // Reward XP globally by emitting
    emit('submit-quiz', {
      quizId: activeQuiz.value.id,
      score,
      userAnswers: userAnswers.value,
      completedAt: compAt
    })
  } else {
    // Emit completed details to parent for standard quizzes
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

// Grader check helper for UI display
const isUserCorrect = (question) => {
  if (!viewingResultQuiz.value) return false
  const studentAns = (viewingResultQuiz.value.userAnswers[question.id] || '').trim().toLowerCase()
  const correctAns = question.correctAnswer.trim().toLowerCase()
  
  if (question.type === 'choice' || question.type === 'match') {
    return studentAns === correctAns
  }
  
  if (question.type === 'fill') {
    let matchesAlternative = false
    if (question.alternativeAnswers) {
      matchesAlternative = question.alternativeAnswers.some(alt => alt.trim().toLowerCase() === studentAns)
    }
    return studentAns === correctAns || matchesAlternative
  }

  return false
}

// Practice Quiz Creator Handlers
const openCreator = () => {
  isCreatingQuiz.value = true
  newQuizTitle.value = ''
  newQuizQuestions.value = [
    { question: '', options: ['', '', '', ''], correctOptionIndex: 0 }
  ]
}

const addQuestionToBuilder = () => {
  newQuizQuestions.value.push({
    question: '',
    options: ['', '', '', ''],
    correctOptionIndex: 0
  })
}

const removeQuestionFromBuilder = (idx) => {
  newQuizQuestions.value.splice(idx, 1)
}

const saveCustomQuiz = () => {
  if (!newQuizTitle.value.trim()) {
    alert("Vui lòng nhập tên đề ôn tập.")
    return
  }

  // Validate questions
  for (let i = 0; i < newQuizQuestions.value.length; i++) {
    const q = newQuizQuestions.value[i]
    if (!q.question.trim()) {
      alert(`Vui lòng nhập nội dung câu hỏi thứ ${i + 1}.`)
      return
    }
    for (let j = 0; j < 4; j++) {
      if (!q.options[j] || !q.options[j].trim()) {
        alert(`Vui lòng nhập đáp án ${String.fromCharCode(65 + j)} cho câu hỏi thứ ${i + 1}.`)
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
      type: "choice",
      question: q.question.trim(),
      options: opts,
      correctAnswer: correctVal,
      explanation: `Đáp án đúng là: ${correctVal}`
    }
  })

  const newQuiz = {
    id: `practice-${Date.now()}`,
    title: newQuizTitle.value.trim(),
    status: 'not_started',
    points: 10,
    score: null,
    timeLimit: 10,
    completedAt: null,
    questions: formattedQuestions
  }

  practiceQuizzes.value.push(newQuiz)
  localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))

  isCreatingQuiz.value = false
  activeTab.value = 'practice'
}

const deletePracticeQuiz = (quizId) => {
  if (confirm("Bạn có chắc chắn muốn xóa đề ôn tập tự luyện này không?")) {
    practiceQuizzes.value = practiceQuizzes.value.filter(q => q.id !== quizId)
    localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
  }
}

// Clean up timer
onUnmounted(() => {
  clearInterval(timerInterval)
})

// Date formats
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('vi-VN', { 
    hour: '2-digit', 
    minute: '2-digit', 
    day: '2-digit', 
    month: '2-digit'
  })
}

const formatDateShort = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('vi-VN', { 
    day: '2-digit', 
    month: '2-digit',
    year: 'numeric'
  })
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
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--primary-light);
  color: var(--primary);
  font-size: 1.8rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
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

.badge.time { background-color: var(--primary-light); color: var(--primary); }
.badge.points { background-color: var(--success-light); color: var(--success); }
.badge.score-badge { background-color: var(--primary-light); color: var(--primary); font-size: 0.8rem; }
.badge.date-completed { background-color: var(--bg-badge); color: var(--text-title); }

.badge.score-badge.practice-score {
  background-color: var(--success-light);
  color: var(--success);
}
.badge.score-badge.pending-practice {
  background-color: var(--bg-badge);
  color: var(--text-muted);
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
  height: 620px;
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
