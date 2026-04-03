/** 隧道查看/编辑子页：隧道 ID、名称仅存 sessionStorage，不出现在地址栏。 */
const SESSION_KEY = 'ztt_admin_tunnel_detail_workspace'

/**
 * @param {number|string} tunnelId
 * @param {string} [tunnelName]
 * @param {{ level?: number|null, pathFromRoot?: string }} [meta] 自根节点起的名称路径与当前行层级（如 1～4）
 */
export function setTunnelDetailWorkspace(tunnelId, tunnelName, meta = {}) {
  if (tunnelId == null || tunnelId === '') {
    sessionStorage.removeItem(SESSION_KEY)
    return
  }
  const level = meta.level != null && meta.level !== '' ? Number(meta.level) : null
  const pathFromRoot = meta.pathFromRoot != null ? String(meta.pathFromRoot).trim() : ''
  sessionStorage.setItem(
    SESSION_KEY,
    JSON.stringify({
      tunnelId: Number(tunnelId),
      tunnelName: tunnelName != null ? String(tunnelName) : '',
      level: level != null && !Number.isNaN(level) ? level : null,
      pathFromRoot
    })
  )
}

export function clearTunnelDetailWorkspace() {
  sessionStorage.removeItem(SESSION_KEY)
}

/** @returns {{ tunnelId: number, tunnelName: string, level: number|null, pathFromRoot: string } | null} */
export function peekTunnelDetailWorkspace() {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY)
    if (!raw) return null
    const o = JSON.parse(raw)
    const tunnelId = Number(o.tunnelId)
    if (Number.isNaN(tunnelId)) return null
    const lv = o.level != null && o.level !== '' ? Number(o.level) : null
    return {
      tunnelId,
      tunnelName: o.tunnelName != null ? String(o.tunnelName) : '',
      level: lv != null && !Number.isNaN(lv) ? lv : null,
      pathFromRoot: o.pathFromRoot != null ? String(o.pathFromRoot).trim() : ''
    }
  } catch {
    return null
  }
}
