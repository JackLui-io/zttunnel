/**
 * 安装里程 deviceNumStr → 入库 deviceNum（与 zt_tunnel_web table5.hexToDec 一致）
 */
export function mileageStrToDeviceNum(val) {
  if (val == null || val === '') return 0
  const s = String(val).trim()
  if (!s) return 0
  if (s.includes('+')) {
    const parts = s.split('+').map((p) => Number(String(p).trim()))
    const big = parts[0]
    const small = parts[1]
    if (Number.isNaN(big) || Number.isNaN(small)) return 0
    const bigHex = big.toString(16).padStart(3, '0')
    const smallHex = small.toString(16).padStart(4, '0')
    return parseInt(bigHex + smallHex, 16)
  }
  const n = Number(s)
  return Number.isNaN(n) ? 0 : n
}
