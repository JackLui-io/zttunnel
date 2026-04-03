/**
 * 从隧道树（level 1～4）解析自根到目标节点的名称路径。
 * @param {Array<{ id: number, tunnelName?: string, children?: array }>} nodes
 * @param {number|string} targetId
 * @returns {string[]} 各级名称，不含空串
 */
export function getTunnelPathLabels(nodes, targetId) {
  const tid = Number(targetId)
  if (Number.isNaN(tid) || !nodes?.length) return []

  const walk = (list, prefix) => {
    for (const node of list) {
      const name = (node.tunnelName != null && String(node.tunnelName).trim()) || ''
      const chain = name ? [...prefix, name] : [...prefix]
      if (node.id === tid) return chain
      if (node.children?.length) {
        const hit = walk(node.children, chain)
        if (hit.length) return hit
      }
    }
    return []
  }

  return walk(nodes, [])
}

/**
 * level-1～level-4 全称（用于「当前隧道」展示）
 * @param {Array} nodes
 * @param {number|string} targetId
 * @param {string} [sep] 默认 「 / 」
 */
export function formatTunnelHierarchyPath(nodes, targetId, sep = ' / ') {
  return getTunnelPathLabels(nodes, targetId).join(sep)
}

/**
 * 写入 session 用的 meta（pathFromRoot 为 level-1～当前节点的全称）
 * @param {Array} nodes 全量隧道树 rawData
 * @param {{ id: number, level?: number, tunnelName?: string }} row 当前行
 */
export function buildTunnelWorkspaceMeta(nodes, row) {
  if (!row?.id) return { pathFromRoot: '', level: null }
  const pathFromRoot = formatTunnelHierarchyPath(nodes, row.id)
  const level = row.level != null && row.level !== '' ? Number(row.level) : null
  return {
    pathFromRoot,
    level: level != null && !Number.isNaN(level) ? level : null
  }
}
