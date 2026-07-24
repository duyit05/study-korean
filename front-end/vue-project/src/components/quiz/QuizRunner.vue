<template>
  <div class="quiz-container-layout animate-scale">
    <!-- LEFT: question stream -->
    <div class="quiz-stream">
      <header class="card-panel-quiz stream-header">
        <div class="quiz-title">
          <h1>{{ activeQuiz.title || 'Bài Thi Đánh Giá Kiến Thức' }}</h1>
          <p>Mã đề: #{{ activeQuiz.id || 'PRACTICE' }} | Quy định: {{ rowsPerPage }} câu/trang. Tổng số: {{ totalQuestionsCount }} câu</p>
        </div>
      </header>

      <div class="questions-list-container" style="display: flex; flex-direction: column; gap: 0.75rem;">
        <div
          v-for="(q, localIndex) in paginatedQuestions"
          :key="q.id"
          class="card-panel-quiz question-card"
          :id="'question-block-' + ((currentPage - 1) * rowsPerPage + localIndex)"
        >
          <div class="question-meta">
            <div class="question-tag">Câu hỏi {{ String((currentPage - 1) * rowsPerPage + localIndex + 1).padStart(2, '0') }}</div>
            <button class="btn-bookmark" :class="{ bookmarked: reviewStatus[q.id] }" @click="toggleReview(q.id)">
              <span>⭐</span> {{ reviewStatus[q.id] ? 'Đang đánh dấu' : 'Đánh dấu xem lại' }}
            </button>
          </div>

          <h2 class="question-text">{{ q.question || '' }}</h2>

          <!-- Question Image -->
          <div v-if="q.imageUrl" class="question-image-container animate-fade" style="margin: 1rem 0; text-align: center;">
            <img :src="q.imageUrl" alt="Hình ảnh câu hỏi" style="max-width: 100%; max-height: 320px; border-radius: 8px; border: 1px solid var(--border-color); object-fit: contain; box-shadow: var(--shadow-sm);" />
          </div>

          <!-- Choice / Match -->
          <div v-if="q.type === 'choice' || q.type === 'match'" class="options-list">
            <label
              v-for="(opt, optIdx) in (q.options || [])"
              :key="opt"
              class="option-item"
              :class="{ selected: userAnswers[q.id] === opt }"
              @click="selectOption(q.id, opt)"
            >
              <div class="option-alpha">{{ getAlpha(optIdx) }}</div>
              <div class="option-label">{{ opt }}</div>
            </label>
          </div>

          <!-- Listening -->
          <div v-else-if="q.type === 'listening'" class="listening-block">
            <div class="audio-player-card" style="display: flex; align-items: center; gap: 1rem; padding: 1rem; background-color: var(--bg-badge); border-radius: var(--radius-md); border: 1px solid var(--border-color);">
              <audio v-if="q.audioUrl" :src="q.audioUrl" controls style="flex-grow: 1; max-width: 100%; outline: none;"></audio>
              <template v-else>
                <button class="audio-play-btn" @click="playSpeech(q.koreanText)" style="display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color); background-color: var(--bg-card); cursor: pointer; font-weight: 600; color: var(--primary);" title="Phát giọng đọc mẫu bằng AI">
                  <AppIcon name="play" size="20" />
                  <span>Phát Giọng AI (TTS) 🔊</span>
                </button>
                <div class="audio-visualizer" :class="{ playing: isSpeaking && (playingKoreanText === q.koreanText || playingKoreanText === q.audioUrl) }">
                  <span class="bar bar-1"></span><span class="bar bar-2"></span><span class="bar bar-3"></span>
                  <span class="bar bar-4"></span><span class="bar bar-5"></span>
                </div>
              </template>
            </div>
            <div class="options-list" style="margin-top: 1.5rem;">
              <label
                v-for="(opt, optIdx) in (q.options || [])"
                :key="opt"
                class="option-item"
                :class="{ selected: userAnswers[q.id] === opt }"
                @click="selectOption(q.id, opt)"
              >
                <div class="option-alpha">{{ getAlpha(optIdx) }}</div>
                <div class="option-label">{{ opt }}</div>
              </label>
            </div>
          </div>

          <!-- Fill -->
          <div v-else-if="q.type === 'fill'" class="fill-input-box">
            <textarea
              v-if="activeQuiz.examType === 'PRACTICE'"
              v-model="userAnswers[q.id]"
              placeholder="Nhập nghĩa tiếng Hàn / bản dịch của bạn vào đây..."
              class="fill-text-field"
              rows="3"
              style="width: 100%; padding: 1rem; border: 1px solid var(--border-color); border-radius: var(--radius-md); font-size: 1rem; background-color: var(--bg-card); color: var(--text-body); resize: vertical;"
            ></textarea>
            <input
              v-else
              type="text"
              v-model="userAnswers[q.id]"
              placeholder="Nhập câu trả lời của bạn..."
              class="fill-text-field"
              style="width: 100%; padding: 1rem; border: 1px solid var(--border-color); border-radius: var(--radius-md); font-size: 1rem; background-color: var(--bg-card); color: var(--text-body);"
            >
            <p class="input-hint" style="margin-top: 0.5rem; font-size: 0.85rem; color: var(--text-muted);">
              {{ activeQuiz.examType === 'PRACTICE' ? '* Gõ nghĩa tiếng Hàn hoặc dịch câu chính xác.' : '* Điền từ hoặc cụm từ tiếng Hàn chính xác.' }}
            </p>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="card-panel-quiz pagination-container">
        <div class="pagination-info" id="pagination-info">Trang {{ currentPage }} / {{ totalPages }}</div>
        <div class="pagination-buttons">
          <button class="btn-page btn-page-nav" id="btn-page-prev" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M15 18l-6-6 6-6"/></svg>
            Trang trước
          </button>
          <button v-for="p in totalPages" :key="p" class="btn-page btn-page-num" :class="{ active: currentPage === p }" @click="goToPage(p)">{{ p }}</button>
          <button class="btn-page btn-page-nav" id="btn-page-next" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">
            Trang sau
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- RIGHT: sidebar -->
    <aside class="card-panel-quiz sidebar-content">
      <div v-if="activeQuiz.examType !== 'PRACTICE'" class="timer-widget">
        <div class="timer-left">⏱️ Thời gian còn lại</div>
        <div id="countdown-timer">{{ padZero(timerMinutes) }}:{{ padZero(timerSeconds) }}</div>
      </div>

      <div class="progress-wrapper">
        <div class="progress-text">
          <span>Tiến độ hoàn thành</span>
          <span>{{ answeredQuestionsCount }} / {{ totalQuestionsCount }} Câu</span>
        </div>
        <div class="progress-track">
          <div class="progress-bar" :style="{ width: progressPercentage + '%' }"></div>
        </div>
      </div>

      <div class="sidebar-title">Tổng số câu ({{ totalQuestionsCount }} câu)</div>

      <div class="matrix-grid">
        <button
          v-for="(q, index) in (activeQuiz.questions || [])"
          :key="q.id"
          class="matrix-item"
          :class="{
            done: userAnswers[q.id] !== null && userAnswers[q.id] !== undefined && userAnswers[q.id] !== '',
            review: reviewStatus[q.id]
          }"
          @click="jumpToQuestion(index)"
        >{{ index + 1 }}</button>
      </div>

      <div class="legend-list">
        <div class="legend-item"><div class="legend-color c-done"></div><span>Đã chọn đáp án</span></div>
        <div class="legend-item"><div class="legend-color c-review"></div><span>Đánh dấu xem lại sau</span></div>
        <div class="legend-item"><div class="legend-color c-empty"></div><span>Chưa làm</span></div>
      </div>

      <button class="btn-submit" @click="submitQuiz">Nộp bài</button>
    </aside>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

