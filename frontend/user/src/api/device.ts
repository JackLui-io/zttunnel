import request from './request'

/** 区段信息（getZone 返回项） */
export interface ZoneItem {
  nodeCode?: string | number
  [key: string]: unknown
}

/** 灯具终端（getDeviceLamp 返回项） */
export interface TunnelLampsTerminal {
  deviceNum?: number
  zone?: number | string
  loopNumber?: string
  [key: string]: unknown
}

/** 提取 data 字段（兼容多种后端响应格式） */
function extractData<T>(res: { data?: unknown }, fallback: T): T {
  const body = res.data as Record<string, unknown> | undefined
  if (!body) return fallback
  if (Array.isArray(body)) return body as unknown as T
  for (const key of ['data', 'rows', 'result']) {
    const val = (body as Record<string, unknown>)[key]
    if (Array.isArray(val)) return val as T
  }
  return fallback
}

/** 获取区段列表 */
export function getZone(): Promise<ZoneItem[]> {
  return request.get('/device/getZone').then((res) => {
    const list = extractData<ZoneItem[]>(res, [])
    return Array.isArray(list) ? list : []
  })
}

/** 获取隧道灯具列表 */
export function getDeviceLamp(tunnelId: number): Promise<TunnelLampsTerminal[]> {
  return request.post('/tunnel/get/device/lamp', { tunnelId }).then((res) => {
    const list = extractData<TunnelLampsTerminal[]>(res, [])
    return Array.isArray(list) ? list : []
  })
}
