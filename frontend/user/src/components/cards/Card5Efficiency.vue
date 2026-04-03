<template>
  <div class="card card-5">
    <div class="card-5-header">
      <img src="/page1/Group69.png" alt="" class="card-5-header-bg" />
      <span class="card-5-header-title">总体月度节能效率指标</span>
    </div>
    <div class="card-5-body">
      <div class="card-5-inner">
        <div class="card-5-item">
          <div class="card-5-item-header">
            <span class="card-5-item-label">年度节电率</span>
            <span class="card-5-item-value">{{ efficiencyData.powerRate }}%</span>
          </div>
          <div class="card-5-bar-wrap card-5-bar--power">
            <div class="card-5-bar-fill" :style="{ width: efficiencyData.powerRate + '%' }"></div>
          </div>
        </div>
        <div class="card-5-item">
          <div class="card-5-item-header">
            <span class="card-5-item-label">年度碳减排率</span>
            <span class="card-5-item-value card-5-item-value--carbon">{{ efficiencyData.carbonRate }}%</span>
          </div>
          <div class="card-5-bar-wrap card-5-bar--carbon">
            <div class="card-5-bar-fill" :style="{ width: efficiencyData.carbonRate + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StatisticsVo } from '@/api/analyze'

const props = defineProps<{
  statistics?: StatisticsVo | null
}>()

/** 从 statistics 解析节能效率数据，无数据时显示占位（后端 BigDecimal 可能序列化为 number 或 string） */
const efficiencyData = computed(() => {
  const s = props.statistics
  const powerRate = Number(s?.theoreticalPowerSavingRate ?? 0)
  const carbonRate = Number(s?.theoreticalCarbonEmissionReduction ?? 0)
  return {
    powerRate,
    carbonRate,
  }
})
</script>

<style scoped>
.card-5 {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: visible;
  background: transparent;
  border: none;
  flex: 1;
  min-height: 0;
}

.card-5-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
}
.card-5-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-5-header-title {
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

.card-5-body {
  flex: 1;
  min-height: 0;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.card-5-inner {
  padding: 16px 24px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 22px;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.card-5-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-5-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-5-item-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
}

.card-5-item-value {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 24px;
  line-height: 32px;
  color: #0f7f62;
}
.card-5-item-value--carbon {
  color: #618430;
}

.card-5-bar-wrap {
  width: 100%;
  height: 14px;
  border-radius: 99px;
  overflow: hidden;
}

.card-5-bar--power {
  background: rgba(145, 202, 167, 0.4);
}
.card-5-bar--carbon {
  background: rgba(178, 212, 124, 0.4);
}

.card-5-bar-fill {
  height: 100%;
  border-radius: 99px;
  transition: width 0.3s ease;
}
.card-5-bar--power .card-5-bar-fill {
  background: #6bb88d;
}
.card-5-bar--carbon .card-5-bar-fill {
  background: #a8c96a;
}
</style>
