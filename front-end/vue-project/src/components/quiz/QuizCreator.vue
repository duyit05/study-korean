<template>
  <div class="quiz-creator-container animate-scale">
    <button class="back-link-btn" @click="isCreatingQuiz = false">
      <AppIcon name="chevron-left" size="16" /> Hủy và quay lại danh sách
    </button>

    <div class="creator-header">
      <h3>{{ creatorQuizType === 'listening' ? 'Thiết Kế Đề Thi Nghe (Listening) 🎧' : 'Thiết Kế Đề Đọc / Từ Vựng 📖' }}</h3>
      <p>{{ creatorQuizType === 'listening' ? 'Cấu trúc bài nghe bao gồm đoạn audio tự chọn (tải lên file .mp3 hoặc dán link) và 4 đáp án trắc nghiệm A B C D.' : 'Cấu trúc trắc nghiệm chữ kiểm tra mặt chữ, ngữ pháp và nghĩa từ vựng.' }}</p>
    </div>

    <div class="creator-body-grid">
      <div class="creator-form-card">
        <div class="form-group">
          <label for="new-quiz-title" class="label-bold">Tên đề thi tự luyện</label>
          <input type="text" id="new-quiz-title" v-model="newQuizTitle" placeholder="Ví dụ: Đề thi thử nghe tiếng Hàn sơ cấp" class="creator-input-field">
        </div>

        <div class="questions-builder-list">
          <div v-for="(q, qIdx) in newQuizQuestions" :key="qIdx" class="question-builder-item">
            <div class="builder-item-header">
              <h4>Câu hỏi {{ qIdx + 1 }}</h4>
              <button v-if="newQuizQuestions.length > 1" class="remove-q-btn" @click="removeQuestionFromBuilder(qIdx)">
                <AppIcon name="x" size="14" /> Xóa câu hỏi
              </button>
            </div>

            <!-- Listening audio source -->
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
                <input type="text" v-model="q.koreanText" placeholder="Ví dụ: 안녕하세요 hoặc 봄" class="creator-input-field">
              </div>
              <div class="form-group animate-fade" v-else style="margin-bottom: 1rem;">
                <label>Liên kết âm thanh (.mp3) hoặc chọn file từ máy tính</label>
                <input type="text" v-model="q.audioUrl" placeholder="Dán link audio: https://example.com/audio.mp3" class="creator-input-field" style="margin-bottom: 0.5rem;">
                <div class="file-uploader-box">
                  <input type="file" accept="audio/*" @change="handleAudioUpload($event, qIdx)" :id="'file-upload-' + qIdx" class="hidden-file-input">
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
              <input type="text" v-model="q.question" :placeholder="creatorQuizType === 'listening' ? 'Ví dụ: Lắng nghe đoạn audio hội thoại sau và chọn nghĩa đúng:' : 'Ví dụ: Từ nào sau đây có nghĩa là Mùa Đông?'" class="creator-input-field">
            </div>

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
                <input type="text" v-model="q.pronunciation" placeholder="Ví dụ: [ka-da]" class="creator-input-field">
              </div>
            </div>

            <div class="options-builder-grid">
              <div class="option-builder-row" v-for="(opt, optIdx) in 4" :key="optIdx">
                <div class="opt-label-wrapper">
                  <input type="radio" :name="'correct-opt-' + qIdx" :value="optIdx" v-model="q.correctOptionIndex" :id="'radio-' + qIdx + '-' + optIdx">
                  <label :for="'radio-' + qIdx + '-' + optIdx">Đáp án {{ String.fromCharCode(65 + optIdx) }}</label>
                </div>
                <input type="text" v-model="q.options[optIdx]" :placeholder="'Nhập câu trả lời ' + String.fromCharCode(65 + optIdx)" class="creator-input-field-opt">
              </div>
            </div>
            <p class="correct-hint">* Nhấp chọn nút tròn bên cạnh đáp án đúng của câu hỏi này.</p>
          </div>
        </div>

        <div class="creator-actions">
          <button class="add-q-btn" @click="addQuestionToBuilder">+ Thêm câu hỏi mới</button>
          <button class="import-q-btn" @click="openStudentImportModal">📁 Import từ file / text</button>
          <button class="save-quiz-btn" @click="saveCustomQuiz">Lưu đề thi</button>
        </div>
      </div>

      <!-- RIGHT: preview table -->
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
                <td><span class="type-pill">{{ q.wordType || 'Động từ' }}</span></td>
                <td><span class="correct-badge-pill">✓ {{ q.options[q.correctOptionIndex] || '(Chưa chọn)' }}</span></td>
                <td class="action-cell">
                  <button v-if="newQuizQuestions.length > 1" class="delete-mini-btn" @click="removeQuestionFromBuilder(idx)" title="Xóa câu này">&times;</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Import Modal -->
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
            <textarea v-model="studentImportText" placeholder="Ví dụ:&#10;가다 | Động từ | ka-da | Đi&#10;오다 | Động từ | o-da | Đến" rows="8" class="creator-input-field" style="width: 100%; font-family: monospace; font-size: 0.9rem;"></textarea>
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
  </div>
