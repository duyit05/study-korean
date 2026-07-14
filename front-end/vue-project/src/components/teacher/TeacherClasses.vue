<template>
  <div class="teacher-classes animate-fade">
    <div class="header-section">
      <div class="title-area">
        <h2>Quản lý lớp học</h2>
        <p>Xem danh sách lớp, tạo mã tham gia và theo dõi tiến độ của học viên.</p>
      </div>
      <button class="primary-btn" @click="showCreateModal = true">
        <AppIcon name="user" size="18" />
        <span>Tạo lớp học mới</span>
      </button>
    </div>

    <!-- Classes Grid -->
    <div class="classes-grid">
      <div 
        v-for="cls in classes" 
        :key="cls.id" 
        class="class-card"
        :class="{ active: selectedClass && selectedClass.id === cls.id }"
        @click="selectedClass = cls"
      >
        <div class="class-header">
          <div class="class-badge">한</div>
          <h3>{{ cls.name }}</h3>
        </div>
        <div class="class-body">
          <div class="info-row">
            <AppIcon name="calendar" size="16" class="icon" />
            <span>{{ cls.schedule }}</span>
          </div>
          <div class="info-row">
            <AppIcon name="search" size="16" class="icon" />
            <span>{{ cls.room }}</span>
          </div>
          <div class="info-row code">
            <strong>Mã lớp:</strong>
            <span class="code-badge">{{ cls.code }}</span>
          </div>
        </div>
        <div class="class-footer">
          <span>{{ cls.studentsCount }} Học viên</span>
          <span class="detail-link">Xem chi tiết &rarr;</span>
        </div>
      </div>
    </div>

    <!-- Selected Class Details Panel -->
    <div v-if="selectedClass" class="class-detail-panel animate-scale">
      <div class="panel-header">
        <div class="header-title">
          <h3>Chi tiết lớp: {{ selectedClass.name }}</h3>
          <p>{{ selectedClass.schedule }} | {{ selectedClass.room }}</p>
        </div>
        <div class="header-actions">
          <button class="delete-btn-sm" @click="handleDeleteClass(selectedClass.id)">Xóa lớp học</button>
          <button class="close-btn" @click="selectedClass = null">&times;</button>
        </div>
      </div>

      <div class="panel-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'students' }" 
          @click="activeTab = 'students'"
        >
          Danh sách học viên ({{ selectedClass.students.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'materials' }" 
          @click="activeTab = 'materials'"
        >
          File Sách ({{ materials.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'assignments' }" 
          @click="activeTab = 'assignments'"
        >
          Bài tập & Đề thi đã giao ({{ assignedQuizzes.length }})
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'sessions' }" 
          @click="activeTab = 'sessions'"
        >
          Lịch trình & Buổi học ({{ sessions.length }})
        </button>
      </div>

      <!-- Tab Content: Students -->
      <div v-if="activeTab === 'students'" class="tab-content students-tab">
        <div class="tab-actions-row">
          <button class="primary-btn-sm" @click="openEnrollModal">
            <AppIcon name="plus" size="14" />
            Thêm học viên vào lớp
          </button>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>Học viên</th>
              <th>Email</th>
              <th>Tiến độ từ vựng</th>
              <th>Điểm trung bình</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in (selectedClass.students || [])" :key="student.id">
              <td>
                <div class="student-profile">
                  <div class="avatar-circle">{{ student.name ? student.name[0] : 'U' }}</div>
                  <strong>{{ student.name }}</strong>
                </div>
              </td>
              <td>{{ student.email }}</td>
              <td>
                <div class="progress-bar-wrapper">
                  <div class="progress-bar" :style="{ width: (student.vocabProgress || 0) + '%' }"></div>
                  <span class="progress-text">{{ student.vocabProgress || 0 }}%</span>
                </div>
              </td>
              <td><span class="score-badge">{{ student.avgScore || 0 }} / 100</span></td>
              <td>
                <button class="text-btn">Gửi nhắc nhở</button>
              </td>
            </tr>
            <tr v-if="!selectedClass.students || selectedClass.students.length === 0">
              <td colspan="5" class="empty-row" style="text-align: center; color: var(--text-muted); padding: 3rem;">
                Lớp học này chưa có học viên nào tham gia. Hãy cung cấp Mã lớp cho học viên!
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Tab Content: File Sách / Materials -->
      <div v-if="activeTab === 'materials'" class="tab-content materials-tab" style="padding: 1.5rem 0;">
        <div class="tab-actions-row" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; gap: 1rem; flex-wrap: wrap;">
          <div class="materials-filter-box">
            <AppIcon name="search" size="16" class="filter-search-icon" />
            <input 
              type="text" 
              v-model="materialSearchQuery" 
              placeholder="Tìm kiếm tài liệu..."
              class="filter-search-input"
            >
            <AppSelect v-model="materialTypeFilter" :options="materialTypeOptions" placeholder="Tất cả định dạng" style="max-width: 200px;" />
          </div>
          <button class="primary-btn-sm" @click="triggerFileInput">
            <AppIcon name="plus" size="14" />
            Tải tài liệu lên
          </button>
          <input 
            type="file" 
            ref="fileInputRef" 
            @change="handleFileUpload" 
            style="display: none;"
          >
        </div>

        <!-- Timeline Grouped List of Materials -->
        <div v-if="materialStore.loading && materials.length === 0" class="select-loading-spinner" style="padding: 4rem 1rem;">
          <div class="spinner"></div>
          <span>Đang tải danh sách tài liệu...</span>
        </div>

        <div v-else-if="groupedMaterials.length === 0" class="empty-state">
          Không có tài liệu nào được tải lên hoặc không tìm thấy kết quả phù hợp.
        </div>

        <div v-else class="materials-timeline">
          <div 
            v-for="group in groupedMaterials" 
            :key="group.dateLabel" 
            class="timeline-group"
          >
            <div class="timeline-header" @click="toggleGroup(group.dateLabel)">
              <span class="timeline-date-label">
                {{ group.dateLabel }}
              </span>
              <span class="timeline-count">({{ group.items.length }} tài liệu)</span>
              <span class="timeline-toggle-icon">
                {{ collapsedGroups[group.dateLabel] ? '▶' : '▼' }}
              </span>
            </div>

            <div v-show="!collapsedGroups[group.dateLabel]" class="timeline-items">
              <div 
                v-for="item in group.items" 
                :key="item.id" 
                class="material-item"
              >
                <div class="material-info-area">
                  <div class="material-icon-box" :class="getFileIconClass(item.contentType)">
                    <AppIcon :name="getFileIcon(item.contentType)" size="20" />
                  </div>
                  <div class="material-details">
                    <a :href="item.downloadUrl" target="_blank" class="material-title-link">
                      {{ item.title }}
                    </a>
                    <div class="material-meta">
                      <span>{{ formatBytes(item.fileSize) }}</span>
                      <span class="dot">•</span>
                      <span>Tải lên lúc {{ formatTime(item.createdAt) }}</span>
                    </div>
                  </div>
                </div>
                <div class="material-actions">
                  <a :href="item.downloadUrl" target="_blank" class="download-link-btn" title="Tải xuống">
                    Tải xuống
                  </a>
                  <button class="delete-btn-sm" @click="handleDeleteMaterial(item.id)" title="Xóa tài liệu">
                    Xóa
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab Content: Assignments -->
      <div v-else-if="activeTab === 'assignments'" class="tab-content assignments-tab">
        <div class="assignments-list">
          <div v-for="quiz in assignedQuizzes" :key="quiz.id" class="assignment-item">
            <div class="assign-icon">
              <AppIcon name="quiz" size="20" />
            </div>
            <div class="assign-info">
              <h4>{{ quiz.title }}</h4>
              <p>Hạn nộp: {{ quiz.dueDate }} | Dạng: {{ quiz.type }}</p>
            </div>
            <div class="assign-status">
              <span class="status-badge">Đã nộp: 8/12</span>
            </div>
          </div>
          <div v-if="assignedQuizzes.length === 0" class="empty-state">
            Chưa có bài tập nào được giao cho lớp này.
          </div>
        </div>
      </div>

      <!-- Tab Content: Sessions (Quản lý Buổi học) -->
      <div v-else-if="activeTab === 'sessions'" class="tab-content sessions-tab" style="padding: 1.5rem 0;">
        <div class="tab-actions-row" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; gap: 1rem; flex-wrap: wrap;">
          <div class="sessions-info-box">
            <span class="info-lbl" style="font-size: 0.85rem; color: var(--text-muted);">Quản lý lịch trình, bài tập về nhà và link Zoom cho từng buổi học.</span>
          </div>
          <button class="primary-btn-sm" @click="openCreateSessionModal">
            <AppIcon name="plus" size="14" />
            Thêm buổi học mới
          </button>
        </div>

        <div v-if="sessionStore.loading" class="select-loading-spinner" style="padding: 4rem 1rem;">
          <div class="spinner"></div>
          <span>Đang tải danh sách buổi học...</span>
        </div>

        <div v-else-if="sessions.length === 0" class="empty-state">
          Lớp học này chưa có buổi học nào được lên lịch trình.
        </div>

        <div v-else class="sessions-list-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Buổi số</th>
                <th>Chủ đề bài học</th>
                <th>Thời gian / Trạng thái</th>
                <th>Bài tập về nhà</th>
                <th>Link phòng học</th>
                <th class="text-right">Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="session in sessions" :key="session.id">
                <td><strong>Buổi {{ session.sessionNumber }}</strong></td>
                <td>
                  <div class="topic-title" style="font-weight: 700; color: var(--text-title);">{{ session.topic || 'Chưa soạn chủ đề' }}</div>
                  <span class="teacher-notes" v-if="session.teacherNotes" style="font-size: 0.75rem; color: var(--primary); display: block; margin-top: 0.25rem;">📝 Ghi chú GV: {{ session.teacherNotes }}</span>
                </td>
                <td>
                  <div class="time-sched" style="font-size: 0.8rem; color: var(--text-body); font-weight: 600;">{{ formatSessionDateTime(session.scheduledAt) }}</div>
                  <span class="status-badge-sm" :class="session.status ? session.status.toLowerCase() : 'scheduled'" style="display: inline-block; padding: 0.15rem 0.4rem; font-size: 0.7rem; font-weight: 700; border-radius: var(--radius-sm); margin-top: 0.25rem;">
                    {{ getStatusLabel(session.status) }}
                  </span>
                </td>
                <td>
                  <div class="homework-txt" :title="session.homework" style="font-size: 0.8rem; color: var(--text-muted); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ session.homework || 'Không có bài tập' }}</div>
                </td>
                <td>
                  <a v-if="session.meetingUrl" :href="session.meetingUrl" target="_blank" class="meeting-link-badge" style="display: inline-block; padding: 0.2rem 0.5rem; background-color: var(--primary-light); color: var(--primary); font-size: 0.75rem; font-weight: 700; border-radius: var(--radius-sm); text-decoration: none;">
                    Vào Zoom
                  </a>
                  <span v-else class="no-meeting" style="font-size: 0.75rem; color: var(--text-muted);">Offline / Phòng học</span>
                </td>
                <td class="text-right">
                  <button class="action-btn-sm edit" @click="openEditSessionModal(session)" title="Chỉnh sửa buổi học" style="margin-right: 0.25rem; border: none; background: transparent; cursor: pointer; color: var(--text-body);">
                    <AppIcon name="edit" size="14" />
                  </button>
                  <button class="action-btn-sm delete" @click="handleDeleteSession(session.id)" title="Xóa buổi học" style="border: none; background: transparent; cursor: pointer; color: var(--danger);">
                    <AppIcon name="trash" size="14" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Create Class Modal -->
    <div v-if="showCreateModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Tạo lớp học mới</h3>
          <button class="close-btn" @click="showCreateModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleCreateClass" class="modal-form">
          <div class="form-group">
            <label for="className">Tên lớp học</label>
            <AppSelect id="className" v-model="newClassLevelId" :options="levelOptions" placeholder="-- Chọn lớp học từ Cấp độ TOPIK --" />
          </div>
          <div class="form-group">
            <label for="classCourse">Khóa học liên kết</label>
            <AppSelect id="classCourse" v-model="newClassCourseId" :options="courseOptions" placeholder="-- Không liên kết khóa học --" />
          </div>
          <div class="form-group">
            <label for="classSchedule">Lịch học</label>
            <input type="text" id="classSchedule" v-model="newClassSchedule" placeholder="Ví dụ: Thứ 2, 4, 6 (18:00 - 19:30)" required>
          </div>
          <div class="form-group">
            <label for="classRoom">Phòng học / Link Zoom</label>
            <input type="text" id="classRoom" v-model="newClassRoom" placeholder="Ví dụ: Online Zoom A2 hoặc Phòng 401" required>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showCreateModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn">Tạo lớp</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Add Student Modal -->
    <div v-if="showEnrollModal" class="modal-overlay">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>Thêm học viên vào lớp</h3>
          <button class="close-btn" @click="showEnrollModal = false">&times;</button>
        </div>
        <div class="modal-body" style="padding: 1.5rem 0 0 0;">
          <div class="search-box-group">
            <label for="studentSearch">Tìm kiếm học viên</label>
            <input 
              type="text" 
              id="studentSearch" 
              v-model="studentSearchQuery" 
              placeholder="Nhập tên hoặc email..." 
              class="search-input"
            >
          </div>
          
          <div class="students-select-list">
            <div v-if="loadingStudents" class="select-loading-spinner">
              <div class="spinner"></div>
              <span>Đang tải danh sách học viên...</span>
            </div>
            <template v-else>
              <div 
                v-for="st in filteredStudents" 
                :key="st.id" 
                class="student-select-item"
                :class="{ selected: selectedStudentId === st.id }"
                @click="selectedStudentId = st.id"
              >
                <div class="student-select-info">
                  <strong>{{ st.fullName || st.username }}</strong>
                  <span>{{ st.email }}</span>
                </div>
                <div class="select-indicator">
                  <span v-if="selectedStudentId === st.id" class="check-icon">&#10004;</span>
                </div>
              </div>
              <div v-if="filteredStudents.length === 0" class="empty-select-list">
                Không tìm thấy học viên phù hợp hoặc tất cả đã tham gia lớp.
              </div>
            </template>
          </div>
          
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="showEnrollModal = false">Hủy bỏ</button>
            <button 
              type="button" 
              class="submit-btn" 
              :disabled="!selectedStudentId || enrolling" 
              @click="handleEnrollStudent"
              style="display: flex; align-items: center; justify-content: center; gap: 0.5rem;"
            >
              <div v-if="enrolling" class="spinner-sm"></div>
              {{ enrolling ? 'Đang thêm...' : 'Thêm vào lớp' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- Create / Edit Session Modal -->
    <div v-if="showSessionModal" class="modal-overlay" @click.self="showSessionModal = false">
      <div class="modal-content animate-scale">
        <div class="modal-header">
          <h3>{{ isEditSessionMode ? 'Cập nhật buổi học' : 'Thêm buổi học mới' }}</h3>
          <button class="close-btn" @click="showSessionModal = false">&times;</button>
        </div>
        <form @submit.prevent="handleSessionSubmit" class="modal-form">
          <div class="form-row-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <div class="form-group">
              <label for="sessionNum">Buổi học số <span class="required">*</span></label>
              <input type="number" id="sessionNum" v-model.number="sessionForm.sessionNumber" min="1" required style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem;">
            </div>
            <div class="form-group">
              <label for="sessionStatus">Trạng thái</label>
              <AppSelect id="sessionStatus" v-model="sessionForm.status" :options="sessionStatusOptions" />
            </div>
          </div>

          <div class="form-group" style="margin-top: 1rem;">
            <label for="sessionTopic">Chủ đề bài học <span class="required">*</span></label>
            <input type="text" id="sessionTopic" v-model="sessionForm.topic" placeholder="Ví dụ: Giới thiệu bản thân và bảng chữ cái Hangeul" required style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem;">
          </div>

          <div class="form-row-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1rem;">
            <div class="form-group">
              <label for="sessionDate">Thời gian học <span class="required">*</span></label>
              <input type="datetime-local" id="sessionDate" v-model="sessionForm.scheduledAt" required style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem;">
            </div>
            <div class="form-group">
              <label for="sessionMeeting">Link Zoom / Google Meet</label>
              <input type="text" id="sessionMeeting" v-model="sessionForm.meetingUrl" placeholder="Ví dụ: https://zoom.us/j/..." style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem;">
            </div>
          </div>

          <div class="form-group" v-if="sessionForm.status === 'CANCELLED'" style="margin-top: 1rem;">
            <label for="cancelReason">Lý do hủy buổi học</label>
            <input type="text" id="cancelReason" v-model="sessionForm.cancelledReason" placeholder="Ví dụ: Giáo viên bị ốm đột xuất" style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem;">
          </div>

          <div class="form-group" style="margin-top: 1rem;">
            <label for="sessionHomework">Bài tập về nhà</label>
            <textarea id="sessionHomework" v-model="sessionForm.homework" placeholder="Nhập yêu cầu bài tập cho học viên..." rows="2" style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem; resize: vertical;"></textarea>
          </div>

          <div class="form-row-2" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1rem;">
            <div class="form-group">
              <label for="teacherNotes">Ghi chú cho giáo viên (Nội bộ)</label>
              <textarea id="teacherNotes" v-model="sessionForm.teacherNotes" placeholder="Chuẩn bị tài liệu ôn tập..." rows="2" style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem; resize: vertical;"></textarea>
            </div>
            <div class="form-group">
              <label for="studentNotes">Ghi chú cho học viên</label>
              <textarea id="studentNotes" v-model="sessionForm.studentNotes" placeholder="Đọc trước giáo trình bài số 2..." rows="2" style="width: 100%; padding: 0.65rem; border: 1px solid var(--border-color); border-radius: var(--radius-sm); font-size: 0.9rem; resize: vertical;"></textarea>
            </div>
          </div>

          <div class="modal-actions" style="margin-top: 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem;">
            <button type="button" class="cancel-btn" @click="showSessionModal = false">Hủy bỏ</button>
            <button type="submit" class="submit-btn" :disabled="sessionSubmitting">
              {{ isEditSessionMode ? 'Lưu thay đổi' : 'Tạo buổi học' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import AppIcon from '../icons/AppIcon.vue'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const props = defineProps({
  quizzes: {
    type: Array,
    default: () => []
  },
  studySets: {
    type: Array,
    default: () => []
  }
})

import { useStudySetStore } from '../../stores/studySet'
import { useQuizStore } from '../../stores/quiz'
import { useTopikLevelStore } from '../../stores/topikLevel'
import { useMaterialStore } from '../../stores/material'
import { useCourseStore } from '../../stores/course'
import { useSessionStore } from '../../stores/session'

const studySetStore = useStudySetStore()
const quizStore = useQuizStore()
const topikLevelStore = useTopikLevelStore()
const materialStore = useMaterialStore()
const courseStore = useCourseStore()
const sessionStore = useSessionStore()

const topikLevels = computed(() => topikLevelStore.levels)
const courses = computed(() => courseStore.courses)
const sessions = computed(() => sessionStore.sessions)
const materials = computed(() => materialStore.materials)

const levelOptions = computed(() => {
  return topikLevels.value.map(lvl => ({ label: lvl.name, value: lvl.id }))
})
const courseOptions = computed(() => {
  const list = courses.value.map(c => ({ label: `${c.title} (${c.level})`, value: c.id }))
  return [{ label: '-- Không liên kết khóa học --', value: '' }, ...list]
})
const sessionStatusLabels = {
  'SCHEDULED': 'Chờ diễn ra (Scheduled)',
  'ONGOING': 'Đang diễn ra (Ongoing)',
  'COMPLETED': 'Đã kết thúc (Completed)',
  'CANCELLED': 'Đã hủy (Cancelled)',
  'RESCHEDULED': 'Lên lịch lại (Rescheduled)'
}

const sessionStatusOptions = computed(() => {
  return (sessionStore.statuses || []).map(status => ({
    label: sessionStatusLabels[status] || status,
    value: status
  }))
})

const materialTypeLabels = {
  'PDF': 'Sách / PDF',
  'AUDIO': 'Âm thanh / Audio',
  'IMAGE': 'Hình ảnh / Ảnh',
  'OTHER': 'Tài liệu khác'
}

const materialTypeOptions = computed(() => {
  const list = (materialStore.materialTypes || []).map(type => ({
    label: materialTypeLabels[type] || type,
    value: type.toLowerCase()
  }))
  return [{ label: 'Tất cả định dạng', value: '' }, ...list]
})

onMounted(async () => {
  try {
    await topikLevelStore.fetchLevels()
    await courseStore.fetchCourses()
    await sessionStore.fetchStatuses()
    await materialStore.fetchMaterialTypes()
  } catch (e) {
    console.error("Failed to fetch topics, courses, or metadata enums:", e)
  }
})

const classes = computed(() => {
  const dbClasses = studySetStore.classes || []
  return dbClasses.map(cls => ({
    ...cls,
    students: cls.students || [],
    studentsCount: cls.studentsCount || 0
  }))
})

const selectedClass = ref(null)
const activeTab = ref('students')
const showCreateModal = ref(false)

// Materials states
const materialSearchQuery = ref('')
const materialTypeFilter = ref('')
const fileInputRef = ref(null)
const collapsedGroups = reactive({})

watch(selectedClass, (newClass) => {
  if (newClass) {
    materialStore.fetchMaterials(newClass.id)
    sessionStore.fetchSessionsByClass(newClass.id)
  }
})

const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (file.size > 50 * 1024 * 1024) {
    toast.warning("Dung lượng file không được vượt quá 50MB.")
    return
  }

  try {
    await materialStore.uploadMaterial(selectedClass.value.id, file)
    toast.success("Tải tài liệu lên thành công!")
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  } catch (e) {
    console.error("Upload error:", e)
    toast.error("Tải tài liệu lên thất bại.")
  }
}

const handleDeleteMaterial = async (materialId) => {
  if (!confirm("Bạn có chắc chắn muốn xóa tài liệu này?")) return
  try {
    await materialStore.deleteMaterial(selectedClass.value.id, materialId)
    toast.success("Xóa tài liệu thành công!")
  } catch (e) {
    console.error("Delete material error:", e)
    toast.error("Xóa tài liệu thất bại.")
  }
}

const toggleGroup = (label) => {
  collapsedGroups[label] = !collapsedGroups[label]
}

const formatDateLabel = (dateStr) => {
  if (!dateStr) return 'Không rõ thời gian'
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date()
  yesterday.setDate(today.getDate() - 1)

  const isToday = date.toDateString() === today.toDateString()
  const isYesterday = date.toDateString() === yesterday.toDateString()

  if (isToday) return 'Hôm nay'
  if (isYesterday) return 'Hôm qua'

  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}/${month}/${year}`
}

const groupedMaterials = computed(() => {
  let list = materials.value || []

  if (materialSearchQuery.value.trim()) {
    const q = materialSearchQuery.value.toLowerCase()
    list = list.filter(m => (m.title || '').toLowerCase().includes(q))
  }

  if (materialTypeFilter.value) {
    const filter = materialTypeFilter.value
    list = list.filter(m => {
      const type = (m.contentType || '').toLowerCase()
      if (filter === 'pdf') return type.includes('pdf') || type.includes('document')
      if (filter === 'audio') return type.includes('audio') || type.includes('mpeg') || type.includes('mp3')
      if (filter === 'image') return type.includes('image')
      if (filter === 'other') {
        return !type.includes('pdf') && !type.includes('document') &&
               !type.includes('audio') && !type.includes('mpeg') && !type.includes('mp3') &&
               !type.includes('image')
      }
      return true
    })
  }

  const groups = {}
  list.forEach(item => {
    const datePart = item.createdAt ? item.createdAt.substring(0, 10) : 'unknown'
    const label = formatDateLabel(datePart)
    if (!groups[label]) {
      groups[label] = []
    }
    groups[label].push(item)
  })

  return Object.keys(groups).map(label => ({
    dateLabel: label,
    items: groups[label]
  }))
})

const getFileIcon = (contentType) => {
  const type = (contentType || '').toLowerCase()
  if (type.includes('pdf') || type.includes('document') || type.includes('epub')) return 'book'
  if (type.includes('audio')) return 'play'
  if (type.includes('image')) return 'eye'
  return 'quiz'
}

const getFileIconClass = (contentType) => {
  const type = (contentType || '').toLowerCase()
  if (type.includes('pdf')) return 'pdf-type'
  if (type.includes('audio')) return 'audio-type'
  if (type.includes('image')) return 'image-type'
  return 'other-type'
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// New Class Fields
const newClassLevelId = ref('')
const newClassCourseId = ref('')
const newClassSchedule = ref('')
const newClassRoom = ref('')

const assignedQuizzes = computed(() => {
  if (!selectedClass.value) return []
  return quizStore.quizzes.filter(q => q.classId === selectedClass.value.id || q.clazzId === selectedClass.value.id)
})

const handleCreateClass = async () => {
  if (!newClassLevelId.value || !newClassSchedule.value || !newClassRoom.value) return

  try {
    await studySetStore.createClass({
      topikLevelId: newClassLevelId.value,
      courseId: newClassCourseId.value || null,
      schedule: newClassSchedule.value,
      room: newClassRoom.value,
      notes: ''
    })
    toast.success("Tạo lớp học mới thành công!")
  } catch (e) {
    console.error("Failed to create class via API:", e)
    toast.error("Tạo lớp học mới thất bại.")
  }

  // Reset fields
  newClassLevelId.value = ''
  newClassCourseId.value = ''
  newClassSchedule.value = ''
  newClassRoom.value = ''
  showCreateModal.value = false
}

const handleDeleteClass = async (classId) => {
  if (!confirm("Bạn có chắc chắn muốn xóa lớp học này?")) return
  try {
    await studySetStore.deleteClass(classId)
    selectedClass.value = null
    toast.success("Xóa lớp học thành công!")
  } catch (e) {
    console.error("Failed to delete class:", e)
    toast.error("Có lỗi xảy ra khi xóa lớp học.")
  }
}

// Add Student Enrollment state and actions
const showEnrollModal = ref(false)
const studentSearchQuery = ref('')
const selectedStudentId = ref(null)
const enrolling = ref(false)
const loadingStudents = ref(false)

const openEnrollModal = async () => {
  loadingStudents.value = true
  showEnrollModal.value = true
  try {
    await studySetStore.fetchStudents()
    studentSearchQuery.value = ''
    selectedStudentId.value = null
  } catch (err) {
    console.error(err)
    toast.error("Không thể tải danh sách học viên")
  } finally {
    loadingStudents.value = false
  }
}

const filteredStudents = computed(() => {
  const list = studySetStore.studentsList || []
  const enrolledIds = (selectedClass.value?.students || []).map(s => s.id)
  return list.filter(st => {
    if (enrolledIds.includes(st.id)) return false
    const q = studentSearchQuery.value.toLowerCase()
    return (st.fullName || '').toLowerCase().includes(q) || 
           (st.username || '').toLowerCase().includes(q) || 
           (st.email || '').toLowerCase().includes(q)
  })
})

const handleEnrollStudent = async () => {
  if (!selectedStudentId.value || !selectedClass.value) return
  enrolling.value = true
  try {
    const updated = await studySetStore.enrollStudent(selectedClass.value.id, selectedStudentId.value)
    selectedClass.value = {
      ...selectedClass.value,
      students: updated.students || [],
      studentsCount: updated.studentsCount || 0
    }
    toast.success("Thêm học viên vào lớp thành công!")
    showEnrollModal.value = false
  } catch (err) {
    console.error(err)
    toast.error("Thêm học viên vào lớp thất bại.")
  } finally {
    enrolling.value = false
  }
}

// Session states and actions
const showSessionModal = ref(false)
const isEditSessionMode = ref(false)
const sessionSubmitting = ref(false)
const selectedSessionId = ref(null)

const sessionForm = ref({
  sessionNumber: 1,
  topic: '',
  scheduledAt: '',
  meetingUrl: '',
  homework: '',
  status: 'SCHEDULED',
  cancelledReason: '',
  teacherNotes: '',
  studentNotes: ''
})

const openCreateSessionModal = () => {
  isEditSessionMode.value = false
  selectedSessionId.value = null
  
  // Calculate next session number
  const nextNum = sessions.value.length > 0 
    ? Math.max(...sessions.value.map(s => s.sessionNumber || 0)) + 1 
    : 1

  sessionForm.value = {
    sessionNumber: nextNum,
    topic: '',
    scheduledAt: new Date().toISOString().substring(0, 16),
    meetingUrl: '',
    homework: '',
    status: 'SCHEDULED',
    cancelledReason: '',
    teacherNotes: '',
    studentNotes: ''
  }
  showSessionModal.value = true
}

const openEditSessionModal = (session) => {
  isEditSessionMode.value = true
  selectedSessionId.value = session.id
  
  // Convert ISO string to datetime-local format (YYYY-MM-DDTHH:mm)
  let formattedDate = ''
  if (session.scheduledAt) {
    formattedDate = session.scheduledAt.substring(0, 16)
  }

  sessionForm.value = {
    sessionNumber: session.sessionNumber,
    topic: session.topic || '',
    scheduledAt: formattedDate,
    meetingUrl: session.meetingUrl || '',
    homework: session.homework || '',
    status: session.status || 'SCHEDULED',
    cancelledReason: session.cancelledReason || '',
    teacherNotes: session.teacherNotes || '',
    studentNotes: session.studentNotes || ''
  }
  showSessionModal.value = true
}

const handleSessionSubmit = async () => {
  if (!selectedClass.value) return
  sessionSubmitting.value = true
  try {
    const payload = {
      classId: selectedClass.value.id,
      sessionNumber: sessionForm.value.sessionNumber,
      topic: sessionForm.value.topic,
      scheduledAt: sessionForm.value.scheduledAt ? new Date(sessionForm.value.scheduledAt).toISOString() : null,
      meetingUrl: sessionForm.value.meetingUrl,
      homework: sessionForm.value.homework,
      status: sessionForm.value.status,
      cancelledReason: sessionForm.value.status === 'CANCELLED' ? sessionForm.value.cancelledReason : '',
      teacherNotes: sessionForm.value.teacherNotes,
      studentNotes: sessionForm.value.studentNotes
    }

    if (isEditSessionMode.value) {
      await sessionStore.updateSession(selectedSessionId.value, payload)
      toast.success('Cập nhật buổi học thành công!')
    } else {
      await sessionStore.createSession(payload)
      toast.success('Tạo buổi học mới thành công!')
    }
    showSessionModal.value = false
  } catch (err) {
    console.error(err)
    toast.error('Có lỗi xảy ra: ' + err.message)
  } finally {
    sessionSubmitting.value = false
  }
}

const handleDeleteSession = async (sessionId) => {
  if (!confirm('Bạn có chắc chắn muốn xóa buổi học này?')) return
  try {
    await sessionStore.deleteSession(sessionId)
    toast.success('Xóa buổi học thành công!')
  } catch (err) {
    console.error(err)
    toast.error('Xóa buổi học thất bại.')
  }
}

const getStatusLabel = (status) => {
  if (!status) return 'Chờ diễn ra'
  switch (status.toUpperCase()) {
    case 'SCHEDULED': return 'Chờ diễn ra'
    case 'ONGOING': return 'Đang diễn ra'
    case 'COMPLETED': return 'Đã kết thúc'
    case 'CANCELLED': return 'Đã hủy'
    case 'RESCHEDULED': return 'Thay đổi lịch'
    default: return status
  }
}

const formatSessionDateTime = (dateStr) => {
  if (!dateStr) return 'Chưa xếp lịch'
  const date = new Date(dateStr)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes} - ${day}/${month}/${year}`
}
</script>

<style scoped>
.teacher-classes {
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

.primary-btn {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.65rem 1.25rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  box-shadow: 0 4px 12px var(--primary-glow);
  transition: all var(--transition-fast);
}

.primary-btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.classes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.25rem;
}

.class-card {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.class-card:hover, .class-card.active {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.class-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.class-badge {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background-color: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.class-header h3 {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-title);
}

.class-body {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.info-row.code {
  font-size: 0.85rem;
  color: var(--text-title);
  margin-top: 0.25rem;
}

.code-badge {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.15rem 0.4rem;
  border-radius: var(--radius-sm);
  font-family: monospace;
  font-weight: 700;
  color: var(--primary);
}

.class-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-color);
  padding-top: 0.75rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.detail-link {
  color: var(--primary);
  font-weight: 600;
}

.class-detail-panel {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
}

.header-title h3 {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-title);
}

.header-title p {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
}

.close-btn:hover {
  color: var(--danger);
}

.panel-tabs {
  display: flex;
  gap: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.tab-btn {
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  padding: 0.5rem 0.25rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tab-btn.active, .tab-btn:hover {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th, .data-table td {
  padding: 0.75rem 1rem;
  font-size: 0.85rem;
  border-bottom: 1px solid var(--border-color);
}

.data-table th {
  color: var(--text-muted);
  font-weight: 600;
}

.student-profile {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: var(--primary-glow);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.8rem;
}

.progress-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-bar {
  height: 6px;
  background-color: var(--primary);
  border-radius: var(--radius-sm);
  min-width: 8px;
}

.progress-text {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.score-badge {
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
}

.text-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 600;
  font-size: 0.8rem;
  cursor: pointer;
}

.text-btn:hover {
  text-decoration: underline;
}

.assignments-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.assignment-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.assign-icon {
  width: 36px;
  height: 36px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  color: var(--primary);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.assign-info {
  flex-grow: 1;
}

.assign-info h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 0.15rem;
}

.assign-info p {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.status-badge {
  background-color: var(--primary-glow);
  color: var(--primary);
  padding: 0.2rem 0.5rem;
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  font-weight: 600;
}

/* Modal styling */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 450px;
  padding: 1.75rem;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 0.75rem;
  margin-bottom: 1rem;
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-title);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.modal-form .form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.modal-form label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.modal-form input {
  padding: 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.modal-form input:focus {
  border-color: var(--primary);
  outline: none;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 0.5rem;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.delete-btn-sm {
  background-color: transparent;
  color: #ef4444;
  border: 1px solid #ef4444;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.delete-btn-sm:hover {
  background-color: #ef4444;
  color: #fff;
}

.tab-actions-row {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 1rem;
}

.primary-btn-sm {
  background-color: var(--primary);
  color: #fff;
  border: none;
  padding: 0.45rem 0.9rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  transition: all 0.2s;
}

.primary-btn-sm:hover {
  background-color: var(--primary-hover);
}

.search-box-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 1rem;
}

.search-box-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-title);
}

.search-input {
  padding: 0.6rem 0.8rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
}

.students-select-list {
  max-height: 220px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-bottom: 1.5rem;
}

.student-select-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color 0.2s;
}

.student-select-item:last-child {
  border-bottom: none;
}

.student-select-item:hover {
  background-color: var(--bg-card);
}

.student-select-item.selected {
  background-color: rgba(219, 142, 113, 0.1);
}

.student-select-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.student-select-info strong {
  color: var(--text-title);
  font-size: 0.9rem;
}

.student-select-info span {
  color: var(--text-muted);
  font-size: 0.8rem;
}

.select-indicator {
  color: var(--primary);
  font-size: 1rem;
}

.empty-select-list {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* Spinner Animations */
.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-sm {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.select-loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* Materials tab styles */
.materials-tab {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.materials-filter-box {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.filter-search-icon {
  color: var(--text-muted);
}

.filter-search-input {
  flex: 1;
  max-width: 320px;
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.85rem;
}

.filter-search-input:focus {
  border-color: var(--primary);
  outline: none;
}

.filter-select {
  padding: 0.5rem 2rem 0.5rem 0.75rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background-color: var(--bg-body);
  color: var(--text-title);
  font-size: 0.85rem;
  cursor: pointer;
}

.filter-select:focus {
  border-color: var(--primary);
  outline: none;
}

/* Materials Timeline */
.materials-timeline {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 0.5rem;
}

.timeline-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--border-color);
  cursor: pointer;
  user-select: none;
}

.timeline-date-label {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-title);
}

.timeline-count {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.timeline-toggle-icon {
  margin-left: auto;
  font-size: 0.75rem;
  color: var(--text-muted);
  transition: transform 0.2s;
}

.timeline-items {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding-left: 0.5rem;
}

.material-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.9rem 1.25rem;
  background-color: var(--bg-body);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.material-item:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.material-info-area {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
  min-width: 0;
}

.material-icon-box {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pdf-type {
  background-color: rgba(239, 68, 68, 0.08);
  color: #ef4444;
}

.audio-type {
  background-color: rgba(59, 130, 246, 0.08);
  color: #3b82f6;
}

.image-type {
  background-color: rgba(16, 185, 129, 0.08);
  color: #10b981;
}

.other-type {
  background-color: rgba(107, 114, 128, 0.08);
  color: #6b7280;
}

.material-details {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  min-width: 0;
}

.material-title-link {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-title);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-title-link:hover {
  color: var(--primary);
  text-decoration: underline;
}

.material-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.material-meta .dot {
  font-size: 0.5rem;
  vertical-align: middle;
}

.material-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.download-link-btn {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--primary);
  text-decoration: none;
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-card);
  transition: all 0.2s;
}

.download-link-btn:hover {
  background-color: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.delete-btn-sm {
  font-size: 0.85rem;
  font-weight: 600;
  color: #ef4444;
  background: transparent;
  border: 1px solid rgba(239, 68, 68, 0.2);
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.delete-btn-sm:hover {
  background-color: #ef4444;
  color: #fff;
  border-color: #ef4444;
}

.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  color: var(--text-muted);
  font-size: 0.95rem;
}
</style>
