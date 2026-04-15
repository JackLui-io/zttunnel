import { getTunnelEdgeTerminalColumnMeta } from '@/api/tunnel'

let cachedList = null
let loadingPromise = null

/**
 * 拉取并缓存边缘终端表字段元数据（含 COMMENT）；失败返回空数组。
 */
export async function fetchEdgeTerminalColumnMeta() {
  if (cachedList) return cachedList
  if (loadingPromise) return loadingPromise
  loadingPromise = (async () => {
    try {
      const res = await getTunnelEdgeTerminalColumnMeta()
      if (res.code === 200 && Array.isArray(res.data)) {
        cachedList = res.data
        return cachedList
      }
    } catch (e) {
      console.error(e)
    }
    cachedList = []
    return cachedList
  })()
  try {
    return await loadingPromise
  } finally {
    loadingPromise = null
  }
}

export function buildCommentByPropertyMap(metaList) {
  const m = {}
  if (!Array.isArray(metaList)) return m
  for (const row of metaList) {
    const p = row?.propertyName
    if (p && row.comment) m[p] = row.comment.trim()
  }
  return m
}

/** 单测或重新登录后如需刷新 */
export function clearEdgeTerminalColumnMetaCache() {
  cachedList = null
}
