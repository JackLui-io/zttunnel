import type { TunnelNode } from '@/api/tunnel'

export type { TunnelNode }

/** 从隧道树聚合隧道概况：高速公路数、隧道总数、总里程 */
export function computeTunnelSummary(nodes: TunnelNode[]): {
  highwayCount: number
  tunnelCount: number
  totalMileage: number
} {
  let highwayCount = 0
  let tunnelCount = 0
  let totalMileage = 0
  const level3WithMileage = new Set<number>() // level-3 已有里程的隧道 id
  const level4AddedForParent = new Set<number>() // 已从 level-4 计过里程的 level-3 id（避免左右线重复）

  function walk(list: TunnelNode[]) {
    for (const n of list) {
      if (n.level === 2) highwayCount++
      else if (n.level === 3) {
        tunnelCount++
        if (n.tunnelMileage != null && n.tunnelMileage > 0 && n.id != null) {
          totalMileage += n.tunnelMileage
          level3WithMileage.add(n.id)
        }
      } else if (n.level === 4 && n.tunnelMileage != null && n.tunnelMileage > 0) {
        const parentId = n.parentId ?? 0
        if (!level3WithMileage.has(parentId) && !level4AddedForParent.has(parentId)) {
          level4AddedForParent.add(parentId)
          totalMileage += n.tunnelMileage
        }
      }
      if (n.children?.length) walk(n.children)
    }
  }
  walk(nodes)
  return { highwayCount, tunnelCount, totalMileage: Math.round(totalMileage * 10) / 10 }
}

/** 静态隧道树数据 */
export const tunnelData: TunnelNode[] = [
  {
    name: '陕西省凤翔高速有限公司',
    children: [
      {
        name: '旬凤高速',
        children: [
          {
            name: '老君顶隧道',
            children: [
              { name: '左线（原叶一号隧）' },
              { name: '左线（旬邑一凤翔）' },
            ],
          },
          { name: '刘家山1+2隧道' },
          { name: '石沟隧道' },
          { name: '南沟隧道' },
          { name: '十八岔隧道' },
        ],
      },
    ],
  },
  { name: '成科院项目组' },
  { name: '广西中铁南路高速公路有限公司' },
  {
    name: '中铁沪慈蒙龙高速有限公司',
    children: [
      {
        name: '炉慈高速',
        children: [
          {
            name: '维新隧1号隧道',
            children: [
              { name: '左线（慈利一炉红山）' },
              { name: '左线（炉红山一慈利）' },
            ],
          },
          { name: '紫龙高速' },
        ],
      },
    ],
  },
  { name: '恒集高速运营管理处' },
]

/** 获取隧道树中第一个 level-4（线路）节点，用于默认选中 */
export function getFirstLevel4Node(data: TunnelNode[]): TunnelNode | undefined {
  function find(nodes: TunnelNode[]): TunnelNode | undefined {
    for (const node of nodes) {
      if (node.children?.length) {
        const found = find(node.children)
        if (found) return found
      } else if (node.id != null) {
        return node
      }
    }
    return undefined
  }
  return find(data)
}

/** 根据选中的线路 id（level-4）查找完整路径：level2—level3—level4 */
export function getTunnelPathById(data: TunnelNode[], tunnelId: number): string[] | null {
  function search(nodes: TunnelNode[], path: string[]): string[] | null {
    for (const node of nodes) {
      const nextPath = [...path, node.name]
      if (node.children?.length) {
        const found = search(node.children, nextPath)
        if (found) return found
      } else if (node.id === tunnelId && path.length >= 2) {
        return [...path.slice(-2), node.name]
      }
    }
    return null
  }
  return search(data, [])
}

/** 根据选中的线路名（level-4）查找完整路径，用于兼容（多个同名时可能不准确） */
export function getTunnelPath(data: TunnelNode[], lineName: string): string[] | null {
  function search(nodes: TunnelNode[], path: string[]): string[] | null {
    for (const node of nodes) {
      const nextPath = [...path, node.name]
      if (node.children?.length) {
        const found = search(node.children, nextPath)
        if (found) return found
      } else if (node.name === lineName && path.length >= 2) {
        return [...path.slice(-2), node.name]
      }
    }
    return null
  }
  return search(data, [])
}
