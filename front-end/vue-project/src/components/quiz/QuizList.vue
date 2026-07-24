<template>
  <div>
    <!-- Header -->
    <div class="quiz-header-section">
      <div>
        <h2>{{ mode === 'translation' ? 'Luyện Dịch &amp; Viết từ 📝' : 'Bài Tập &amp; Kiểm Tra 📝' }}</h2>
        <p>{{ mode === 'translation' ? 'Danh sách bài tập tự ôn luyện dịch tiếng Hàn được giáo viên giao và hướng dẫn chấm bài.' : 'Danh mục bài tập được phân loại theo kỹ năng giúp bạn dễ dàng ôn tập và học tập tốt hơn.' }}</p>
      </div>

      <div class="quiz-status-tabs">
        <button class="tab-btn" :class="{ active: activeTab === 'reading' }" @click="activeTab = 'reading'">
          {{ mode === 'translation' ? '📝 Bài Tập Luyện Dịch' : '📖 Đọc &amp; Từ Vựng' }} ({{ readingQuizzesCount }})
        </button>
        <button class="tab-btn completed-tab" :class="{ active: activeTab === 'completed' }" @click="activeTab = 'completed'">
          ✅ Lịch Sử Làm Bài ({{ completedQuizzes.length }})
        </button>
      </div>
    </div>

    <!-- LIST -->
    <div class="quizzes-grid animate-scale">
      <!-- Tab 1: Reading / Translation -->
      <template v-if="activeTab === 'reading'">
        <div v-for="quiz in readingQuizzes" :key="quiz.id" class="quiz-card pending">
          <div class="quiz-badge-row">
            <span class="badge type-badge reading">{{ mode === 'translation' ? '📝 Luyện dịch &amp; viết' : '📖 Đọc &amp; Từ Vựng' }}</span>
            <span v-if="quiz.examType !== 'PRACTICE'" class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit || 10 }} Phút</span>
            <span v-if="quiz.examType !== 'PRACTICE' && quiz.score !== null && quiz.score !== undefined" class="badge score-badge practice-score">Điểm gần nhất: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
          </div>
          <h3>{{ quiz.title || (mode === 'translation' ? 'Bài luyện dịch' : 'Đề ôn tập đọc') }}</h3>
          <p class="due-date" v-if="quiz.dueDate && quiz.status !== 'completed'">Hạn nộp: {{ formatDate(quiz.dueDate) }}</p>
          <p class="summary-q" v-else>{{ quiz.questionCount || 0 }} câu hỏi ôn tập</p>
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

      <!-- Tab 2: Listening (only in quizzes mode) -->
      <template v-if="activeTab === 'listening'">
        <div v-for="quiz in listeningQuizzes" :key="quiz.id" class="quiz-card pending">
          <div class="quiz-badge-row">
            <span class="badge type-badge listening">🎧 Kỹ Năng Nghe</span>
            <span class="badge time"><AppIcon name="clock" size="14" /> {{ quiz.timeLimit || 10 }} Phút</span>
            <span v-if="quiz.score !== null && quiz.score !== undefined" class="badge score-badge practice-score">Điểm gần nhất: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
          </div>
          <h3>{{ quiz.title || 'Đề ôn tập nghe' }}</h3>
          <p class="due-date" v-if="quiz.dueDate && quiz.status !== 'completed'">Hạn nộp: {{ formatDate(quiz.dueDate) }}</p>
          <p class="summary-q" v-else>{{ quiz.questionCount || 0 }} câu hỏi luyện nghe</p>
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

      <!-- Tab 3: Completed History -->
      <template v-if="activeTab === 'completed'">
        <div v-for="quiz in completedQuizzes" :key="quiz.id" class="quiz-card completed">
          <div class="quiz-badge-row">
            <span class="badge type-badge" :class="quiz.examType === 'PRACTICE' ? 'reading' : (quiz.quizType === 'listening' ? 'listening' : 'reading')">
              {{ quiz.examType === 'PRACTICE' ? '📝 Luyện dịch' : (quiz.quizType === 'listening' ? '🎧 Bài Nghe' : '📖 Bài Đọc') }}
            </span>
            <span v-if="quiz.examType !== 'PRACTICE'" class="badge score-badge">Điểm: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
            <span v-else-if="quiz.attemptStatus === 'COMPLETED' && quiz.score !== null" class="badge score-badge">Điểm: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
            <span v-if="quiz.examType === 'PRACTICE' && quiz.attemptStatus === 'PENDING_GRADING'" class="badge type-badge" style="background-color: rgba(249, 115, 22, 0.1); color: rgb(249, 115, 22); font-weight: 600; border-radius: 20px;">
              Chờ sửa
            </span>
            <span v-else-if="quiz.examType === 'PRACTICE' && quiz.attemptStatus === 'COMPLETED'" class="badge type-badge" style="background-color: rgba(16, 185, 129, 0.1); color: #10b981; font-weight: 600; border-radius: 20px;">
              Đã sửa
            </span>
            <span v-if="quiz.topikLevelResult" class="badge type-badge" style="background-color: var(--primary-light); color: var(--primary); font-weight: 700;">
              🏆 {{ quiz.topikLevelResult }}
            </span>
            <span class="badge date-completed" v-if="quiz.completedAt"><AppIcon name="check" size="14" /> {{ formatDateShort(quiz.completedAt) }}</span>
          </div>
          <h3>{{ quiz.title || 'Đề hoàn thành' }}</h3>
          <p class="summary-q">{{ quiz.questionCount || 0 }} câu hỏi đã nộp</p>
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
  </div>
