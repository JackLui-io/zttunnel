/**
 * 时间格式化工具
 */

/**
 * 格式化日期时间，将ISO格式的T替换为空格
 * @param {string|Date} datetime - 日期时间
 * @param {boolean} showSeconds - 是否显示秒，默认true
 * @returns {string} 格式化后的时间字符串
 */
export function formatDateTime(datetime, showSeconds = true) {
  if (!datetime) return '-'

  let dateStr = ''

  if (datetime instanceof Date) {
    const year = datetime.getFullYear()
    const month = String(datetime.getMonth() + 1).padStart(2, '0')
    const day = String(datetime.getDate()).padStart(2, '0')
    const hour = String(datetime.getHours()).padStart(2, '0')
    const minute = String(datetime.getMinutes()).padStart(2, '0')
    const second = String(datetime.getSeconds()).padStart(2, '0')

    if (showSeconds) {
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    } else {
      return `${year}-${month}-${day} ${hour}:${minute}`
    }
  } else if (typeof datetime === 'string') {
    dateStr = datetime
  } else {
    dateStr = String(datetime)
  }

  // 替换T为空格
  dateStr = dateStr.replace('T', ' ')

  // 截取到秒或分钟
  if (showSeconds) {
    return dateStr.substring(0, 19)
  } else {
    return dateStr.substring(0, 16)
  }
}

/**
 * 格式化日期，只显示年月日
 * @param {string|Date} date - 日期
 * @returns {string} YYYY-MM-DD格式
 */
export function formatDate(date) {
  if (!date) return '-'

  if (date instanceof Date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const dateStr = String(date)
  return dateStr.substring(0, 10)
}

/**
 * 获取当前日期字符串 YYYY-MM-DD
 * @returns {string}
 */
export function getCurrentDate() {
  const today = new Date()
  return formatDate(today)
}

/**
 * 获取当前日期时间字符串 YYYY-MM-DD HH:mm:ss
 * @returns {string}
 */
export function getCurrentDateTime() {
  return formatDateTime(new Date())
}

/**
 * 将datetime-local格式转换为标准格式
 * @param {string} datetimeLocal - datetime-local格式 (YYYY-MM-DDTHH:mm)
 * @returns {string} YYYY-MM-DD HH:mm:ss
 */
export function fromDatetimeLocal(datetimeLocal) {
  if (!datetimeLocal) return ''
  return datetimeLocal.replace('T', ' ') + ':00'
}

/**
 * 将标准格式转换为datetime-local格式
 * @param {string} datetime - 标准格式 (YYYY-MM-DD HH:mm:ss)
 * @returns {string} YYYY-MM-DDTHH:mm
 */
export function toDatetimeLocal(datetime) {
  if (!datetime) return ''
  const dateStr = String(datetime).replace(' ', 'T')
  return dateStr.substring(0, 16)
}

/**
 * 将Date对象转换为datetime-local格式
 * @param {Date} date - Date对象
 * @returns {string} YYYY-MM-DDTHH:mm
 */
export function dateToDatetimeLocal(date) {
  if (!date || !(date instanceof Date)) return ''

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')

  return `${year}-${month}-${day}T${hour}:${minute}`
}
