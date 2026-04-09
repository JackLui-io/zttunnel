import type { TunnelNode } from '@/api/tunnel'

/** 每条线路（level=2）及其下属隧道（level=3）条数 */
export interface LineTunnelRow {
  lineName: string
  tunnelCount: number
}

/** 大屏地图热点行（由隧道树推导） */
export interface ScreenMapHotspotRegion {
  tunnelId: number
  provinceName: string
  companyName: string
  /** 按 level=2 线路分组，每条线路下列出的 level=3 隧道数量 */
  lineTunnelRows: LineTunnelRow[]
}

/** level=1 节点 id、省份、单位名兜底（树中无该节点时用） */
export const MAP_HOTSPOT_PRESETS = [
  { tunnelId: 1, provinceName: '陕西', companyFallback: '陕西旬凤韩黄高速有限公司' },
  { tunnelId: 542, provinceName: '广西', companyFallback: '广西中铁南横高速公路有限公司' },
  { tunnelId: 552, provinceName: '湖南', companyFallback: '中铁炉慈桑龙高速有限公司' },
  { tunnelId: 600, provinceName: '吉林', companyFallback: '桓集高速运营管理处' },
] as const

function findNodeById(nodes: TunnelNode[], id: number): TunnelNode | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const f = findNodeById(n.children, id)
      if (f) return f
    }
  }
  return null
}

/** 统计节点子树中 level=3 节点个数 */
function countLevel3InSubtree(n: TunnelNode): number {
  let c = 0
  const walk = (x: TunnelNode) => {
    if (x.level === 3) c++
    x.children?.forEach(walk)
  }
  n.children?.forEach(walk)
  return c
}

/**
 * 在 level=1 根节点下，按直接（或子树中）的 level=2 线路列出：线路名 + 该线路下隧道数
 * 与后台约定：L2=高速公路/线路，L3=隧道
 */
function lineTunnelRowsFromRoot(root: TunnelNode): LineTunnelRow[] {
  const rows: LineTunnelRow[] = []
  const visit = (node: TunnelNode) => {
    if (node.level === 2) {
      rows.push({
        lineName: node.name?.trim() || '未命名线路',
        tunnelCount: countLevel3InSubtree(node),
      })
    } else {
      node.children?.forEach(visit)
    }
  }
  root.children?.forEach(visit)
  return rows
}

/** 基于 /tunnel/tree/list 返回的树组装热点数据 */
export function buildScreenMapRegionsFromTree(tree: TunnelNode[] | null | undefined): ScreenMapHotspotRegion[] {
  return MAP_HOTSPOT_PRESETS.map((p) => {
    const node = tree?.length ? findNodeById(tree, p.tunnelId) : null
    const lineTunnelRows = node ? lineTunnelRowsFromRoot(node) : []
    const name = node?.name?.trim()
    const companyName = name ? name : p.companyFallback
    return {
      tunnelId: p.tunnelId,
      provinceName: p.provinceName,
      companyName,
      lineTunnelRows,
    }
  })
}
