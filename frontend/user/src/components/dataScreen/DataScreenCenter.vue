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
    <div class="screen-map-wrap" @click.self="closePopover">
      <div class="screen-map-stage">
        <div class="screen-map-img-shell">
          <img class="screen-map-img" src="/dataScreen/images/cmap.png" alt="" />
          <div
            v-for="item in hotspotsMerged"
            :key="item.tunnelId"
            class="map-hotspot-slot"
            :class="{ 'map-hotspot-slot--active': activeTunnelId === item.tunnelId }"
            :style="{ left: item.hotspotLeft, top: item.hotspotTop }"
          >
            <button
              type="button"
              class="map-pin-btn"
              :class="{ 'map-pin-btn--active': activeTunnelId === item.tunnelId }"
              :aria-label="`查看${item.provinceName}（图标）`"
              @click.stop="toggleHotspot(item.tunnelId!)"
            >
              <span class="map-pin-stack">
                <span class="map-pin-frame">
                  <span class="map-pin-inner">
                    <svg class="map-pin-lightning" viewBox="0 0 24 24" aria-hidden="true">
                      <path
                        fill="#ffffff"
                        d="M11 15H6l7-14v8h5l-7 14v-8z"
                      />
                    </svg>
                  </span>
                </span>
                <span class="map-pin-pointer" aria-hidden="true"></span>
              </span>
            </button>
            <button
              type="button"
              class="map-hotspot-btn"
              :class="{ 'map-hotspot-btn--active': activeTunnelId === item.tunnelId }"
              :aria-label="`查看${item.provinceName}（热力点）`"
              @click.stop="toggleHotspot(item.tunnelId!)"
            >
              <span class="map-hotspot-pulse" />
              <span class="map-hotspot-core" />
            </button>
            <div
              v-if="activeTunnelId === item.tunnelId"
              class="map-popover"
              role="dialog"
            >
              <div class="map-popover-head" aria-hidden="true"></div>
              <div class="map-popover-body">
                <div class="map-popover-line">
                  <span class="map-popover-label">管理单位：</span>
                  <span class="map-popover-value map-popover-value--text">{{ item.companyName }}</span>
                </div>
                <template v-if="item.lineTunnelRows.length > 0">
                  <div
                    v-for="(row, li) in item.lineTunnelRows"
                    :key="`${item.tunnelId}-${li}`"
                    class="map-popover-line map-popover-line--stack"
                  >
                    <span class="map-popover-label map-popover-label--line">{{ row.lineName }}：</span>
                    <span class="map-popover-value">{{ row.tunnelCount }} 条隧道</span>
                  </div>
                </template>
                <div v-else class="map-popover-line map-popover-line--muted">
                  <span class="map-popover-note">暂无线路数据</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import type { DataScreenInfo } from './dataScreenTypes'
import type { TunnelNode } from '@/api/tunnel'
import { buildScreenMapRegionsFromTree } from './mapHotspotsFromTree'

/**
 * 热点在 cmap.png 上的位置（相对 .screen-map-img-shell 的百分比）
 * 1 陕西 / 542 广西 / 552 湖南 / 600 吉林 — 已按省界示意校正（仍可按换图再调）
 */
const HOTSPOT_LAYOUT: Record<number, { left: string; top: string }> = {
  1: { left: '54%', top: '54%' },
  542: { left: '56%', top: '82%' },
  552: { left: '58%', top: '70%' },
  600: { left: '80%', top: '30%' },
}

const props = defineProps<{
  screenData: DataScreenInfo
  loading?: boolean
  tunnelTree?: TunnelNode[] | null
}>()

const activeTunnelId = ref<number | null>(null)

const hotspotsMerged = computed(() => {
  const list = buildScreenMapRegionsFromTree(props.tunnelTree ?? null)
  return list.map((r) => {
    const tid = Number(r.tunnelId)
    const pos = HOTSPOT_LAYOUT[tid] ?? { left: '50%', top: '50%' }
    return {
      ...r,
      tunnelId: tid,
      hotspotLeft: pos.left,
      hotspotTop: pos.top,
    }
  })
})

function toggleHotspot(tunnelId: number) {
  activeTunnelId.value = activeTunnelId.value === tunnelId ? null : tunnelId
}

function closePopover() {
  activeTunnelId.value = null
}

function onDocPointerDown(ev: MouseEvent) {
  const t = ev.target
  if (!(t instanceof Element)) return
  if (t.closest('.map-hotspot-btn') || t.closest('.map-pin-btn') || t.closest('.map-popover')) return
  activeTunnelId.value = null
}

onMounted(() => {
  document.addEventListener('pointerdown', onDocPointerDown, true)
})
onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', onDocPointerDown, true)
})
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  position: relative;
}

.screen-map-stage {
  width: 100%;
  flex: 1;
  min-height: 220px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 4%;
}

.screen-map-img-shell {
  position: relative;
  z-index: 0;
  display: inline-block;
  max-width: 100%;
  line-height: 0;
}

