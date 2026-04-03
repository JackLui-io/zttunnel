import request from './request'
import type { ApiResponse } from './request'

/** 后端隧道树节点 */
export interface TunnelNameResult {
  id?: number
  tunnelName?: string
  parentId?: number
  level?: number
  tunnelMileage?: number
  children?: TunnelNameResult[]
}

/** 前端隧道树节点 */
export interface TunnelNode {
  name: string
  id?: number
  parentId?: number
  level?: number
  tunnelMileage?: number
  children?: TunnelNode[]
}

function mapToTunnelNode(item: TunnelNameResult, parentId?: number): TunnelNode {
  const node: TunnelNode = {
    name: item.tunnelName ?? '',
    id: item.id,
    parentId: item.parentId ?? parentId,
    level: item.level,
    tunnelMileage: item.tunnelMileage != null ? Number(item.tunnelMileage) : undefined,
  }
  if (item.children?.length) {
    node.children = item.children.map((ch) => mapToTunnelNode(ch, item.id))
  }
  return node
}

/** 获取隧道树（按用户权限） */
export function getTunnelTree(): Promise<TunnelNode[]> {
  return request.get<ApiResponse<TunnelNameResult[]>>('/tunnel/tree/list').then((res) => {
    const body = res.data as ApiResponse<TunnelNameResult[]>
    const list = (body?.data ?? body) as TunnelNameResult[] | undefined
    if (!Array.isArray(list)) return []
    return list.map((item) => mapToTunnelNode(item))
  })
}
