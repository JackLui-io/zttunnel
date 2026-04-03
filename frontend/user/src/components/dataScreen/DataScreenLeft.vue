<template>
  <aside class="screen-left">
    <CardEnergyRank :list="screenData.energyRank" />
    <CardMonthlySaving :list="screenData.monthlySaving" />
    <CardEfficiency :list="screenData.efficiency" />
  </aside>
</template>

<script setup lang="ts">
import CardEnergyRank from './cards/CardEnergyRank.vue'
import CardMonthlySaving from './cards/CardMonthlySaving.vue'
import CardEfficiency from './cards/CardEfficiency.vue'
import type { DataScreenInfo } from './dataScreenTypes'

defineProps<{ screenData: DataScreenInfo; loading?: boolean }>()
</script>

<style scoped>
.screen-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: -15px;
  min-height: 0;
  min-width: 0;
  overflow: hidden;
}

.screen-left :deep(.screen-card:nth-child(1)),
.screen-left :deep(.screen-card:nth-child(2)) {
  flex: 1.2;
}

.screen-left :deep(.screen-card:nth-child(3)) {
  flex: 0.75;
}

/* 左侧卡片标题图标随卡片宽度适配 */
.screen-left :deep(.screen-card-header) {
  background-size: 100% 100%;
}
.screen-left :deep(.screen-card-header-title) {
  margin-left: 9%;
}

@media (max-width: 768px) {
  .screen-left {
    margin-top: 0;
    gap: 12px;
    flex: none;
    min-height: auto;
  }

  .screen-left :deep(.screen-card) {
    flex: none !important;
    min-height: 180px;
  }
}
</style>
