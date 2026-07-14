<template>
  <div class="vocab-container animate-fade">
    <!-- Header -->
    <div class="vocab-header-section">
      <div>
        <h2>Học Từ Vựng & Flashcard 🗂️</h2>
        <p>Chọn bộ từ vựng dưới đây để học và ôn tập bằng thẻ ghi nhớ thông minh.</p>
      </div>
      <div class="vocab-mode-toggle" v-if="selectedSet">
        <button 
          class="toggle-btn" 
          :class="{ active: viewMode === 'list' }"
          @click="viewMode = 'list'"
        >
          <AppIcon name="dashboard" size="16" /> Danh sách
        </button>
        <button 
          class="toggle-btn" 
          :class="{ active: viewMode === 'flashcard' }"
          @click="startFlashcardMode"
        >
          <AppIcon name="play" size="16" /> Flashcard
        </button>
      </div>
    </div>

    <!-- Mode 1: Select a Study Set -->
    <div v-if="!selectedSet" class="study-sets-grid animate-scale">
      <div 
        v-for="set in studySets" 
        :key="set.id" 
        class="study-set-card"
      >
        <div class="set-meta">
          <span class="vocab-count-badge">{{ set.wordCount || 0 }} Từ</span>
        </div>
        <h3>{{ set.name }}</h3>
        <p>{{ set.description }}</p>
        
        <div class="set-progress">
          <div class="progress-info">
            <span>Tiến độ thuộc:</span>
            <span class="pct">{{ getSetProgressPercentage(set) }}%</span>
          </div>
          <div class="set-progress-bar">
            <div class="fill" :style="{ width: getSetProgressPercentage(set) + '%' }"></div>
          </div>
        </div>

        <div class="set-actions">
          <button class="set-btn list" @click="selectSet(set, 'list')">Xem từ vựng</button>
          <button class="set-btn flash" @click="selectSet(set, 'flashcard')">
            <AppIcon name="play" size="16" /> Học Flashcard
          </button>
        </div>
      </div>
    </div>

    <!-- Mode 2: Vocabularies Inside Selected Set -->
    <div v-else class="selected-set-view">
      <!-- Back navigation button -->
      <button class="back-link-btn" @click="selectedSet = null">
        <AppIcon name="chevron-left" size="16" /> Quay lại danh sách bộ từ
      </button>

      <div class="set-title-row">
        <h3>{{ selectedSet.name }}</h3>
        <p>{{ selectedSet.description }}</p>
      </div>

      <!-- LIST VIEW -->
      <div v-if="viewMode === 'list'" class="list-mode-layout animate-scale">
        <!-- Search and Filter Panel -->
        <div class="filter-panel">
          <div class="search-box">
            <AppIcon name="search" class="search-icon" size="18" />
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="Tìm kiếm từ vựng (Hàn, Việt, Phiên âm)..."
            >
          </div>
          
          <div class="status-filters">
            <button 
              class="filter-tag" 
              :class="{ active: activeFilter === 'all' }"
              @click="activeFilter = 'all'"
            >
              Tất cả ({{ selectedSet.words.length }})
            </button>
            <button 
              class="filter-tag success" 
              :class="{ active: activeFilter === 'learned' }"
              @click="activeFilter = 'learned'"
            >
              Đã thuộc ({{ getWordCountByStatus('learned') }})
            </button>
            <button 
              class="filter-tag warning" 
              :class="{ active: activeFilter === 'review' }"
              @click="activeFilter = 'review'"
            >
              Cần ôn tập ({{ getWordCountByStatus('review') }})
            </button>
            <button 
              class="filter-tag unlearned" 
              :class="{ active: activeFilter === 'unlearned' }"
              @click="activeFilter = 'unlearned'"
            >
              Chưa thuộc ({{ getWordCountByStatus('unlearned') }})
            </button>
          </div>
        </div>

        <!-- Vocab Table/List -->
        <div class="vocab-list">
          <div 
            v-for="item in filteredWords" 
            :key="item.id" 
            class="vocab-row-card"
            :class="item.status"
          >
            <div class="row-korean">
              <span class="hangul">{{ item.word }}</span>
              <span class="pronounce">/{{ item.pronunciation }}/</span>
            </div>
            
            <div class="row-vietnamese">
              <span class="meaning">{{ item.meaning }}</span>
              <span class="example" v-if="item.example">
                <strong>Ví dụ:</strong> {{ item.example }} ({{ item.exampleMeaning }})
              </span>
            </div>

            <!-- Interactive Memorize Status Controller -->
            <div class="row-status-actions">
              <button 
                class="status-btn check" 
                :class="{ active: item.status === 'learned' }"
                title="Đã thuộc từ này"
                @click="updateWordStatus(item.id, 'learned')"
              >
                <AppIcon name="check" size="14" />
                <span>Đã thuộc</span>
              </button>
              
              <button 
                class="status-btn warn" 
                :class="{ active: item.status === 'review' }"
                title="Cần ôn tập thêm"
                @click="updateWordStatus(item.id, 'review')"
              >
                <AppIcon name="alert" size="14" />
                <span>Cần ôn tập</span>
              </button>

              <button 
                class="status-btn unset" 
                :class="{ active: item.status === 'unlearned' }"
                title="Chưa học từ này"
                @click="updateWordStatus(item.id, 'unlearned')"
              >
                <span>Chưa thuộc</span>
              </button>
            </div>
          </div>

          <div v-if="filteredWords.length === 0" class="empty-state">
            <AppIcon name="alert" size="32" />
            <p>Không tìm thấy từ vựng nào khớp với bộ lọc.</p>
          </div>
        </div>
      </div>

      <!-- FLASHCARD VIEW -->
      <div v-else class="flashcard-mode-layout animate-scale">
        <div class="flashcard-deck">
          <div class="deck-progress">
            <span>Tiến trình: Thẻ {{ currentCardIndex + 1 }} / {{ activeFlashcards.length }}</span>
            <div class="progress-bar-tiny">
              <div class="fill" :style="{ width: ((currentCardIndex + 1) / activeFlashcards.length) * 100 + '%' }"></div>
            </div>
          </div>

          <!-- Flashcard Card - Click to flip -->
          <div 
            class="flashcard-box" 
            :class="{ flipped: isFlipped }"
            @click="isFlipped = !isFlipped"
          >
            <!-- FRONT SIDE: HANGUL -->
            <div class="card-side card-front">
              <div class="card-indicator">Từ tiếng Hàn</div>
              <div class="korean-text">{{ currentCard.word }}</div>
              <div class="flip-hint">Nhấp vào thẻ để lật mặt sau 🔄</div>
            </div>

            <!-- BACK SIDE: MEANING, PRONUNCIATION, EXAMPLE -->
            <div class="card-side card-back">
              <div class="card-indicator">Ý nghĩa & Cách phát âm</div>
              <div class="meaning-text">{{ currentCard.meaning }}</div>
              <div class="pronounce-text">/ {{ currentCard.pronunciation }} /</div>
              
              <div class="card-example-box" v-if="currentCard.example" @click.stop>
                <span class="label">Câu ví dụ:</span>
                <p class="kr-ex">{{ currentCard.example }}</p>
                <p class="vi-ex">{{ currentCard.exampleMeaning }}</p>
              </div>

              <div class="flip-hint">Nhấp vào thẻ để xem lại từ tiếng Hàn 🔄</div>
            </div>
          </div>

          <!-- Card Controls -->
          <div class="card-navigation">
            <button 
              class="nav-btn" 
              :disabled="currentCardIndex === 0"
              @click="prevCard"
            >
              <AppIcon name="chevron-left" size="18" /> Trước đó
            </button>

            <!-- Memorize flags directly on card -->
            <div class="memorize-toolbar">
              <button 
                class="toolbar-btn review-btn"
                :class="{ active: currentCard.status === 'review' }"
                @click.stop="setCardStatus('review')"
              >
                <AppIcon name="alert" size="16" /> Cần ôn tập
              </button>
              
              <button 
                class="toolbar-btn learn-btn"
                :class="{ active: currentCard.status === 'learned' }"
                @click.stop="setCardStatus('learned')"
              >
                <AppIcon name="check" size="16" /> Đã thuộc
              </button>
            </div>

            <button 
              class="nav-btn" 
              :disabled="currentCardIndex === activeFlashcards.length - 1"
              @click="nextCard"
            >
              Tiếp theo <AppIcon name="chevron-right" size="18" />
            </button>
          </div>

          <!-- End Set Alert when reached last card -->
          <div class="deck-footer-hint" v-if="currentCardIndex === activeFlashcards.length - 1">
            <span>✨ Bạn đã đi tới cuối bộ flashcard! Quay lại danh sách để kiểm tra.</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppIcon from './icons/AppIcon.vue'
