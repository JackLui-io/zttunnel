/** 列表/VO 展示用字段，不属于或不应随 update 提交的列 */
const READONLY_LIST_KEYS = [
  'lastUpdate',
  'radarStatus',
  'brightnessMeterStatus',
  'electricityMeterNum',
  'id'
]

/**
 * 组装 POST /tunnel/update/devicelist 的请求体：去掉 lastUpdate 等只读字段，避免 Jackson 反序列化 Date 失败。
 * @param {Record<string, any>} row 列表行（含接口返回的扩展字段）
 * @param {Record<string, any>} [patch] 覆盖字段
 */
export function sanitizeDevicelistForUpdate(row, patch = {}) {
  const out = { ...row, ...patch }
  for (const k of READONLY_LIST_KEYS) delete out[k]
  delete out.tunnelId
  delete out.deviceType
  return out
}
