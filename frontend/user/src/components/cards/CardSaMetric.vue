<template>
  <div class="card-sa card-sa-metric">
    <div class="card-sa-body">
      <div class="sa-metric-card">
        <div class="sa-metric-header">
          <img :src="metric.icon" alt="" class="sa-metric-icon" />
          <span class="sa-metric-value">{{ metric.value }}</span>
          <span class="sa-metric-title">{{ metric.title }}</span>
        </div>
        <div class="sa-metric-body">
          <div
            v-for="(row, i) in metric.rows.slice(0, 2)"
            :key="i"
            class="sa-metric-row-box sa-metric-row-box--220"
          >
            <div class="sa-metric-row">
              <span class="label">{{ row.label }}</span>
              <span class="data">{{ row.data }}</span>
            </div>
          </div>
          <div
            v-if="metric.rows[2]"
            class="sa-metric-row-box sa-metric-row-box--222-merged"
          >
            <div class="sa-metric-row">
              <span class="label">{{ metric.rows[2].label }}</span>
              <span class="val">{{ metric.rows[2].val }}</span>
            </div>
            <div class="sa-metric-bar">
              <div class="sa-metric-bar-fill" :style="{ width: (metric.rows[2].barWidth ?? 0) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  metric: {
    icon: string
    value: string
    title: string
    rows: Array<{
      label: string
      data?: string
      val?: string
      barWidth?: number
    }>
  }
}>()
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
.card-sa-body {
  width: 100%;
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 15px;
  overflow: hidden;
}
.sa-metric-card {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 15px;
  padding: 16px 16px 16px;
}
.sa-metric-header {
  box-sizing: border-box;
  width: 100%;
  height: 125px;
  min-height: 125px;
  background: linear-gradient(180deg, #91caa7 0%, #bbdf9f 50%, #def0d6 100%);
  border: 1px solid #91caa7;
  border-radius: 5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.sa-metric-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  margin-bottom: 6px;
}
.sa-metric-value {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 22px;
  line-height: 32px;
  text-align: center;
  color: #0f7f62;
}
.sa-metric-title {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  text-align: center;
  color: #0f7f62;
  margin-top: 4px;
}
.sa-metric-body {
  flex: 1;
  padding: 10px 0 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
  min-height: 0;
}
.sa-metric-row-box {
  box-sizing: border-box;
  width: 100%;
  min-height: 32px;
  padding: 8px 10px;
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
}
.sa-metric-row-box--220 {
  background-image: url('/page1/Rectangle220.png');
}
.sa-metric-row-box--222-merged {
  box-sizing: border-box;
  width: 100%;
  min-height: 72px;
  padding: 12px 12px;
  background-image: url('/page1/Rectangle222.png');
  background-size: 100% 100%;
  background-repeat: no-repeat;
  background-position: center;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.sa-metric-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
}
.sa-metric-row .label {
  color: #5f646b;
}
.sa-metric-row .data {
  color: #0f7f62;
  text-align: right;
}
.sa-metric-row .val {
  font-weight: 700;
  color: #0f7f62;
  text-align: right;
}
.sa-metric-bar {
  width: min(195px, 100%);
  height: 22px;
  min-height: 22px;
  position: relative;
  overflow: hidden;
}
.sa-metric-bar::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: repeating-linear-gradient(
    90deg,
    rgba(184, 224, 200, 0.5) 0,
    rgba(184, 224, 200, 0.5) 2px,
    transparent 2px,
    transparent 5px
  );
}
.sa-metric-bar-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1;
  background: repeating-linear-gradient(90deg, #91caa7 0, #91caa7 2px, transparent 2px, transparent 5px);
}

@media (max-width: 768px) {
  .sa-metric-header {
    min-height: 100px;
  }
  .sa-metric-value {
    font-size: 20px;
  }
}
</style>
