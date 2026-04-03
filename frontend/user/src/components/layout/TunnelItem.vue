<template>
  <div
    class="tunnel-item"
    :class="[
      `level-${level}`,
      hasChildren ? 'expanded' : 'leaf',
      { 'is-expanded': isExpanded },
      { 'is-company-selected': isCompanySelected },
      { 'is-selected': isSelected },
    ]"
    @click.stop="handleClick"
  >
    <div class="tunnel-row">
      <span class="tunnel-bg"></span>
      <span class="tunnel-icon">
        <template v-if="level === 1">
          <img src="/page1/Group2.png" alt="" class="icon-img icon-unsel" />
          <img src="/page1/Group1.png" alt="" class="icon-img icon-sel" />
        </template>
        <template v-else-if="level === 2">
          <img src="/page1/delete.png" alt="" class="icon-img icon-collapsed" />
          <img src="/page1/Add.png" alt="" class="icon-img icon-expanded" />
        </template>
        <template v-else-if="level === 3">
          <img src="/page1/Vector-down.png" alt="" class="icon-img icon-collapsed" />
          <img src="/page1/Vector-up.png" alt="" class="icon-img icon-expanded" />
        </template>
        <template v-else-if="level === 4">
          <img src="/page1/point.png" alt="" class="icon-img icon-unsel" />
          <img src="/page1/point-dark.png" alt="" class="icon-img icon-sel" />
        </template>
      </span>
      <span class="tunnel-text">{{ item.name }}</span>
    </div>
    <div v-if="hasChildren" class="tunnel-children" :style="{ display: isExpanded ? 'block' : 'none' }">
      <TunnelItem
        v-for="(ch, i) in item.children"
        :key="i"
        :item="ch"
        :level="level + 1"
        :company-idx="companyIdx"
        :default-expanded="level <= 2 || expandAllInCompany"
        :expand-all-in-company="expandAllInCompany && isExpanded"
        :selected-tunnel-id="props.selectedTunnelId"
        @company-expand="$emit('companyExpand', $event)"
        @select-line="$emit('selectLine', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { TunnelNode } from '@/data/tunnel'

const props = withDefaults(
  defineProps<{
    item: TunnelNode
    level: number
    companyIdx?: number
    defaultExpanded?: boolean
    isCompanySelected?: boolean
    expandAllInCompany?: boolean
    selectedTunnelId?: number | null
  }>(),
  {
    companyIdx: -1,
    defaultExpanded: false,
    isCompanySelected: false,
    expandAllInCompany: false,
    selectedTunnelId: null,
  }
)

const emit = defineEmits<{
  companyExpand: [idx: number]
  selectLine: [payload: { id: number; name: string; parentId?: number }]
}>()

const hasChildren = computed(() => Boolean(props.item.children?.length))
const isExpanded = ref((props.defaultExpanded || (props.expandAllInCompany && props.level >= 2 && props.level <= 3)) && hasChildren.value)

watch(
  () => [props.expandAllInCompany, props.defaultExpanded],
  () => {
    if (hasChildren.value && ((props.expandAllInCompany && props.level >= 2 && props.level <= 3) || props.defaultExpanded)) {
      isExpanded.value = true
    }
  }
)
const isSelected = computed(
  () => props.level === 4 && props.selectedTunnelId != null && props.item.id === props.selectedTunnelId
)

function handleClick() {
  if (props.level === 1 && hasChildren.value) {
    isExpanded.value = !isExpanded.value
    emit('companyExpand', isExpanded.value ? (props.companyIdx >= 0 ? props.companyIdx : 0) : -1)
  } else if (props.level === 4 && props.item.id != null) {
    emit('selectLine', { id: props.item.id, name: props.item.name, parentId: props.item.parentId })
  } else if (hasChildren.value) {
    isExpanded.value = !isExpanded.value
  }
}
</script>

<style scoped>
.tunnel-item {
  position: relative;
  width: 100%;
  min-height: 44px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}
.tunnel-item .tunnel-row {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 44px;
  flex-shrink: 0;
}
.tunnel-item .tunnel-bg {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: transparent;
  z-index: 0;
}
.tunnel-item.is-selected .tunnel-bg {
  background: linear-gradient(90deg, #91caa7 0%, #bbdf9f 50%, #def0d6 100%);
}
.tunnel-item.level-4.is-selected > .tunnel-row {
  margin-left: -60px;
  padding-left: 60px;
  width: calc(100% + 60px);
}
.tunnel-item.level-4.is-selected > .tunnel-row .tunnel-bg {
  left: 0;
  width: 274px;
}
.tunnel-item .tunnel-icon {
  position: relative;
  flex-shrink: 0;
  margin-left: 9px;
  z-index: 1;
}
.tunnel-item.level-1 .tunnel-icon {
  width: 24px;
  height: 24px;
}
.tunnel-item.level-2 .tunnel-icon,
.tunnel-item.level-3 .tunnel-icon {
  width: 10px;
  height: 10px;
}
.tunnel-item.level-4 .tunnel-icon {
  width: 6px;
  height: 6px;
}
.tunnel-item .tunnel-icon .icon-img {
  position: absolute;
  left: 0;
  top: 0;
  object-fit: contain;
}
.tunnel-item.level-1 .tunnel-icon .icon-img {
  width: 24px;
  height: 24px;
}
.tunnel-item.level-2 .tunnel-icon .icon-img,
.tunnel-item.level-3 .tunnel-icon .icon-img {
  width: 10px;
  height: 10px;
}
.tunnel-item.level-4 .tunnel-icon .icon-img {
  width: 6px;
  height: 6px;
}
.tunnel-item .tunnel-icon .icon-unsel {
  display: block;
}
.tunnel-item .tunnel-icon .icon-sel {
  display: none;
}
.tunnel-item .tunnel-icon .icon-collapsed {
  display: block;
}
.tunnel-item .tunnel-icon .icon-expanded {
  display: none;
}
.tunnel-item.is-company-selected .tunnel-icon .icon-unsel {
  display: none;
}
.tunnel-item.is-company-selected .tunnel-icon .icon-sel {
  display: block;
}
.tunnel-item.is-expanded > .tunnel-row .tunnel-icon .icon-collapsed {
  display: none;
}
.tunnel-item.is-expanded > .tunnel-row .tunnel-icon .icon-expanded {
  display: block;
}
.tunnel-item.is-selected .tunnel-icon .icon-unsel {
  display: none;
}
.tunnel-item.is-selected .tunnel-icon .icon-sel {
  display: block;
}
.tunnel-item .tunnel-text {
  margin-left: 15px;
  font-family: 'Microsoft YaHei', sans-serif;
  color: #000000;
  z-index: 1;
}
.tunnel-item.is-selected .tunnel-text {
  color: #ffffff;
}
.tunnel-item.is-company-selected .tunnel-text,
.tunnel-item.is-expanded .tunnel-text {
  color: #0f7f62;
}
.tunnel-item.level-1 .tunnel-text,
.tunnel-item.level-2 .tunnel-text,
.tunnel-item.level-3 .tunnel-text,
.tunnel-item.level-4 .tunnel-text {
  font-size: 14px;
  line-height: 18px;
}
.tunnel-children {
  padding-left: 20px;
  overflow: visible;
}
</style>
