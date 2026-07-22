<template>
  <div class="teacher-grading animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Cổng chấm điểm bài tự luận (Writing)</h2>
        <p>Xem danh sách bài nộp phần Viết của học sinh, cho điểm thành phần và gửi nhận xét.</p>
      </div>
    </div>

    <!-- Main Section: List of Pending Submissions / Detailed Grading View -->
    <div v-if="!selectedAttempt" class="submissions-list-wrapper">
      <div class="panel-card">
        <div class="panel-header">
          <h3>Bài tự luận chờ chấm điểm ({{ pendingAttempts.length }})</h3>
        </div>
        
        <div class="submissions-table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Học viên</th>
                <th>Tên bài thi / Đề kiểm tra</th>
                <th>Nộp lúc</th>
                <th>Số câu tự luận</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="attempt in pendingAttempts" :key="attempt.id">
                <td>
                  <div class="student-info">
                    <div class="avatar-badge">{{ attempt.studentName[0] }}</div>
                    <strong>{{ attempt.studentName }}</strong>
                  </div>
                </td>
                <td>{{ attempt.quizTitle }}</td>
                <td>{{ attempt.submittedAt }}</td>
                <td>{{ attempt.essayCount }} câu</td>
                <td><span class="status-badge orange">Chờ chấm điểm</span></td>
                <td>
                  <button class="primary-btn-sm" @click="startGrading(attempt)">
                    Chấm bài
                  </button>
                </td>
              </tr>
              <tr v-if="pendingAttempts.length === 0">
                <td colspan="6" class="empty-row">
                  Hiện không có bài nộp tự luận nào đang chờ chấm điểm. Tuyệt vời!
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Detailed Grading Form View -->
    <div v-else class="detailed-grading animate-scale">
      <div class="panel-header-sticky">
        <div class="header-info">
          <button class="back-btn" @click="selectedAttempt = null">&larr; Quay lại danh sách</button>
          <h3>Chấm điểm bài làm: {{ selectedAttempt.studentName }}</h3>
          <p>Bài thi: <strong>{{ selectedAttempt.quizTitle }}</strong> | Nộp ngày: {{ selectedAttempt.submittedAt }}</p>
        </div>
        <div class="total-score-box">
          <span class="label">Tổng điểm trắc nghiệm đã chấm:</span>
          <strong>{{ autoGradedScore }} / {{ totalMaxPoints }}</strong>
        </div>
      </div>

      <div class="questions-grading-list">
        <div 
          v-for="(answer, index) in selectedAttempt.answers" 
          :key="answer.id" 
          class="question-grading-card"
          :class="{ essay: answer.questionType === 'WRITING' }"
        >
          <div class="card-header">
            <span class="q-num">Câu {{ index + 1 }}</span>
            <span class="q-type" :class="answer.questionType.toLowerCase()">
              {{ answer.questionType === 'WRITING' ? 'Tự luận (Viết)' : 'Trắc nghiệm' }}
            </span>
            <span class="max-points">Tối đa: {{ answer.maxPoints }} điểm</span>
          </div>

          <div class="card-body">
            <p class="question-text"><strong>Câu hỏi:</strong> {{ answer.questionText }}</p>
            
            <div class="student-reply">
              <strong>Học sinh viết:</strong>
              <div class="reply-text">{{ answer.studentAnswer || '(Không có câu trả lời)' }}</div>
            </div>

            <!-- Grading Section -->
            <div v-if="answer.questionType === 'WRITING'" class="grading-inputs-wrapper">
              <div class="points-input-group">
                <label :for="'score-' + answer.id">Điểm số đạt được:</label>
                <input 
                  type="number" 
                  :id="'score-' + answer.id" 
                  v-model.number="answer.score" 
                  :max="answer.maxPoints" 
                  min="0"
                  step="0.5"
                  class="points-input"
                  required
                >
                <span class="max-limit">/ {{ answer.maxPoints }} điểm</span>
              </div>

              <div class="feedback-input-group">
                <label :for="'feedback-' + answer.id">Lời nhận xét / Sửa lỗi ngữ pháp:</label>
                <textarea 
                  :id="'feedback-' + answer.id" 
                  v-model="answer.feedback" 
                  placeholder="Ví dụ: Cần sử dụng ngữ pháp -(으)면서 để nối 2 vế câu..." 
                  rows="3"
                ></textarea>
              </div>
            </div>

            <div v-else class="auto-grade-result">
              <span class="badge" :class="answer.isCorrect ? 'correct' : 'wrong'">
                {{ answer.isCorrect ? 'Đúng' : 'Sai' }}
              </span>
              <span>Được tự động chấm: <strong>{{ answer.score }} / {{ answer.maxPoints }}</strong> điểm</span>
            </div>
          </div>
        </div>
      </div>

      <div class="submit-grading-footer">
        <button class="cancel-btn" @click="selectedAttempt = null">Hủy bỏ</button>
        <button class="submit-btn" @click="submitGrades">
          Hoàn tất chấm điểm & Gửi kết quả
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import { useQuizStore } from '../../stores/quiz'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const quizStore = useQuizStore()

