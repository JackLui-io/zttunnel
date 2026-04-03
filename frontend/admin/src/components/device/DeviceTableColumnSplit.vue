<template>
  <div class="device-table-column-split" :class="{ 'is-plain': !showColumnPanel }">
    <template v-if="showColumnPanel">
      <aside class="column-panel" :style="{ width: panelWidth > 0 ? `${panelWidth}px` : '0px' }" aria-label="列显示">
        <div v-show="panelWidth > 0" class="column-panel-inner">
          <div class="column-panel-head">{{ title }}</div>
          <el-scrollbar class="column-panel-scroll" max-height="360px">
            <el-checkbox-group :model-value="modelValue" class="column-checkbox-group" @change="onGroupChange">
              <div v-for="opt in options" :key="opt.key" class="column-checkbox-row">
                <el-checkbox :label="opt.key">{{ opt.label }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </el-scrollbar>
        </div>
      </aside>
      <div
        class="split-handle"
        :class="{ 'is-dragging': dragging }"
        title="拖动调整列设置区宽度（左右拖动）"
        role="separator"
        aria-orientation="vertical"
        aria-label="拖动调整列设置区域宽度"
        @mousedown.prevent="startDrag"
      >
        <span class="split-handle-grip" aria-hidden="true" />
      </div>
    </template>
    <div class="split-table-area">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  /** { key, label }[]，与表格列 v-if 的 key 一致 */
  options: { type: Array, required: true },
  modelValue: { type: Array, required: true },
  /** localStorage 区分各 Tab */
  storageKey: { type: String, default: '' },
  title: { type: String, default: '显示列' },
  defaultPanelWidth: { type: Number, default: 200 },
  maxPanelWidth: { type: Number, default: 380 },
  /** 为 false 时隐藏左侧「显示列」与拖拽条，并始终展示全部可选列（隧道查看/编辑详情用） */
  showColumnPanel: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue'])

const emitAllColumnKeys = () => {
  const keys = props.options.map((o) => o.key).filter((k) => k != null && k !== '')
  if (keys.length) emit('update:modelValue', keys)
}

watch(
  () => [props.showColumnPanel, props.options],
  () => {
    if (!props.showColumnPanel) emitAllColumnKeys()
  },
  { deep: true, immediate: true }
)

const lsW = () => (props.storageKey ? `deviceColSplit:w:${props.storageKey}` : '')
const lsV = () => (props.storageKey ? `deviceColSplit:v:${props.storageKey}` : '')

const panelWidth = ref(props.defaultPanelWidth)
const dragging = ref(false)
let dragStartX = 0
let dragStartW = 0

const persistWidth = () => {
  const k = lsW()
  if (k) localStorage.setItem(k, String(panelWidth.value))
}

const persistVisible = (arr) => {
  const k = lsV()
  if (k) localStorage.setItem(k, JSON.stringify(arr))
}

onMounted(() => {
  if (!props.showColumnPanel) {
    emitAllColumnKeys()
    return
  }
  const kw = lsW()
  if (kw) {
    const n = parseInt(localStorage.getItem(kw), 10)
    if (!Number.isNaN(n)) {
      panelWidth.value = Math.min(props.maxPanelWidth, Math.max(0, n))
    }
  }
  const kv = lsV()
  if (kv) {
    try {
      const parsed = JSON.parse(localStorage.getItem(kv))
      if (!Array.isArray(parsed) || !parsed.length) return
      const allow = new Set(props.options.map((o) => o.key))
      const valid = parsed.filter((x) => allow.has(x))
      if (valid.length) emit('update:modelValue', valid)
    } catch {
      /* ignore */
    }
  }
})

watch(panelWidth, persistWidth)

const onGroupChange = (val) => {
  if (!props.showColumnPanel) return
  let next = Array.isArray(val) ? [...val] : []
  const allow = new Set(props.options.map((o) => o.key))
  next = next.filter((k) => allow.has(k))
  if (next.length === 0 && props.options[0]) next = [props.options[0].key]
  emit('update:modelValue', next)
  persistVisible(next)
}

const startDrag = (e) => {
  dragging.value = true
  dragStartX = e.clientX
  dragStartW = panelWidth.value
  window.addEventListener('mousemove', onMove)
  window.addEventListener('mouseup', onEnd)
  document.body.classList.add('device-col-split--dragging')
}

const onMove = (e) => {
  const delta = e.clientX - dragStartX
  const next = Math.min(props.maxPanelWidth, Math.max(0, dragStartW + delta))
  panelWidth.value = next
}

const onEnd = () => {
  dragging.value = false
  window.removeEventListener('mousemove', onMove)
  window.removeEventListener('mouseup', onEnd)
  document.body.classList.remove('device-col-split--dragging')
}

onUnmounted(onEnd)
</script>

<style scoped lang="scss">
.device-table-column-split {
  display: flex;
  align-items: stretch;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  min-height: 0;
  flex: 1;
  overflow: hidden;
  box-sizing: border-box;

  &.is-plain .split-table-area {
    border-radius: 8px;
  }
}

.column-panel {
  flex-shrink: 0;
  overflow: hidden;
  border: 1px solid var(--el-border-color);
  border-radius: 8px 0 0 8px;
  background: var(--el-fill-color-blank);
  box-sizing: border-box;
}

.column-panel-inner {
  height: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.column-panel-head {
  flex-shrink: 0;
  padding: 10px 12px;
  font-weight: 600;
  font-size: 13px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-primary);
}

.column-panel-scroll {
  flex: 1;
  min-height: 0;
}

.column-checkbox-group {
  display: flex;
  flex-direction: column;
  padding: 10px 12px 12px;
  gap: 2px;
}

.column-checkbox-row {
  :deep(.el-checkbox) {
    width: 100%;
    margin-right: 0;
    height: auto;
    align-items: flex-start;
  }
  :deep(.el-checkbox__label) {
    white-space: normal;
    line-height: 1.35;
    font-size: 13px;
  }
}

.split-handle {
  flex: 0 0 14px;
  width: 14px;
  min-width: 14px;
  z-index: 3;
  cursor: col-resize;
  align-self: stretch;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 -2px;
  border-radius: 4px;
  background: linear-gradient(
    90deg,
    var(--el-border-color-dark) 0%,
    var(--el-color-primary-light-5) 42%,
    var(--el-color-primary-light-3) 50%,
    var(--el-color-primary-light-5) 58%,
    var(--el-border-color-dark) 100%
  );
  border: 1px solid var(--el-border-color-darker);
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.35),
    0 1px 4px rgba(0, 0, 0, 0.08);
  transition:
    background 0.15s ease,
    box-shadow 0.15s ease;

  &:hover,
  &.is-dragging {
    background: linear-gradient(
      90deg,
      var(--el-color-primary-dark-2) 0%,
      var(--el-color-primary) 50%,
      var(--el-color-primary-dark-2) 100%
    );
    border-color: var(--el-color-primary);
    box-shadow:
      0 0 0 2px var(--el-color-primary-light-7),
      0 2px 8px rgba(64, 158, 255, 0.35);
  }
}

