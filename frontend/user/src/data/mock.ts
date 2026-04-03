/** 待处理项 */
export interface PendingItem {
  label: string
  num: number
  isAlert: boolean
  icon: string
}

/** 消息通知分类 */
export interface MessageCategory {
  name: string
  count: number
  collapsed: boolean
  items: { time: string; text: string }[]
}

/** 待处理静态数据 */
export const pendingData: PendingItem[] = [
  { label: '系统通知', num: 2, isAlert: false, icon: 'sound' },
  { label: '操作通知', num: 0, isAlert: false, icon: 'sound' },
  { label: '实时警报', num: 39, isAlert: true, icon: 'alert' },
  { label: '设备告警', num: 20, isAlert: true, icon: 'alert' },
]

/** 消息通知静态数据 */
export const messageData: MessageCategory[] = [
  {
    name: '系统通知',
    count: 2,
    collapsed: false,
    items: [
      { time: '2025-12-17 16:07:18', text: '计划于北京时间12月13日9:00进行系统维护更新。' },
      { time: '2025-12-17 16:07:18', text: '请各位用户注意账号安全,不要将账号信息泄露给他人。' },
    ],
  },
  {
    name: '其他通知',
    count: 3,
    collapsed: false,
    items: [
      { time: '2025-12-17 16:07:18', text: '隧道设备运行正常。' },
      { time: '2025-12-17 16:07:18', text: '能耗数据已更新。' },
      { time: '2025-12-17 16:07:18', text: '请及时查看设备告警信息。' },
    ],
  },
]
