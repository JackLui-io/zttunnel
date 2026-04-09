<template>
  <div class="card card-rt card-rt-entrance">
    <div class="card-rt-header">
      <img src="/page1/Group65.png" alt="" class="card-rt-header-bg" />
      <span class="card-rt-header-title">入口里程-出口里程</span>
    </div>
    <div class="card-rt-body">
      <div class="entrance-mileage-box">
        <img src="/page1/Union157.png" alt="" class="mileage-arrow" />
        <span class="mileage-value">{{ inMileage }}</span>
        <div class="mileage-label-box"><span class="mileage-label">入口里程</span></div>
      </div>
      <!-- 用 CSS 绘制隧道，便于灯具精确定位 -->
      <div ref="tunnelWrapRef" class="entrance-tunnel-wrap">
        <!-- Rectangle 269：隧道主体 -->
        <div class="entrance-tunnel-inner">
          <div class="entrance-tunnel-lamps">
            <div
              v-for="(ratio, i) in lampPositions"
              :key="i"
              class="entrance-lamp"
              :class="{ 'entrance-lamp--on': isLampOn(ratio, i) }"
              :style="{ left: `${ratio * 100}%` }"
            />
          </div>
          <!-- Rectangle 270：底部深色路形 -->
          <div class="entrance-tunnel-road">
            <!-- Group 99：路形中的虚线 -->
            <div class="entrance-tunnel-line" />
          </div>
          <!-- WebSocket 车流：多车按 0–1 比例与灯具共用同一坐标轴 -->
          <img
            v-for="c in cars"
            v-show="cars.length > 0"
            :key="c.key"
            src="/page1/car.png"
            alt=""
            class="entrance-tunnel-car entrance-tunnel-car--moving"
            :style="carMovingStyle(c.x)"
          />
        </div>
      </div>
      <div class="exit-mileage-box">
        <img src="/page1/Union158.png" alt="" class="mileage-arrow" />
        <span class="mileage-value">{{ outMileage }}</span>
        <div class="mileage-label-box"><span class="mileage-label">出口里程</span></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount, computed } from 'vue'
import { getCurrentModel, decToHexMileage, decToHexNum } from '@/api/analyze'
import { getDeviceLamp } from '@/api/device'
import { useTunnelWebSocket } from '@/composables/useTunnelWebSocket'

const props = defineProps<{ tunnelId?: number; parentTunnelId?: number }>()

const tunnelIdRef = computed(() => props.tunnelId ?? null)
const parentTunnelIdRef = computed(() => props.parentTunnelId ?? null)
const { cars, running } = useTunnelWebSocket(tunnelIdRef, parentTunnelIdRef)

/** 照明模式：0=无极调光 1=智慧调光 2=固定功率。来自 getCurrentModel.mode */
const lampMode = ref<number | undefined>(undefined)

/**
 * 灯态（与小车数据源一致）：
 * - 小车 **仅由** WebSocket 车流推送产生；无车时无动画车。
 * - 无车：mode != 1 常量全亮；mode == 1 全暗。
 * - 有车：按段随车亮（车在相邻灯比例的中点区间内则该灯亮）。
 */
function isLampOn(ratio: number, index: number) {
  const positions = lampPositions.value
  const prev: number = index > 0 ? (positions[index - 1] ?? 0) : 0
  const next: number = index < positions.length - 1 ? (positions[index + 1] ?? 1) : 1
  const left = (prev + ratio) / 2
  const right = (ratio + next) / 2
  const carInSegment = running.value.some((x) => x >= left && x <= right)
  if (running.value.length > 0) {
    return carInSegment
  }
  if (lampMode.value != null && lampMode.value !== 1) return true
  return false
}

/** 与 useTunnelWebSocket 中 running 一致：沿洞轴 0–1，与灯具 left 百分比对齐 */
function carMovingStyle(x: number) {
  return {
    left: `${x * 100}%`,
    transform: 'translateX(-50%)',
  } as Record<string, string>
}

const tunnelWrapRef = ref<HTMLElement | null>(null)
/** 灯具位置（0-1 比例），严格按 /tunnel/get/device/lamp 返回的 data 逐项计算 */
const lampPositions = ref<number[]>([])

/** 严格依据 API：getDeviceLamp 的 data + getCurrentModel 的 in/out 里程，计算每个灯的位置比例 */
async function fetchLampPositions() {
  const tid = props.tunnelId
  if (tid == null) {
    lampPositions.value = []
    return
  }
  try {
    const [lampsSettled, modelSettled] = await Promise.allSettled([
      getDeviceLamp(tid),
      getCurrentModel(tid),
    ])
    let list = lampsSettled.status === 'fulfilled' ? (Array.isArray(lampsSettled.value) ? lampsSettled.value : []) : []
    let modelRes = modelSettled.status === 'fulfilled' ? modelSettled.value : null

    if (list.length === 0 && props.parentTunnelId != null) {
      const [pLamps, pModel] = await Promise.all([
        getDeviceLamp(props.parentTunnelId).catch(() => []),
        getCurrentModel(props.parentTunnelId).catch(() => null),
      ])
      list = Array.isArray(pLamps) ? pLamps : []
      modelRes = pModel
    } else if (list.length > 0 && modelRes == null && props.parentTunnelId != null) {
      modelRes = await getCurrentModel(props.parentTunnelId).catch(() => null)
    }

    const inM = decToHexNum(modelRes?.inMileageNum)
    const outM = decToHexNum(modelRes?.outMileageNum)
    const tunnelLong = outM - inM

    if (list.length === 0 || tunnelLong === 0) {
      lampPositions.value = []
      lampMode.value = undefined
      return
    }

    lampMode.value = typeof modelRes?.mode === 'number' ? modelRes.mode : undefined

    const positions: number[] = []
    for (const item of list) {
      const deviceNum = decToHexNum(Number(item.deviceNum ?? 0))
      const ratio = (deviceNum - inM) / tunnelLong
      const clamped = Math.min(1, Math.max(0, ratio))
      positions.push(clamped)
    }
    lampPositions.value = positions
  } catch {
    lampPositions.value = []
  }
}