.split-handle-grip {
  width: 4px;
  height: 56px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.92);
  pointer-events: none;
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.2);
}

.split-table-area {
  flex: 1;
  min-width: 0;
  min-height: 0;
  max-width: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  /*
   * 不要用外层 overflow-x:auto 滚整张表——会整块平移，导致 fixed-right 失效。
   * 横向滚动由 el-table 内部 ElScrollbar（body-wrapper）负责，此处只限宽不溢出。
   */
  :deep(.table-wrapper.table-responsive) {
    flex: 1;
    min-height: 0;
    min-width: 0;
    max-width: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  :deep(.table-wrapper .el-table) {
    width: 100%;
    max-width: 100%;
    min-height: 0;
  }

  /* 加粗 Element Plus 表格内置横向滚动条，便于拖拽 */
  :deep(.table-wrapper .el-table__body-wrapper .el-scrollbar__bar.is-horizontal) {
    height: 11px;
    bottom: 0;
    border-radius: 6px;
  }

  :deep(.table-wrapper .el-table__body-wrapper .el-scrollbar__thumb) {
    border-radius: 6px;
    background-color: var(--el-color-primary-light-3);
  }

  :deep(.table-wrapper .el-table__body-wrapper .el-scrollbar__thumb:hover) {
    background-color: var(--el-color-primary);
  }
}
</style>