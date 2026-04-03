<template>
  <header class="screen-header" @click="goDashboard">
    <div class="screen-header-bg" />
    <div class="screen-header-left">
      <span class="screen-header-date">{{ dateStr }}</span>
      <span class="screen-header-week">{{ weekStr }}</span>
      <span class="screen-header-time">{{ timeStr }}</span>
    </div>
    <div class="screen-header-right">
      <button type="button" class="screen-header-btn" aria-label="智控平台" @click.stop="goDashboard">
        智控平台
      </button>
      <span class="screen-header-region">{{ region }}</span>
      <span class="screen-header-weather">{{ weather }}</span>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

function goDashboard() {
  router.push('/dashboard')
}

const dateStr = ref('')
const weekStr = ref('')
const timeStr = ref('')
const region = ref('--')
const weather = ref('--')

const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

/** Open-Meteo 天气码转中文（简化） */
const weatherCodeMap: Record<number, string> = {
  0: '晴',
  1: '晴',
  2: '局部多云',
  3: '阴',
  45: '雾',
  48: '雾',
  51: '毛毛雨',
  53: '毛毛雨',
  55: '毛毛雨',
  61: '小雨',
  63: '中雨',
  65: '大雨',
  71: '小雪',
  73: '中雪',
  75: '大雪',
  80: '阵雨',
  81: '阵雨',
  82: '强阵雨',
  85: '阵雪',
  86: '强阵雪',
  95: '雷暴',
  96: '雷暴',
  99: '雷暴',
}

async function fetchLocationAndWeather() {
  try {
    const ipRes = await axios.get<{
      status?: string
      city?: string
      regionName?: string
      country?: string
      lat?: number
      lon?: number
    }>('http://ip-api.com/json/?lang=zh-CN&fields=status,city,regionName,country,lat,lon', {
      timeout: 5000,
    })
    const { city, regionName, country } = ipRes.data
    region.value = city ?? regionName ?? country ?? '--'

    const lat = ipRes.data.lat
    const lon = ipRes.data.lon
    if (lat != null && lon != null) {
      const weatherRes = await axios.get<{
        current_weather?: { temperature?: number; weathercode?: number }
      }>(
        `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true`,
        { timeout: 5000 }
      )
      const cw = weatherRes.data?.current_weather
      if (cw) {
        const temp = cw.temperature != null ? Math.round(cw.temperature) : null
        const desc = cw.weathercode != null ? weatherCodeMap[cw.weathercode] ?? '--' : '--'
        weather.value = temp != null ? `${desc} ${temp}°C` : desc
      }
    }
  } catch {
    region.value = '--'
    weather.value = '--'
  }
}

function pad(n: number) {
  return String(n).padStart(2, '0')
}

function tick() {
  const d = new Date()
  dateStr.value = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  weekStr.value = weekDays[d.getDay()] ?? '星期日'
  timeStr.value = `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

let timer: ReturnType<typeof setInterval>
onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
  fetchLocationAndWeather()
})
onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.screen-header {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100px;
  z-index: 100;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-sizing: border-box;
}

.screen-header-bg {
  position: absolute;
  inset: 0;
  background: url('/dataScreen/images/head.png') no-repeat center top;
  background-size: 100% auto;
  z-index: 0;
  pointer-events: none;
}

.screen-header-left,
.screen-header-right {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Microsoft Ya Hei', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: rgb(15, 127, 98);
}

.screen-header-right {
  gap: 16px;
}

/* 智控平台按钮：使用 btn_selected.png，与 dashboard 数据大屏按钮一致 */
.screen-header-btn {
  flex-shrink: 0;
  width: 168px;
  height: 42px;
  margin-top: 8px;
  padding: 0;
  border: none;
  background: url('/page1/btn_selected.png') center / contain no-repeat;
  cursor: pointer;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: rgb(255, 255, 255);
  transition: opacity 0.2s;
}
.screen-header-btn:hover {
  opacity: 0.9;
}

@media (max-width: 1024px) {
  .screen-header {
    height: 80px;
    padding: 0 16px;
  }

  .screen-header-left,
  .screen-header-right {
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .screen-header {
    height: 70px;
    padding: 0 12px;
  }

  .screen-header-left,
  .screen-header-right {
    font-size: 13px;
    gap: 6px;
  }

  .screen-header-week {
    display: none;
  }

  .screen-header-btn {
    width: 120px;
    height: 36px;
    font-size: 14px;
  }
}
</style>
