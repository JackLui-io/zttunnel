<template>
  <div class="screen-card energy-rank-card">
    <div class="screen-card-header">
      <span class="screen-card-header-title">隧道能耗排行</span>
    </div>
    <div class="screen-card-body">
      <div v-if="list.length === 0" class="energy-rank-empty">暂无数据</div>
      <div v-else class="energy-rank-list">
        <div
          v-for="item in list"
          :key="item.rank"
          class="energy-rank-row"
        >
          <span :class="['rank-badge', `rank-badge--${item.type}`]">
            <span class="rank-badge-inner">{{ String(item.rank).padStart(2, '0') }}</span>
          </span>
          <span class="rank-month">{{ item.month }}</span>
          <div class="rank-progress-wrap">
            <div
              v-if="item.consumptionPercent > 0"
              :class="['rank-progress-fill', `rank-progress-fill--${item.type}`]"
              :style="{ width: `${item.consumptionPercent}%` }"
            />
            <div
              v-if="item.savingPercent > 0"
              class="rank-progress-fill rank-progress-fill--saving"
              :style="{ width: `${item.savingPercent}%` }"
            />
          </div>
          <span :class="['rank-value', `rank-value--${item.type}`]">{{ item.value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  list: Array<{
    rank: number
    month: string
    consumptionPercent: number
    savingPercent: number
    value: string
    type: 'orange' | 'yellow' | 'green'
  }>
}>()
</script>

<style scoped>
@import '../dataScreenCards.css';
</style>
