<template>
  <div class="screen-card screen-card--device-status">
    <div class="screen-card-header">
      <span class="screen-card-header-title">设备状态</span>
    </div>
    <div class="screen-card-body device-status-body">
      <svg
        class="device-status-lines"
        viewBox="0 0 400 280"
        preserveAspectRatio="none"
      >
        <path d="M 149 108 Q 130 95 115 85" fill="none" stroke="#3aa964" stroke-width="3" stroke-dasharray="6 6" />
        <path d="M 251 108 Q 270 95 285 85" fill="none" stroke="#3aa964" stroke-width="3" stroke-dasharray="6 6" />
        <path d="M 149 172 Q 130 185 115 195" fill="none" stroke="#3aa964" stroke-width="3" stroke-dasharray="6 6" />
        <path d="M 251 172 Q 270 185 285 195" fill="none" stroke="#3aa964" stroke-width="3" stroke-dasharray="6 6" />
      </svg>
      <div class="device-status-center">
        <div class="device-status-bg" />
        <div class="device-status-chart" />
      </div>
      <div class="device-status-item device-status-item--tl">
        <span class="device-status-num">{{ data.total }}</span>
        <div class="device-status-icon">
          <img src="/dataScreen/images/device3copy.png" alt="" class="device-status-icon-bg" />
          <img src="/dataScreen/images/device3.png" alt="" />
        </div>
        <span class="device-status-label">设备总数(台)</span>
      </div>
      <div class="device-status-item device-status-item--tr">
        <span class="device-status-num">{{ data.online }}</span>
        <div class="device-status-icon">
          <img src="/dataScreen/images/device3copy.png" alt="" class="device-status-icon-bg" />
          <img src="/dataScreen/images/device3.png" alt="" />
        </div>
        <span class="device-status-label">在线设备(台)</span>
      </div>
      <div class="device-status-item device-status-item--bl">
        <span class="device-status-num">{{ data.offline }}</span>
        <div class="device-status-icon">
          <img src="/dataScreen/images/device3copy.png" alt="" class="device-status-icon-bg" />
          <img src="/dataScreen/images/device3.png" alt="" />
        </div>
        <span class="device-status-label">离线设备(台)</span>
      </div>
      <div class="device-status-item device-status-item--br">
        <span class="device-status-num" :class="{ 'device-status-num--warn': (data.fault ?? 0) > 0 }">{{ data.fault ?? 0 }}</span>
        <div class="device-status-icon">
          <img src="/dataScreen/images/device3copy.png" alt="" class="device-status-icon-bg" />
          <img src="/dataScreen/images/device3.png" alt="" />
        </div>
        <span class="device-status-label">故障设备(台)</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  data: { total: number; online: number; offline: number; fault?: number }
}>()
</script>

<style scoped>
@import '../dataScreenCards.css';
</style>

<style scoped>
.screen-card--device-status .screen-card-body {
  padding: 8px;
  overflow: visible;
  position: relative;
  min-height: 100px;
  display: block;
}

.device-status-body {
  display: block;
}

.device-status-lines {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.device-status-center {
  z-index: 1;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-status-bg {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: url('/dataScreen/images/bgcircle.png') no-repeat center;
  background-size: contain;
}

.device-status-chart {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: url('/dataScreen/images/copy.png') no-repeat center;
  background-size: contain;
  transform: scale(0.75);
  transform-origin: center center;
  z-index: 1;
}

.device-status-item {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  z-index: 2;
}

.device-status-item--tl {
  left: 28px;
  top: 22px;
}
.device-status-item--tr {
  right: 28px;
  top: 22px;
}
.device-status-item--bl {
  left: 28px;
  bottom: 12px;
}
.device-status-item--br {
  right: 28px;
  bottom: 12px;
}

.device-status-num {
  font-size: 14px;
  color: #37a272;
  font-weight: bold;
  font-family: 'Microsoft Ya Hei', sans-serif;
  text-align: center;
  transform: translateY(-6px);
}

.device-status-num--warn {
  color: #ff742e;
}

.device-status-label {
  font-size: 14px;
  color: #4f5352;
  font-family: 'Microsoft Ya Hei', sans-serif;
  text-align: center;
}

.device-status-icon {
  position: relative;
  width: 48px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
}

.device-status-icon img {
  width: 140%;
  height: 140%;
  object-fit: contain;
}

.device-status-icon .device-status-icon-bg {
  position: absolute;
  left: -10px;
  top: -20%;
  width: 140%;
  height: 140%;
  z-index: 0;
}

.device-status-icon img:not(.device-status-icon-bg) {
  position: relative;
  z-index: 1;
  transform: translateY(-10px);
}
</style>
