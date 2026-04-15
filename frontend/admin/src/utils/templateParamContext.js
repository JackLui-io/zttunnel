/** 模板详情/编辑：模板 id 仅存 sessionStorage，不出现在地址栏。 */
const SESSION_KEY = 'ztt_admin_tunnel_param_template_id'

export function setTemplateParamContextId(id) {
  if (id == null || id === '') {
    sessionStorage.removeItem(SESSION_KEY)
    return
  }
  const n = Number(id)
  if (Number.isNaN(n)) {
    sessionStorage.removeItem(SESSION_KEY)
    return
  }
  sessionStorage.setItem(SESSION_KEY, String(n))
}

export function clearTemplateParamContextId() {
  sessionStorage.removeItem(SESSION_KEY)
}

/** @returns {number|null} */
export function peekTemplateParamContextId() {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY)
    if (!raw) return null
    const n = Number(raw)
    return Number.isNaN(n) ? null : n
  } catch {
    return null
  }
}
