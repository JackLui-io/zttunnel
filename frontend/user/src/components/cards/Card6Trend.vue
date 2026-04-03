<template>
  <div class="card card-6">
    <div class="card-6-header">
      <img src="/page1/Group70.png" alt="" class="card-6-header-bg" />
      <span class="card-6-header-title">总体月度用电/节电趋势</span>
    </div>
    <div class="card-6-body">
      <div class="card-6-inner">
        <div
          v-for="(d, i) in displayData"
          :key="i"
          class="card-6-month"
        >
          <div class="card-6-month-corner"></div>
          <div class="card-6-month-corner-hover"></div>
          <span class="card-6-month-label">{{ d.month }}月</span>
          <div class="card-6-month-content">
            <!-- 第一行：用电/节电 标签和图标 -->
            <div class="card-6-month-row1">
              <div class="card-6-month-cell">
                <img src="/page1/Vector1.png" alt="" class="card-6-month-icon" />
                <span class="card-6-month-label-text">用电</span>
              </div>
              <div class="card-6-month-cell">
                <img src="/page1/Vector2.png" alt="" class="card-6-month-icon" />
                <span class="card-6-month-label-text">节电</span>
              </div>
            </div>
            <!-- 第二行：具体数据量 -->
            <div class="card-6-month-row2">
              <div class="card-6-month-cell">
                <span class="card-6-month-value card-6-month-value--power">{{ (d.power ?? 0).toFixed(2) }}</span>
                <span class="card-6-month-unit">万kWh</span>
              </div>
              <div class="card-6-month-cell">
                <span class="card-6-month-value card-6-month-value--saving">{{ (d.saving ?? 0).toFixed(2) }}</span>
                <span class="card-6-month-unit">万kWh</span>
              </div>
            </div>
            <!-- 第三行：底部进度条 -->
            <div class="card-6-month-row3">
              <div class="card-6-bar card-6-bar--power">
                <span
                  v-for="j in BAR_SEGMENTS"
                  :key="j"
                  class="card-6-bar-seg"
                  :class="j <= powerFilled(d) ? 'filled' : 'unfilled'"
                ></span>
              </div>
              <div class="card-6-bar card-6-bar--saving">
                <span
                  v-for="j in BAR_SEGMENTS"
                  :key="j"
                  class="card-6-bar-seg"
                  :class="j <= savingFilled(d) ? 'filled' : 'unfilled'"
                ></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { BAR_SEGMENTS } from '@/data/dashboard'
import type { MonthlyData } from '@/data/dashboard'

/** 接口返回 kWh，卡片展示万kWh */
const KWH_TO_WAN_KWH = 10000

const props = withDefaults(
  defineProps<{
    monthlyData?: Array<{ month: number; power: number; saving: number }>
  }>(),
  { monthlyData: () => [] }
)

const displayData = computed<MonthlyData[]>(() => {
  const list = props.monthlyData
  const result: MonthlyData[] = []
  for (let m = 1; m <= 12; m++) {
    const found = list.find((d) => d.month === m)
    const powerKwh = found?.power ?? 0
    const savingKwh = found?.saving ?? 0
    result.push({
      month: m,
      power: powerKwh / KWH_TO_WAN_KWH,
      saving: savingKwh / KWH_TO_WAN_KWH,
    })
  }
  return result
})

/** 用电量占比 = power / (power + saving)，节电量占比 = saving / (power + saving) */
function powerFilled(d: MonthlyData) {
  const total = (d.power ?? 0) + (d.saving ?? 0)
  if (total <= 0) return 0
  return Math.round(BAR_SEGMENTS * (d.power ?? 0) / total)
}

function savingFilled(d: MonthlyData) {
  const total = (d.power ?? 0) + (d.saving ?? 0)
  if (total <= 0) return 0
  return Math.round(BAR_SEGMENTS * (d.saving ?? 0) / total)
}
</script>

<style scoped>
.card-6 {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: visible;
  background: transparent;
  border: none;
  min-height: 0;
  flex: 1;
}

