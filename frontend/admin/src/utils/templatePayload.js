/**
 * 隧道参数模板方向快照（与后端 TunnelParamTemplatePayloadV1 结构一致）
 */

export const TEMPLATE_PAYLOAD_SCHEMA_VERSION = 1

const ARRAY_KEYS = [
  'devicelists',
  'devicelistTunnelinfos',
  'deviceParams',
  'powerEdgeComputing',
  'lampsTerminals',
  'lampsEdgeComputings',
  'lampsTerminalNodes',
  'outOfRadars',
  'tunnelDevices',
  'approachLampsTerminals',
  'longitudes'
]

export function normalizeTemplatePayload(raw) {
  const p = raw && typeof raw === 'object' && !Array.isArray(raw) ? { ...raw } : {}
  p.schemaVersion = p.schemaVersion || TEMPLATE_PAYLOAD_SCHEMA_VERSION
  ARRAY_KEYS.forEach((k) => {
    if (!Array.isArray(p[k])) p[k] = []
  })
  if (p.edgeTerminal === undefined) p.edgeTerminal = null
  return p
}

export function parseTemplatePayloadJson(str) {
  if (str == null || String(str).trim() === '') {
    return normalizeTemplatePayload({})
  }
  try {
    return normalizeTemplatePayload(JSON.parse(String(str)))
  } catch {
    return normalizeTemplatePayload({})
  }
}

export function serializeTemplatePayload(p) {
  return JSON.stringify(p)
}

/**
 * 删除 devicelist 及其在快照内的关联数据（与复制/应用时的块范围一致）
 */
export function removeDevicelistFromPayload(p, deviceId) {
  const id = Number(deviceId)
  if (!Number.isFinite(id)) return
  const dev = (p.devicelists || []).find((d) => d && Number(d.deviceId) === id)
  const typeId = dev != null ? Number(dev.deviceTypeId) : NaN

  if (typeId === 1) {
    const rels = (p.lampsEdgeComputings || []).filter((r) => r && Number(r.devicelistId) === id)
    const lampIds = new Set(
      rels.map((r) => r.uniqueId).filter((x) => x != null && x !== '')
    )
    p.lampsTerminals = (p.lampsTerminals || []).filter((l) => !lampIds.has(l?.uniqueId))
    p.lampsTerminalNodes = (p.lampsTerminalNodes || []).filter((n) => !lampIds.has(n?.uniqueId))
    p.lampsEdgeComputings = (p.lampsEdgeComputings || []).filter((r) => Number(r.devicelistId) !== id)
    p.outOfRadars = (p.outOfRadars || []).filter((r) => Number(r.devicelistId) !== id)
  }

  p.powerEdgeComputing = (p.powerEdgeComputing || []).filter((r) => Number(r.devicelistId) !== id)
  p.deviceParams = (p.deviceParams || []).filter((x) => Number(x.devicelistId) !== id)
  p.devicelistTunnelinfos = (p.devicelistTunnelinfos || []).filter((l) => Number(l.devicelistId) !== id)
  p.devicelists = (p.devicelists || []).filter((d) => Number(d.deviceId) !== id)
}

/**
 * 新增边缘/电能终端及 tunnel_devicelist_tunnelinfo 关联
 */
/**
 * 删除一条灯具终端及其 lampsEdgeComputings / lampsTerminalNodes 关联
 */
export function removeLampTerminalFromPayload(p, uniqueId) {
  const uid = uniqueId != null ? String(uniqueId).trim() : ''
  if (!uid) return
  p.lampsTerminals = (p.lampsTerminals || []).filter((l) => String(l?.uniqueId ?? '') !== uid)
  p.lampsEdgeComputings = (p.lampsEdgeComputings || []).filter((r) => String(r?.uniqueId ?? '') !== uid)
  p.lampsTerminalNodes = (p.lampsTerminalNodes || []).filter((n) => String(n?.uniqueId ?? '') !== uid)
}

function splicePayloadRow(arr, row) {
  if (!Array.isArray(arr) || row == null) return
  const i = arr.indexOf(row)
  if (i >= 0) arr.splice(i, 1)
}

/** 删除引道灯控制器快照行（按对象引用） */
export function removeApproachLampRow(p, row) {
  if (!Array.isArray(p.approachLampsTerminals)) return
  splicePayloadRow(p.approachLampsTerminals, row)
}

/** 删除电表（powerEdgeComputing）快照行 */
export function removePowerMeterRow(p, row) {
  if (!Array.isArray(p.powerEdgeComputing)) return
  splicePayloadRow(p.powerEdgeComputing, row)
}

