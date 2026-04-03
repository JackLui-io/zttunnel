/** 设备绑定上下文：隧道 ID、名称仅存 sessionStorage，避免出现在地址栏（可配合刷新同页恢复）。 */
const SESSION_KEY = 'ztt_admin_device_bind_tunnel_id'
const SESSION_NAME_KEY = 'ztt_admin_device_bind_tunnel_name'
const SESSION_META_KEY = 'ztt_admin_device_bind_tunnel_meta'

export function setDeviceBindContextTunnelId(id, tunnelName, meta = {}) {
  if (id == null || id === '') {
    sessionStorage.removeItem(SESSION_KEY)
    sessionStorage.removeItem(SESSION_NAME_KEY)
    sessionStorage.removeItem(SESSION_META_KEY)
    return
  }
  sessionStorage.setItem(SESSION_KEY, String(id))
  if (tunnelName != null && tunnelName !== '') {
    sessionStorage.setItem(SESSION_NAME_KEY, String(tunnelName))
  } else {
    sessionStorage.removeItem(SESSION_NAME_KEY)
  }
  const level = meta.level != null && meta.level !== '' ? Number(meta.level) : null
  const pathFromRoot = meta.pathFromRoot != null ? String(meta.pathFromRoot).trim() : ''
  if (pathFromRoot || (level != null && !Number.isNaN(level))) {
    sessionStorage.setItem(
      SESSION_META_KEY,
      JSON.stringify({
        pathFromRoot,
        level: level != null && !Number.isNaN(level) ? level : null
      })
    )
  } else {
    sessionStorage.removeItem(SESSION_META_KEY)
  }
}

export function clearDeviceBindContextTunnelId() {
  sessionStorage.removeItem(SESSION_KEY)
  sessionStorage.removeItem(SESSION_NAME_KEY)
  sessionStorage.removeItem(SESSION_META_KEY)
}

/** @returns {{ pathFromRoot: string, level: number|null }} */
export function peekDeviceBindTunnelMeta() {
  try {
    const raw = sessionStorage.getItem(SESSION_META_KEY)
    if (!raw) return { pathFromRoot: '', level: null }
    const o = JSON.parse(raw)
    const lv = o.level != null && o.level !== '' ? Number(o.level) : null
    return {
      pathFromRoot: o.pathFromRoot != null ? String(o.pathFromRoot).trim() : '',
      level: lv != null && !Number.isNaN(lv) ? lv : null
    }
  } catch {
    return { pathFromRoot: '', level: null }
  }
}

export function peekDeviceBindTunnelName() {
  return sessionStorage.getItem(SESSION_NAME_KEY) || ''
}

export function peekDeviceBindContextTunnelId() {
  const v = sessionStorage.getItem(SESSION_KEY)
  if (v == null || v === '') return null
  const n = Number(v)
  return Number.isNaN(n) ? null : n
}