.card-6-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
}
.card-6-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-6-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  line-height: 22px;
  color: #0f7f62;
}

.card-6-body {
  flex: 1;
  min-height: 0;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.card-6-inner {
  padding: 14px 20px 14px;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  grid-template-rows: repeat(2, minmax(110px, 1fr));
  gap: 10px 12px;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.card-6-month {
  position: relative;
  display: flex;
  flex-direction: column;
  background: url('/page1/Rectangle83.png') no-repeat center;
  background-size: 100% 100%;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  min-height: 0;
  transition: background 0.2s ease;
  container-type: size;
}
.card-6-month:hover {
  background-image: url('/page1/Rectangle91.png');
}

.card-6-month-corner {
  position: absolute;
  right: 0;
  top: 0;
  width: 48px;
  height: 32px;
  background: url('/page1/Rectangle84.png') no-repeat right top;
  background-size: contain;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.card-6-month-corner-hover {
  position: absolute;
  right: 0;
  top: 0;
  width: 48px;
  height: 32px;
  background: url('/page1/Rectangle92.png') no-repeat right top;
  background-size: contain;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}
.card-6-month:hover .card-6-month-corner-hover {
  opacity: 1;
}

.card-6-month-label {
  position: absolute;
  right: 4px;
  top: 6px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 12px;
  font-weight: 600;
  color: #ffffff;
  z-index: 1;
}

.card-6-month-content {
  padding: 16px 10px 10px 14px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
}

/* 三行：第一行略小，第二、三行均分；第二行与第三行之间留出间距 */
.card-6-month-row1 {
  flex: 0.9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  min-height: 0;
}
.card-6-month-row2,
.card-6-month-row3 {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  min-height: 0;
}
.card-6-month-row2 {
  align-items: flex-start;
  padding-top: 4px;
  margin-bottom: 8px;
}
.card-6-month-row3 {
  flex-direction: column;
  justify-content: flex-end;
  align-items: stretch;
  gap: 4px;
  padding-bottom: 2px;
}
.card-6-month-row3 .card-6-bar {
  width: 100%;
}

.card-6-month-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 0;
}

.card-6-month-icon {
  width: 14px;
  height: 14px;
  object-fit: contain;
}
.card-6-month-label-text {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  font-weight: 400;
  color: #5f646b;
}
.card-6-month-row1 .card-6-month-cell {
  flex-direction: row;
  align-items: center;
  gap: 4px;
}
.card-6-month-row1 .card-6-month-cell:last-child {
  justify-content: flex-end;
}
.card-6-month-row2 .card-6-month-cell:last-child {
  align-items: flex-end;
}

.card-6-month-value {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 22px;
  line-height: 1.2;
}
.card-6-month-value--power {
  color: #feb603;
}
.card-6-month-value--saving {
  color: #91caa7;
}
.card-6-month-unit {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 12px;
  line-height: 1.2;
  color: #9aaa98;
  margin-top: 2px;
}

.card-6-bar {
  display: flex;
  gap: 1px;
  min-height: 18px;
  flex: 1;
  align-items: stretch;
}

.card-6-bar-seg {
  flex: 1;
  min-width: 1px;
  border-radius: 0;
  transition: background 0.2s;
}
.card-6-bar--power .card-6-bar-seg.filled {
  background: rgba(254, 182, 3, 0.8);
}
.card-6-bar--power .card-6-bar-seg.unfilled {
  background: rgba(254, 182, 3, 0.25);
}
.card-6-bar--saving .card-6-bar-seg.filled {
  background: rgba(145, 202, 167, 0.9);
}
.card-6-bar--saving .card-6-bar-seg.unfilled {
  background: rgba(145, 202, 167, 0.3);
}

@media (max-width: 768px) {
  .card-6-header {
    height: 44px;
  }

  .card-6-header-title {
    font-size: 14px;
    left: 16px;
  }

  .card-6-inner {
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(4, 1fr);
    padding: 12px;
    gap: 8px;
  }

  .card-6-month {
    min-height: 100px;
  }
}
</style>
