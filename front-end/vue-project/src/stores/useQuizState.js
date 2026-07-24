import { ref, computed, onUnmounted, onMounted, nextTick } from 'vue'
import { useQuizStore } from './quiz'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

// ============================================================
// Module-level state – shared across all quiz sub-components
// ============================================================
const activeTab = ref('reading')
const activeQuiz = ref(null)
const viewingResultQuiz = ref(null)
const isCreatingQuiz = ref(false)
const creatorQuizType = ref('reading')
const newQuizTitle = ref('')
const newQuizQuestions = ref([
  { type: 'choice', question: '', koreanText: '', audioSource: 'tts', audioUrl: '', options: ['', '', '', ''], correctOptionIndex: 0 }
])
const currentQuestionIndex = ref(0)
const userAnswers = ref({})
const timerMinutes = ref(0)
const timerSeconds = ref(0)
const currentPage = ref(1)
const rowsPerPage = ref(10)
const reviewStatus = ref({})
const isSpeaking = ref(false)
const playingKoreanText = ref('')
const currentAudio = ref(null)
const reviewFilter = ref('all')
const showStudentImportModal = ref(false)
const studentImportTab = ref('paste')
const studentImportText = ref('')
const studentImportFileName = ref('')

let timerInterval = null

const practiceQuizzes = ref([
  {
    id: 'practice-seed-reading',
    title: 'Đề tự luyện Đọc: Từ vựng Động từ cơ bản 📖',
    quizType: 'reading',
    status: 'not_started',
    points: 10,
    score: null,
    timeLimit: 10,
    completedAt: null,
    questions: [
      {
        id: 'pq-reading-1',
        type: 'choice',
        question: "Từ '일어나다' trong tiếng Hàn có nghĩa là gì?",
        options: ['A. Đi làm', 'B. Thức dậy', 'C. Ăn cơm', 'D. Thể dục'],
        correctAnswer: 'B. Thức dậy',
        explanation: '일어나다 nghĩa là Thức dậy.'
      },
      {
        id: 'pq-reading-2',
        type: 'choice',
        question: "Từ nào có nghĩa là 'Học tập'?",
        options: ['A. 운동하다', 'B. 공부하다', 'C. 세수하다', 'D. 일하다'],
        correctAnswer: 'B. 공부하다',
        explanation: '공부하다 nghĩa là Học tập.'
      }
    ]
  },
  {
    id: 'practice-seed-listening',
    title: 'Đề thi thử Nghe: TOEIC Hàn ngữ Part 1 🎧',
    quizType: 'listening',
    status: 'not_started',
    points: 10,
    score: null,
    timeLimit: 10,
    completedAt: null,
    questions: [
      {
        id: 'pq-listening-1',
        type: 'listening',
        question: 'Hãy nghe đoạn âm thanh tiếng Hàn sau và chọn ý nghĩa chính xác:',
        koreanText: '오늘 날씨가 아주 따뜻합니다',
        audioSource: 'tts',
        audioUrl: '',
        options: ['A. Thời tiết hôm nay rất lạnh.', 'B. Thời tiết hôm nay rất ấm áp.', 'C. Thời tiết hôm nay mát mẻ.', 'D. Hôm nay trời mưa lớn.'],
        correctAnswer: 'B. Thời tiết hôm nay rất ấm áp.',
        explanation: "Âm thanh tiếng Hàn nói: '오늘 날씨가 아주 따뜻합니다' (Thời tiết hôm nay rất ấm áp)."
      },
      {
        id: 'pq-listening-2',
        type: 'listening',
        question: 'Hãy nghe phát âm và chọn động từ mô tả hành động tương ứng:',
        koreanText: '세수하다',
        audioSource: 'tts',
        audioUrl: '',
        options: ['A. Rửa mặt', 'B. Thức dậy', 'C. Đi ngủ', 'D. Ăn cơm'],
        correctAnswer: 'A. Rửa mặt',
        explanation: "Từ nghe được là '세수하다', nghĩa là Rửa mặt."
      }
    ]
  }
])