const pendingAttempts = computed(() => {
  const dbAttempts = quizStore.pendingAttempts || []
  return dbAttempts.map(att => {
    return {
      id: att.id,
      studentName: att.studentName,
      quizTitle: att.quizTitle,
      submittedAt: att.submittedAt ? att.submittedAt.replace('T', ' ').substring(0, 16) : 'Vừa mới đây',
      essayCount: att.answers ? att.answers.filter(a => a.questionType === 'WRITING').length : 0,
      answers: att.answers ? att.answers.map(ans => ({
        id: ans.id,
        questionText: ans.questionText,
        studentAnswer: ans.studentAnswer,
        questionType: ans.questionType,
        maxPoints: 10,
        score: ans.pointsEarned !== null && ans.pointsEarned !== undefined ? ans.pointsEarned : 0,
        feedback: ans.feedback || ''
      })) : []
    }
  })
})

const selectedAttempt = ref(null)

onMounted(async () => {
  try {
    await quizStore.fetchPendingAttempts()
  } catch (e) {
    console.warn("Failed to load pending attempts via API:", e)
  }
})

const startGrading = (attempt) => {
  selectedAttempt.value = attempt
}

// Calculate max points and auto-graded scores
const totalMaxPoints = computed(() => {
  if (!selectedAttempt.value) return 0
  return selectedAttempt.value.answers.reduce((sum, ans) => sum + ans.maxPoints, 0)
})

const autoGradedScore = computed(() => {
  if (!selectedAttempt.value) return 0
  return selectedAttempt.value.answers
    .filter(ans => ans.questionType !== 'WRITING')
    .reduce((sum, ans) => sum + ans.score, 0)
})

const submitGrades = async () => {
  if (!selectedAttempt.value) return

  // Validate scores
  const gradesPayload = []
  for (const ans of selectedAttempt.value.answers) {
    if (ans.questionType === 'WRITING') {
      if (ans.score === undefined || ans.score === null || ans.score < 0 || ans.score > ans.maxPoints) {
        toast.warning(`Vui lòng nhập điểm hợp lệ cho Câu hỏi tự luận (Tối đa ${ans.maxPoints}đ)`)
        return
      }
      gradesPayload.push({
        answerId: ans.id,
        pointsEarned: parseFloat(ans.score),
        feedback: ans.feedback
      })
    }
  }

  try {
    await quizStore.submitGrading(selectedAttempt.value.id, gradesPayload)
    await quizStore.fetchPendingAttempts()
    toast.success('Chấm điểm và gửi nhận xét thành công!')
  } catch (e) {
    console.error("Failed to submit grades via API:", e)
    toast.error("Có lỗi xảy ra khi gửi điểm chấm.")
  }

  selectedAttempt.value = null
}
</script>

