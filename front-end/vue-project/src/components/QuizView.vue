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
        <!-- Quizzes under Reading category -->
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

      <!-- 2. Listening Skills Tab -->
      <template v-if="activeTab === 'listening'">
        <!-- Quizzes under Listening category -->
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
            <span class="badge score-badge">Điểm: {{ quiz.score }} / {{ quiz.totalScore || 10 }}</span>
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

      <div class="creator-body-grid">
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

              <!-- Additional POS (Loại từ) and Pronunciation (Phiên âm) Fields -->
              <div class="form-row-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 0.75rem; margin-bottom: 0.75rem;">
                <div class="form-group">
                  <label>Loại từ (Từ loại)</label>
                  <select v-model="q.wordType" class="creator-input-field" style="padding: 0.5rem 0.75rem;">
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
              Lưu đề thi
            </button>
          </div>
        </div>

        <!-- RIGHT PANEL: Từ vựng đã tạo (Matching Screenshot 2) -->
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
    <div v-else-if="activeQuiz" class="quiz-container-layout animate-scale">
      <!-- CỘT TRÁI: DÒNG CÂU HỎI & PHÂN TRANG -->
      <div class="quiz-stream">
        <header class="card-panel-quiz stream-header">
          <div class="quiz-title">
            <h1>{{ activeQuiz.title || 'Bài Thi Đánh Giá Kiến Thức' }}</h1>
            <p>Mã đề: #{{ activeQuiz.id || 'PRACTICE' }} | Quy định: {{ rowsPerPage }} câu/trang. Tổng số: {{ totalQuestionsCount }} câu</p>
          </div>
        </header>

        <!-- Vùng chứa danh sách câu hỏi của trang hiện tại -->
        <div class="questions-list-container" style="display: flex; flex-direction: column; gap: 0.75rem;">
          <div 
            v-for="(q, localIndex) in paginatedQuestions" 
            :key="q.id" 
            class="card-panel-quiz question-card"
            :id="'question-block-' + ((currentPage - 1) * rowsPerPage + localIndex)"
          >
            <div class="question-meta">
              <div class="question-tag">Câu hỏi {{ String((currentPage - 1) * rowsPerPage + localIndex + 1).padStart(2, '0') }}</div>
              <button 
                class="btn-bookmark" 
                :class="{ bookmarked: reviewStatus[q.id] }"
                @click="toggleReview(q.id)"
              >
                <span>⭐</span> {{ reviewStatus[q.id] ? 'Đang đánh dấu' : 'Đánh dấu xem lại' }}
              </button>
            </div>

            <h2 class="question-text">
              {{ q.question || '' }}
            </h2>

            <!-- Question Image -->
            <div v-if="q.imageUrl" class="question-image-container animate-fade" style="margin: 1rem 0; text-align: center;">
              <img 
                :src="q.imageUrl" 
                alt="Hình ảnh câu hỏi" 
                style="max-width: 100%; max-height: 320px; border-radius: 8px; border: 1px solid var(--border-color); object-fit: contain; box-shadow: var(--shadow-sm);"
              />
            </div>

            <!-- Standard Option Choice Question -->
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

            <!-- Listening (Audio Player + Option Choice) Question -->
            <div v-else-if="q.type === 'listening'" class="listening-block">
              <div class="audio-player-card" style="display: flex; align-items: center; gap: 1rem; padding: 1rem; background-color: var(--bg-badge); border-radius: var(--radius-md); border: 1px solid var(--border-color);">
                <!-- Custom MP3 / URL playback controls -->
                <audio 
                  v-if="q.audioUrl"
                  :src="q.audioUrl"
                  controls
                  style="flex-grow: 1; max-width: 100%; outline: none;"
                ></audio>
                
                <!-- Fallback to TTS -->
                <template v-else>
                  <button 
                    class="audio-play-btn" 
                    @click="playSpeech(q.koreanText)"
                    style="display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; border-radius: var(--radius-sm); border: 1px solid var(--border-color); background-color: var(--bg-card); cursor: pointer; font-weight: 600; color: var(--primary);"
                    title="Phát giọng đọc mẫu bằng AI"
                  >
                    <AppIcon name="play" size="20" />
                    <span>Phát Giọng AI (TTS) 🔊</span>
                  </button>
                  
                  <div class="audio-visualizer" :class="{ playing: isSpeaking && (playingKoreanText === q.koreanText || playingKoreanText === q.audioUrl) }">
                    <span class="bar bar-1"></span>
                    <span class="bar bar-2"></span>
                    <span class="bar bar-3"></span>
                    <span class="bar bar-4"></span>
                    <span class="bar bar-5"></span>
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

            <!-- Text Fill Question -->
            <div v-else-if="q.type === 'fill'" class="fill-input-box">
              <input 
                type="text" 
                v-model="userAnswers[q.id]" 
                placeholder="Nhập câu trả lời của bạn..."
                class="fill-text-field"
                style="width: 100%; padding: 1rem; border: 1px solid var(--border-color); border-radius: var(--radius-md); font-size: 1rem; background-color: var(--bg-card); color: var(--text-body);"
              >
              <p class="input-hint" style="margin-top: 0.5rem; font-size: 0.85rem; color: var(--text-muted);">* Điền từ hoặc cụm từ tiếng Hàn chính xác.</p>
            </div>
          </div>
        </div>

        <!-- Thanh điều hướng phân trang dữ liệu -->
        <div class="card-panel-quiz pagination-container">
          <div class="pagination-info" id="pagination-info">Trang {{ currentPage }} / {{ totalPages }}</div>
          <div class="pagination-buttons">
            <button class="btn-page btn-page-nav" id="btn-page-prev" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M15 18l-6-6 6-6"/></svg>
              Trang trước
            </button>
            <button 
              v-for="p in totalPages" 
              :key="p"
              class="btn-page btn-page-num" 
              :class="{ active: currentPage === p }" 
              @click="goToPage(p)"
            >
              {{ p }}
            </button>
            <button class="btn-page btn-page-nav" id="btn-page-next" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">
              Trang sau
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 18l6-6-6-6"/></svg>
            </button>
          </div>
        </div>
      </div>

      <!-- CỘT PHẢI: BẢNG TIẾN ĐỘ & TRẠNG THÁI TOÀN ĐỀ -->
      <aside class="card-panel-quiz sidebar-content">
        <div class="timer-widget">
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

        <div class="sidebar-title">Bảng điều hướng nhanh ({{ totalQuestionsCount }} câu)</div>

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
          >
            {{ index + 1 }}
          </button>
        </div>

        <div class="legend-list">
          <div class="legend-item">
            <div class="legend-color c-done"></div>
            <span>Đã chọn đáp án</span>
          </div>
          <div class="legend-item">
            <div class="legend-color c-review"></div>
            <span>Đánh dấu xem lại sau</span>
          </div>
          <div class="legend-item">
            <div class="legend-color c-empty"></div>
            <span>Chưa làm</span>
          </div>
        </div>

        <button class="btn-submit" @click="submitQuiz">Nộp bài thi</button>
      </aside>
    </div>

    <!-- DETAILED QUIZ RESULTS REVIEW VIEW -->
    <div v-else-if="viewingResultQuiz" class="results-review-container animate-scale">
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
              <div class="score-summary-top">
                <span class="score-summary-num">{{ viewingResultQuiz.score || 0 }}</span>
                <span class="score-summary-total">/ {{ viewingResultQuiz.totalScore || 10 }}</span>
              </div>
              <p class="score-summary-date"><AppIcon name="clock" size="12" /> {{ formatDate(viewingResultQuiz.completedAt) }}</p>
              <div class="score-summary-tags">
                <span class="score-tag xp-tag"><AppIcon name="award" size="12" /> +{{ Math.round((viewingResultQuiz.score || 0) * 10) }} XP</span>
                <span class="score-tag topik-tag" v-if="viewingResultQuiz.topikLevelResult">🏆 {{ viewingResultQuiz.topikLevelResult }}</span>
              </div>
              
              <!-- Retry Button inside score card -->
              <button class="retry-btn-sidebar" @click="retryQuiz(viewingResultQuiz)">
                <AppIcon name="play" size="16" /> Làm lại bài thi
              </button>
            </div>

            <!-- Donut stats -->
            <div class="review-stat-card review-donut-card">
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
                  :class="isUserCorrect(q) ? 'correct' : 'incorrect'"
                  @click="scrollToReviewCard(q.id)"
                  :title="'Câu ' + (idx + 1)"
                >{{ idx + 1 }}</button>
              </div>
            </div>
          </div>
        </aside>

        <!-- MAIN FEED -->
        <main class="review-main">
          <div class="review-filter-bar">
            <button
              class="filter-chip"
              :class="{ active: reviewFilter === 'all' }"
              @click="reviewFilter = 'all'"
            >Tất cả</button>
            <button
              class="filter-chip"
              :class="{ active: reviewFilter === 'incorrect' }"
              @click="reviewFilter = 'incorrect'"
            >Câu sai (✕)</button>
            <button
              class="filter-chip"
              :class="{ active: reviewFilter === 'correct' }"
              @click="reviewFilter = 'correct'"
            >Câu đúng (✓)</button>
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
                <span class="feed-status-pill" :class="isUserCorrect(q) ? 'correct' : 'incorrect'">
                  {{ isUserCorrect(q) ? '✓ CHÍNH XÁC' : (hasUserAnswer(q) ? '✕ CHƯA ĐÚNG' : '○ CHƯA LÀM') }}
                </span>
              </div>

              <h2 class="feed-question-text">{{ q.question || '' }}</h2>

              <!-- Question Image in Review -->
              <div v-if="q.imageUrl" class="question-image-container-review animate-fade" style="margin: 1rem 0; text-align: center;">
                <img 
                  :src="q.imageUrl" 
                  alt="Hình ảnh câu hỏi" 
                  style="max-width: 100%; max-height: 250px; border-radius: 8px; border: 1px solid var(--border-color); object-fit: contain;"
                />
              </div>

              <!-- Replay buttons in Review for Listening -->
              <div class="review-listening-replay" v-if="q.type === 'listening'" style="margin-top: 0.5rem;">
                <audio v-if="q.audioUrl" :src="q.audioUrl" controls style="max-width: 100%; border-radius: var(--radius-sm); outline: none;"></audio>
                <button v-else class="replay-btn" @click="playSpeech(q.koreanText)">
                  <AppIcon name="play" size="14" /> Phát lại giọng đọc AI: <strong>{{ q.koreanText }}</strong>
                </button>
              </div>

              <!-- Multiple choice / listening option comparison grid -->
              <div v-if="q.options && q.options.length" class="feed-options-grid">
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

              <!-- Fallback text-answer comparison (fill-in-the-blank) -->
              <div v-else class="review-answers-grid">
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

              <!-- Analysis box -->
              <div class="feed-analysis-box" v-if="q.explanation">
                <div class="feed-analysis-icon">💡</div>
                <div class="feed-analysis-body">
                  <h4 class="feed-analysis-title">Tại sao {{ isUserCorrect(q) ? 'bạn đúng?' : 'bạn sai?' }}</h4>
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
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, onMounted, nextTick } from 'vue'
import AppIcon from './icons/AppIcon.vue'
import { useQuizStore } from '../stores/quiz'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

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