import { useStudySetStore } from '../stores/studySet'

const props = defineProps({
  studySets: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['update-vocab-status'])

const selectedSet = ref(null)
const viewMode = ref('list') // list, flashcard
const searchQuery = ref('')
const activeFilter = ref('all') // all, learned, review, unlearned

// Flashcard navigation state
const currentCardIndex = ref(0)
const isFlipped = ref(false)

// Select set
const selectSet = async (set, mode) => {
  selectedSet.value = set
  viewMode.value = mode
  searchQuery.value = ''
  activeFilter.value = 'all'
  currentCardIndex.value = 0
  isFlipped.value = false

  if (!set.words || set.words.length === 0) {
    try {
      const studySetStore = useStudySetStore()
      const cards = await studySetStore.fetchCardsForSet(set.id)
      selectedSet.value.words = cards || []
    } catch (e) {
      console.warn("Failed to load cards for set:", e)
    }
  }
}

// Start Flashcard Mode
const startFlashcardMode = () => {
  viewMode.value = 'flashcard'
  currentCardIndex.value = 0
  isFlipped.value = false
}

// Calculations
const getSetProgressPercentage = (set) => {
  const total = set.wordCount || 0
  if (total === 0) return 0
  const learned = set.learnedCount || 0
  return Math.round((learned / total) * 100)
}

const getWordCountByStatus = (status) => {
  if (!selectedSet.value || !selectedSet.value.words) return 0
  return selectedSet.value.words.filter(w => w.status === status).length
}

// Filters vocab list
const filteredWords = computed(() => {
  if (!selectedSet.value || !selectedSet.value.words) return []
  return selectedSet.value.words.filter(word => {
    // Check search query
    const matchSearch = 
      word.word.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      word.meaning.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      word.pronunciation.toLowerCase().includes(searchQuery.value.toLowerCase())
    
    // Check status filter
    const matchFilter = activeFilter.value === 'all' || word.status === activeFilter.value

    return matchSearch && matchFilter
  })
})

// Flashcard deck filters - all words in set
const activeFlashcards = computed(() => {
  return selectedSet.value ? (selectedSet.value.words || []) : []
})

const currentCard = computed(() => {
  return activeFlashcards.value[currentCardIndex.value] || {}
})

// Flashcard actions
const prevCard = () => {
  if (currentCardIndex.value > 0) {
    isFlipped.value = false
    setTimeout(() => {
      currentCardIndex.value--
    }, 150)
  }
}

const nextCard = () => {
  if (currentCardIndex.value < activeFlashcards.value.length - 1) {
    isFlipped.value = false
    setTimeout(() => {
      currentCardIndex.value++
    }, 150)
  }
}

const setCardStatus = (status) => {
  if (currentCard.value.id) {
    updateWordStatus(currentCard.value.id, status)
  }
}

// Emits status edits to save globally
const updateWordStatus = (wordId, status) => {
  emit('update-vocab-status', {
    setId: selectedSet.value.id,
    wordId,
    status
  })
}
</script>

<style scoped>
.vocab-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Header section */
.vocab-header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1.25rem;
}

