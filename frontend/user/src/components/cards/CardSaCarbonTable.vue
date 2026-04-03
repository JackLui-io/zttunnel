<template>
  <div class="card-sa card-sa-carbon-table">
    <div class="card-sa-row3-header">
      <img src="/page1/Group70.png" alt="" class="card-sa-row3-header-bg" />
      <span class="card-sa-row3-header-title">分析表格</span>
    </div>
    <div class="card-sa-body">
      <div v-if="flattenedData.length > 0" class="sa-table-wrap">
        <table class="sa-table">
          <thead>
            <tr>
              <th v-for="col in tableColumns" :key="col.value">{{ col.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in flattenedData" :key="idx" :class="{ 'sa-table-row-alt': idx % 2 === 1 }">
              <td v-for="col in tableColumns" :key="col.value">{{ formatCell((row as Record<string, unknown>)[col.value], col.value) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="sa-table-empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CarbonVo, MeterReadingVo } from '@/api/analyze'

const props = defineProps<{
  data: CarbonVo[]
}>()

/** 固定列 + 动态 meterReading 列，与原前端 table4 一致 */
const tableColumns = computed(() => {
  const base = [
    { label: '日期', value: 'uploadTime' },
    { label: '当日耗电量（kWh）', value: 'dailyPowerConsumption' },
    { label: '累计耗电量（kWh）', value: 'cumulativePowerConsumption' },
  ]
  const vos = meterReadingVos.value
  if (vos.length === 0) return base
  return [
    ...base,
    ...vos.map((v, i) => ({
      label: (v.loopName ?? '') + '(kWh)',
      value: 'meterReading' + i,
    })),
  ]
})

/** 从 data 中取最后一条有 meterReadingVos 的记录的 vos 用于构建列（与原前端 table4 一致） */
const meterReadingVos = computed((): MeterReadingVo[] => {
  let last: MeterReadingVo[] = []
  for (const item of props.data ?? []) {
    const vos = item.meterReadingVos
    if (vos && vos.length > 0) last = vos
  }
  return last
})

/** 扁平化数据：为每行添加 meterReading0, meterReading1, ... */
const flattenedData = computed(() => {
  const list = props.data ?? []
  return list.map((item) => {
    const row = { ...item } as Record<string, unknown>
    const vos = item.meterReadingVos
    if (vos && vos.length > 0) {
      vos.forEach((v, i) => {
        row['meterReading' + i] = v.dataValue
      })
    }
    return row
  })
})

function formatCell(val: unknown, _key: string): string {
  if (val == null || val === '') return '--'
  return String(val)
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
  height: 100%;
  min-height: 0;
}
.card-sa-row3-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
  z-index: 1;
  overflow: hidden;
}
.card-sa-row3-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-sa-row3-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  color: #0f7f62;
}
.card-sa-body {
  flex: 1;
  min-height: 150px;
  padding: 12px 16px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 4px 8px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: visible;
}
.sa-table-wrap {
  overflow-x: auto;
  overflow-y: auto;
  max-height: 280px;
  scrollbar-width: thin;
  scrollbar-color: #91CAA7 transparent;
}
.sa-table-wrap::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.sa-table-wrap::-webkit-scrollbar-track {
  background: transparent;
}
.sa-table-wrap::-webkit-scrollbar-thumb {
  background: #91CAA7;
  border-radius: 4px;
}
.sa-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  font-family: 'Microsoft YaHei', sans-serif;
}
.sa-table th,
.sa-table td {
  padding: 10px 12px;
  text-align: center;
  color: #5f646b;
  border-bottom: 1px solid rgba(184, 224, 200, 0.6);
}
.sa-table th {
  background: rgba(15, 127, 98, 0.15);
  color: #0f7f62;
  font-weight: 600;
}
.sa-table-row-alt td {
  background: rgba(184, 224, 200, 0.2);
}
.sa-table-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: #5f646b;
  font-size: 14px;
}
</style>