// New states for layout-quiz.html styling compatibility
const currentPage = ref(1)
const rowsPerPage = ref(10)
const reviewStatus = ref({})

const getAlpha = (idx) => {
  return ['A', 'B', 'C', 'D'][idx] || ''
}

const totalPages = computed(() => {
  if (!activeQuiz.value || !activeQuiz.value.questions) return 1
  return Math.ceil(activeQuiz.value.questions.length / rowsPerPage.value)
})

const paginatedQuestions = computed(() => {
  if (!activeQuiz.value || !activeQuiz.value.questions) return []
  const start = (currentPage.value - 1) * rowsPerPage.value
  return activeQuiz.value.questions.slice(start, start + rowsPerPage.value)
})

const totalQuestionsCount = computed(() => {
  return activeQuiz.value?.questions?.length || 0
})

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

const goToPage = (p) => {
  if (p < 1 || p > totalPages.value) return
  currentPage.value = p
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const toggleReview = (qId) => {
  reviewStatus.value[qId] = !reviewStatus.value[qId]
}

const selectOption = (qId, opt) => {
  userAnswers.value[qId] = opt
}

const jumpToQuestion = (index) => {
  const targetPage = Math.floor(index / rowsPerPage.value) + 1
  if (currentPage.value !== targetPage) {
    currentPage.value = targetPage
  }
  setTimeout(() => {
    const el = document.getElementById(`question-block-${index}`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
    }
  }, 50)
}

// Audio playing TTS state
const isSpeaking = ref(false)
const playingKoreanText = ref('')
const currentAudio = ref(null)

// Result view states
const viewingResultQuiz = ref(null)
const reviewFilter = ref('all') // all, correct, incorrect

const currentQuestion = computed(() => {
  if (!activeQuiz.value || !activeQuiz.value.questions || !Array.isArray(activeQuiz.value.questions)) {
    return {}
  }
  return activeQuiz.value.questions[currentQuestionIndex.value] || {}
})

onMounted(() => {
  // Practice quizzes are initialized directly in memory. LocalStorage access is bypassed to prevent sandbox browser security blocks.
})

// Categories lists - teacher assigned quizzes only for Bài tập page
const readingQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  return list.filter(q => q.status !== 'completed' && q.quizType !== 'listening')
})

const listeningQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  return list.filter(q => q.status !== 'completed' && q.quizType === 'listening')
})

const completedQuizzes = computed(() => {
  const list = Array.isArray(props.quizzes) ? props.quizzes : []
  const teacherCompleted = list.filter(q => q.status === 'completed')
  return [...teacherCompleted].sort((a, b) => {
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
    toast.error("Trình duyệt không hỗ trợ phát âm thanh tự động.")
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
    toast.error("Không thể phát file âm thanh này. Hãy kiểm tra lại định dạng file hoặc liên kết đường dẫn.")
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

const shuffleArray = (array) => {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    const temp = array[i];
    array[i] = array[j];
    array[j] = temp;
  }
  return array;
}

const cleanAnswer = (ans) => {
  return (ans || '')
    .toString()
    .trim()
    .toLowerCase()
    .replace(/^[a-d]\.\s*/, ''); // strip "A. ", "B. ", "C. ", "D. " prefix
}

// Start a quiz - heavily guarded against undefined arrays
const startQuiz = async (quiz) => {
  if (!quiz) return

  let fullQuiz = quiz
  if ((!quiz.questions || quiz.questions.length === 0) && !quiz.id.toString().startsWith('practice-')) {
    try {
      const quizStore = useQuizStore()
      fullQuiz = await quizStore.fetchQuizDetails(quiz.id)
    } catch (e) {
      console.error("Failed to fetch quiz details:", e)
      toast.error("Không thể lấy nội dung câu hỏi bài tập.")
      return
    }
  }
  
  // Map backend quiz questions to the format expected by QuizView.vue
  const mappedQuestions = (fullQuiz.questions || []).map(q => {
    // If it's already a frontend-style question, return it
    if (q.options && q.question) return q;

    // Build raw options list
    let rawOptions = [];
    if (q.wrongAnswers && q.wrongAnswers.length === 4) {
      rawOptions = [...q.wrongAnswers];
    } else {
      if (q.correctAnswer) rawOptions.push(q.correctAnswer);
      if (q.wrongAnswers && Array.isArray(q.wrongAnswers)) {
        rawOptions.push(...q.wrongAnswers);
      }
    }

    const optionsList = [...rawOptions];

    // Determine the type
    let type = 'choice';
    if (q.questionType === 'ESSAY') {
      type = 'fill';
    } else if (q.audioUrl || q.section === 'LISTENING') {
      type = 'listening';
    }

    return {
      id: q.id,
      type: type,
      question: q.questionText || '',
      koreanText: q.koreanText || '',
      audioUrl: q.audioUrl || '',
      audioSource: q.audioSource || 'tts',
      imageUrl: q.imageUrl || '',
      options: optionsList,
      correctAnswer: q.correctAnswer || '',
      explanation: q.explanation || `Đáp án đúng là: ${q.correctAnswer}.`
    };
  });

  const mappedQuiz = {
    ...fullQuiz,
    timeLimit: fullQuiz.timeLimitMins || fullQuiz.timeLimit || 10,
    questions: mappedQuestions
  };

  activeQuiz.value = mappedQuiz
  currentQuestionIndex.value = 0
  userAnswers.value = {}
  currentPage.value = 1
  reviewStatus.value = {}
  stopAudio()

  // Set default values for all inputs safely
  if (mappedQuiz.questions && Array.isArray(mappedQuiz.questions)) {
    mappedQuiz.questions.forEach(q => {
      if (q && q.id) {
        userAnswers.value[q.id] = ''
      }
    })
  }

  // Start countdown timer
  timerMinutes.value = mappedQuiz.timeLimit || 10
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
      if (cleanAnswer(studentAns) === cleanAnswer(correctAns)) {
        correctCount++
      }
    } else if (q.type === 'fill') {
      let matchesAlternative = false
      if (q.alternativeAnswers) {
        matchesAlternative = q.alternativeAnswers.some(alt => (alt || '').toString().trim().toLowerCase() === studentAns)
      }
      if (cleanAnswer(studentAns) === cleanAnswer(correctAns) || matchesAlternative) {
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
  reviewFilter.value = 'all'
  viewingResultQuiz.value = completedRef
}

// View details of previous submission
const viewResults = async (quiz) => {
  let fullQuiz = quiz
  if ((!quiz.questions || quiz.questions.length === 0) && !quiz.id.toString().startsWith('practice-')) {
    try {
      const quizStore = useQuizStore()
      fullQuiz = await quizStore.fetchQuizDetails(quiz.id)
    } catch (e) {
      console.error("Failed to fetch quiz details:", e)
      toast.error("Không thể lấy nội dung câu hỏi bài tập.")
      return
    }
  }
  reviewFilter.value = 'all'
  viewingResultQuiz.value = fullQuiz
}

// Grader check helper for UI display - with fallback typeguards
const isUserCorrect = (question) => {
  if (!question || !viewingResultQuiz.value || !viewingResultQuiz.value.userAnswers) return false
  const studentAns = (viewingResultQuiz.value.userAnswers[question.id] || '').toString().trim().toLowerCase()
  const correctAns = (question.correctAnswer || '').toString().trim().toLowerCase()
  
  if (question.type === 'choice' || question.type === 'match' || question.type === 'listening') {
    return cleanAnswer(studentAns) === cleanAnswer(correctAns)
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

// Helpers for the redesigned results-review screen
const correctAnswersCount = (quiz) => {
  if (!quiz || !quiz.questions) return 0
  return quiz.questions.filter(q => isUserCorrect(q)).length
}

const scorePercent = (score, total) => {
  const t = total || 10
  const s = score || 0
  if (t <= 0) return 0
  return Math.max(0, Math.min(100, (s / t) * 100))
}

const scoreTier = (score, total) => {
  const pct = scorePercent(score, total)
  if (pct >= 80) return 'tier-excellent'
  if (pct >= 50) return 'tier-good'
  return 'tier-weak'
}

const scoreTierLabel = (score, total) => {
  const pct = scorePercent(score, total)
  if (pct >= 80) return '🌟 Xuất sắc'
  if (pct >= 50) return '👍 Khá tốt'
  return '💪 Cần cố gắng thêm'
}

const originalQuestionIndex = (question) => {
  if (!viewingResultQuiz.value || !viewingResultQuiz.value.questions) return 0
  return viewingResultQuiz.value.questions.findIndex(q => q.id === question.id)
}

const filteredReviewQuestions = computed(() => {
  if (!viewingResultQuiz.value || !viewingResultQuiz.value.questions) return []
  const list = viewingResultQuiz.value.questions
  if (reviewFilter.value === 'correct') return list.filter(q => isUserCorrect(q))
  if (reviewFilter.value === 'incorrect') return list.filter(q => !isUserCorrect(q))
  return list
})

// CSS conic-gradient donut chart style: green slice = correct, red slice = incorrect
const donutStyle = (quiz) => {
  const total = (quiz.questions || []).length || 1
  const correct = correctAnswersCount(quiz)
  const pct = (correct / total) * 100
  return {
    background: `conic-gradient(var(--success) 0% ${pct}%, var(--danger) ${pct}% 100%)`
  }
}

const questionTypeLabel = (q) => {
  if (q.type === 'listening') return '🎧 Nghe hiểu'
  if (q.type === 'fill') return '✍️ Điền từ'
  if (q.type === 'match') return '🔗 Nối câu'
  return '📖 Đọc hiểu'
}

const hasUserAnswer = (q) => {
  if (!viewingResultQuiz.value || !viewingResultQuiz.value.userAnswers) return false
  const ans = viewingResultQuiz.value.userAnswers[q.id]
  return ans !== null && ans !== undefined && ans !== ''
}

// Jump from the sidebar matrix to a question card in the feed, switching filter if needed
const scrollToReviewCard = (qId) => {
  if (!viewingResultQuiz.value || !viewingResultQuiz.value.questions) return
  const q = viewingResultQuiz.value.questions.find(item => item.id === qId)
  if (!q) return

  const correct = isUserCorrect(q)
  if (reviewFilter.value === 'correct' && !correct) reviewFilter.value = 'all'
  if (reviewFilter.value === 'incorrect' && correct) reviewFilter.value = 'all'

  nextTick(() => {
    setTimeout(() => {
      const el = document.getElementById(`q-card-${qId}`)
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' })
        el.classList.add('flash-highlight')
        setTimeout(() => el.classList.remove('flash-highlight'), 1200)
      }
    }, 50)
  })
}

// Retry a completed quiz straight from the results screen
const retryQuiz = (quiz) => {
  viewingResultQuiz.value = null
  startQuiz(quiz)
}

// Practice Quiz Creator Handlers
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
    toast.error("Không thể phân tích dữ liệu từ vựng. Vui lòng kiểm tra định dạng dòng!")
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
      meaning + ' (khác)',
      'Không có nghĩa này',
      'Từ trái nghĩa'
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
      wordType: 'Động từ',
      pronunciation: '',
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
    wordType: 'Động từ',
    pronunciation: '',
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
      toast.warning("Lưu ý: Khuyên dùng file MP3 nhỏ dưới 1.2MB để đảm bảo lưu trữ nhanh trong trình duyệt.")
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
    toast.warning("Vui lòng nhập tên đề thi ôn tập.")
    return
  }

  // Validate questions
  for (let i = 0; i < newQuizQuestions.value.length; i++) {
    const q = newQuizQuestions.value[i]
    if (!q.question.trim()) {
      toast.warning(`Vui lòng nhập câu hỏi phụ cho câu hỏi thứ ${i + 1}.`)
      return
    }
    if (q.type === 'listening') {
      if (q.audioSource === 'tts' && (!q.koreanText || !q.koreanText.trim())) {
        toast.warning(`Vui lòng nhập đoạn tiếng Hàn để AI phát âm cho câu hỏi ${i + 1}.`)
        return
      }
      if (q.audioSource === 'file' && (!q.audioUrl || !q.audioUrl.trim())) {
        toast.warning(`Vui lòng tải lên file âm thanh MP3 hoặc chèn liên kết link cho câu hỏi ${i + 1}.`)
        return
      }
    }
    for (let j = 0; j < 4; j++) {
      if (!q.options[j] || !q.options[j].trim()) {
        toast.warning(`Vui lòng điền đáp án ${String.fromCharCode(65 + j)} cho câu hỏi thứ ${i + 1}.`)
        return
      }
    }
  }

  // Format question array
  const formattedQuestions = newQuizQuestions.value.map((q, idx) => {
    const opts = q.options.map((opt, optIdx) => `${String.fromCharCode(65 + optIdx)}. ${(opt || '').trim()}`)
    const correctVal = opts[q.correctOptionIndex]
    
    return {
      id: `pq-${idx}-${Date.now()}`,
      type: q.type,
      question: (q.question || '').trim(),
      koreanText: q.audioSource === 'tts' ? (q.koreanText || '').trim() : null,
      audioUrl: q.audioSource === 'file' ? (q.audioUrl || '').trim() : null,
      options: opts,
      correctAnswer: correctVal,
      explanation: q.type === 'listening'
        ? (q.audioSource === 'tts' 
            ? `Đáp án đúng là: ${correctVal}. Từ tiếng Hàn được phát âm: '${(q.koreanText || '').trim()}'.`
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
  localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))

  isCreatingQuiz.value = false
  activeTab.value = creatorQuizType.value
}

const deletePracticeQuiz = (quizId) => {
  if (confirm("Bạn có chắc chắn muốn xóa bài thi tự luyện này không?")) {
    practiceQuizzes.value = practiceQuizzes.value.filter(q => q.id !== quizId)
    localStorage.setItem('sk_practice_quizzes', JSON.stringify(practiceQuizzes.value))
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
  max-width: 1200px;
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

/* ==========================================================================
   RESULTS REVIEW SCREEN (Redesigned)
   ========================================================================== */
.results-review-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 1rem 0;
}

.review-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.review-title-area {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.review-title-text {
  display: flex;
  flex-direction: column;
}

.review-page-title {
  font-size: 1.85rem;
  font-weight: 800;
  color: var(--text-title);
  line-height: 1.2;
}

.review-page-subtitle {
  font-size: 0.95rem;
  color: var(--text-muted);
  font-weight: 500;
  margin-top: 0.15rem;
}

.btn-back-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  color: var(--text-title);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.btn-back-circle:hover {
  background-color: var(--primary-light);
  border-color: var(--primary);
  color: var(--primary);
  transform: translateX(-4px);
  box-shadow: 0 4px 12px var(--primary-glow);
}

.retry-btn-sidebar {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background-color: var(--primary);
  color: white;
  padding: 0.8rem;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.9rem;
  border: none;
  cursor: pointer;
  margin-top: 1.25rem;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 10px var(--primary-glow);
}

.retry-btn-sidebar:hover {
  background-color: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--primary-glow);
}

.review-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 2rem;
  align-items: start;
}

.review-sidebar {
  grid-column: 2;
  grid-row: 1;
  position: sticky;
  top: 90px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  z-index: 10;
}

.review-sidebar-sticky {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.review-stat-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.score-summary-card {
  text-align: center;
  background: linear-gradient(135deg, var(--bg-card), var(--primary-light));
}

.score-summary-top {
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.score-summary-num {
  font-size: 3rem;
  font-weight: 800;
  color: var(--primary);
  line-height: 1;
}

.score-summary-total {
  font-size: 1.25rem;
  color: var(--text-muted);
  font-weight: 700;
  margin-left: 0.25rem;
}

.score-summary-date {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin: 0.5rem 0 1rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
}

.score-summary-tags {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.score-tag {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
}

.xp-tag {
  background-color: rgba(241, 196, 15, 0.15);
  color: #b7860b;
}

.topik-tag {
  background-color: var(--primary-light);
  color: var(--primary);
}

.sidebar-label {
  font-size: 0.75rem;
  font-weight: 800;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 1rem;
}

.donut-wrap {
  display: flex;
  justify-content: center;
  margin: 1rem 0;
}

.donut-chart {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
}

.donut-hole {
  width: 106px;
  height: 106px;
  background-color: var(--bg-card);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.donut-percent {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-title);
}

.donut-caption {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
}

.stat-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.stat-box {
  padding: 0.75rem;
  border-radius: 16px;
  text-align: center;
}

.stat-box.correct-box {
  background-color: var(--success-light);
}

.stat-box-label {
  font-size: 0.65rem;
  font-weight: 800;
  text-transform: uppercase;
  color: var(--success);
}

.stat-box-num {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--success);
  margin-top: 0.15rem;
}

.stat-box.incorrect-box {
  background-color: var(--danger-light);
}

.stat-box.incorrect-box .stat-box-label {
  color: var(--danger);
}

.stat-box.incorrect-box .stat-box-num {
  color: var(--danger);
}

.review-matrix-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.5rem;
}

.review-matrix-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
  background-color: var(--bg-card);
  color: var(--text-body);
}

.review-matrix-item.correct {
  background-color: var(--success-light);
  color: var(--success);
  border-color: var(--success-border);
}

.review-matrix-item.incorrect {
  background-color: var(--danger-light);
  color: var(--danger);
  border-color: var(--danger-light);
}

.review-matrix-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.review-main {
  grid-column: 1;
  grid-row: 1;
  flex-grow: 1;
}

.review-filter-bar {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-sm);
}

.filter-chip {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  font-weight: 700;
  border-radius: 10px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  background-color: transparent;
}

.filter-chip:hover {
  background-color: var(--bg-hover);
  color: var(--text-title);
}

.filter-chip.active {
  background-color: var(--primary);
  color: white;
}

.filter-count {
  margin-left: auto;
  padding-right: 1rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
  text-transform: uppercase;
}

.review-feed {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.review-feed-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 2rem;
  box-shadow: var(--shadow-sm);
  scroll-margin-top: 3rem;
  transition: all var(--transition-fast);
}

.review-feed-card:hover {
  border-color: var(--primary-glow);
  box-shadow: var(--shadow-md);
}

.feed-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.feed-card-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.feed-index-badge {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background-color: var(--text-title);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  font-weight: 800;
}

.feed-type-label {
  font-size: 0.65rem;
  font-weight: 800;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  display: block;
}

.feed-status-pill {
  font-size: 0.7rem;
  font-weight: 800;
  padding: 0.4rem 0.85rem;
  border-radius: 99px;
  letter-spacing: 0.02em;
}

.feed-status-pill.correct {
  background-color: var(--success-light);
  color: var(--success);
}

.feed-status-pill.incorrect {
  background-color: var(--danger-light);
  color: var(--danger);
}

.feed-question-text {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 2rem;
  line-height: 1.45;
}

.feed-options-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.feed-option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-radius: 16px;
  border: 1.5px solid var(--border-color);
  background-color: var(--bg-card);
  transition: all 0.2s ease;
}

.feed-option-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.feed-option-alpha {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 800;
  border: 1px solid rgba(0, 0, 0, 0.05);
  color: var(--text-muted);
}

.feed-option-text {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-body);
}

.feed-option-icon {
  font-size: 0.95rem;
  font-weight: 800;
}

.feed-option-item.is-correct {
  background-color: var(--success-light);
  border-color: var(--success);
  color: var(--success);
}

.feed-option-item.is-correct .feed-option-alpha {
  background-color: var(--success);
  color: white;
}

.feed-option-item.is-correct .feed-option-text {
  color: var(--success);
}

.feed-option-item.is-user-wrong {
  background-color: var(--danger-light);
  border-color: var(--danger);
  color: var(--danger);
}

.feed-option-item.is-user-wrong .feed-option-alpha {
  background-color: var(--danger);
  color: white;
}

.feed-option-item.is-user-wrong .feed-option-text {
  color: var(--danger);
}

.feed-analysis-box {
  background-color: var(--bg-badge);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
}

.feed-analysis-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background-color: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
  color: var(--primary);
}

.feed-analysis-body {
  flex-grow: 1;
}

.feed-analysis-title {
  font-size: 0.85rem;
  font-weight: 800;
  color: var(--text-title);
  margin-bottom: 0.25rem;
  text-transform: uppercase;
}

.feed-analysis-text {
  font-size: 0.9rem;
  line-height: 1.5;
  color: var(--text-body);
}

.feed-vocab-tags {
  margin-top: 1rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.feed-vocab-tag {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  padding: 0.3rem 0.65rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-muted);
}

.flash-highlight {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 4px var(--primary-glow) !important;
}

@media (max-width: 992px) {
  .review-layout {
    grid-template-columns: 1fr;
  }
  .review-sidebar {
    grid-column: auto;
    grid-row: auto;
    position: static;
  }
  .review-main {
    grid-column: auto;
    grid-row: auto;
  }
}

@media (max-width: 768px) {
  .feed-options-grid {
    grid-template-columns: 1fr;
  }
  .review-page-header {
    flex-direction: column;
    align-items: flex-start;
  }
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

/* 2-column Layout for Student Creator */
.creator-body-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.75rem;
  align-items: start;
}

@media (max-width: 1024px) {
  .creator-body-grid {
    grid-template-columns: 1fr;
  }
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

.table-container-side {
  overflow-x: auto;
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
  letter-spacing: 0.5px;
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

.question-icon {
  color: var(--danger, #ef4444);
  font-weight: 700;
  font-size: 0.95rem;
}

.q-text-group {
  display: flex;
  flex-direction: column;
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
  white-space: nowrap;
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
  white-space: nowrap;
}

.delete-mini-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0 0.25rem;
  transition: color var(--transition-fast);
}

.delete-mini-btn:hover {
  color: var(--danger);
}

.import-q-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--text-title);
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.import-q-btn:hover {
  background-color: var(--bg-hover);
  border-color: var(--primary);
  color: var(--primary);
}

/* Modal Floating Overlay CSS */
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
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.15), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
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
  color: var(--text-title);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: var(--danger);
}

