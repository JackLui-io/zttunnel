<template>
  <div class="screen-card monthly-saving-card">
    <div class="screen-card-header">
      <span class="screen-card-header-title">月度节能统计</span>
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
              :class="['rank-progress-fill', `rank-progress-fill--${item.type}`, progressClass(item.percent)]"
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
  list: Array<{ rank: number; month: string; percent: number; value: string; type: 'orange' | 'yellow' | 'green' }>
}>()

function progressClass(percent: number) {
  if (percent >= 70) return 'rank-progress-fill--short-1'
  if (percent >= 60) return 'rank-progress-fill--short-2'
  return 'rank-progress-fill--short-3'
}
</script>

<style scoped>
@import '../dataScreenCards.css';

.monthly-saving-card .rank-progress-fill--short-1 {
  width: 73%;
}
.monthly-saving-card .rank-progress-fill--short-2 {
  width: 63%;
}
.monthly-saving-card .rank-progress-fill--short-3 {
  width: 48%;
}
</style>
