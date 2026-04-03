import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAllTunnelTree } from '@/api/tunnel'

export const useTunnelStore = defineStore('tunnel', () => {
  // 当前选中的隧道路径（级联选择器的值，数组形式）
  const currentTunnelPath = ref(JSON.parse(localStorage.getItem('currentTunnelPath') || '[]'))

  // 隧道树数据（用于级联选择器）
  const tunnelTree = ref([])

  // 所有隧道的扁平列表（包含所有级别）
  const allTunnelList = ref([])

  // level=4的隧道列表（用于某些只需要最终隧道的场景）
  const level4TunnelList = ref([])

  // 是否已加载
  const loaded = ref(false)

  // 当前选中的隧道ID（级联选择器最后一个值）
  const currentTunnelId = computed(() => {
    if (currentTunnelPath.value && currentTunnelPath.value.length > 0) {
      return currentTunnelPath.value[currentTunnelPath.value.length - 1]
    }
    return null
  })

  // 当前选中的隧道信息
  const currentTunnel = computed(() => {
    if (!currentTunnelId.value || allTunnelList.value.length === 0) return null
    return allTunnelList.value.find(t => t.id === currentTunnelId.value) || null
  })

  // 设置当前隧道路径
  const setCurrentTunnelPath = (path) => {
    currentTunnelPath.value = path || []
    localStorage.setItem('currentTunnelPath', JSON.stringify(currentTunnelPath.value))
  }

  // 设置当前隧道ID（兼容旧的调用方式，会自动查找路径）
  const setCurrentTunnelId = (tunnelId) => {
    if (!tunnelId) {
      setCurrentTunnelPath([])
      return
    }
    // 查找隧道路径
    const path = findTunnelPath(tunnelTree.value, tunnelId)
    if (path) {
      setCurrentTunnelPath(path)
    }
  }

  // 在树中查找隧道路径
  const findTunnelPath = (tree, targetId, currentPath = []) => {
    for (const node of tree) {
      const newPath = [...currentPath, node.id]
      if (node.id === targetId) {
        return newPath
      }
      if (node.children && node.children.length > 0) {
        const result = findTunnelPath(node.children, targetId, newPath)
        if (result) return result
      }
    }
    return null
  }

  // 从树形数据中提取所有隧道（扁平化）
  const extractAllTunnels = (list, result = []) => {
    for (const item of list) {
      result.push({
        id: item.id,
        tunnelName: item.tunnelName,
        level: item.level,
        parentId: item.parentId
      })
      if (item.children && item.children.length > 0) {
        extractAllTunnels(item.children, result)
      }
    }
    return result
  }

  // 从树形数据中提取level=4的隧道
  const extractLevel4Tunnels = (list, result = []) => {
    for (const item of list) {
      if (item.level === 4) {
        result.push({ id: item.id, tunnelName: item.tunnelName })
      }
      if (item.children && item.children.length > 0) {
        extractLevel4Tunnels(item.children, result)
      }
    }
    return result
  }

  // 转换树数据为级联选择器格式
  const convertToCascaderData = (list) => {
    return list.map(item => ({
      value: item.id,
      label: item.tunnelName,
      children: item.children && item.children.length > 0
        ? convertToCascaderData(item.children)
        : undefined
    }))
  }

  // 过滤失效状态的隧道节点（只保留status=0的有效隧道）
  const filterValidTunnels = (list) => {
    return list
      .filter(item => item.status === 0) // 只保留有效状态的隧道
      .map(item => ({
        ...item,
        children: item.children && item.children.length > 0
          ? filterValidTunnels(item.children)
          : undefined
      }))
  }

  // 级联选择器数据
  const cascaderOptions = computed(() => convertToCascaderData(tunnelTree.value))


  // 加载隧道数据
  const loadTunnelData = async () => {
    if (loaded.value) return tunnelTree.value

    try {
      const res = await getAllTunnelTree()
      if (res.code === 200 && res.data) {
        // 过滤掉失效状态的隧道，只保留有效状态（status=0）的隧道
        const validTunnels = filterValidTunnels(res.data)
        tunnelTree.value = validTunnels
        allTunnelList.value = extractAllTunnels(validTunnels)
        level4TunnelList.value = extractLevel4Tunnels(validTunnels)
        loaded.value = true

        // 如果没有选中的隧道，默认选择第一个level=4的隧道
        if (currentTunnelPath.value.length === 0 && level4TunnelList.value.length > 0) {
          const firstTunnel = level4TunnelList.value[0]
          const path = findTunnelPath(tunnelTree.value, firstTunnel.id)
          if (path) {
            setCurrentTunnelPath(path)
          }
        }

        // 验证当前选中的隧道是否存在
        if (currentTunnelPath.value.length > 0) {
          const lastId = currentTunnelPath.value[currentTunnelPath.value.length - 1]
          if (!allTunnelList.value.find(t => t.id === lastId)) {
            // 当前选中的隧道不存在，重置为第一个
            if (level4TunnelList.value.length > 0) {
              const firstTunnel = level4TunnelList.value[0]
              const path = findTunnelPath(tunnelTree.value, firstTunnel.id)
              if (path) {
                setCurrentTunnelPath(path)
              }
            } else {
              setCurrentTunnelPath([])
            }
          }
        }
      }
      return tunnelTree.value
    } catch (error) {
      console.error('加载隧道数据失败:', error)
      return []
    }
  }

  // 重新加载隧道数据
  const reloadTunnelData = async () => {
    loaded.value = false
    return await loadTunnelData()
  }

  return {
    currentTunnelPath,
    currentTunnelId,
    tunnelTree,
    allTunnelList,
    level4TunnelList,
    cascaderOptions,
    currentTunnel,
    loaded,
    setCurrentTunnelPath,
    setCurrentTunnelId,
    loadTunnelData,
    reloadTunnelData
  }
})