.cancel-btn {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  color: var(--text-body);
  padding: 0.6rem 1.2rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.cancel-btn:hover {
  background-color: var(--bg-hover);
}

.submit-btn {
  background-color: var(--primary);
  border: none;
  color: #ffffff;
  padding: 0.6rem 1.2rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.submit-btn:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .quiz-header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .quiz-header-section .primary-btn {
    align-self: flex-start;
  }

  .active-quiz-container {
    padding: 1.25rem;
    border-radius: var(--radius-md);
  }

  .modal-content {
    width: 95%;
    max-height: 90vh;
    overflow-y: auto;
  }

  .creator-body-grid {
    grid-template-columns: 1fr;
  }
}

/* ==========================================================================
   QUIZ ACTIVE RUN LAYOUT (layout-quiz.html)
   ========================================================================== */
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

.quiz-stream {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stream-header {
  padding: 2rem 2.5rem;
}

.quiz-title h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 0.25rem;
}

.quiz-title p {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 500;
}

.question-card {
  padding: 1.25rem 1.75rem;
  transition: all 0.2s ease;
  scroll-margin-top: 2rem;
}

.question-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.question-tag {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.btn-bookmark {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.3rem 0.6rem;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
}

.btn-bookmark:hover {
  background: var(--bg-badge);
  color: #f59e0b;
}

.btn-bookmark.bookmarked {
  color: #f59e0b;
  font-weight: 700;
}

.question-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-title);
  line-height: 1.5;
  margin-bottom: 1rem;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1.1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: var(--bg-card);
}

