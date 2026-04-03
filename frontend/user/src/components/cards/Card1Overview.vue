<template>
  <div class="card card-1">
    <div class="card-1-header">
      <img src="/page1/Group65.png" alt="" class="card-1-header-bg" />
      <span class="card-1-header-title">年度数据概览</span>
    </div>
    <div class="card-1-body">
      <div class="card-1-overview-grid">
        <div v-for="(item, i) in overviewData" :key="i" class="card-1-overview-item">
          <div class="card-1-overview-icon-wrap">
            <img :src="`/page1/${item.icon}`" alt="" class="card-1-overview-icon" />
          </div>
          <div class="card-1-overview-text">
            <span class="card-1-overview-label">{{ item.label }}</span>
            <span class="card-1-overview-value">
              {{ item.value }}<span class="card-1-overview-unit">{{ item.unit }}</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StatisticsVo } from '@/api/analyze'
import type { OverviewItem } from '@/data/dashboard'

const props = defineProps<{
  statistics?: StatisticsVo | null
}>()

/** 从 statistics 解析为年度数据概览，无数据时显示占位 */
const overviewData = computed<OverviewItem[]>(() => {
  const s = props.statistics
  if (!s) {
    return [
      { label: '年度总耗电量', value: '--', unit: '万kWh', icon: 'Group147.png' },
      { label: '年总理论节电量', value: '--', unit: '万kWh', icon: 'Group148.png' },
      { label: '年总理论碳减排率', value: '--', unit: '%', icon: 'Group149.png' },
      { label: '年总理论节电率', value: '--', unit: '%', icon: 'Group150.png' },
    ]
  }
  const actual = s.actualPowerConsumption ?? 0
  const original = s.originalPowerConsumption ?? 0
  const saving = Math.max(0, original - actual)
  // 后端为 kWh，除以 10000 转为万 kWh；≥1 万保留 1 位小数，<1 万保留 2 位
  const toWanKwh = (v: number) => {
    const wan = v / 10000
    return wan >= 1 ? wan.toFixed(1) : wan.toFixed(2)
  }
  return [
    { label: '年度总耗电量', value: toWanKwh(actual), unit: '万kWh', icon: 'Group147.png' },
    { label: '年总理论节电量', value: toWanKwh(saving), unit: '万kWh', icon: 'Group148.png' },
    {
      label: '年总理论碳减排率',
      value: Number(s.theoreticalCarbonEmissionReduction ?? 0).toFixed(1),
      unit: '%',
      icon: 'Group149.png',
    },
    {
      label: '年总理论节电率',
      value: Number(s.theoreticalPowerSavingRate ?? 0).toFixed(1),
      unit: '%',
      icon: 'Group150.png',
    },
  ]
})
</script>

<style scoped>
.card-1 {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: visible;
  background: transparent;
  border: none;
}

.card-1-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
}
.card-1-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-1-header-title {
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

.card-1-body {
  flex: 1;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.card-1-overview-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 19px 30px 20px;
  height: 100%;
  box-sizing: border-box;
}

.card-1-overview-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.card-1-overview-icon-wrap {
  flex-shrink: 0;
  width: 81px;
  height: 79px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-1-overview-icon {
  width: 81px;
  height: 79px;
  object-fit: contain;
}

.card-1-overview-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.card-1-overview-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
}

.card-1-overview-value {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 24px;
  line-height: 32px;
  color: #0f7f62;
}

.card-1-overview-unit {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 12px;
  line-height: 16px;
  color: #9aaa98;
  margin-left: 2px;
}

/* 手机端：四项各占一行 */
@media (max-width: 768px) {
  .card-1-overview-grid {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 16px 20px;
  }

  .card-1-overview-item {
    flex: none;
    min-width: auto;
    padding: 12px 0;
    border-bottom: 1px solid rgba(176, 190, 197, 0.3);
  }
  .card-1-overview-item:last-child {
    border-bottom: none;
  }

  .card-1-overview-icon-wrap {
    width: 48px;
    height: 48px;
  }
  .card-1-overview-icon {
    width: 48px;
    height: 48px;
  }

  .card-1-overview-value {
    font-size: 20px;
  }
}
</style>