.vocab-header-section h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.vocab-header-section p {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.vocab-mode-toggle {
  display: flex;
  background-color: var(--bg-badge);
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}

.toggle-btn {
  padding: 0.4rem 1rem;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  color: var(--text-body);
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.toggle-btn.active {
  background-color: var(--bg-card);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

/* Back Link */
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

/* Set Grid cards */
.study-sets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}

.study-set-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.study-set-card:hover {
  transform: translateY(-4px);
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
}

.set-meta {
  margin-bottom: 0.75rem;
}

.vocab-count-badge {
  background-color: var(--primary-light);
  color: var(--primary);
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: 50px;
}

.study-set-card h3 {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.5rem;
}

.study-set-card p {
  font-size: 0.85rem;
  color: var(--text-muted);
  flex-grow: 1;
  margin-bottom: 1.5rem;
}

/* Progress bar inside card */
.set-progress {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  margin-bottom: 1.25rem;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-body);
}

.set-progress-bar {
  height: 6px;
  background-color: var(--bg-badge);
  border-radius: 50px;
  overflow: hidden;
}

.set-progress-bar .fill {
  height: 100%;
  background-color: var(--success);
  border-radius: 50px;
  transition: width 0.4s ease;
}

.set-actions {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 0.75rem;
}

.set-btn {
  padding: 0.6rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
}

.set-btn.list {
  background-color: var(--bg-badge);
  color: var(--text-title);
}
.set-btn.list:hover { background-color: var(--border-color); }

.set-btn.flash {
  background-color: var(--primary);
  color: #fff;
}
.set-btn.flash:hover { background-color: var(--primary-hover); }

/* Set title inside subview */
.set-title-row {
  margin-bottom: 1.5rem;
}

.set-title-row h3 {
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.set-title-row p {
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* LIST MODE: Filter Panel & Search */
.filter-panel {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  margin-bottom: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--text-muted);
}

.search-box input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-app);
}

.search-box input:focus {
  border-color: var(--primary);
  background-color: var(--bg-card);
}

.status-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.filter-tag {
  padding: 0.35rem 0.75rem;
  border-radius: 50px;
  font-size: 0.8rem;
  font-weight: 600;
  background-color: var(--bg-badge);
  color: var(--text-body);
  border: 1px solid transparent;
}

.filter-tag:hover {
  background-color: var(--border-color);
}

.filter-tag.active {
  border-color: var(--primary);
  background-color: var(--primary-light);
  color: var(--primary);
}

