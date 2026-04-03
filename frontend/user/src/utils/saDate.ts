/**
 * 统计分析日期计算 - 与原前端 zt_tunnel_web 完全一致
 * 参考：index.vue timeInterval、getOldMonth、getMonthDay、timeClick
 */

function formatDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 获取某年某月的天数（month 为 1-12，与原前端 getMonthDay 一致） */
function getMonthDay(year: number, month: number): number {
  const d = new Date(year, month, 0)
  return d.getDate()
}

/** 获取往前 n 个月的总天数（与原前端 getOldMonth 一致） */
export function getOldMonthDays(n: number): number {
  const date = new Date()
  const Y = date.getFullYear()
  const M = date.getMonth() + 1 // 1-12
  let d = 0
  for (let i = 1; i <= n; i++) {
    if (M - i > 0) {
      d += getMonthDay(Y, M - i)
    } else {
      d += getMonthDay(Y - 1, M - i + 12)
    }
  }
  return d
}

/** 近七天：6 天前至今（与原前端 timeClick('1') -> timeInterval(6) 一致） */
export function getRangeNear7Days(): { startTime: string; endTime: string } {
  const end = new Date()
  const start = new Date(end.getTime() - 6 * 24 * 60 * 60 * 1000)
  return { startTime: formatDate(start), endTime: formatDate(end) }
}

/** 近一月：往前 1 个月总天数的日期范围 */
export function getRangeNear1Month(): { startTime: string; endTime: string } {
  const days = getOldMonthDays(1)
  const end = new Date()
  const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000)
  return { startTime: formatDate(start), endTime: formatDate(end) }
}

/** 近三月：往前 3 个月总天数的日期范围 */
export function getRangeNear3Months(): { startTime: string; endTime: string } {
  const days = getOldMonthDays(3)
  const end = new Date()
  const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000)
  return { startTime: formatDate(start), endTime: formatDate(end) }
}
