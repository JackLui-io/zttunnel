<template>
  <section class="screen-center">
    <div class="screen-info-row">
      <div
        v-for="(item, i) in screenData.infoBoxes"
        :key="i"
        class="screen-info-box"
        :class="[`screen-info-box--icon-${item.icon}`]"
      >
        <span class="value" :class="{ 'value--warn': item.warn }">{{ item.value }}</span>
        <span class="label">{{ item.label }}</span>
      </div>
    </div>
    <div class="screen-map-wrap" />
  </section>
</template>

<script setup lang="ts">
import type { DataScreenInfo } from './dataScreenTypes'

defineProps<{ screenData: DataScreenInfo; loading?: boolean }>()
</script>

<style scoped>
.screen-center {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.screen-info-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  flex-wrap: wrap;
}

.screen-info-box {
  flex: 1 1 calc(16.666% - 14px);
  min-width: 100px;
  max-width: 160px;
  aspect-ratio: 1 / 1;
  box-sizing: border-box;
  border: 2px solid #ffffff;
  border-radius: 12px;
  padding: 5px;
  padding-top: 97px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-repeat: no-repeat, no-repeat;
  background-position: center 4px, 0 0;
  background-size: 106px 106px, 100% 100%;
}

.screen-info-box--icon-1 {
  background-image: url('/dataScreen/images/top1.png'),
    linear-gradient(0deg, #ffffff 0%, #c7e4ae 80.27%, #8cc8a6 99.61%);
}
.screen-info-box--icon-2 {
  background-image: url('/dataScreen/images/top2.png'),
    linear-gradient(0deg, #ffffff 0%, #c7e4ae 80.27%, #8cc8a6 99.61%);
}
.screen-info-box--icon-3 {
  background-image: url('/dataScreen/images/top3.png'),
    linear-gradient(0deg, #ffffff 0%, #c7e4ae 80.27%, #8cc8a6 99.61%);
}
.screen-info-box--icon-4 {
  background-image: url('/dataScreen/images/top4.png'),
    linear-gradient(0deg, #ffffff 0%, #c7e4ae 80.27%, #8cc8a6 99.61%);
}
.screen-info-box--icon-5 {
  background-image: url('/dataScreen/images/top5.png'),
    linear-gradient(0deg, #ffffff 0%, #c7e4ae 80.27%, #8cc8a6 99.61%);
}
.screen-info-box--icon-6 {
  background-image: url('/dataScreen/images/top6.png'),
    linear-gradient(0deg, #ffffff 0%, #c7e4ae 80.27%, #8cc8a6 99.61%);
}

.screen-info-box .value {
  font-size: 20px;
  color: #37a272;
  font-weight: bold;
  font-family: 'Microsoft Ya Hei', sans-serif;
  text-align: center;
  line-height: 1.3;
}

.screen-info-box .value--warn {
  color: #ff742e;
}

.screen-info-box .label {
  font-size: 14px;
  color: #4f5352;
  font-family: 'Microsoft Ya Hei', sans-serif;
  text-align: center;
  margin-top: 4px;
  white-space: nowrap;
}

/* 1400px 以下：信息框更紧凑，支持换行 */
@media (max-width: 1400px) {
  .screen-info-row {
    gap: 12px;
  }

  .screen-info-box {
    flex: 1 1 calc(33.333% - 10px);
    min-width: 90px;
    max-width: 140px;
  }

  .screen-info-box .value {
    font-size: 16px;
  }

  .screen-info-box .label {
    font-size: 12px;
  }
}

.screen-map-wrap {
  flex: 1;
  min-height: 0;
}

@media (max-width: 768px) {
  .screen-info-row {
    flex-wrap: wrap;
    gap: 12px;
  }

  .screen-info-box {
    min-width: 120px;
    max-width: none;
    flex: 1 1 calc(50% - 6px);
  }

  .screen-map-wrap {
    min-height: 250px;
  }
}
</style>
