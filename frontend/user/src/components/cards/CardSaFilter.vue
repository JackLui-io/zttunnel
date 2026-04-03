<template>
  <div class="card-sa card-sa-filter">
    <div class="card-sa-body">
      <div class="card-sa-row1-row1">
        <div class="sa-time-filter sa-date-picker-wrap">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            class="sa-el-date-picker"
            @change="onDateChange"
          />
        </div>
        <button type="button" class="sa-quick-date-btn" @click="setRange('7')">近7天</button>
        <button type="button" class="sa-quick-date-btn" @click="setRange('1m')">近一月</button>
        <button type="button" class="sa-quick-date-btn" @click="setRange('3m')">近三月</button>
      </div>
      <div class="card-sa-row1-row2">
        <button type="button" class="sa-func-btn" :class="{ active: activeFunc === 'overall' }" @click="setActiveFunc('overall')">
          总体分析
        </button>
        <button type="button" class="sa-func-btn" :class="{ active: activeFunc === 'traffic' }" @click="setActiveFunc('traffic')">
          车流/车速
        </button>
        <button type="button" class="sa-func-btn" :class="{ active: activeFunc === 'brightness' }" @click="setActiveFunc('brightness')">
          洞内外亮度
        </button>
        <button type="button" class="sa-func-btn" :class="{ active: activeFunc === 'energy' }" @click="setActiveFunc('energy')">
          能碳数据
        </button>
        <button type="button" class="sa-func-btn sa-export-btn" title="数据导出" @click="$emit('export')">
          数据导出
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getRangeNear7Days, getRangeNear1Month, getRangeNear3Months } from '@/utils/saDate'

export interface SaDateRange {
  startTime: string
  endTime: string
}

const props = defineProps<{
  modelValue: SaDateRange
  activeFunc?: 'overall' | 'traffic' | 'brightness' | 'energy'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: SaDateRange]
  'update:activeFunc': [value: 'overall' | 'traffic' | 'brightness' | 'energy']
  export: []
}>()

const activeFunc = computed({
  get: () => props.activeFunc ?? 'overall',
  set: (v) => emit('update:activeFunc', v),
})

function setActiveFunc(v: 'overall' | 'traffic' | 'brightness' | 'energy') {
  emit('update:activeFunc', v)
}

/** el-date-picker 的 v-model，格式 [startTime, endTime] */
const dateRange = ref<[string, string] | null>([
  props.modelValue.startTime,
  props.modelValue.endTime,
])

watch(
  () => [props.modelValue.startTime, props.modelValue.endTime],
  () => {
    dateRange.value = [props.modelValue.startTime, props.modelValue.endTime]
  }
)

/** 与原前端 timeClick 一致：近7天=timeInterval(6)，近一月=getOldMonth(1)，近三月=getOldMonth(3) */
function setRange(key: '7' | '1m' | '3m') {
  let range: SaDateRange
  if (key === '7') range = getRangeNear7Days()
  else if (key === '1m') range = getRangeNear1Month()
  else range = getRangeNear3Months()
  dateRange.value = [range.startTime, range.endTime]
  emit('update:modelValue', range)
}

function onDateChange(val: [string, string] | null) {
  if (val && val[0] && val[1]) {
    emit('update:modelValue', { startTime: val[0], endTime: val[1] })
  }
}
</script>

<style scoped>
.card-sa {
  border: none;
  background: transparent;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: visible;
  width: 100%;
  min-height: 0;
}
.card-sa-body {
  width: 100%;
  flex: 1;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 15px;
  overflow: hidden;
  padding: 24px 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.card-sa-row1-row1 {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.sa-time-filter {
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  align-items: center;
  width: min(400px, 100%);
  height: 40px;
  padding: 0 12px;
  gap: 8px;
  background: #def0d6;
  border: 1px solid #b8e0c8;
  box-shadow: 0 4px 4px rgba(0, 0, 0, 0.25);
  border-radius: 5px;
}
.sa-time-filter:hover {
  opacity: 0.95;
}
.sa-date-picker-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sa-el-date-picker {
  flex: 1;
  min-width: 200px;
}
.sa-el-date-picker :deep(.el-date-editor),
.sa-el-date-picker :deep(.el-range-editor),
.sa-el-date-picker :deep(.el-input__wrapper) {
  background: #def0d6 !important;
  box-shadow: none !important;
  border: none !important;
}
.sa-el-date-picker :deep(.el-input) {
  --el-input-bg-color: #def0d6;
  --el-input-border-color: transparent;
  --el-input-hover-border-color: transparent;
  --el-input-focus-border-color: transparent;
}
.sa-el-date-picker :deep(.el-input__wrapper) {
  padding: 0 8px;
}
.sa-el-date-picker :deep(.el-input__inner) {
  color: #0f7f62;
  font-size: 14px;
  font-family: 'Microsoft YaHei', sans-serif;
}
.sa-el-date-picker :deep(.el-input__wrapper:hover),
.sa-el-date-picker :deep(.el-input__wrapper.is-focus),
.sa-el-date-picker :deep(.el-date-editor:hover),
.sa-el-date-picker :deep(.el-date-editor.is-focus) {
  background: #def0d6 !important;
  box-shadow: none !important;
}
.sa-el-date-picker :deep(.el-range-separator) {
  color: #0f7f62;
  font-size: 14px;
}
.sa-el-date-picker :deep(.el-range-input) {
  color: #0f7f62;
  background: transparent;
}
.sa-el-date-picker :deep(.el-icon) {
  color: #0f7f62;
}
.sa-time-filter-to {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #0f7f62;
  margin: 0 4px;
}
.sa-quick-date-btn {
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 11px 19px;
  gap: 10px;
  min-width: 80px;
  height: 40px;
  background: #def0d6;
  border: 1px solid #b8e0c8;
  border-radius: 5px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
  cursor: pointer;
}
.sa-quick-date-btn:hover {
  background: linear-gradient(180deg, #91caa7 0%, #bbdf9f 50%, #def0d6 100%);
}
.card-sa-row1-row2 {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}
.sa-func-btn {
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 11px 19px;
  gap: 10px;
  min-width: 94px;
  height: 40px;
  background: #def0d6;
  border: 1px solid #b8e0c8;
  border-radius: 5px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
  cursor: pointer;
}
.sa-func-btn:hover {
  background: linear-gradient(180deg, #91caa7 0%, #bbdf9f 50%, #def0d6 100%);
}
.sa-func-btn.active {
  background: linear-gradient(180deg, #91caa7 0%, #bbdf9f 50%, #def0d6 100%);
  border-color: #b8e0c8;
  color: #0f7f62;
  font-weight: 600;
}
.sa-export-btn {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.sa-export-icon-img {
  display: block;
  height: 18px;
  width: auto;
  object-fit: contain;
}

@media (max-width: 768px) {
  .card-sa-body {
    padding: 16px;
    gap: 12px;
  }
  .card-sa-row1-row1 {
    gap: 12px;
  }
  .card-sa-row1-row2 {
    gap: 12px;
  }
  .sa-func-btn {
    min-width: 80px;
    padding: 8px 12px;
    font-size: 13px;
  }
}
</style>

<style>
/* 非 scoped，覆盖 Element Plus 日期选择器默认白色背景，与按钮同色 */
.card-sa-filter .sa-time-filter .el-date-editor,
.card-sa-filter .sa-time-filter .el-range-editor,
.card-sa-filter .sa-time-filter .el-input__wrapper {
  background-color: #def0d6 !important;
  background: #def0d6 !important;
  box-shadow: none !important;
}
</style>
