/**
 * 从隧道树中筛出「叶子隧道」id（与业务一致：level === 4）
 * @param {number[]} checkedKeys el-tree getCheckedKeys() 返回值
 * @param {Array<{ id: number, level?: number, children?: array }>} tunnelTree
 * @returns {number[]}
 */
export function leafTunnelIdsFromTreeChecks(checkedKeys, tunnelTree) {
  if (!checkedKeys?.length || !tunnelTree?.length) return []
  const keySet = new Set(checkedKeys.map((k) => Number(k)))
  const out = []
  const walk = (nodes) => {
    for (const n of nodes || []) {
      const id = Number(n.id)
      if (keySet.has(id) && Number(n.level) === 4) {
        out.push(n.id)
      }
      if (n.children?.length) walk(n.children)
    }
  }
  walk(tunnelTree)
  return [...new Set(out)]
}
