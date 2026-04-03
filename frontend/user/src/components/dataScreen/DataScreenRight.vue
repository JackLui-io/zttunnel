<template>
  <aside class="screen-right">
    <CardDeviceStatus :data="screenData.deviceStatus" />
    <CardRadar :data="screenData.radarData" />
    <CardStats :chart="screenData.statsChart" />
  </aside>
</template>

<script setup lang="ts">
import CardDeviceStatus from './cards/CardDeviceStatus.vue'
import CardRadar from './cards/CardRadar.vue'
import CardStats from './cards/CardStats.vue'
import type { DataScreenInfo } from './dataScreenTypes'

defineProps<{ screenData: DataScreenInfo; loading?: boolean }>()
</script>

<style scoped>
.screen-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: -15px;
  min-height: 0;
  min-width: 0;
  overflow: hidden;
}

.screen-right :deep(.screen-card:nth-child(1)),
.screen-right :deep(.screen-card:nth-child(2)) {
  flex: 1.2;
}

.screen-right :deep(.screen-card:nth-child(3)) {
  flex: 0.75;
}

@media (max-width: 768px) {
  .screen-right {
    margin-top: 0;
    gap: 12px;
    flex: none;
    min-height: auto;
  }

  .screen-right :deep(.screen-card) {
    flex: none !important;
    min-height: 180px;
  }
}
</style>
