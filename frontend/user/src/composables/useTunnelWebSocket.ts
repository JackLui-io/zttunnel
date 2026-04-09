/**
 * 隧道随车动画（小车 + 灯段）——数据来源约定：
 *
 * - **仅此 WebSocket**（/websocket/3，payload 含 tunnelId、flag）会生成/追加小车并驱动「车在段内则亮灯」。
 * - **不按** 小时统计接口（如 clByHouse）自动发车：统计是聚合数据，无法代表「当前这一刻是否有车经过」，
 *   点开隧道就动画会与现场脱节，故不做 seed。
 * - **车流量统计卡片** 仍用 analyze/clByHouse，与入口卡片动画解耦。
 *
 * 参考原项目 zt_tunnel_web tunnelDh.vue：收到 flag 时加车、定时推进位置。
 */
import { ref, computed, watch, onBeforeUnmount, type Ref } from 'vue'

/** WebSocket 消息格式（与原项目 tunnelDh 一致） */
function coerceTrafficPayload(raw: unknown): { tunnelId: number; flag: number } | null {
  if (raw == null || typeof raw !== 'object') return null
  const o = raw as Record<string, unknown>
  const nest = o.data != null && typeof o.data === 'object' ? (o.data as Record<string, unknown>) : null
  const tidRaw = o.tunnelId ?? o.tunnel_id ?? nest?.tunnelId ?? nest?.tunnel_id
  const flagRaw = o.flag ?? nest?.flag
  const num = (v: unknown) => {
    if (typeof v === 'number' && Number.isFinite(v)) return v
    if (typeof v === 'string' && v.trim() !== '') return Number(v)
    return NaN
  }
  const tunnelId = num(tidRaw)
  const flag = num(flagRaw)
  if (!Number.isFinite(tunnelId) || !Number.isFinite(flag) || flag <= 0) return null
  return { tunnelId, flag }
}

function isTunnelWsDebug(): boolean {
  return (import.meta.env.VITE_TUNNEL_WS_DEBUG as string | undefined)?.trim() === '1'
}

/** HTTPS 页面禁止 Mixed Content：若误配 VITE_WS_URL=ws://，自动改为 wss:// */
function coerceWssWhenPageIsHttps(wsUrl: string): string {
  if (typeof window === 'undefined' || window.location.protocol !== 'https:') return wsUrl
  if (wsUrl.startsWith('ws://')) return `wss://${wsUrl.slice('ws://'.length)}`
  return wsUrl
}

const CAR_SPEED = 0.012 // 每 100ms 移动比例（0-1）
const RUN_INTERVAL_MS = 100
/** WS flag→本帧追加车数（与后端 trafficFlow*umax 量级配套，可调） */
const WS_FLAG_DIVISOR = 80
const WS_MAX_CARS_PER_MESSAGE = 12

function getWebSocketUrl(): string {
  if (typeof window === 'undefined' || !window.WebSocket) return ''
  const pageIsSecure = window.location.protocol === 'https:'
  const wsProtocol = pageIsSecure ? 'wss:' : 'ws:'

  const explicitWs = (import.meta.env.VITE_WS_URL as string | undefined)?.trim()
  if (explicitWs) return coerceWssWhenPageIsHttps(explicitWs)

  const apiBase = import.meta.env.VITE_API_BASE_URL as string | undefined
  const apiDomain = import.meta.env.VITE_API_DOMAIN as string | undefined
  const base = (apiBase || apiDomain)?.trim()
  if (base && (base.startsWith('http://') || base.startsWith('https://'))) {
    try {
      const u = new URL(base)
      const wsProto = u.protocol === 'https:' ? 'wss:' : 'ws:'
      return `${wsProto}//${u.host}/websocket/3`
    } catch {
      // fallthrough
    }
  }

  // 生产常见：VITE_API_BASE_URL=/api（相对路径）。HTTP 走同源的 /api，WebSocket 仍连到 Java 的 /websocket/3，
  // 需 Nginx（或网关）把 /websocket/ 反代到后端并做 Upgrade；若 WS 挂在 /api 下请配置 VITE_WS_URL。
  if (base && base.startsWith('/')) {
    return `${wsProtocol}//${window.location.host}/websocket/3`
  }

  /**
   * 开发环境且 API 走相对路径（VITE_API_BASE_URL 为空）时，不能用 ws://页面host/websocket/3：
   * 会连到 Vite 端口（如 5173），DevTools Messages 里只有 type:connected、vite-plugin-inspect 等，
   * 永远收不到 Java 的 tunnelId/flag。必须直连后端端口（与 vite server.proxy target 一致）。
   */
  if (import.meta.env.DEV) {
    const devPort = (import.meta.env.VITE_DEV_BACKEND_PORT as string | undefined)?.trim() || '8026'
    return `${wsProtocol}//${window.location.hostname}:${devPort}/websocket/3`
  }

  return `${wsProtocol}//${window.location.host}/websocket/3`
}

/**
 * 订阅隧道车流 WebSocket，实现小车动画与按段亮灯
 * @param tunnelId 当前选中的隧道 ID（通常 level-4 线路 id）
 * @param parentTunnelId 可选 level-3 隧道 id，便于与绑定表里另一种 id 对齐
 */
export function useTunnelWebSocket(
  tunnelId: Ref<number | null | undefined>,
  parentTunnelId?: Ref<number | null | undefined>
) {
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
    const count = Math.min(
      Math.max(1, Math.floor(flag / WS_FLAG_DIVISOR)),
      WS_MAX_CARS_PER_MESSAGE
    )
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
    }, RUN_INTERVAL_MS)
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
      ws.onopen = () => {
        if (isTunnelWsDebug()) console.log('[tunnel-ws] open', url)
      }
      ws.onerror = () => {
        if (isTunnelWsDebug()) console.warn('[tunnel-ws] error', url)
      }
      ws.onclose = (ev) => {
        if (isTunnelWsDebug()) console.log('[tunnel-ws] close', ev.code, ev.reason || '')
      }
      ws.onmessage = (ev) => {
        if (isTunnelWsDebug()) console.log('[tunnel-ws] message', ev.data)
        try {
          const parsed = JSON.parse(ev.data)
          const payload = coerceTrafficPayload(parsed)
          const tid = tunnelId.value
          if (tid == null || payload == null) return
          const pid = parentTunnelId?.value
          const matchTid = Number(payload.tunnelId) === Number(tid)
          const matchParent = pid != null && Number(payload.tunnelId) === Number(pid)
          if (!matchTid && !matchParent) {
            if (import.meta.env.DEV || isTunnelWsDebug()) {
              console.debug(
                '[tunnel-ws] 已收车流帧但 tunnelId 不匹配',
                { payloadTunnelId: payload.tunnelId, selectedTunnelId: tid, parentTunnelId: pid }
              )
            }
            return
          }
          carsPush(payload.flag)
          carsRun()
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
    () => {
      // 切换线路时必须清空，否则上一隧道的车会卡在新卡片上（动画停、灯错）
      stopRun()
    },
    { immediate: true }
  )

  connect()
  onBeforeUnmount(disconnect)

  return { carVisible, cars, running }
}
