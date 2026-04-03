<template>
  <div class="supervisory-control-main">
      <div class="sc-header">
        <CentralNavTabs />
        <CentralTunnelLabel />
      </div>
      <div class="sc-cell sc-cell-entrance">
        <CardEntrance
          :tunnel-id="tunnelStore.selectedTunnelId ?? undefined"
          :parent-tunnel-id="tunnelStore.selectedParentTunnelId ?? undefined"
        />
      </div>
      <div class="sc-cell sc-cell-brightness">
        <CardBrightness :tunnel-id="tunnelStore.selectedTunnelId ?? undefined" />
      </div>
      <div class="sc-cell sc-cell-traffic">
        <CardTraffic :tunnel-id="tunnelStore.selectedTunnelId ?? undefined" />
      </div>
      <div class="sc-cell sc-cell-power">
        <CardPower :tunnel-id="tunnelStore.selectedTunnelId ?? undefined" />
      </div>
      <div class="sc-cell sc-cell-speed">
        <CardSpeed :tunnel-id="tunnelStore.selectedTunnelId ?? undefined" />
      </div>
    </div>
</template>

<script setup lang="ts">
import { useTunnelStore } from '@/stores/tunnel'
import CentralTunnelLabel from '@/components/layout/CentralTunnelLabel.vue'
import CentralNavTabs from '@/components/layout/CentralNavTabs.vue'
import CardEntrance from '@/components/cards/CardEntrance.vue'
import CardBrightness from '@/components/cards/CardBrightness.vue'
import CardTraffic from '@/components/cards/CardTraffic.vue'
import CardPower from '@/components/cards/CardPower.vue'
import CardSpeed from '@/components/cards/CardSpeed.vue'

const tunnelStore = useTunnelStore()
</script>

<style scoped>
.supervisory-control-main {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: auto 150px minmax(200px, 1.2fr) minmax(200px, 1.2fr);
  gap: 10px;
  width: 100%;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
 /*  padding-bottom: 12px; 为底部卡片阴影留出空间 */
}

.sc-header {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 49px;
}

.sc-cell {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.sc-cell-entrance {
  grid-column: 1 / -1;
}
.sc-cell-entrance > * {
  flex: 1;
  min-height: 0;
}

.sc-cell-brightness {
  grid-column: 1;
  grid-row: 3;
}

.sc-cell-traffic {
  grid-column: 2;
  grid-row: 3;
}

.sc-cell-power {
  grid-column: 1;
  grid-row: 4;
}

.sc-cell-speed {
  grid-column: 2;
  grid-row: 4;
}

/* 平板：保持 2 列，缩小间距 */
@media (max-width: 1024px) {
  .supervisory-control-main {
    gap: 8px;
    grid-template-rows: auto 120px minmax(180px, 1.2fr) minmax(180px, 1.2fr);
  }
}

/* 移动端：单列堆叠 */
@media (max-width: 768px) {
  .supervisory-control-main {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto auto auto auto;
    gap: 12px;
    min-height: auto;
  }

  .sc-header {
    flex-wrap: wrap;
  }

  .sc-cell-entrance {
    grid-column: 1;
    min-height: 100px;
  }

  .sc-cell-brightness,
  .sc-cell-traffic,
  .sc-cell-power,
  .sc-cell-speed {
    grid-column: 1;
    grid-row: auto;
    min-height: 200px;
  }
}
</style>
