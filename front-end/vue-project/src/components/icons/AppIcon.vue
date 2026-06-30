<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    :stroke-width="strokeWidth"
    stroke-linecap="round"
    stroke-linejoin="round"
    class="app-icon"
  >
    <!-- Dashboard / Home -->
    <path v-if="name === 'dashboard'" d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
    <polyline v-if="name === 'dashboard'" points="9 22 9 12 15 12 15 22" />

    <!-- Vocabulary / Book -->
    <path v-if="name === 'book'" d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z" />
    <path v-if="name === 'book'" d="M6 6h10" />
    <path v-if="name === 'book'" d="M6 10h10" />

    <!-- Quiz / FileText -->
    <path v-if="name === 'quiz'" d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z" />
    <path v-if="name === 'quiz'" d="M14 2v4a2 2 0 0 0 2 2h4" />
    <path v-if="name === 'quiz'" d="M10 9H8" />
    <path v-if="name === 'quiz'" d="M16 13H8" />
    <path v-if="name === 'quiz'" d="M16 17H8" />

    <!-- Schedule / Calendar -->
    <path v-if="name === 'schedule' || name === 'calendar'" d="M8 2v4" />
    <path v-if="name === 'schedule' || name === 'calendar'" d="M16 2v4" />
    <rect v-if="name === 'schedule' || name === 'calendar'" width="18" height="18" x="3" y="4" rx="2" />
    <path v-if="name === 'schedule' || name === 'calendar'" d="M3 10h18" />

    <!-- Profile / User -->
    <path v-if="name === 'profile' || name === 'user'" d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
    <circle v-if="name === 'profile' || name === 'user'" cx="12" cy="7" r="4" />

    <!-- Lock -->
    <rect v-if="name === 'lock'" x="3" y="11" width="18" height="11" rx="2" ry="2" />
    <path v-if="name === 'lock'" d="M7 11V7a5 5 0 0 1 10 0v4" />

    <!-- Email -->
    <rect v-if="name === 'email'" width="20" height="16" x="2" y="4" rx="2" />
    <path v-if="name === 'email'" d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />

    <!-- Search -->
    <circle v-if="name === 'search'" cx="11" cy="11" r="8" />
    <path v-if="name === 'search'" d="m21 21-4.3-4.3" />

    <!-- Logout -->
    <path v-if="name === 'logout'" d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <polyline v-if="name === 'logout'" points="16 17 21 12 16 7" />
    <line v-if="name === 'logout'" x1="21" x2="9" y1="12" y2="12" />

    <!-- Login -->
    <path v-if="name === 'login'" d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
    <polyline v-if="name === 'login'" points="10 17 15 12 10 7" />
    <line v-if="name === 'login'" x1="15" x2="3" y1="12" y2="12" />

    <!-- Bell / Notifications -->
    <path v-if="name === 'bell'" d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
    <path v-if="name === 'bell'" d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />

    <!-- Sun (Light mode) -->
    <circle v-if="name === 'sun'" cx="12" cy="12" r="4" />
    <path v-if="name === 'sun'" d="M12 2v2" />
    <path v-if="name === 'sun'" d="M12 20v2" />
    <path v-if="name === 'sun'" d="m4.93 4.93 1.41 1.41" />
    <path v-if="name === 'sun'" d="m17.66 17.66 1.41 1.41" />
    <path v-if="name === 'sun'" d="M2 12h2" />
    <path v-if="name === 'sun'" d="M20 12h2" />
    <path v-if="name === 'sun'" d="m6.34 17.66-1.41 1.41" />
    <path v-if="name === 'sun'" d="m19.07 4.93-1.41 1.41" />

    <!-- Moon (Dark mode) -->
    <path v-if="name === 'moon'" d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />

    <!-- Check / Success -->
    <polyline v-if="name === 'check'" points="20 6 9 17 4 12" />

    <!-- X / Close -->
    <line v-if="name === 'x'" x1="18" x2="6" y1="6" y2="18" />
    <line v-if="name === 'x'" x1="6" x2="18" y1="6" y2="18" />

    <!-- Clock -->
    <circle v-if="name === 'clock'" cx="12" cy="12" r="10" />
    <polyline v-if="name === 'clock'" points="12 6 12 12 16 14" />

    <!-- Award / Progress -->
    <circle v-if="name === 'award'" cx="12" cy="8" r="7" />
    <polyline v-if="name === 'award'" points="8.21 13.89 7 23 12 20 17 23 15.79 13.88" />

    <!-- Play -->
    <polygon v-if="name === 'play'" points="6 3 20 12 6 21 6 3" />

    <!-- Chevron Right -->
    <polyline v-if="name === 'chevron-right'" points="9 18 15 12 9 6" />

    <!-- Chevron Left -->
    <polyline v-if="name === 'chevron-left'" points="15 18 9 12 15 6" />

    <!-- Info / Alert -->
    <circle v-if="name === 'alert'" cx="12" cy="12" r="10" />
    <path v-if="name === 'alert'" d="M12 16v-4" />
    <path v-if="name === 'alert'" d="M12 8h.01" />

    <!-- Refresh -->
    <path v-if="name === 'refresh'" d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8" />
    <path v-if="name === 'refresh'" d="M21 3v5h-5" />
    <path v-if="name === 'refresh'" d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16" />
    <path v-if="name === 'refresh'" d="M3 21v-5h5" />

    <!-- Edit -->
    <path v-if="name === 'edit'" d="M12 20h9" />
    <path v-if="name === 'edit'" d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z" />

    <!-- Thumbs up -->
    <path v-if="name === 'thumbs-up'" d="M7 10v12" />
    <path v-if="name === 'thumbs-up'" d="M15 5.88 14 10h5.83a2 2 0 0 1 1.92 2.56l-2.33 8A2 2 0 0 1 17.5 22H4a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h3l3.15-7.15a1 1 0 0 1 1.42 0L15 5.88Z" />

    <!-- Streak / Fire -->
    <path v-if="name === 'streak'" d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z" />

    <!-- Star -->
    <polygon v-if="name === 'star'" points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
  </svg>
</template>

<script setup>
defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: [Number, String],
    default: 20
  },
  strokeWidth: {
    type: [Number, String],
    default: 2
  }
})
</script>

<style scoped>
.app-icon {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
</style>
