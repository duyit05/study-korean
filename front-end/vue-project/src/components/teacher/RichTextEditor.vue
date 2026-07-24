<template>
  <div class="rich-text-editor-container">
    <div class="rich-editor-toolbar">
      <button type="button" class="tool-btn" @click="exec('bold')" title="Bôi đậm"><b>B</b></button>
      <button type="button" class="tool-btn" @click="exec('italic')" title="In nghiêng"><i>I</i></button>
      <button type="button" class="tool-btn" @click="exec('underline')" title="Gạch chân"><u>U</u></button>
      
      <span class="tool-separator"></span>
      
      <!-- Text Colors -->
      <button type="button" class="tool-btn color-dot red" @click="exec('foreColor', '#d90429')" title="Chữ đỏ">A</button>
      <button type="button" class="tool-btn color-dot blue" @click="exec('foreColor', '#0077b6')" title="Chữ xanh">A</button>
      <button type="button" class="tool-btn color-dot green" @click="exec('foreColor', '#38b000')" title="Chữ xanh lá">A</button>
      
      <!-- Highlight Color -->
      <button type="button" class="tool-btn highlight-btn" @click="exec('hiliteColor', '#fff3cd')" title="Tô nền vàng">🖍️</button>
      
      <span class="tool-separator"></span>
      
      <button type="button" class="tool-btn" @click="exec('removeFormat')" title="Xóa định dạng">❌</button>
    </div>
    <div
      ref="editorRef"
      class="rich-editor-content"
      contenteditable="true"
      @input="onInput"
      @blur="onInput"
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])
const editorRef = ref(null)

onMounted(() => {
  if (editorRef.value) {
    editorRef.value.innerHTML = props.modelValue
  }
})

// Sync changes from parent, but only if they are different from current innerHTML
watch(() => props.modelValue, (newVal) => {
  if (editorRef.value && editorRef.value.innerHTML !== newVal) {
    editorRef.value.innerHTML = newVal || ''
  }
})

const onInput = () => {
  if (editorRef.value) {
    emit('update:modelValue', editorRef.value.innerHTML)
  }
}

const exec = (command, value = null) => {
  document.execCommand(command, false, value)
  onInput()
}
</script>

<style scoped>
.rich-text-editor-container {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background-color: var(--bg-card);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-top: 0.5rem;
}

.rich-editor-toolbar {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.6rem;
  background-color: var(--bg-body);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.tool-btn {
  background: none;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-body);
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.tool-btn:hover {
  background-color: var(--border-color);
}

.color-dot.red {
  color: #d90429;
  font-weight: 700;
}

.color-dot.blue {
  color: #0077b6;
  font-weight: 700;
}

.color-dot.green {
  color: #38b000;
  font-weight: 700;
}

.highlight-btn {
  font-size: 0.85rem;
}

.tool-separator {
  width: 1px;
  height: 18px;
  background-color: var(--border-color);
  margin: 0 0.2rem;
}

.rich-editor-content {
  padding: 0.75rem 1rem;
  min-height: 120px;
  outline: none;
  font-size: 0.95rem;
  line-height: 1.6;
  color: var(--text-body);
  background-color: var(--bg-card);
}

.rich-editor-content[contenteditable="true"]:empty:before {
  content: "Nhập nhận xét hoặc bôi màu lỗi sai của học sinh...";
  color: var(--text-muted);
  font-style: italic;
}
</style>