<style scoped>
.teacher-grading {
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

.panel-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.panel-header h3 {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-title);
}

.submissions-table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th, .data-table td {
  padding: 1rem 1.5rem;
  font-size: 0.85rem;
  border-bottom: 1px solid var(--border-color);
}

.data-table th {
  color: var(--text-muted);
  font-weight: 600;
  background-color: var(--bg-body);
}

.student-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--primary-glow);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.status-badge {
  font-size: 0.75rem;
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.status-badge.orange {
  background-color: rgba(249, 115, 22, 0.1);
  color: rgb(249, 115, 22);
}

.primary-btn-sm {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.4rem 0.85rem;
  border-radius: var(--radius-md);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.primary-btn-sm:hover {
  background-color: var(--primary-hover);
}

.empty-row {
  text-align: center;
  color: var(--text-muted);
  padding: 3rem !important;
}

/* Detailed Grading Page */
.detailed-grading {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.panel-header-sticky {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
}

.back-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  margin-bottom: 0.5rem;
  display: block;
}

.back-btn:hover {
  text-decoration: underline;
}

.header-info h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.header-info p {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.total-score-box {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.total-score-box .label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.total-score-box strong {
  font-size: 1.5rem;
  color: var(--primary);
  font-weight: 700;
}

.questions-grading-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.question-grading-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  background-color: var(--bg-body);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.question-grading-card.essay {
  border-left: 4px solid var(--primary);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.q-num {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--text-title);
}

.q-type {
  font-size: 0.7rem;
  padding: 0.15rem 0.45rem;
  border-radius: var(--radius-sm);
  font-weight: 700;
}

.q-type.writing { background-color: rgba(249, 115, 22, 0.1); color: rgb(249, 115, 22); }
.q-type.multiple_choice { background-color: rgba(59, 130, 246, 0.1); color: rgb(59, 130, 246); }

.max-points {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-muted);
  margin-left: auto;
}

.question-text {
  font-size: 0.9rem;
  color: var(--text-title);
  line-height: 1.4;
}

.student-reply {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  padding: 0.75rem 1rem;
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.student-reply strong {
  font-size: 0.8rem;
  color: var(--primary);
}

.reply-text {
  font-size: 0.9rem;
  color: var(--text-title);
  line-height: 1.5;
  white-space: pre-wrap;
}

.grading-inputs-wrapper {
  border-top: 1px dashed var(--border-color);
  padding-top: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.points-input-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.points-input-group label, .feedback-input-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.points-input {
  width: 70px;
  padding: 0.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-title);
  text-align: center;
  font-weight: 700;
}

.max-limit {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.feedback-input-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.feedback-input-group textarea {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-title);
  font-size: 0.85rem;
  resize: vertical;
}

.auto-grade-result {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.auto-grade-result .badge {
  font-size: 0.7rem;
  padding: 0.15rem 0.45rem;
  border-radius: var(--radius-sm);
  font-weight: 700;
}

.auto-grade-result .badge.correct { background-color: rgba(16, 185, 129, 0.1); color: rgb(16, 185, 129); }
.auto-grade-result .badge.wrong { background-color: rgba(239, 68, 68, 0.1); color: rgb(239, 68, 68); }

.submit-grading-footer {
  border-top: 1px solid var(--border-color);
  padding-top: 1.25rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
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

@media (max-width: 768px) {
  .teacher-grading {
    padding: 1rem;
  }

  .header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .submissions-table-wrapper {
    overflow-x: auto;
  }

  .panel-header-sticky {
    position: static;
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
    padding: 1rem;
    background-color: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: 1rem;
  }

  .total-score-box {
    align-items: flex-start;
  }

  .submit-grading-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .submit-grading-footer button {
    width: 100%;
  }
}
</style>