</template>

<script setup>
import { inject } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

const {
  activeTab,
  readingQuizzes, listeningQuizzes, completedQuizzes,
  readingQuizzesCount, listeningQuizzesCount,
  startQuiz, viewResults, deletePracticeQuiz,
  formatDate, formatDateShort
} = inject('quizState')
</script>

<style scoped>
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
.quiz-card:hover { transform: translateY(-4px); border-color: var(--primary); }
.quiz-badge-row { display: flex; gap: 0.5rem; margin-bottom: 1rem; flex-wrap: wrap; }
.badge {
  font-size: 0.75rem; font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm);
  display: inline-flex; align-items: center; gap: 0.25rem;
}
.badge.time { background-color: var(--bg-badge); color: var(--text-title); }
.badge.score-badge { background-color: var(--primary-light); color: var(--primary); font-size: 0.8rem; }
.badge.date-completed { background-color: var(--bg-badge); color: var(--text-title); }
.badge.type-badge { background-color: var(--primary-light); color: var(--primary); }
.badge.type-badge.listening { background-color: var(--warning-light); color: var(--warning); }
.badge.score-badge.practice-score { background-color: var(--success-light); color: var(--success); }
.quiz-card h3 {
  font-size: 1.15rem; font-weight: 700; color: var(--text-title);
  margin-bottom: 0.5rem; line-height: 1.3;
}
.due-date { font-size: 0.85rem; color: var(--warning); font-weight: 600; margin-bottom: 1.5rem; flex-grow: 1; }
.summary-q { font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.5rem; flex-grow: 1; }
.quiz-action-bar { display: flex; gap: 0.5rem; }
.start-btn {
  width: 100%; padding: 0.65rem;
  background-color: var(--primary); color: #fff;
  border-radius: var(--radius-sm); font-weight: 600;
  display: flex; align-items: center; justify-content: center; gap: 0.35rem;
}
.start-btn:hover { background-color: var(--primary-hover); }
.review-btn {
  width: 100%; padding: 0.65rem;
  background-color: var(--bg-badge); color: var(--text-title);
  border-radius: var(--radius-sm); font-weight: 600;
  display: flex; align-items: center; justify-content: center; gap: 0.35rem;
}
.review-btn:hover { background-color: var(--border-color); }
.delete-practice-btn {
  padding: 0.65rem 0.85rem;
  border-radius: var(--radius-sm);
  background-color: var(--danger-light); color: var(--danger);
}
.delete-practice-btn:hover { background-color: var(--danger); color: #fff; }
.empty-state {
  text-align: center; padding: 3rem; color: var(--text-muted);
  display: flex; flex-direction: column; align-items: center; gap: 0.5rem;
}
@media (max-width: 600px) {
  .quiz-header-section { flex-direction: column; align-items: flex-start; }
  .quiz-status-tabs { width: 100%; margin-top: 1rem; }
  .tab-btn { flex-grow: 1; text-align: center; }
}
</style>
