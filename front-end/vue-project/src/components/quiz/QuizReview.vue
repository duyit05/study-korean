<template>
  <div class="results-review-container animate-scale">
    <!-- Page header -->
    <div class="review-page-header">
      <div class="review-title-area">
        <button class="btn-back-circle" @click="viewingResultQuiz = null" title="Quay lại danh sách bài tập">
          <AppIcon name="chevron-left" size="20" />
        </button>
        <div class="review-title-text">
          <h1 class="review-page-title">Chi tiết bài kiểm tra</h1>
          <p class="review-page-subtitle">{{ viewingResultQuiz.title || 'Đề ôn tập' }}</p>
        </div>
      </div>
    </div>

    <div class="review-layout">
      <!-- SIDEBAR -->
      <aside class="review-sidebar">
        <div class="review-sidebar-sticky">
          <!-- Score summary -->
          <div class="review-stat-card score-summary-card">
            <div v-if="viewingResultQuiz.examType === 'PRACTICE'" class="score-summary-top" style="justify-content: center; text-align: center; padding: 0.5rem 0;">
              <span class="score-summary-num" style="font-size: 1.8rem; color: var(--primary);">ĐÃ NỘP BÀI</span>
            </div>
            <div v-else class="score-summary-top">
              <span class="score-summary-num">{{ viewingResultQuiz.score || 0 }}</span>
              <span class="score-summary-total">/ {{ viewingResultQuiz.totalScore || 10 }}</span>
            </div>
            <p class="score-summary-date"><AppIcon name="clock" size="12" /> {{ formatDate(viewingResultQuiz.completedAt) }}</p>
            <div class="score-summary-tags">
              <span v-if="viewingResultQuiz.examType !== 'PRACTICE'" class="score-tag xp-tag"><AppIcon name="award" size="12" /> +{{ Math.round((viewingResultQuiz.score || 0) * 10) }} XP</span>
              <span class="score-tag topik-tag" v-if="viewingResultQuiz.topikLevelResult">🏆 {{ viewingResultQuiz.topikLevelResult }}</span>
            </div>
            <button class="retry-btn-sidebar" @click="retryQuiz(viewingResultQuiz)">
              <AppIcon name="play" size="16" /> Làm lại bài
            </button>
          </div>

          <!-- Donut chart (non-practice only) -->
          <div v-if="viewingResultQuiz.examType !== 'PRACTICE'" class="review-stat-card review-donut-card">
            <h3 class="sidebar-label">Thống kê nhanh</h3>
            <div class="donut-wrap">
              <div class="donut-chart" :style="donutStyle(viewingResultQuiz)">
                <div class="donut-hole">
                  <span class="donut-percent">{{ correctAnswersCount(viewingResultQuiz) }}/{{ (viewingResultQuiz.questions || []).length }}</span>
                  <span class="donut-caption">câu đúng</span>
                </div>
              </div>
            </div>
            <div class="stat-grid-2">
              <div class="stat-box correct-box">
                <p class="stat-box-label">Đúng</p>
                <p class="stat-box-num">{{ correctAnswersCount(viewingResultQuiz) }}</p>
              </div>
              <div class="stat-box incorrect-box">
                <p class="stat-box-label">Sai</p>
                <p class="stat-box-num">{{ (viewingResultQuiz.questions || []).length - correctAnswersCount(viewingResultQuiz) }}</p>
              </div>
            </div>
          </div>

          <!-- Question matrix nav -->
          <div class="review-stat-card review-nav-card">
            <h3 class="sidebar-label">Danh sách câu hỏi</h3>
            <div class="review-matrix-grid">
              <button
                v-for="(q, idx) in (viewingResultQuiz.questions || [])"
                :key="q.id"
                class="review-matrix-item"
                :class="viewingResultQuiz.examType === 'PRACTICE' ? '' : (isUserCorrect(q) ? 'correct' : 'incorrect')"
                @click="scrollToReviewCard(q.id)"
                :title="'Câu ' + (idx + 1)"
              >{{ idx + 1 }}</button>
            </div>
          </div>
        </div>
      </aside>

      <!-- MAIN FEED -->
      <main class="review-main">
        <div v-if="viewingResultQuiz.examType !== 'PRACTICE'" class="review-filter-bar">
          <button class="filter-chip" :class="{ active: reviewFilter === 'all' }" @click="reviewFilter = 'all'">Tất cả</button>
          <button class="filter-chip" :class="{ active: reviewFilter === 'incorrect' }" @click="reviewFilter = 'incorrect'">Câu sai (✕)</button>
          <button class="filter-chip" :class="{ active: reviewFilter === 'correct' }" @click="reviewFilter = 'correct'">Câu đúng (✓)</button>
          <span class="filter-count">{{ filteredReviewQuestions.length }} câu hỏi</span>
        </div>

        <div class="review-feed">
          <div
            v-for="q in filteredReviewQuestions"
            :key="q.id"
            :id="'q-card-' + q.id"
            class="review-feed-card"
          >
            <div class="feed-card-header">
              <div class="feed-card-header-left">
                <span class="feed-index-badge">#{{ originalQuestionIndex(q) + 1 }}</span>
                <span class="feed-type-label">{{ questionTypeLabel(q) }}</span>
              </div>
              <span v-if="viewingResultQuiz.examType === 'PRACTICE'" class="feed-status-pill done" style="background-color: var(--bg-badge); color: var(--text-title);">
                {{ hasUserAnswer(q) ? '✓ ĐÃ TRẢ LỜI' : '○ CHƯA TRẢ LỜI' }}
              </span>
              <span v-else class="feed-status-pill" :class="isUserCorrect(q) ? 'correct' : 'incorrect'">
                {{ isUserCorrect(q) ? '✓ CHÍNH XÁC' : (hasUserAnswer(q) ? '✕ CHƯA ĐÚNG' : '○ CHƯA LÀM') }}
              </span>
            </div>

            <h2 class="feed-question-text">{{ q.question || '' }}</h2>

            <!-- Image in review -->
            <div v-if="q.imageUrl" class="question-image-container-review animate-fade" style="margin: 1rem 0; text-align: center;">
              <img :src="q.imageUrl" alt="Hình ảnh câu hỏi" style="max-width: 100%; max-height: 250px; border-radius: 8px; border: 1px solid var(--border-color); object-fit: contain;" />
            </div>

            <!-- Listening replay -->
            <div class="review-listening-replay" v-if="q.type === 'listening'" style="margin-top: 0.5rem;">
              <audio v-if="q.audioUrl" :src="q.audioUrl" controls style="max-width: 100%; border-radius: var(--radius-sm); outline: none;"></audio>
              <button v-else class="replay-btn" @click="playSpeech(q.koreanText)">
                <AppIcon name="play" size="14" /> Phát lại giọng đọc AI: <strong>{{ q.koreanText }}</strong>
              </button>
            </div>

            <!-- Multiple choice / listening options grid -->
            <div v-if="q.type !== 'fill' && q.options && q.options.length" class="feed-options-grid">
              <div
                v-for="(opt, optIdx) in q.options"
                :key="optIdx"
                class="feed-option-item"
                :class="{
                  'is-correct': cleanAnswer(opt) === cleanAnswer(q.correctAnswer),
                  'is-user-wrong': viewingResultQuiz.userAnswers && cleanAnswer(viewingResultQuiz.userAnswers[q.id]) === cleanAnswer(opt) && cleanAnswer(opt) !== cleanAnswer(q.correctAnswer)
                }"
              >
                <div class="feed-option-left">
                  <span class="feed-option-alpha">{{ getAlpha(optIdx) }}</span>
                  <span class="feed-option-text">{{ opt }}</span>
                </div>
                <span class="feed-option-icon" v-if="cleanAnswer(opt) === cleanAnswer(q.correctAnswer)">✓</span>
                <span class="feed-option-icon" v-else-if="viewingResultQuiz.userAnswers && cleanAnswer(viewingResultQuiz.userAnswers[q.id]) === cleanAnswer(opt)">✕</span>
              </div>
            </div>

            <!-- Fill-in text comparison -->
            <div v-else class="review-answers-grid">
              <div class="ans-row">
                <span class="label">Câu trả lời của bạn:</span>
                <span class="ans-val student-ans" :class="{ err: viewingResultQuiz.examType !== 'PRACTICE' && !isUserCorrect(q) }">
                  {{ viewingResultQuiz.userAnswers ? (viewingResultQuiz.userAnswers[q.id] || '(Không trả lời)') : '(Không trả lời)' }}
                </span>
              </div>
              <div class="ans-row" v-if="viewingResultQuiz.examType === 'PRACTICE' ? q.correctAnswer : !isUserCorrect(q)">
                <span class="label">Bản dịch mẫu / Gợi ý:</span>
                <span class="ans-val correct-ans">{{ q.correctAnswer || '' }}</span>
              </div>
            </div>

            <!-- Teacher Feedback -->
            <div class="feed-analysis-box teacher-feedback-box" v-if="q.feedback || (viewingResultQuiz.feedback && viewingResultQuiz.feedback[q.id])" style="background-color: var(--primary-light); border-left: 4px solid var(--primary); margin-top: 1rem;">
              <div class="feed-analysis-body">
                <h4 class="feed-analysis-title" style="color: var(--primary);">Nhận xét:</h4>
                <div class="feed-analysis-text" v-html="q.feedback || viewingResultQuiz.feedback[q.id]"></div>
              </div>
            </div>

            <!-- Explanation box -->
            <div class="feed-analysis-box" v-if="q.explanation">
              <div class="feed-analysis-icon">💡</div>
              <div class="feed-analysis-body">
                <h4 class="feed-analysis-title">
                  {{ viewingResultQuiz.examType === 'PRACTICE' ? 'Giải thích / Hướng dẫn' : ('Tại sao ' + (isUserCorrect(q) ? 'bạn đúng?' : 'bạn sai?')) }}
                </h4>
                <p class="feed-analysis-text">{{ q.explanation }}</p>
                <div v-if="q.vocab && q.vocab.length" class="feed-vocab-tags mt-4">
                  <span v-for="v in q.vocab" :key="v" class="feed-vocab-tag">{{ v }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="filteredReviewQuestions.length === 0" class="empty-state">
            <AppIcon name="alert" size="32" />
            <p>Không có câu hỏi nào phù hợp với bộ lọc này.</p>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

const {
  viewingResultQuiz, reviewFilter, filteredReviewQuestions,
  isUserCorrect, correctAnswersCount,
  donutStyle, hasUserAnswer, originalQuestionIndex,
  questionTypeLabel, scrollToReviewCard, retryQuiz,
  getAlpha, cleanAnswer, playSpeech,
  formatDate
} = inject('quizState')
</script>

<style scoped>
.results-review-container { max-width: 1500px; margin: 0 auto; padding: 1rem 0; }
.review-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
.review-title-area { display: flex; align-items: center; gap: 1.25rem; }
.review-title-text { display: flex; flex-direction: column; }
.review-page-title { font-size: 1.85rem; font-weight: 800; color: var(--text-title); line-height: 1.2; }
.review-page-subtitle { font-size: 0.95rem; color: var(--text-muted); font-weight: 500; margin-top: 0.15rem; }
.btn-back-circle {
  width: 44px; height: 44px; border-radius: 50%;
  border: 1px solid var(--border-color); background-color: var(--bg-card);
  color: var(--text-title); display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm); flex-shrink: 0;
}
.btn-back-circle:hover { background-color: var(--primary-light); border-color: var(--primary); color: var(--primary); transform: translateX(-4px); box-shadow: 0 4px 12px var(--primary-glow); }
.review-layout { display: grid; grid-template-columns: 1fr 320px; gap: 2rem; align-items: start; }
.review-sidebar { grid-column: 2; grid-row: 1; position: sticky; top: 90px; display: flex; flex-direction: column; gap: 1.5rem; z-index: 10; }
.review-sidebar-sticky { display: flex; flex-direction: column; gap: 1.5rem; }
.review-stat-card { background-color: var(--bg-card); border: 1px solid var(--border-color); border-radius: 24px; padding: 1.5rem; box-shadow: var(--shadow-sm); }
.score-summary-card { text-align: center; background: linear-gradient(135deg, var(--bg-card), var(--primary-light)); }
.score-summary-top { display: flex; align-items: baseline; justify-content: center; }
.score-summary-num { font-size: 3rem; font-weight: 800; color: var(--primary); line-height: 1; }
.score-summary-total { font-size: 1.25rem; color: var(--text-muted); font-weight: 700; margin-left: 0.25rem; }
.score-summary-date { font-size: 0.8rem; color: var(--text-muted); margin: 0.5rem 0 1rem 0; display: flex; align-items: center; justify-content: center; gap: 0.25rem; }
.score-summary-tags { display: flex; gap: 0.5rem; justify-content: center; flex-wrap: wrap; }
.score-tag { font-size: 0.75rem; font-weight: 700; padding: 0.35rem 0.75rem; border-radius: 999px; }
.xp-tag { background-color: rgba(241, 196, 15, 0.15); color: #b7860b; }
.topik-tag { background-color: var(--primary-light); color: var(--primary); }
.retry-btn-sidebar {
  width: 100%; display: flex; align-items: center; justify-content: center; gap: 0.5rem;
  background-color: var(--primary); color: white; padding: 0.8rem; border-radius: 12px;
  font-weight: 700; font-size: 0.9rem; border: none; cursor: pointer; margin-top: 1.25rem;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 0 4px 10px var(--primary-glow);
}
.retry-btn-sidebar:hover { background-color: var(--primary-hover); transform: translateY(-2px); box-shadow: 0 6px 16px var(--primary-glow); }
.sidebar-label { font-size: 0.75rem; font-weight: 800; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 1rem; }
.donut-wrap { display: flex; justify-content: center; margin: 1rem 0; }
.donut-chart { width: 140px; height: 140px; border-radius: 50%; display: flex; align-items: center; justify-content: center; position: relative; transition: all 0.3s ease; }
.donut-hole { width: 106px; height: 106px; background-color: var(--bg-card); border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 2; }
.donut-percent { font-size: 1.25rem; font-weight: 800; color: var(--text-title); }
.donut-caption { font-size: 0.7rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; }
.stat-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; }
.stat-box { padding: 0.75rem; border-radius: 16px; text-align: center; }
.stat-box.correct-box { background-color: var(--success-light); }
.stat-box-label { font-size: 0.65rem; font-weight: 800; text-transform: uppercase; color: var(--success); }
.stat-box-num { font-size: 1.1rem; font-weight: 800; color: var(--success); margin-top: 0.15rem; }
.stat-box.incorrect-box { background-color: var(--danger-light); }
.stat-box.incorrect-box .stat-box-label { color: var(--danger); }
.stat-box.incorrect-box .stat-box-num { color: var(--danger); }
.review-matrix-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 0.5rem; }
.review-matrix-item { aspect-ratio: 1; display: flex; align-items: center; justify-content: center; border-radius: 8px; font-size: 0.8rem; font-weight: 700; cursor: pointer; border: 1px solid var(--border-color); transition: all var(--transition-fast); background-color: var(--bg-card); color: var(--text-body); }
.review-matrix-item.correct { background-color: var(--success-light); color: var(--success); border-color: var(--success-border); }
.review-matrix-item.incorrect { background-color: var(--danger-light); color: var(--danger); border-color: var(--danger-light); }
.review-matrix-item:hover { transform: translateY(-2px); box-shadow: var(--shadow-sm); }
.review-main { grid-column: 1; grid-row: 1; flex-grow: 1; }
.review-filter-bar { background-color: var(--bg-card); border: 1px solid var(--border-color); border-radius: 16px; padding: 0.5rem; display: flex; align-items: center; gap: 0.5rem; margin-bottom: 2rem; box-shadow: var(--shadow-sm); }
.filter-chip { padding: 0.5rem 1rem; font-size: 0.85rem; font-weight: 700; border-radius: 10px; color: var(--text-muted); cursor: pointer; transition: all var(--transition-fast); background-color: transparent; }
.filter-chip:hover { background-color: var(--bg-hover); color: var(--text-title); }
.filter-chip.active { background-color: var(--primary); color: white; }
.filter-count { margin-left: auto; padding-right: 1rem; font-size: 0.75rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; }
.review-feed { display: flex; flex-direction: column; gap: 1.5rem; }
.review-feed-card { background-color: var(--bg-card); border: 1px solid var(--border-color); border-radius: 24px; padding: 2rem; box-shadow: var(--shadow-sm); scroll-margin-top: 3rem; transition: all var(--transition-fast); }
.review-feed-card:hover { border-color: var(--primary-glow); box-shadow: var(--shadow-md); }
.feed-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.feed-card-header-left { display: flex; align-items: center; gap: 0.75rem; }
.feed-index-badge { width: 44px; height: 44px; border-radius: 14px; background-color: var(--text-title); color: white; display: flex; align-items: center; justify-content: center; font-size: 1.1rem; font-weight: 800; }
.feed-type-label { font-size: 0.65rem; font-weight: 800; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; display: block; }
.feed-status-pill { font-size: 0.7rem; font-weight: 800; padding: 0.4rem 0.85rem; border-radius: 99px; letter-spacing: 0.02em; }
.feed-status-pill.correct { background-color: var(--success-light); color: var(--success); }
.feed-status-pill.incorrect { background-color: var(--danger-light); color: var(--danger); }
.feed-question-text { font-size: 1.35rem; font-weight: 700; color: var(--text-title); margin-bottom: 2rem; line-height: 1.45; }
.feed-options-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.75rem; margin-bottom: 1.5rem; }
.feed-option-item { display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.25rem; border-radius: 16px; border: 1.5px solid var(--border-color); background-color: var(--bg-card); transition: all 0.2s ease; }
.feed-option-left { display: flex; align-items: center; gap: 0.75rem; }
.feed-option-alpha { width: 28px; height: 28px; border-radius: 8px; background-color: rgba(0,0,0,0.03); display: flex; align-items: center; justify-content: center; font-size: 0.8rem; font-weight: 800; border: 1px solid rgba(0,0,0,0.05); color: var(--text-muted); }
.feed-option-text { font-size: 0.9rem; font-weight: 700; color: var(--text-body); }
.feed-option-icon { font-size: 0.95rem; font-weight: 800; }
.feed-option-item.is-correct { background-color: var(--success-light); border-color: var(--success); color: var(--success); }
.feed-option-item.is-correct .feed-option-alpha { background-color: var(--success); color: white; }
.feed-option-item.is-correct .feed-option-text { color: var(--success); }
.feed-option-item.is-user-wrong { background-color: var(--danger-light); border-color: var(--danger); color: var(--danger); }
.feed-option-item.is-user-wrong .feed-option-alpha { background-color: var(--danger); color: white; }
.feed-option-item.is-user-wrong .feed-option-text { color: var(--danger); }
.review-answers-grid { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1rem; }
.ans-row { display: flex; flex-direction: column; gap: 0.3rem; }
.label { font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; }
.ans-val { font-size: 1rem; padding: 0.75rem 1rem; border-radius: var(--radius-md); background-color: var(--bg-badge); }
.ans-val.student-ans { border-left: 3px solid var(--border-color); }
.ans-val.err { border-left-color: var(--danger); color: var(--danger); }
.ans-val.correct-ans { border-left: 3px solid var(--success); color: var(--success); background-color: var(--success-light); }
.feed-analysis-box { background-color: var(--bg-badge); border: 1px solid var(--border-color); border-radius: 24px; padding: 1.5rem; display: flex; gap: 1rem; margin-top: 1rem; }
.feed-analysis-icon { width: 40px; height: 40px; border-radius: 12px; background-color: var(--bg-card); display: flex; align-items: center; justify-content: center; font-size: 1.25rem; flex-shrink: 0; box-shadow: var(--shadow-sm); color: var(--primary); }
.feed-analysis-body { flex-grow: 1; }
.feed-analysis-title { font-size: 0.85rem; font-weight: 800; color: var(--text-title); margin-bottom: 0.25rem; text-transform: uppercase; }
.feed-analysis-text { font-size: 0.9rem; line-height: 1.5; color: var(--text-body); }
.feed-vocab-tags { margin-top: 1rem; display: flex; flex-wrap: wrap; gap: 0.5rem; }
.feed-vocab-tag { background-color: var(--bg-card); border: 1px solid var(--border-color); padding: 0.3rem 0.65rem; border-radius: 8px; font-size: 0.75rem; font-weight: 700; color: var(--text-muted); }
.replay-btn { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color); background-color: var(--bg-badge); font-size: 0.85rem; font-weight: 600; cursor: pointer; color: var(--primary); }
.replay-btn:hover { background-color: var(--primary-light); }
.empty-state { text-align: center; padding: 3rem; color: var(--text-muted); display: flex; flex-direction: column; align-items: center; gap: 0.5rem; }
.flash-highlight { border-color: var(--primary) !important; box-shadow: 0 0 0 4px var(--primary-glow) !important; }
@media (max-width: 992px) {
  .review-layout { grid-template-columns: 1fr; }
  .review-sidebar { grid-column: auto; grid-row: auto; position: static; }
  .review-main { grid-column: auto; grid-row: auto; }
}
@media (max-width: 768px) {
  .feed-options-grid { grid-template-columns: 1fr; }
  .review-page-header { flex-direction: column; align-items: flex-start; }
}
</style>
