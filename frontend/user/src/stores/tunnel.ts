import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTunnelStore = defineStore('tunnel', () => {
  const selectedTunnelId = ref<number | null>(null)
  /** 选中线路的父级隧道 id（level-3），用于左线灯具 fallback */
  const selectedParentTunnelId = ref<number | null>(null)

  function setSelectedTunnelId(id: number | null, parentId?: number | null) {
    selectedTunnelId.value = id
    selectedParentTunnelId.value = parentId ?? null
  }

  return {
    selectedTunnelId,
    selectedParentTunnelId,
    setSelectedTunnelId,
  }
})