.filter-tag.success.active {
  border-color: var(--success);
  background-color: var(--success-light);
  color: var(--success);
}

.filter-tag.warning.active {
  border-color: var(--warning);
  background-color: var(--warning-light);
  color: var(--warning);
}

.filter-tag.unlearned.active {
  border-color: var(--text-muted);
  background-color: var(--bg-hover);
  color: var(--text-muted);
}

/* Vocab rows */
.vocab-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.vocab-row-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  display: grid;
  grid-template-columns: 1fr 2fr 1.2fr;
  align-items: center;
  gap: 1.5rem;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), border-color var(--transition-fast);
}

.vocab-row-card:hover {
  border-color: var(--border-color-hover);
  transform: translateX(3px);
}

.vocab-row-card.learned {
  border-left: 4px solid var(--success);
}
.vocab-row-card.review {
  border-left: 4px solid var(--warning);
}
.vocab-row-card.unlearned {
  border-left: 4px solid var(--text-muted);
}

.row-korean {
  display: flex;
  flex-direction: column;
}

.row-korean .hangul {
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 1.45rem;
  font-weight: 700;
  color: var(--text-title);
}

.row-korean .pronounce {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.row-vietnamese {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.row-vietnamese .meaning {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text-title);
}

.row-vietnamese .example {
  font-size: 0.8rem;
  color: var(--text-body);
  opacity: 0.9;
}

.row-status-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.status-btn {
  padding: 0.35rem 0.65rem;
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid var(--border-color);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.status-btn.check:hover, .status-btn.check.active {
  background-color: var(--success-light);
  border-color: var(--success);
  color: var(--success);
}

.status-btn.warn:hover, .status-btn.warn.active {
  background-color: var(--warning-light);
  border-color: var(--warning);
  color: var(--warning);
}

.status-btn.unset:hover, .status-btn.unset.active {
  background-color: var(--bg-badge);
  border-color: var(--text-muted);
  color: var(--text-title);
}

/* FLASHCARD DECK */
.flashcard-deck {
  max-width: 540px;
  margin: 1rem auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.deck-progress {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
}

.progress-bar-tiny {
  height: 5px;
  background-color: var(--border-color);
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar-tiny .fill {
  height: 100%;
  background-color: var(--primary);
  transition: width 0.3s ease;
}

/* 3D Flashcard box */
.flashcard-box {
  height: 320px;
  position: relative;
  perspective: 1000px;
  cursor: pointer;
}

.card-side {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  transition: transform var(--transition-slow);
}

.card-front {
  transform: rotateY(0deg);
  background: radial-gradient(circle at 50% 10%, var(--primary-light) 0%, var(--bg-card) 60%);
}

.card-back {
  transform: rotateY(180deg);
  background: radial-gradient(circle at 50% 10%, var(--success-light) 0%, var(--bg-card) 60%);
}

.flashcard-box.flipped .card-front {
  transform: rotateY(-180deg);
}

.flashcard-box.flipped .card-back {
  transform: rotateY(0deg);
}

.card-indicator {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--text-muted);
}

.korean-text {
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 3.2rem;
  font-weight: 900;
  color: var(--text-title);
  letter-spacing: -1px;
}

.meaning-text {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-title);
  text-align: center;
}

.pronounce-text {
  font-size: 1.15rem;
  color: var(--primary);
  font-weight: 600;
  margin-top: -0.5rem;
}

.card-example-box {
  background-color: var(--bg-app);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.85rem 1.25rem;
  width: 100%;
  text-align: left;
}

.card-example-box .label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
  display: block;
  margin-bottom: 0.25rem;
}

.card-example-box .kr-ex {
  font-family: 'Noto Sans KR', sans-serif;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
}

.card-example-box .vi-ex {
  font-size: 0.85rem;
  color: var(--text-body);
}

.flip-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Deck controls */
.card-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.nav-btn {
  padding: 0.6rem 1.15rem;
  border-radius: var(--radius-sm);
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--text-title);
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.nav-btn:hover:not(:disabled) {
  background-color: var(--bg-hover);
  border-color: var(--primary);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.memorize-toolbar {
  display: flex;
  gap: 0.5rem;
}

.toolbar-btn {
  padding: 0.6rem 1rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  font-weight: 600;
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.toolbar-btn.review-btn:hover, .toolbar-btn.review-btn.active {
  background-color: var(--warning-light);
  border-color: var(--warning);
  color: var(--warning);
}

.toolbar-btn.learn-btn:hover, .toolbar-btn.learn-btn.active {
  background-color: var(--success-light);
  border-color: var(--success);
  color: var(--success);
}

.deck-footer-hint {
  text-align: center;
  color: var(--success);
  font-weight: 600;
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

@media (max-width: 768px) {
  .vocab-row-card {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  .row-status-actions {
    justify-content: flex-start;
  }
  .vocab-header-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
