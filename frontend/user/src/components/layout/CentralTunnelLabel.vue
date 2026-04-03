<template>
  <div
    class="central-tunnel-label"
    :class="{ 'central-tunnel-label--hidden': !displayPath }"
  >
    {{ displayPath }}
  </div>
</template>

<script setup lang="ts">
import { inject, computed } from 'vue'

const selectedTunnelPath = inject<{ value: string[] | null }>('selectedTunnelPath')

const displayPath = computed(() => {
  const path = selectedTunnelPath?.value
  if (!path?.length) return ''
  return path.join('—')
})
</script>

<style scoped>
.central-tunnel-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 280px;
  flex-shrink: 1;
}
.central-tunnel-label.central-tunnel-label--hidden {
  display: none;
}
</style>