.option-item input[type="radio"] {
  display: none;
}

.option-alpha {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--bg-badge);
  border-radius: var(--radius-sm);
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--text-muted);
  margin-right: 0.75rem;
  transition: all 0.2s ease;
}

.option-label {
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--text-body);
}

.option-item:hover {
  border-color: var(--primary);
  background: var(--bg-badge);
  transform: translateX(4px);
}

.option-item.selected {
  border-color: var(--primary);
  background: var(--primary-light);
}

.option-item.selected .option-alpha {
  background: var(--primary);
  color: white;
}

.option-item.selected .option-label {
  color: var(--primary);
  font-weight: 600;
}

/* Thanh phân trang ở cuối luồng câu hỏi */
.pagination-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.25rem 2rem;
    margin-top: 0.5rem;
    background: var(--bg-card);
    border: 1px solid var(--border-color);
    border-radius: 16px;
}

.pagination-info {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-muted);
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.pagination-buttons {
    display: flex;
    align-items: center;
    gap: 0.6rem;
}

/* Style chung cho nút trang */
.btn-page {
    height: 42px;
    padding: 0 1rem;
    border-radius: 10px;
    border: 1px solid var(--border-color);
    background: var(--bg-card);
    font-weight: 600;
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.2s ease;
    color: var(--text-body);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.4rem;
    box-shadow: 0 2px 4px rgba(15, 23, 42, 0.02);
    user-select: none;
}