// ============================================================
// Main composable factory
// ============================================================
export function useQuizState(props, emit) {
  const quizStore = useQuizStore()

  // ---- Merged quizzes (teacher-assigned + attempts) ----
  const mergedQuizzes = computed(() => {
    const rawQuizzes = Array.isArray(props.quizzes) ? props.quizzes : []
    const attempts = quizStore.studentAttempts || []
    return rawQuizzes.map(quiz => {
      const attempt = attempts.find(a => a.quizId === quiz.id)
      if (attempt) {
        return {
          ...quiz,
          status: 'completed',
          score: attempt.score !== null && attempt.score !== undefined ? attempt.score : null,
          completedAt: attempt.submittedAt,
          topikLevelResult: attempt.topikLevelResult,
          attemptStatus: attempt.status
        }
      }
      return quiz
    })
  })

  // ---- Category computed lists ----
  const readingQuizzes = computed(() => {
    const list = mergedQuizzes.value
    if (props.mode === 'translation') {
      return list.filter(q => q.status !== 'completed' && q.examType === 'PRACTICE')
    }
    return list.filter(q => q.status !== 'completed' && q.examType !== 'PRACTICE' && q.quizType !== 'listening')
  })

  const listeningQuizzes = computed(() => {
    const list = mergedQuizzes.value
    if (props.mode === 'translation') return []
    return list.filter(q => q.status !== 'completed' && q.examType !== 'PRACTICE' && q.quizType === 'listening')
  })

  const completedQuizzes = computed(() => {
    const list = mergedQuizzes.value
    const filterCond = q => q.status === 'completed' && (props.mode === 'translation' ? q.examType === 'PRACTICE' : q.examType !== 'PRACTICE')
    return list.filter(filterCond).sort((a, b) => {
      const dateA = a.completedAt ? new Date(a.completedAt).getTime() : 0
      const dateB = b.completedAt ? new Date(b.completedAt).getTime() : 0
      return dateB - dateA
    })
  })

  const readingQuizzesCount = computed(() => readingQuizzes.value.length)
  const listeningQuizzesCount = computed(() => listeningQuizzes.value.length)

  // ---- Runner computed ----
  const totalPages = computed(() => {
    if (!activeQuiz.value || !activeQuiz.value.questions) return 1
    return Math.ceil(activeQuiz.value.questions.length / rowsPerPage.value)
  })

  const paginatedQuestions = computed(() => {
    if (!activeQuiz.value || !activeQuiz.value.questions) return []
    const start = (currentPage.value - 1) * rowsPerPage.value
    return activeQuiz.value.questions.slice(start, start + rowsPerPage.value)
  })

  const totalQuestionsCount = computed(() => activeQuiz.value?.questions?.length || 0)

  const answeredQuestionsCount = computed(() => {
    if (!activeQuiz.value || !activeQuiz.value.questions) return 0
    return activeQuiz.value.questions.filter(q => {
      const ans = userAnswers.value[q.id]
      return ans !== null && ans !== undefined && ans !== ''
    }).length
  })

  const progressPercentage = computed(() => {
    const total = totalQuestionsCount.value
    if (total === 0) return 0
    return (answeredQuestionsCount.value / total) * 100
  })

  const currentQuestion = computed(() => {
    if (!activeQuiz.value || !activeQuiz.value.questions || !Array.isArray(activeQuiz.value.questions)) return {}
    return activeQuiz.value.questions[currentQuestionIndex.value] || {}
  })

  // ---- Review computed ----
  const filteredReviewQuestions = computed(() => {
    if (!viewingResultQuiz.value || !viewingResultQuiz.value.questions) return []
    const list = viewingResultQuiz.value.questions
    if (reviewFilter.value === 'correct') return list.filter(q => isUserCorrect(q))
    if (reviewFilter.value === 'incorrect') return list.filter(q => !isUserCorrect(q))
    return list
  })

  // ---- Helpers ----
  const getAlpha = (idx) => ['A', 'B', 'C', 'D'][idx] || ''
  const padZero = (num) => num.toString().padStart(2, '0')

  const cleanAnswer = (ans) =>
    (ans || '').toString().trim().toLowerCase().replace(/^[a-d]\.\s*/, '')

  // ---- Navigation ----
  const goToPage = (p) => {
    if (p < 1 || p > totalPages.value) return
    currentPage.value = p
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const toggleReview = (qId) => { reviewStatus.value[qId] = !reviewStatus.value[qId] }
  const selectOption = (qId, opt) => { userAnswers.value[qId] = opt }

  const jumpToQuestion = (index) => {
    const targetPage = Math.floor(index / rowsPerPage.value) + 1
    if (currentPage.value !== targetPage) currentPage.value = targetPage
    setTimeout(() => {
      const el = document.getElementById('question-block-' + index)
      if (el) el.scrollIntoView({ behavior: 'smooth' })
    }, 50)
  }

  const prevQuestion = () => {
    if (currentQuestionIndex.value > 0) { currentQuestionIndex.value--; stopAudio() }
  }
  const nextQuestion = () => {
    const maxIdx = activeQuiz.value && activeQuiz.value.questions ? activeQuiz.value.questions.length - 1 : 0
    if (currentQuestionIndex.value < maxIdx) { currentQuestionIndex.value++; stopAudio() }
  }

  // ---- Audio ----
  const playSpeech = (text) => {
    if (!text) return
    stopAudio()
    if ('speechSynthesis' in window) {
      const utterance = new SpeechSynthesisUtterance(text)
      utterance.lang = 'ko-KR'
      utterance.rate = 0.8
      utterance.onstart = () => { isSpeaking.value = true; playingKoreanText.value = text }
      utterance.onend = () => { isSpeaking.value = false; playingKoreanText.value = '' }
      utterance.onerror = () => { isSpeaking.value = false; playingKoreanText.value = '' }
      window.speechSynthesis.speak(utterance)
    } else {
      toast.error('Trình duyệt không hỗ trợ phát âm thanh tự động.')
    }
  }

  const playCustomAudio = (url) => {
    if (!url) return
    stopAudio()
    const audio = new Audio(url)
    currentAudio.value = audio
    isSpeaking.value = true
    playingKoreanText.value = url
    audio.play()
    audio.onended = () => { isSpeaking.value = false; playingKoreanText.value = ''; currentAudio.value = null }
    audio.onerror = () => {
      isSpeaking.value = false; playingKoreanText.value = ''; currentAudio.value = null
      toast.error('Không thể phát file âm thanh này. Hãy kiểm tra lại định dạng file hoặc liên kết đường dẫn.')
    }
  }

  const stopAudio = () => {
    if (currentAudio.value) { currentAudio.value.pause(); currentAudio.value = null }
    if ('speechSynthesis' in window) window.speechSynthesis.cancel()
    isSpeaking.value = false
    playingKoreanText.value = ''
  }

  // ---- Question mapping (shared) ----
  const mapQuestions = (fullQuiz) => {
    return (fullQuiz.questions || []).map(q => {
      if (q.options && q.question) return q
      let rawOptions = []
      if (q.wrongAnswers && q.wrongAnswers.length === 4) {
        rawOptions = [...q.wrongAnswers]
      } else {
        if (q.correctAnswer) rawOptions.push(q.correctAnswer)
        if (q.wrongAnswers && Array.isArray(q.wrongAnswers)) rawOptions.push(...q.wrongAnswers)
      }
      let type = 'choice'
      if (q.questionType === 'ESSAY' || q.questionType === 'WRITING') type = 'fill'
      else if (q.audioUrl || q.section === 'LISTENING') type = 'listening'
      return {
        id: q.id, type,
        question: q.questionText || '',
        koreanText: q.koreanText || '',
        audioUrl: q.audioUrl || '',
        audioSource: q.audioSource || 'tts',
        imageUrl: q.imageUrl || '',
        options: [...rawOptions],
        correctAnswer: q.correctAnswer || '',
        explanation: q.explanation || (fullQuiz.examType === 'PRACTICE' ? null : 'Đáp án đúng là: ' + q.correctAnswer + '.')
      }
    })
  }

  // ---- Start quiz ----
  const startQuiz = async (quiz) => {
    if (!quiz) return
    let fullQuiz = quiz
    if ((!quiz.questions || quiz.questions.length === 0) && !quiz.id.toString().startsWith('practice-')) {
      try {
        fullQuiz = await quizStore.fetchQuizDetails(quiz.id)
      } catch (e) {
        console.error('Failed to fetch quiz details:', e)
        toast.error('Không thể lấy nội dung câu hỏi bài tập.')
        return
      }
    }
    const mappedQuiz = {
      ...fullQuiz,
      timeLimit: fullQuiz.timeLimitMins || fullQuiz.timeLimit || 10,
      questions: mapQuestions(fullQuiz)
    }
    activeQuiz.value = mappedQuiz
    currentQuestionIndex.value = 0
    userAnswers.value = {}
    currentPage.value = 1
    reviewStatus.value = {}
    stopAudio()
    if (mappedQuiz.questions && Array.isArray(mappedQuiz.questions)) {
      mappedQuiz.questions.forEach(q => { if (q && q.id) userAnswers.value[q.id] = '' })
    }
    if (mappedQuiz.examType !== 'PRACTICE') {
      timerMinutes.value = mappedQuiz.timeLimit || 10
      timerSeconds.value = 0
      clearInterval(timerInterval)
      timerInterval = setInterval(() => {
        if (timerSeconds.value > 0) {
          timerSeconds.value--
        } else if (timerMinutes.value > 0) {
          timerMinutes.value--; timerSeconds.value = 59
        } else {
          submitQuiz()
        }
      }, 1000)
    } else {
      clearInterval(timerInterval)
    }
  }

  // ---- Submit ----
  const submitQuiz = () => {
    clearInterval(timerInterval)
    stopAudio()
    if (!activeQuiz.value) return
    let correctCount = 0
    const questionsList = activeQuiz.value.questions || []
    questionsList.forEach(q => {
      if (!q) return
      const studentAns = (userAnswers.value[q.id] || '').toString().trim().toLowerCase()
      const correctAns = (q.correctAnswer || '').toString().trim().toLowerCase()
      if (q.type === 'choice' || q.type === 'match' || q.type === 'listening') {
        if (cleanAnswer(studentAns) === cleanAnswer(correctAns)) correctCount++
      } else if (q.type === 'fill') {
        let matchesAlt = false
        if (q.alternativeAnswers) matchesAlt = q.alternativeAnswers.some(alt => (alt || '').toString().trim().toLowerCase() === studentAns)
        if (cleanAnswer(studentAns) === cleanAnswer(correctAns) || matchesAlt) correctCount++
      }
    })
    const totalQ = questionsList.length || 1
    const score = Math.round((correctCount / totalQ) * 10 * 10) / 10
    const compAt = new Date().toISOString()

    if (activeQuiz.value.id && activeQuiz.value.id.toString().startsWith('practice-')) {
      const idx = practiceQuizzes.value.findIndex(pq => pq.id === activeQuiz.value.id)
      if (idx !== -1) {
        practiceQuizzes.value[idx].status = 'completed'
        practiceQuizzes.value[idx].score = score
        practiceQuizzes.value[idx].userAnswers = userAnswers.value
        practiceQuizzes.value[idx].completedAt = compAt
        localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
      }
    }

    const answersArray = Object.keys(userAnswers.value).map(qId => ({
      questionId: parseInt(qId),
      selectedAnswer: userAnswers.value[qId],
      writtenAnswer: userAnswers.value[qId],
      timeTakenMs: 5000
    }))
    emit('submit-quiz', { quizId: activeQuiz.value.id, score, userAnswers: answersArray, completedAt: compAt })

    const completedRef = { ...activeQuiz.value, score, userAnswers: userAnswers.value, completedAt: compAt }
    activeQuiz.value = null
    reviewFilter.value = 'all'
    viewingResultQuiz.value = completedRef
  }

  // ---- View results ----
  const viewResults = async (quiz) => {
    let fullQuiz = quiz
    if ((!quiz.questions || quiz.questions.length === 0) && !quiz.id.toString().startsWith('practice-')) {
      try {
        fullQuiz = await quizStore.fetchQuizDetails(quiz.id)
      } catch (e) {
        console.error('Failed to fetch quiz details:', e)
        toast.error('Không thể lấy nội dung câu hỏi bài tập.')
        return
      }
    }
    fullQuiz = { ...fullQuiz, questions: mapQuestions(fullQuiz) }
    const attempt = quizStore.studentAttempts.find(a => a.quizId === quiz.id)
    if (attempt && attempt.answers) {
      const userAnswersObj = {}
      const feedbackObj = {}
      attempt.answers.forEach(ans => {
        userAnswersObj[ans.questionId] = ans.studentAnswer
        feedbackObj[ans.questionId] = ans.feedback
      })
      fullQuiz = {
        ...fullQuiz,
        userAnswers: userAnswersObj,
        feedback: feedbackObj,
        score: attempt.score !== null && attempt.score !== undefined ? attempt.score : null,
        completedAt: attempt.submittedAt,
        attemptStatus: attempt.status,
        topikLevelResult: attempt.topikLevelResult
      }
    }
    reviewFilter.value = 'all'
    viewingResultQuiz.value = fullQuiz
  }

  // ---- Review helpers ----
  const isUserCorrect = (question) => {
    if (!question || !viewingResultQuiz.value) return false
    if (viewingResultQuiz.value.examType === 'PRACTICE') {
      const attempt = quizStore.studentAttempts.find(a => a.quizId === viewingResultQuiz.value.id)
      if (attempt && attempt.answers) {
        const ans = attempt.answers.find(a => a.questionId === question.id)
        return ans ? ans.isCorrect === true : false
      }
    }
    if (!viewingResultQuiz.value.userAnswers) return false
    const studentAns = (viewingResultQuiz.value.userAnswers[question.id] || '').toString().trim().toLowerCase()
    const correctAns = (question.correctAnswer || '').toString().trim().toLowerCase()
    if (question.type === 'choice' || question.type === 'match' || question.type === 'listening') {
      return cleanAnswer(studentAns) === cleanAnswer(correctAns)
    }
    if (question.type === 'fill') {
      let matchesAlt = false
      if (question.alternativeAnswers) matchesAlt = question.alternativeAnswers.some(alt => (alt || '').toString().trim().toLowerCase() === studentAns)
      return studentAns === correctAns || matchesAlt
    }
    return false
  }

  const correctAnswersCount = (quiz) => {
    if (!quiz || !quiz.questions) return 0
    return quiz.questions.filter(q => isUserCorrect(q)).length
  }

  const donutStyle = (quiz) => {
    const total = (quiz.questions || []).length || 1
    const pct = (correctAnswersCount(quiz) / total) * 100
    return { background: 'conic-gradient(var(--success) 0% ' + pct + '%, var(--danger) ' + pct + '% 100%)' }
  }

  const hasUserAnswer = (q) => {
    if (!viewingResultQuiz.value || !viewingResultQuiz.value.userAnswers) return false
    const ans = viewingResultQuiz.value.userAnswers[q.id]
    return ans !== null && ans !== undefined && ans !== ''
  }

  const originalQuestionIndex = (question) => {
    if (!viewingResultQuiz.value || !viewingResultQuiz.value.questions) return 0
    return viewingResultQuiz.value.questions.findIndex(q => q.id === question.id)
  }

  const questionTypeLabel = (q) => {
    if (q.type === 'listening') return '🎧 Nghe hiểu'
    if (q.type === 'fill') return 'Luyện từ'
    if (q.type === 'match') return '🔗 Nối câu'
    return '📖 Đọc hiểu'
  }

  const scrollToReviewCard = (qId) => {
    if (!viewingResultQuiz.value || !viewingResultQuiz.value.questions) return
    const q = viewingResultQuiz.value.questions.find(item => item.id === qId)
    if (!q) return
    const correct = isUserCorrect(q)
    if (reviewFilter.value === 'correct' && !correct) reviewFilter.value = 'all'
    if (reviewFilter.value === 'incorrect' && correct) reviewFilter.value = 'all'
    nextTick(() => {
      setTimeout(() => {
        const el = document.getElementById('q-card-' + qId)
        if (el) {
          el.scrollIntoView({ behavior: 'smooth', block: 'center' })
          el.classList.add('flash-highlight')
          setTimeout(() => el.classList.remove('flash-highlight'), 1200)
        }
      }, 50)
    })
  }

  const retryQuiz = (quiz) => { viewingResultQuiz.value = null; startQuiz(quiz) }

  // ---- Creator helpers ----
  const openStudentImportModal = () => {
    studentImportText.value = ''; studentImportFileName.value = ''
    studentImportTab.value = 'paste'; showStudentImportModal.value = true
  }

  const handleStudentFileSelected = (e) => {
    const file = e.target.files[0]
    if (!file) return
    studentImportFileName.value = file.name
    const reader = new FileReader()
    reader.onload = (event) => { studentImportText.value = event.target.result }
    reader.readAsText(file)
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
      let wordType = 'Động từ', pron = '', meaning = ''
      if (parts.length >= 4) {
        wordType = parts[1] || 'Động từ'; pron = parts[2] || ''; meaning = parts[3]
      } else if (parts.length === 3) {
        if (['Động từ', 'Tính từ', 'Danh từ', 'Trạng từ', 'Cụm từ', 'Khác'].some(wt => parts[1].toLowerCase().includes(wt.toLowerCase()))) {
          wordType = parts[1]; meaning = parts[2]
        } else { pron = parts[1]; meaning = parts[2] }
      } else { meaning = parts[1] }
      items.push({
        type: creatorQuizType.value === 'listening' ? 'listening' : 'choice',
        audioSource: 'tts', koreanText: korText, audioUrl: '',
        question: 'Từ "' + korText + '" có nghĩa là gì?',
        wordType, pronunciation: pron,
        options: [meaning, meaning + ' (khác)', 'Không có nghĩa này', 'Từ trái nghĩa'],
        correctOptionIndex: 0
      })
    }
    return items
  }

  const confirmStudentImport = () => {
    if (!studentImportText.value.trim()) { toast.warning('Vui lòng dán văn bản hoặc chọn file để import.'); return }
    const parsed = parseTextToQuizQuestions(studentImportText.value)
    if (parsed.length === 0) { toast.error('Không thể phân tích dữ liệu từ vựng. Vui lòng kiểm tra định dạng dòng!'); return }
    if (newQuizQuestions.value.length === 1 && !newQuizQuestions.value[0].question.trim() && !newQuizQuestions.value[0].koreanText.trim()) {
      newQuizQuestions.value = parsed
    } else {
      newQuizQuestions.value.push(...parsed)
    }
    toast.success('Import thành công ' + parsed.length + ' câu hỏi/từ vựng!')
    showStudentImportModal.value = false
  }

  const openCreator = (type) => {
    isCreatingQuiz.value = true
    creatorQuizType.value = type
    newQuizTitle.value = type === 'listening' ? 'Bài ôn tập Nghe tiếng Hàn (TOEIC)' : 'Đề trắc nghiệm đọc từ vựng tự luyện'
    newQuizQuestions.value = [{
      type: type === 'listening' ? 'listening' : 'choice',
      question: type === 'listening' ? 'Lắng nghe đoạn audio tiếng Hàn bên dưới và chọn đáp án chính xác:' : '',
      koreanText: '', audioSource: 'tts', audioUrl: '', wordType: 'Động từ',
      pronunciation: '', options: ['', '', '', ''], correctOptionIndex: 0
    }]
  }

  const addQuestionToBuilder = () => {
    const type = creatorQuizType.value
    newQuizQuestions.value.push({
      type: type === 'listening' ? 'listening' : 'choice',
      question: type === 'listening' ? 'Lắng nghe đoạn audio tiếng Hàn bên dưới và chọn đáp án chính xác:' : '',
      koreanText: '', audioSource: 'tts', audioUrl: '', wordType: 'Động từ',
      pronunciation: '', options: ['', '', '', ''], correctOptionIndex: 0
    })
  }

  const removeQuestionFromBuilder = (idx) => { newQuizQuestions.value.splice(idx, 1) }

  const handleAudioUpload = (event, idx) => {
    const file = event.target.files[0]
    if (file) {
      if (file.size > 1.2 * 1024 * 1024) toast.warning('Lưu ý: Khuyên dùng file MP3 nhỏ dưới 1.2MB để đảm bảo lưu trữ nhanh trong trình duyệt.')
      const reader = new FileReader()
      reader.onload = (e) => { newQuizQuestions.value[idx].audioUrl = e.target.result }
      reader.readAsDataURL(file)
    }
  }

  const saveCustomQuiz = () => {
    if (!newQuizTitle.value.trim()) { toast.warning('Vui lòng nhập tên đề thi ôn tập.'); return }
    for (let i = 0; i < newQuizQuestions.value.length; i++) {
      const q = newQuizQuestions.value[i]
      if (!q.question.trim()) { toast.warning('Vui lòng nhập câu hỏi phụ cho câu hỏi thứ ' + (i + 1) + '.'); return }
      if (q.type === 'listening') {
        if (q.audioSource === 'tts' && (!q.koreanText || !q.koreanText.trim())) {
          toast.warning('Vui lòng nhập đoạn tiếng Hàn để AI phát âm cho câu hỏi ' + (i + 1) + '.'); return
        }
        if (q.audioSource === 'file' && (!q.audioUrl || !q.audioUrl.trim())) {
          toast.warning('Vui lòng tải lên file âm thanh MP3 hoặc chèn liên kết link cho câu hỏi ' + (i + 1) + '.'); return
        }
      }
      for (let j = 0; j < 4; j++) {
        if (!q.options[j] || !q.options[j].trim()) {
          toast.warning('Vui lòng điền đáp án ' + String.fromCharCode(65 + j) + ' cho câu hỏi thứ ' + (i + 1) + '.'); return
        }
      }
    }
    const formattedQuestions = newQuizQuestions.value.map((q, idx) => {
      const opts = q.options.map((opt, optIdx) => String.fromCharCode(65 + optIdx) + '. ' + (opt || '').trim())
      const correctVal = opts[q.correctOptionIndex]
      return {
        id: 'pq-' + idx + '-' + Date.now(),
        type: q.type,
        question: (q.question || '').trim(),
        koreanText: q.audioSource === 'tts' ? (q.koreanText || '').trim() : null,
        audioUrl: q.audioSource === 'file' ? (q.audioUrl || '').trim() : null,
        options: opts, correctAnswer: correctVal,
        explanation: q.type === 'listening'
          ? (q.audioSource === 'tts'
            ? 'Đáp án đúng là: ' + correctVal + ". Từ tiếng Hàn được phát âm: '" + (q.koreanText || '').trim() + "'."
            : 'Đáp án đúng là: ' + correctVal + '. Đoạn hội thoại nghe được từ file audio.')
          : 'Đáp án đúng là: ' + correctVal + '.'
      }
    })
    const newQuiz = {
      id: 'practice-' + Date.now(),
      title: newQuizTitle.value.trim(),
      quizType: creatorQuizType.value,
      status: 'not_started', points: 10, score: null, timeLimit: 10,
      completedAt: null, questions: formattedQuestions
    }
    practiceQuizzes.value.push(newQuiz)
    localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
    isCreatingQuiz.value = false
    activeTab.value = creatorQuizType.value
  }

  const deletePracticeQuiz = (quizId) => {
    if (confirm('Bạn có chắc chắn muốn xóa bài thi tự luyện này không?')) {
      practiceQuizzes.value = practiceQuizzes.value.filter(q => q.id !== quizId)
      localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
    }
  }

  // ---- Date helpers ----
  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    try {
      const date = new Date(dateStr)
      if (isNaN(date.getTime())) return dateStr || ''
      return date.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' })
    } catch (e) { return dateStr || '' }
  }

  const formatDateShort = (dateStr) => {
    if (!dateStr) return ''
    try {
      const date = new Date(dateStr)
      if (isNaN(date.getTime())) return dateStr || ''
      return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
    } catch (e) { return dateStr || '' }
  }

  // ---- Lifecycle ----
  onMounted(() => {
    const saved = localStorage.getItem('sk_practice_quizzes')
    if (saved) {
      try { practiceQuizzes.value = JSON.parse(saved) } catch (e) { console.warn('Could not parse saved practice quizzes') }
    }
  })

  onUnmounted(() => {
    clearInterval(timerInterval)
    stopAudio()
  })

  return {
    // state refs
    activeTab, activeQuiz, viewingResultQuiz, isCreatingQuiz,
    creatorQuizType, newQuizTitle, newQuizQuestions,
    currentQuestionIndex, userAnswers,
    timerMinutes, timerSeconds,
    currentPage, rowsPerPage, reviewStatus,
    isSpeaking, playingKoreanText, currentAudio,
    reviewFilter,
    showStudentImportModal, studentImportTab, studentImportText, studentImportFileName,
    practiceQuizzes,
    // computed
    mergedQuizzes, readingQuizzes, listeningQuizzes, completedQuizzes,
    readingQuizzesCount, listeningQuizzesCount,
    totalPages, paginatedQuestions, totalQuestionsCount,
    answeredQuestionsCount, progressPercentage, currentQuestion,
    filteredReviewQuestions,
    // functions
    getAlpha, padZero, cleanAnswer,
    goToPage, toggleReview, selectOption, jumpToQuestion,
    prevQuestion, nextQuestion,
    playSpeech, playCustomAudio, stopAudio,
    startQuiz, submitQuiz, viewResults,
    isUserCorrect, correctAnswersCount,
    donutStyle, hasUserAnswer, originalQuestionIndex,
    questionTypeLabel, scrollToReviewCard, retryQuiz,
    openStudentImportModal, handleStudentFileSelected,
    confirmStudentImport, openCreator,
    addQuestionToBuilder, removeQuestionFromBuilder,
    handleAudioUpload, saveCustomQuiz, deletePracticeQuiz,
    formatDate, formatDateShort
  }
}