const {
  activeQuiz, userAnswers, reviewStatus,
  timerMinutes, timerSeconds,
  currentPage, rowsPerPage,
  paginatedQuestions, totalPages, totalQuestionsCount,
  answeredQuestionsCount, progressPercentage,
  isSpeaking, playingKoreanText,
  getAlpha, padZero,
  goToPage, toggleReview, selectOption, jumpToQuestion,
  playSpeech, submitQuiz
} = inject('quizState')
</script>

<style scoped>
.quiz-container-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 2rem;
  width: 100%;
  align-items: start;
}
.card-panel-quiz {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}
.quiz-stream { display: flex; flex-direction: column; gap: 1.5rem; }
.stream-header { padding: 2rem 2.5rem; }
.quiz-title h1 { font-size: 1.5rem; font-weight: 700; color: var(--text-title); margin-bottom: 0.25rem; }
.quiz-title p { font-size: 0.85rem; color: var(--text-muted); font-weight: 500; }
.question-card { padding: 1.25rem 1.75rem; transition: all 0.2s ease; scroll-margin-top: 2rem; }
.question-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
.question-tag { font-size: 0.85rem; font-weight: 700; color: var(--primary); text-transform: uppercase; letter-spacing: 0.05em; }
.btn-bookmark {
  background: transparent; border: none; cursor: pointer;
  font-size: 0.9rem; font-weight: 600; color: var(--text-muted);
  display: flex; align-items: center; gap: 0.3rem;
  padding: 0.3rem 0.6rem; border-radius: var(--radius-sm); transition: all 0.2s ease;
}
.btn-bookmark:hover { background: var(--bg-badge); color: #f59e0b; }
.btn-bookmark.bookmarked { color: #f59e0b; font-weight: 700; }
.question-text { font-size: 1.25rem; font-weight: 700; color: var(--text-title); line-height: 1.5; margin-bottom: 1rem; }
.options-list { display: flex; flex-direction: column; gap: 0.6rem; }
.option-item {
  display: flex; align-items: center;
  padding: 0.75rem 1.1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer; transition: all 0.2s ease;
  background-color: var(--bg-card);
}
.option-item:hover { border-color: var(--primary); background: var(--bg-badge); transform: translateX(4px); }
.option-item.selected { border-color: var(--primary); background: var(--primary-light); }
.option-item.selected .option-alpha { background: var(--primary); color: white; }
.option-item.selected .option-label { color: var(--primary); font-weight: 600; }
.option-alpha {
  display: flex; align-items: center; justify-content: center;
  width: 28px; height: 28px;
  background: var(--bg-badge); border-radius: var(--radius-sm);
  font-weight: 700; font-size: 0.9rem; color: var(--text-muted);
  margin-right: 0.75rem; transition: all 0.2s ease;
}
.option-label { font-size: 0.95rem; font-weight: 500; color: var(--text-body); }
.fill-input-box { display: flex; flex-direction: column; gap: 0.75rem; }
.fill-text-field { padding: 0.85rem; border-radius: var(--radius-sm); background-color: var(--bg-app); width: 100%; }
.fill-text-field:focus { border-color: var(--primary); background-color: var(--bg-card); box-shadow: 0 0 0 3px var(--primary-glow); }
/* Audio visualizer */
.audio-visualizer { display: flex; align-items: flex-end; gap: 3px; height: 24px; }
.audio-visualizer .bar { width: 4px; height: 4px; background-color: var(--primary); border-radius: 2px; transition: height 0.15s ease; }
@keyframes wave { 0%, 100% { height: 4px; } 50% { height: 22px; } }
.audio-visualizer.playing .bar { animation: wave 1s ease-in-out infinite; }
.audio-visualizer.playing .bar-1 { animation-delay: 0.1s; }
.audio-visualizer.playing .bar-2 { animation-delay: 0.3s; }
.audio-visualizer.playing .bar-3 { animation-delay: 0.5s; }
.audio-visualizer.playing .bar-4 { animation-delay: 0.2s; }
.audio-visualizer.playing .bar-5 { animation-delay: 0.4s; }
/* Pagination */
.pagination-container { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 2rem; margin-top: 0.5rem; }
.pagination-info { font-size: 0.9rem; font-weight: 600; color: var(--text-muted); display: flex; align-items: center; gap: 0.5rem; }
.pagination-buttons { display: flex; align-items: center; gap: 0.6rem; }
.btn-page {
  height: 42px; padding: 0 1rem; border-radius: 10px;
  border: 1px solid var(--border-color); background: var(--bg-card);
  font-weight: 600; font-size: 0.9rem; cursor: pointer; transition: all 0.2s ease;
  color: var(--text-body); display: inline-flex; align-items: center; justify-content: center; gap: 0.4rem;
  box-shadow: 0 2px 4px rgba(15, 23, 42, 0.02); user-select: none;
}
.btn-page-nav { padding: 0 1.1rem; }
.btn-page-num { width: 42px; padding: 0; }
.btn-page:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); background: var(--primary-light); transform: translateY(-2px); box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15); }
.btn-page.active { background: var(--primary); border-color: var(--primary); color: white; font-weight: 700; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3); }
.btn-page:disabled { opacity: 0.35; cursor: not-allowed; background: #f1f5f9; border-color: var(--border-color); color: var(--text-muted); transform: none !important; box-shadow: none !important; }
/* Sidebar */
.sidebar-content { padding: 1.75rem; position: sticky; top: 2rem; }
.timer-widget { display: flex; justify-content: space-between; align-items: center; background: #fef2f2; border: 1px solid #fca5a5; padding: 0.85rem 1.25rem; border-radius: var(--radius-md); color: #ef4444; font-weight: 700; font-size: 1.1rem; margin-bottom: 1.5rem; }
.timer-left { display: flex; align-items: center; gap: 0.5rem; font-size: 0.9rem; color: #b91c1c; }
.progress-wrapper { margin-bottom: 1.5rem; }
.progress-text { display: flex; justify-content: space-between; font-size: 0.8rem; font-weight: 700; margin-bottom: 0.4rem; color: var(--text-muted); }
.progress-track { height: 6px; background: var(--border-color); border-radius: 10px; overflow: hidden; }
.progress-bar { height: 100%; background: #10b981; width: 0%; border-radius: 10px; transition: width 0.3s ease; }
.sidebar-title { font-size: 0.95rem; font-weight: 700; color: var(--text-title); margin-bottom: 1rem; }
.matrix-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 0.6rem; margin-bottom: 1.5rem; }
.matrix-item { aspect-ratio: 1; border: 1px solid var(--border-color); background: var(--bg-card); border-radius: var(--radius-sm); font-size: 0.9rem; font-weight: 600; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s ease; color: var(--text-body); }
.matrix-item:hover { border-color: var(--primary); color: var(--primary); }
.matrix-item.done { background: #e6f4ea !important; border-color: #10b981 !important; color: #065f46 !important; }
.matrix-item.review { background: #fffbeb !important; border-color: #f59e0b !important; color: #92400e !important; position: relative; }
.matrix-item.review::after { content: ''; position: absolute; top: 3px; right: 3px; width: 5px; height: 5px; background: #f59e0b; border-radius: 50%; }
.legend-list { display: flex; flex-direction: column; gap: 0.65rem; padding-top: 1.25rem; border-top: 1px dashed var(--border-color); margin-bottom: 1.5rem; }
.legend-item { display: flex; align-items: center; gap: 0.75rem; font-size: 0.8rem; font-weight: 500; color: var(--text-muted); }
.legend-color { width: 14px; height: 14px; border-radius: 4px; border: 1px solid var(--border-color); }
.legend-color.c-done { border-color: #10b981; background: #e6f4ea; }
.legend-color.c-review { border-color: #f59e0b; background: #fffbeb; }
.legend-color.c-empty { background: var(--bg-card); }
.btn-submit { width: 100%; background: #10b981; color: white; padding: 0.9rem; font-size: 1rem; font-weight: 700; border: none; border-radius: var(--radius-md); cursor: pointer; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.15); transition: all 0.2s ease; }
.btn-submit:hover { background: #059669; transform: translateY(-1px); }
@media (max-width: 992px) {
  .quiz-container-layout { grid-template-columns: 1fr; }
  .sidebar-content { position: static; }
}
</style>
