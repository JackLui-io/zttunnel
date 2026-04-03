<template>
  <div class="card-sa card-sa-traffic-table">
    <div class="card-sa-row3-header">
      <img src="/page1/Group70.png" alt="" class="card-sa-row3-header-bg" />
      <span class="card-sa-row3-header-title">分析表格</span>
    </div>
    <div class="card-sa-body">
      <div v-if="data.length > 0" class="sa-table-wrap">
        <table class="sa-table">
          <thead>
            <tr>
              <th v-for="col in columns" :key="col.value">{{ col.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in data" :key="idx" :class="{ 'sa-table-row-alt': idx % 2 === 1 }">
              <td v-for="col in columns" :key="col.value">{{ formatCell((row as Record<string, unknown>)[col.value], col.value) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="sa-table-empty">暂无数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TrafficFlowOrSpeedVo } from '@/api/analyze'

const props = defineProps<{
  data: TrafficFlowOrSpeedVo[]
}>()

const columns = [
  { label: '日期', value: 'uploadTime' },
  { label: '每日车流量(辆/d)', value: 'trafficFlow' },
  { label: '最大车流(辆/h)', value: 'maxTrafficFlow' },
  { label: '最小车流(辆/h)', value: 'minTrafficFlow' },
  { label: '平均车流量(辆/h)', value: 'avgTrafficFlow' },
  { label: '最大车速(km/h)', value: 'maxSpeed' },
  { label: '最小车速(km/h)', value: 'minSpeed' },
  { label: '平均车速(km/h)', value: 'avgSpeed' },
]

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