.screen-map-img {
  display: block;
  max-width: 100%;
  width: auto;
  height: auto;
  /* 在上次 1.2 倍基础上再 ×1.2，相对最早约 1.44 倍 */
  max-height: min(74.88vh, 749px);
  object-fit: contain;
  user-select: none;
  pointer-events: none;
}

.map-hotspot-slot {
  position: absolute;
  width: 28px;
  height: 28px;
  transform: translate(-50%, -50%);
  z-index: 2;
}

.map-pin-btn {
  position: absolute;
  left: 50%;
  bottom: calc(100% + 6px);
  transform: translateX(-50%);
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  z-index: 3;
  line-height: 0;
}

.map-pin-btn:focus-visible {
  outline: 2px solid #fff;
  outline-offset: 2px;
  border-radius: 12px;
}

.map-pin-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.map-pin-frame {
  padding: 6px;
  border-radius: 10px;
  background: #c8e8d4;
  box-shadow: 0 2px 8px rgba(0, 70, 40, 0.18);
}

.map-pin-inner {
  width: 25px;
  height: 25px;
  border-radius: 7px;
  background: linear-gradient(165deg, #6fb884 0%, #4a9d62 55%, #3d8a54 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-pin-lightning {
  width: 20px;
  height: 20px;
  display: block;
}

.map-pin-pointer {
  width: 0;
  height: 0;
  border-left: 7px solid transparent;
  border-right: 7px solid transparent;
  border-top: 8px solid #c8e8d4;
  margin-top: -1px;
  filter: drop-shadow(0 1px 1px rgba(0, 40, 25, 0.12));
}

.map-pin-btn--active .map-pin-frame {
  box-shadow: 0 0 0 2px rgba(227, 37, 43, 0.45), 0 2px 10px rgba(0, 70, 40, 0.22);
}

/* 展开信息框时抬到最上层，避免其他红色热力点盖住弹层文字 */
.map-hotspot-slot--active {
  z-index: 1000;
}

.map-hotspot-btn {
  position: relative;
  z-index: 1;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
}

.map-hotspot-core {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 12px;
  height: 12px;
  margin: -6px 0 0 -6px;
  border-radius: 50%;
  background: #e3252b;
  box-shadow:
    0 0 0 2px rgba(255, 255, 255, 0.98),
    0 0 10px 3px rgba(227, 37, 43, 0.75),
    0 0 22px 6px rgba(227, 37, 43, 0.38);
}

.map-hotspot-pulse {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 30px;
  height: 30px;
  margin: -15px 0 0 -15px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(227, 37, 43, 0.45) 0%, rgba(227, 37, 43, 0.08) 55%, transparent 70%);
  animation: map-pulse 1.8s ease-out infinite;
}

.map-hotspot-btn--active .map-hotspot-pulse {
  animation: none;
  background: radial-gradient(circle, rgba(227, 37, 43, 0.55) 0%, rgba(227, 37, 43, 0.12) 55%, transparent 70%);
}

@keyframes map-pulse {
  0% {
    transform: scale(0.65);
    opacity: 1;
  }
  100% {
    transform: scale(1.35);
    opacity: 0;
  }
}

.map-popover {
  position: absolute;
  left: 50%;
  /* 红点上方 + 闪电图钉高度 + 间距，避免挡住文案 */
  bottom: calc(100% + 50px);
  transform: translateX(-50%);
  min-width: 240px;
  max-width: 300px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 22px rgba(0, 50, 35, 0.22), 0 0 1px rgba(126, 184, 145, 0.6);
  text-align: left;
  pointer-events: auto;
  z-index: 50;
  font-family: 'Microsoft Ya Hei', 'PingFang SC', sans-serif;
}

.map-popover-head {
  height: 14px;
  min-height: 14px;
  background: linear-gradient(90deg, #76b891 0%, #c8e6b2 100%);
}

.map-popover-body {
  padding: 12px 14px 14px;
  background: #eaf6ee;
  border-radius: 0 0 11px 11px;
  max-height: min(52vh, 360px);
  overflow-y: auto;
}

.map-popover-line {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.45;
}

.map-popover-line:first-child {
  margin-top: 0;
}

.map-popover-label {
  flex-shrink: 0;
  color: #4a6354;
}

.map-popover-value {
  color: #43a074;
  font-weight: 600;
  text-align: right;
}

.map-popover-value--text {
  font-weight: 600;
  max-width: 62%;
  word-break: break-all;
}

.map-popover-line--stack {
  align-items: flex-start;
}

.map-popover-label--line {
  flex: 1 1 auto;
  min-width: 0;
  word-break: break-word;
}

.map-popover-line--muted {
  justify-content: center;
}

.map-popover-note {
  color: #7a8f82;
  font-size: 13px;
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

  .screen-map-img {
    max-height: 57.6vh;
  }
}
</style>