</template>

<script setup>
import { inject } from 'vue'
import AppIcon from '../icons/AppIcon.vue'

const {
  isCreatingQuiz, creatorQuizType, newQuizTitle, newQuizQuestions,
  showStudentImportModal, studentImportTab, studentImportText, studentImportFileName,
  openStudentImportModal, handleStudentFileSelected, confirmStudentImport,
  addQuestionToBuilder, removeQuestionFromBuilder,
  handleAudioUpload, saveCustomQuiz
} = inject('quizState')
</script>

<style scoped>
.quiz-creator-container { max-width: 1200px; width: 100%; margin: 0 auto; }
.back-link-btn { display: inline-flex; align-items: center; gap: 0.35rem; color: var(--primary); font-weight: 600; font-size: 0.9rem; margin-bottom: 1rem; }
.back-link-btn:hover { color: var(--primary-hover); transform: translateX(-2px); }
.creator-header { margin-bottom: 1.5rem; }
.creator-header h3 { font-size: 1.35rem; font-weight: 800; color: var(--text-title); margin-bottom: 0.25rem; }
.creator-header p { font-size: 0.9rem; color: var(--text-muted); }
.creator-body-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.75rem; align-items: start; }
.creator-form-card { background-color: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 2rem; box-shadow: var(--shadow-md); }
.label-bold { font-weight: 700; font-size: 0.95rem; color: var(--text-title); }
.label-accent { color: var(--primary); font-weight: 700; display: block; margin-bottom: 0.35rem; }
.creator-input-field { width: 100%; padding: 0.75rem; border-radius: var(--radius-sm); background-color: var(--bg-app); margin-top: 0.35rem; }
.creator-input-field:focus { border-color: var(--primary); background-color: var(--bg-card); }
.source-toggle-group { display: flex; gap: 1.5rem; margin-top: 0.35rem; }
.radio-label { display: flex; align-items: center; gap: 0.4rem; font-size: 0.85rem; font-weight: 600; cursor: pointer; }
.file-uploader-box { margin-top: 0.5rem; display: flex; flex-direction: column; gap: 0.25rem; }
.hidden-file-input { display: none; }
.file-upload-btn { display: inline-block; align-self: flex-start; padding: 0.5rem 1rem; background-color: var(--bg-badge); border: 1px dashed var(--border-color-hover); border-radius: var(--radius-sm); font-size: 0.8rem; font-weight: 700; color: var(--text-title); cursor: pointer; transition: all var(--transition-fast); }
.file-upload-btn:hover { background-color: var(--bg-hover); border-color: var(--primary); }
.file-name-hint { font-size: 0.75rem; color: var(--success); font-weight: 600; }
.questions-builder-list { display: flex; flex-direction: column; gap: 1.5rem; margin: 1.5rem 0; }
.question-builder-item { padding: 1.25rem; border: 1px solid var(--border-color); border-radius: var(--radius-md); background-color: var(--bg-card); position: relative; }
.builder-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem; }
.builder-item-header h4 { font-weight: 700; color: var(--primary); }
.remove-q-btn { font-size: 0.75rem; font-weight: 600; color: var(--danger); display: flex; align-items: center; gap: 0.25rem; }
.remove-q-btn:hover { opacity: 0.8; }
.options-builder-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1rem; }
.option-builder-row { display: flex; flex-direction: column; gap: 0.35rem; }
.opt-label-wrapper { display: flex; align-items: center; gap: 0.5rem; font-size: 0.8rem; font-weight: 700; color: var(--text-title); }
.creator-input-field-opt { width: 100%; padding: 0.6rem; border-radius: var(--radius-sm); background-color: var(--bg-app); }
.creator-input-field-opt:focus { border-color: var(--primary); background-color: var(--bg-card); }
.correct-hint { font-size: 0.75rem; color: var(--text-muted); margin-top: 0.75rem; }
.creator-actions { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 1.5rem; margin-top: 1.5rem; }
.add-q-btn { padding: 0.7rem 1.25rem; border-radius: var(--radius-sm); background-color: var(--bg-badge); color: var(--text-title); font-weight: 700; font-size: 0.85rem; border: 1px solid var(--border-color); }
.add-q-btn:hover { background-color: var(--border-color); }
.import-q-btn { background-color: var(--bg-body); border: 1px solid var(--border-color); color: var(--text-title); padding: 0.65rem 1.25rem; border-radius: var(--radius-md); font-weight: 600; font-size: 0.9rem; cursor: pointer; transition: all var(--transition-fast); }
.import-q-btn:hover { background-color: var(--bg-hover); border-color: var(--primary); color: var(--primary); }
.save-quiz-btn { padding: 0.7rem 1.5rem; border-radius: var(--radius-sm); background-color: var(--success); color: #fff; font-weight: 700; font-size: 0.85rem; box-shadow: 0 4px 10px rgba(46, 204, 113, 0.2); }
.save-quiz-btn:hover { background-color: hsl(142, 72%, 24%); }
/* Preview panel */
.created-items-panel { background-color: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 1.75rem; box-shadow: var(--shadow-sm); width: 100%; }
.panel-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.85rem; }
.panel-header-row h4 { font-size: 1.2rem; font-weight: 800; color: var(--text-title); margin: 0; }
.count-pill { background-color: var(--primary-light, #eff6ff); color: var(--primary); font-weight: 700; font-size: 0.85rem; padding: 0.3rem 0.75rem; border-radius: var(--radius-sm); }
.table-container-side { overflow-x: auto; }
.created-items-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.created-items-table th { text-align: left; padding: 0.75rem 0.6rem; font-size: 0.8rem; font-weight: 700; color: var(--text-muted); border-bottom: 1px solid var(--border-color); letter-spacing: 0.5px; }
.created-items-table td { padding: 0.9rem 0.6rem; border-bottom: 1px solid var(--border-color); vertical-align: middle; }
.q-cell { display: flex; align-items: flex-start; gap: 0.4rem; }
.question-icon { color: var(--danger, #ef4444); font-weight: 700; font-size: 0.95rem; }
.q-text-group { display: flex; flex-direction: column; }
.q-title { color: var(--text-title); font-weight: 700; font-size: 0.9rem; }
.q-pron { color: var(--text-muted); font-size: 0.8rem; }
.type-pill { display: inline-block; background-color: var(--bg-body); border: 1px solid var(--border-color); color: var(--text-body); font-size: 0.8rem; padding: 0.2rem 0.6rem; border-radius: var(--radius-sm); white-space: nowrap; }
.correct-badge-pill { display: inline-flex; align-items: center; gap: 0.2rem; border: 1px solid var(--success, #10b981); color: var(--success, #10b981); background-color: rgba(16, 185, 129, 0.08); font-size: 0.8rem; font-weight: 600; padding: 0.25rem 0.6rem; border-radius: var(--radius-sm); white-space: nowrap; }
.delete-mini-btn { background: none; border: none; color: var(--text-muted); font-size: 1.25rem; cursor: pointer; padding: 0 0.25rem; transition: color var(--transition-fast); }
.delete-mini-btn:hover { color: var(--danger); }
/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.5); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 9999; padding: 1.5rem; }
.modal-content { background-color: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); box-shadow: 0 20px 25px -5px rgba(0,0,0,0.15); width: 100%; overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--border-color); }
.modal-header h3 { margin: 0; font-size: 1.2rem; font-weight: 700; color: var(--text-title); }
.close-btn { background: none; border: none; font-size: 1.5rem; color: var(--text-muted); cursor: pointer; line-height: 1; }
.close-btn:hover { color: var(--danger); }
.cancel-btn { background-color: var(--bg-body); border: 1px solid var(--border-color); color: var(--text-body); padding: 0.6rem 1.2rem; border-radius: var(--radius-md); font-weight: 600; cursor: pointer; }
.cancel-btn:hover { background-color: var(--bg-hover); }
.submit-btn { background-color: var(--primary); border: none; color: #ffffff; padding: 0.6rem 1.2rem; border-radius: var(--radius-md); font-weight: 600; cursor: pointer; }
.submit-btn:hover { opacity: 0.9; }
.tab-btn { padding: 0.4rem 1rem; font-size: 0.85rem; font-weight: 600; border-radius: var(--radius-sm); color: var(--text-body); }
.tab-btn.active { background-color: var(--bg-card); color: var(--primary); box-shadow: var(--shadow-sm); }
@media (max-width: 1024px) { .creator-body-grid { grid-template-columns: 1fr; } }
@media (max-width: 600px) { .options-builder-grid { grid-template-columns: 1fr; } }
</style>