.btn-page-nav {
    padding: 0 1.1rem;
}

/* Nút trang dạng số vuông bo nhẹ */
.btn-page-num {
    width: 42px;
    padding: 0;
}

/* Hiệu ứng Hover */
.btn-page:hover:not(:disabled) {
    border-color: var(--primary);
    color: var(--primary);
    background: var(--primary-light);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

/* Trang đang được chọn (Active) */
.btn-page.active {
    background: var(--primary);
    border-color: var(--primary);
    color: white;
    font-weight: 700;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

/* Trạng thái bị vô hiệu hóa (Disabled) */
.btn-page:disabled {
    opacity: 0.35;
    cursor: not-allowed;
    background: #f1f5f9;
    border-color: var(--border-color);
    color: var(--text-muted);
    transform: none !important;
    box-shadow: none !important;
}

/* BẢNG TRẠNG THÁI CÂU HỎI */
.sidebar-content {
  padding: 1.75rem;
  position: sticky;
  top: 2rem;
}

.timer-widget {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fef2f2;
  border: 1px solid #fca5a5;
  padding: 0.85rem 1.25rem;
  border-radius: var(--radius-md);
  color: #ef4444;
  font-weight: 700;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}

.timer-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #b91c1c;
}

.progress-wrapper {
  margin-bottom: 1.5rem;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  font-weight: 700;
  margin-bottom: 0.4rem;
  color: var(--text-muted);
}

.progress-track {
  height: 6px;
  background: var(--border-color);
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #10b981;
  width: 0%;
  border-radius: 10px;
  transition: width 0.3s ease;
}

.sidebar-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 1rem;
}