/**
 * 新增灯具终端行（不自动写 lampsEdgeComputing，需与边缘关联时请另行维护或后续扩展）
 */
export function addLampTerminalRow(p, row) {
  const uid = row?.uniqueId != null ? String(row.uniqueId).trim() : ''
  if (!uid) throw new Error('请填写唯一标识')
  if (!Array.isArray(p.lampsTerminals)) p.lampsTerminals = []
  if (p.lampsTerminals.some((l) => String(l?.uniqueId ?? '') === uid)) {
    throw new Error('唯一标识已存在')
  }
  p.lampsTerminals.push({
    uniqueId: uid,
    deviceId: row.deviceId != null && row.deviceId !== '' ? Number(row.deviceId) : null,
    deviceName: row.deviceName != null ? String(row.deviceName) : '',
    deviceNum: row.deviceNum != null ? String(row.deviceNum) : '',
    position: row.position != null ? String(row.position) : '',
    loopNumber: row.loopNumber != null ? String(row.loopNumber) : '',
    zone: row.zone != null ? String(row.zone) : '',
    zone2: row.zone2 != null ? String(row.zone2) : '',
    bluetoothNum: row.bluetoothNum != null ? String(row.bluetoothNum) : '',
    ldDeviceId: row.ldDeviceId != null ? String(row.ldDeviceId) : ''
  })
}

export function addApproachLampRow(p, row = {}) {
  if (!Array.isArray(p.approachLampsTerminals)) p.approachLampsTerminals = []
  p.approachLampsTerminals.push({
    devNo: row.devNo != null ? String(row.devNo) : '',
    mileage: row.mileage != null ? String(row.mileage) : '',
    zoneName: row.zoneName != null ? String(row.zoneName) : '',
    bluetoothStrength: row.bluetoothStrength != null ? String(row.bluetoothStrength) : '',
    version: row.version != null ? String(row.version) : '',
    status: row.status != null ? String(row.status) : '',
    lastUpdate: row.lastUpdate != null ? String(row.lastUpdate) : ''
  })
}

export function addPowerMeterRow(p, row) {
  const id = Number(row?.devicelistId)
  if (!Number.isFinite(id)) throw new Error('请选择电能终端设备号')
  if (!Array.isArray(p.powerEdgeComputing)) p.powerEdgeComputing = []
  let en = row.isEnabled
  if (en === true) en = 1
  else if (en === false) en = 0
  else if (en != null && en !== '') en = Number(en)
  else en = 1
  p.powerEdgeComputing.push({
    address: row.address != null ? String(row.address) : '',
    devicelistId: id,
    loopName: row.loopName != null ? String(row.loopName) : '',
    direction: row.direction != null && row.direction !== '' ? Number(row.direction) : null,
    vendorId: row.vendorId != null ? String(row.vendorId) : '',
    isEnabled: en,
    lastTime: row.lastTime != null ? String(row.lastTime) : ''
  })
}

export function addDevicelistToPayload(p, { deviceId, deviceTypeId, nickName }) {
  const id = Number(deviceId)
  const tid = Number(deviceTypeId)
  if (!Number.isFinite(id) || !Number.isFinite(tid)) {
    throw new Error('设备号或类型无效')
  }
  if ((p.devicelists || []).some((d) => d && Number(d.deviceId) === id)) {
    throw new Error('设备号已存在')
  }
  const linkType = tid === 2 ? 2 : 1
  const tunnelRef =
    (p.devicelistTunnelinfos || []).find((t) => t && t.tunnelId != null)?.tunnelId ?? 0

  const now = new Date()
  const lastUpdate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`

  p.devicelists = [
    ...(p.devicelists || []),
    {
      deviceId: id,
      deviceTypeId: tid,
      devicePassword: 0,
      nickName: nickName || '',
      lastUpdate
    }
  ]
  p.devicelistTunnelinfos = [
    ...(p.devicelistTunnelinfos || []),
    {
      tunnelId: tunnelRef,
      devicelistId: id,
      type: linkType
    }
  ]
}

export function countPayloadSummary(p) {
  return {
    devicelists: (p.devicelists || []).length,
    powerRows: (p.powerEdgeComputing || []).length,
    lamps: (p.lampsTerminals || []).length,
    tunnelDevices: (p.tunnelDevices || []).length,
    approach: (p.approachLampsTerminals || []).length,
    longitudes: (p.longitudes || []).length
  }
}
