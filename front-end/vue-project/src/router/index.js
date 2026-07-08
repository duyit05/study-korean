import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

// Layouts
import StudentLayout from '../layouts/StudentLayout.vue';
import TeacherLayout from '../layouts/TeacherLayout.vue';

// Views
import LoginView from '../components/LoginView.vue';
import DashboardView from '../components/DashboardView.vue';
import VocabularyView from '../components/VocabularyView.vue';
import QuizView from '../components/QuizView.vue';
import ScheduleView from '../components/ScheduleView.vue';
import ProfileView from '../components/ProfileView.vue';

// Teacher Views
import TeacherDashboard from '../components/teacher/TeacherDashboard.vue';
import TeacherClasses from '../components/teacher/TeacherClasses.vue';
import TeacherVocabulary from '../components/teacher/TeacherVocabulary.vue';
import TeacherQuizzes from '../components/teacher/TeacherQuizzes.vue';
import TeacherGrading from '../components/teacher/TeacherGrading.vue';
import TeacherTopikLevels from '../components/teacher/TeacherTopikLevels.vue';
import TeacherStudents from '../components/teacher/TeacherStudents.vue';
import TeacherCourses from '../components/teacher/TeacherCourses.vue';

const routes = [
  {
    path: '/',
    redirect: () => {
      const auth = useAuthStore();
      if (!auth.user) return '/login';
      return auth.user.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard';
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { guestOnly: true }
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      { path: '', redirect: '/student/dashboard' },
      { path: 'dashboard', name: 'StudentDashboard', component: DashboardView },
      { path: 'vocabulary', name: 'StudentVocabulary', component: VocabularyView },
      { path: 'quizzes', name: 'StudentQuizzes', component: QuizView },
      { path: 'schedule', name: 'StudentSchedule', component: ScheduleView },
      { path: 'profile', name: 'StudentProfile', component: ProfileView }
    ]
  },
  {
    path: '/teacher',
    component: TeacherLayout,
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      { path: '', redirect: '/teacher/dashboard' },
      { path: 'dashboard', name: 'TeacherDashboard', component: TeacherDashboard },
      { path: 'courses', name: 'TeacherCourses', component: TeacherCourses },
      { path: 'classes', name: 'TeacherClasses', component: TeacherClasses },
      { path: 'vocabulary', name: 'TeacherVocabulary', component: TeacherVocabulary },
      { path: 'quizzes', name: 'TeacherQuizzes', component: TeacherQuizzes },
      { path: 'grading', name: 'TeacherGrading', component: TeacherGrading },
      { path: 'topik-levels', name: 'TeacherTopikLevels', component: TeacherTopikLevels },
      { path: 'students', name: 'TeacherStudents', component: TeacherStudents },
      { path: 'profile', name: 'TeacherProfile', component: ProfileView }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// Navigation Guards
router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  const loggedIn = !!auth.user;

  // Guest only (e.g. login page)
  if (to.meta.guestOnly && loggedIn) {
    return next(auth.user.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard');
  }

  // Authentication check
  if (to.meta.requiresAuth && !loggedIn) {
    return next('/login');
  }

  // Role check
  if (to.meta.role && loggedIn && auth.user.role !== to.meta.role) {
    // Redirect role-mismatched users to their home
    return next(auth.user.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard');
  }

  next();
});

export default router;