.matrix-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.6rem;
  margin-bottom: 1.5rem;
}

.matrix-item {
  aspect-ratio: 1;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: var(--text-body);
}

.matrix-item:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.matrix-item.done {
  background: #e6f4ea !important;
  border-color: #10b981 !important;
  color: #065f46 !important;
}

.matrix-item.review {
  background: #fffbeb !important;
  border-color: #f59e0b !important;
  color: #92400e !important;
  position: relative;
}

.matrix-item.review::after {
  content: '';
  position: absolute;
  top: 3px;
  right: 3px;
  width: 5px;
  height: 5px;
  background: #f59e0b;
  border-radius: 50%;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  padding-top: 1.25rem;
  border-top: 1px dashed var(--border-color);
  margin-bottom: 1.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--text-muted);
}

.legend-color {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

.legend-color.c-done {
  border-color: #10b981;
  background: #e6f4ea;
}

.legend-color.c-review {
  border-color: #f59e0b;
  background: #fffbeb;
}

.legend-color.c-empty {
  background: var(--bg-card);
}

.btn-submit {
  width: 100%;
  background: #10b981;
  color: white;
  padding: 0.9rem;
  font-size: 1rem;
  font-weight: 700;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.15);
  transition: all 0.2s ease;
}

.btn-submit:hover {
  background: #059669;
  transform: translateY(-1px);
}

@media (max-width: 992px) {
  .quiz-container-layout {
    grid-template-columns: 1fr;
  }

  .sidebar-content {
    position: static;
  }
}
</style>