const inMileage = ref('--')
const outMileage = ref('--')
let refreshTimer: ReturnType<typeof setInterval> | null = null

async function fetchModel() {
  if (props.tunnelId == null) {
    inMileage.value = '--'
    outMileage.value = '--'
    lampMode.value = undefined
    return
  }
  try {
    let model = await getCurrentModel(props.tunnelId).catch(() => null)
    if (model == null && props.parentTunnelId != null) {
      model = await getCurrentModel(props.parentTunnelId).catch(() => null)
    }
    const inVal = model?.inMileageNum
    const outVal = model?.outMileageNum
    inMileage.value = decToHexMileage(typeof inVal === 'number' ? inVal : undefined)
    outMileage.value = decToHexMileage(typeof outVal === 'number' ? outVal : undefined)
    lampMode.value = typeof model?.mode === 'number' ? model.mode : undefined
  } catch {
    inMileage.value = '--'
    outMileage.value = '--'
    lampMode.value = undefined
  }
}

watch(
  () => [props.tunnelId, props.parentTunnelId],
  () => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
    fetchModel()
    fetchLampPositions()
    if (props.tunnelId != null) {
      refreshTimer = setInterval(fetchModel, 60000)
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.card-rt {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-start;
  overflow: visible;
  padding: 0;
  border: none;
  background: transparent;
  width: 100%;
  height: 100%;
  min-height: 0;
}
.card-rt-header {
  position: relative;
  width: 100%;
  z-index: 1;
  height: 38px;
  flex-shrink: 0;
  overflow: hidden;
}
.card-rt-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-rt-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  color: #0f7f62;
}
.card-rt-body {
  position: relative;
  width: 100%;
  flex: 1;
  min-height: 60px;
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 2%;
  gap: 4px;
  box-sizing: border-box;
}

.entrance-mileage-box,
.exit-mileage-box {
  box-sizing: border-box;
  flex-shrink: 0;
  width: min(99px, 12%);
  min-width: 60px;
  height: 75px;
  border: 1px solid #b8e0c8;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
}
.entrance-mileage-box .mileage-arrow,
.exit-mileage-box .mileage-arrow {
  width: 16px;
  height: auto;
  object-fit: contain;
  margin-top: 4px;
}
.entrance-mileage-box .mileage-value,
.exit-mileage-box .mileage-value {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 12px;
  line-height: 14px;
  color: #5f646b;
  margin-top: 2px;
}
.entrance-mileage-box .mileage-label-box,
.exit-mileage-box .mileage-label-box {
  box-sizing: border-box;
  width: 100%;
  height: 22px;
  margin-top: auto;
  background: #def0d6;
  border: 1px solid #b8e0c8;
  border-top: none;
  border-radius: 0 0 10px 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.entrance-mileage-box .mileage-label,
.exit-mileage-box .mileage-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 11px;
  line-height: 14px;
  color: #53aa7d;
}

.entrance-tunnel-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  max-width: 80%;
  height: 95%;
  min-height: 70px;
  overflow: hidden;
  display: flex;
  align-items: center;
  margin: 0 8px;
}

/* Rectangle 269：隧道主体 - 原型图 */
.entrance-tunnel-inner {
  box-sizing: border-box;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 56px;
  background: #777777;
  border: 1px solid #000000;
  border-radius: 10px;
  overflow: hidden;
}

/* 灯具：贴隧道顶部，left 为 0–100% 严格按 API 比例 */
.entrance-tunnel-lamps {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 14px;
  z-index: 2;
  pointer-events: none;
}
.entrance-lamp {
  position: absolute;
  top: 2px;
  width: 20px;
  height: 8px;
  transform: translateX(-50%);
  background: #959595;
  border-radius: 2px;
  transition: background 0.2s ease;
}
/* Rectangle 273：亮灯时 */
.entrance-lamp--on {
  background: #FFFFFF;
}

/* Rectangle 270：隧道底部深色路形 - 原型图 */
.entrance-tunnel-road {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 22%;
  min-height: 19px;
  background: #444444;
  border-radius: 0 0 10px 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

/* Group 99：路形中的虚线 - 原型图（2px 避免高 DPI 下 1px 不显示） */
.entrance-tunnel-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 2px;
  min-height: 2px;
  background: repeating-linear-gradient(
    90deg,
    #ffffff 0,
    #ffffff 6px,
    transparent 6px,
    transparent 14px
  );
  transform: translateY(-50%);
  z-index: 1;
}

/* 小车：车轮贴紧路面，与灯具同一轨向（0–100% 为入口→出口） */
.entrance-tunnel-car {
  position: absolute;
  bottom: 4%;
  width: 18%;
  height: 40%;
  object-fit: contain;
  object-position: bottom;
  z-index: 4;
  pointer-events: none;
}
.entrance-tunnel-car--moving {
  transition: left 0.1s linear;
}
</style>
