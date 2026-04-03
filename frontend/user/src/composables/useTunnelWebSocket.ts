/**
 * 隧道车流 WebSocket composable
 * 参考原项目 zt_tunnel_web tunnelDh.vue：收到 flag 时添加小车，小车移动触发按段亮灯
 */
import { ref, computed, watch, onBeforeUnmount, type Ref } from 'vue'

/** WebSocket 消息格式（与原项目 tunnelDh 一致） */
interface TunnelTrafficMessage {
  tunnelId?: number
  flag?: number
}

const CAR_SPEED = 0.012 // 每 100ms 移动比例（0-1）
const RUN_INTERVAL_MS = 100

function getWebSocketUrl(): string {
  if (typeof window === 'undefined' || !window.WebSocket) return ''
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  let wsHost: string
  const apiBase = import.meta.env.VITE_API_BASE_URL as string | undefined
  const apiDomain = import.meta.env.VITE_API_DOMAIN as string | undefined
  const base = apiBase || apiDomain
  if (base && (base.startsWith('http://') || base.startsWith('https://'))) {
    try {
      wsHost = new URL(base).hostname
    } catch {
      wsHost = window.location.hostname
    }
  } else if (base && base.includes(':')) {
    wsHost = base.split(':')[0] ?? window.location.hostname
  } else if (base && base !== 'api' && base !== '/api') {
    wsHost = base
  } else {
    wsHost = window.location.hostname
  }
  return `${wsProtocol}//${wsHost}/websocket/3`
}

/**
 * 订阅隧道车流 WebSocket，实现小车动画与按段亮灯
 * @param tunnelId 当前选中的隧道 ID
 */
export function useTunnelWebSocket(tunnelId: Ref<number | null | undefined>) {
  const cars = ref<{ x: number; key: number }[]>([])
  let runTimer: ReturnType<typeof setInterval> | null = null

  /** 当前所有小车 x 位置（0-1），用于判断灯段是否亮 */
  const running = computed(() => cars.value.map((c) => c.x))

  /** 是否有车在隧道内（兼容：全亮时也可用） */
  const carVisible = computed(() => cars.value.length > 0)

  function carsAdd() {
    cars.value = [...cars.value, { x: -0.02, key: Date.now() }]
  }

  function carsPush(flag: number) {
    const count = Math.min(Math.max(1, Math.floor(flag / 100)), 5)
    for (let i = 0; i < count; i++) {
      carsAdd()
    }
  }

  function carsRun() {
    if (runTimer) return
    runTimer = setInterval(() => {
      cars.value = cars.value
        .map((c) => ({ ...c, x: c.x + CAR_SPEED }))
        .filter((c) => c.x < 1.05)
      if (cars.value.length === 0) {
        stopRun()
      }
    })
  }

  function stopRun() {
    if (runTimer) {
      clearInterval(runTimer)
      runTimer = null
    }
    cars.value = []
  }

  let ws: WebSocket | null = null

  function connect() {
    const url = getWebSocketUrl()
    if (!url) return
    try {
      ws = new WebSocket(url)
      ws.onmessage = (ev) => {
        try {
          const data = JSON.parse(ev.data) as TunnelTrafficMessage
          const tid = tunnelId.value
          if (tid != null && data.tunnelId === tid && data.flag != null && data.flag > 0) {
            carsPush(data.flag)
            carsRun()
          }
        } catch {
          // ignore
        }
      }
    } catch {
      // ignore
    }
  }

  function disconnect() {
    stopRun()
    if (ws) {
      ws.close()
      ws = null
    }
  }

  watch(
    tunnelId,
    (tid) => {
      if (tid == null) stopRun()
    },
    { immediate: true }
  )

  connect()
  onBeforeUnmount(disconnect)

  return { carVisible, cars, running }
}
