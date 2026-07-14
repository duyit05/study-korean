<template>
  <div class="app-select-container" ref="selectRef">
    <!-- Trigger Button -->
    <div 
      class="select-trigger" 
      :class="{ open: isOpen, disabled: disabled }"
      @click="toggleDropdown"
    >
      <span class="trigger-label" v-if="selectedOption">
        {{ selectedOption.label }}
      </span>
      <span class="trigger-placeholder" v-else>
        {{ placeholder || '-- Chọn --' }}
      </span>
      <AppIcon name="chevron-down" size="14" class="arrow-icon" />
    </div>

    <!-- Dropdown Menu -->
    <div v-if="isOpen" class="dropdown-menu animate-scale">
      <div 
        v-for="opt in options" 
        :key="opt.value" 
        class="dropdown-item"
        :class="{ active: opt.value === modelValue }"
        @click="selectItem(opt)"
      >
        {{ opt.label }}
      </div>
      <div v-if="options.length === 0" class="dropdown-empty">
        Không có dữ liệu
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import AppIcon from './icons/AppIcon.vue'

const props = defineProps({
  modelValue: [String, Number, Boolean],
  options: {
    type: Array,
    default: () => []
  },
  placeholder: String,
  disabled: Boolean
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const selectRef = ref(null)

const selectedOption = computed(() => {
  return props.options.find(opt => opt.value === props.modelValue)
})

const toggleDropdown = () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
}

const selectItem = (option) => {
  emit('update:modelValue', option.value)
  emit('change', option.value)
  isOpen.value = false
}

const handleClickOutside = (event) => {
  if (selectRef.value && !selectRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.app-select-container {
  position: relative;
  width: 100%;
}

.select-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.65rem 1rem;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-title);
  font-size: 0.85rem;
  cursor: pointer;
  user-select: none;
  transition: all var(--transition-fast);
}

.select-trigger:hover {
  border-color: var(--primary);
}

.select-trigger.open {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-glow);
}

.select-trigger.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.trigger-label, .trigger-placeholder {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 0.5rem;
  text-align: left;
}

.trigger-placeholder {
  color: var(--text-muted);
}

.arrow-icon {
  transition: transform var(--transition-fast);
  color: var(--text-muted);
}

.select-trigger.open .arrow-icon {
  transform: rotate(180deg);
}

/* Dropdown Menu */
.dropdown-menu {
  position: absolute;
  top: 110%;
  left: 0;
  width: 100%;
  max-height: 220px;
  overflow-y: auto;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  z-index: 1050;
  padding: 0.5rem 0;
}

.dropdown-item {
  padding: 0.65rem 1rem;
  font-size: 0.9rem;
  color: var(--text-body);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.dropdown-item:hover {
  background-color: var(--bg-hover);
  color: var(--text-title);
}

.dropdown-item.active {
  background-color: var(--primary-glow);
  color: var(--primary);
  font-weight: 700;
}

.dropdown-empty {
  padding: 1rem;
  font-size: 0.85rem;
  color: var(--text-muted);
  text-align: center;
}
</style>
