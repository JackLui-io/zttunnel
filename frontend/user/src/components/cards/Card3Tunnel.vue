<template>
  <div class="card card-3">
    <div class="card-3-header">
      <img src="/page1/Group67.png" alt="" class="card-3-header-bg" />
      <span class="card-3-header-title">隧道概况</span>
    </div>
    <div class="card-3-body">
      <div class="card-3-inner">
        <div v-for="(item, i) in tunnelSummary" :key="i" class="card-3-item">
          <div class="card-3-item-icon-wrap">
            <img :src="`/page1/${item.icon}`" alt="" class="card-3-item-icon" />
          </div>
          <div class="card-3-item-text">
            <div class="card-3-item-label">{{ item.label }}</div>
            <div class="card-3-item-unit">{{ item.unit }}</div>
          </div>
          <div class="card-3-item-badge">
            <span class="card-3-item-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TunnelSummaryItem } from '@/data/dashboard'

withDefaults(
  defineProps<{
    tunnelSummary?: TunnelSummaryItem[]
  }>(),
  {
    tunnelSummary: () => [
      { label: '高速公路', unit: '(条)', value: '0', icon: 'Group151.png' },
      { label: '隧道总数', unit: '(条)', value: '0', icon: 'Group152.png' },
      { label: '总里程', unit: '(km)', value: '0', icon: 'Group153.png' },
    ],
  }
)
</script>

<style scoped>
.card-3 {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: visible;
  background: transparent;
  border: none;
  flex: 1;
  min-height: 0;
}

.card-3-header {
  position: relative;
  width: 100%;
  height: 49px;
  flex-shrink: 0;
}
.card-3-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-3-header-title {
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

.card-3-body {
  flex: 1;
  min-height: 0;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.card-3-inner {
  padding: 8px 24px 8px;
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
  box-sizing: border-box;
}

.card-3-item {
  display: flex;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px solid rgba(176, 190, 197, 0.35);
}
.card-3-item:last-child {
  border-bottom: none;
}

.card-3-item-icon-wrap {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  background: rgba(222, 240, 214, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}
.card-3-item-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.card-3-item-text {
  flex: 1;
}
.card-3-item-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 16px;
  color: #5f646b;
}
.card-3-item-unit {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 11px;
  line-height: 14px;
  color: #9aaa98;
  margin-top: 1px;
}

.card-3-item-badge {
  flex-shrink: 0;
  min-width: 40px;
  height: 24px;
  background: #0f7f62;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
}
.card-3-item-value {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 14px;
  line-height: 20px;
  text-align: center;
  color: #ffffff;
}
</style>
