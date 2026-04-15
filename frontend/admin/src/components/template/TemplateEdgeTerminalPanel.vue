<template>
  <div class="template-edge-terminal-panel" v-loading="metaLoading">
    <template v-if="!edgeObj">
      <el-button v-if="!readOnly" type="primary" size="small" @click="initEdge">初始化参数</el-button>
      <el-empty v-else description="当前方向无边缘终端参数" :image-size="64" />
    </template>
    <template v-else>
      <div class="param-four-cols">
        <div
          v-for="sec in EDGE_TERMINAL_PARAM_SECTIONS"
          :key="sec.id"
          class="param-col"
          :class="{ 'param-col--preon': sec.isPreOnTable }"
        >
          <div class="param-col__title">{{ sec.title }}</div>
          <div class="param-col__body" :class="{ 'param-col__body--table': sec.isPreOnTable }">
            <template v-if="sec.isPreOnTable">
              <table class="pre-on-table">
                <thead>
                  <tr>
                    <th>序号</th>
                    <th>等待时长(s)</th>
                    <th>持续时长(s)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in preOnConfigTable" :key="index">
                    <td class="pre-on-table__idx">{{ index + 1 }}</td>
                    <td>
                      <el-input-number
                        v-model="item.wait"
                        :min="0"
                        :max="255"
                        controls-position="right"
                        size="small"
                        class="pre-on-input"
                        :disabled="readOnly"
                      />
                    </td>
                    <td>
                      <el-input-number
                        v-model="item.duration"
                        :min="0"
                        :max="255"
                        controls-position="right"
                        size="small"
                        class="pre-on-input"
                        :disabled="readOnly"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </template>
            <template v-else>
              <div v-for="key in sec.keys" :key="key" class="param-row">
                <span class="param-row__label">{{ labelFor(key) }}</span>
                <div class="param-row__value">
                  <template v-if="key === 'inMileageNum' || key === 'outMileageNum'">
                    <el-input v-model="edgeObj[key]" size="small" :disabled="readOnly" clearable />
                  </template>
                  <template v-else-if="fieldType(key) === 'text'">
                    <el-input v-model="edgeObj[key]" size="small" :disabled="readOnly" clearable />
                  </template>
                  <template v-else-if="fieldType(key) === 'mode'">
                    <el-select
                      v-model="edgeObj[key]"
                      placeholder="请选择"
                      size="small"
                      class="param-select"
                      :disabled="readOnly"
                      clearable
                    >
                      <el-option label="固定功率" :value="1" />
                      <el-option label="无极调光" :value="2" />
                      <el-option label="智慧调光" :value="3" />
                    </el-select>
                  </template>
                  <template v-else>
                    <el-input-number
                      v-model="edgeObj[key]"
                      :precision="numberPrecision(key)"
                      :step="numberStep(key)"
                      :min="0"
                      controls-position="right"
                      size="small"
                      class="param-num"
                      :disabled="readOnly || key === 'l20'"
                      style="width: 100%"
                    />
                  </template>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { EDGE_TERMINAL_PARAM_SECTIONS } from '@/constants/edgeTerminalParamLayout'
import { TEMPLATE_EDGE_FIELD_BY_KEY } from '@/constants/templateEdgeFormFields'
import { fetchEdgeTerminalColumnMeta, buildCommentByPropertyMap } from '@/utils/edgeTerminalColumnMeta'

const props = defineProps({
  payload: {
    type: Object,
    required: true
  },
  readOnly: {
    type: Boolean,
    default: false
  }
})

const metaLoading = ref(false)
const commentByProperty = ref({})

const edgeObj = computed(() => {
  const e = props.payload?.edgeTerminal
  return e && typeof e === 'object' ? e : null
})

const emptyPreOnRows = () => Array.from({ length: 20 }, () => ({ wait: 0, duration: 0 }))

const toPreOnCell = (raw) => {
  if (raw === null || raw === undefined || raw === '') return 0
  const n = Number(raw)
  return Number.isFinite(n) ? n : 0
}

const preOnConfigTable = ref(emptyPreOnRows())

const parsePreOnConfig = (configStr) => {
  if (!configStr || typeof configStr !== 'string') {
    preOnConfigTable.value = emptyPreOnRows()
    return
  }
  const values = configStr.split(',')
  if (values.length !== 40) {
    preOnConfigTable.value = emptyPreOnRows()
    return
  }
  preOnConfigTable.value = Array.from({ length: 20 }, (_, i) => ({
    wait: toPreOnCell(values[i]),
    duration: toPreOnCell(values[i + 20])
  }))
}

const serializePreOnConfig = () => {
  const cellStr = (v) => String(toPreOnCell(v))
  const waits = preOnConfigTable.value.map((item) => cellStr(item.wait))
  const durations = preOnConfigTable.value.map((item) => cellStr(item.duration))
  return [...waits, ...durations].join(',')
}

function labelFor(key) {
  const c = commentByProperty.value[key]
  if (c) return c
  return TEMPLATE_EDGE_FIELD_BY_KEY[key]?.label || key
}

function fieldType(key) {
  return TEMPLATE_EDGE_FIELD_BY_KEY[key]?.type || 'number'
}

function numberPrecision(key) {
  if (key === 'carbonEmissionFactor') return 4
  if (key === 'coeffL' || key === 'mDesign' || key === 'equivalentTreeConstant') return 2
  return undefined
}

function numberStep(key) {
  if (key === 'carbonEmissionFactor') return 0.0001
  if (key === 'coeffL' || key === 'mDesign') return 0.1
  return 1
}

function initEdge() {
  props.payload.edgeTerminal = { lineName: '', tunnelName: '', direction: '' }
}

watch(
  () => edgeObj.value?.preOnConfig,
  (s) => {
    if (edgeObj.value) parsePreOnConfig(s)
  },
  { immediate: true }
)

watch(
  preOnConfigTable,
  () => {
    if (!edgeObj.value || props.readOnly) return
    edgeObj.value.preOnConfig = serializePreOnConfig()
  },
  { deep: true }
)

onMounted(async () => {
  metaLoading.value = true
  try {
    const list = await fetchEdgeTerminalColumnMeta()
    commentByProperty.value = buildCommentByPropertyMap(list)
  } finally {
    metaLoading.value = false
  }
})
</script>

<style lang="scss" scoped>
.template-edge-terminal-panel {
  .param-four-cols {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 10px;
    align-items: start;
  }

  .param-col {
    min-width: 0;
    border: 1px solid rgba(0, 234, 255, 0.2);
    border-radius: 6px;
    background: rgba(0, 234, 255, 0.03);
    max-height: calc(100vh - 240px);
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .param-col__title {
    flex-shrink: 0;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-color-primary);
    padding: 8px 10px;
    border-bottom: 1px solid rgba(0, 234, 255, 0.18);
    background: rgba(0, 234, 255, 0.06);
  }

  .param-col__body {
    padding: 6px 8px 10px;
    overflow-y: auto;
    flex: 1;
    min-height: 0;
  }

  .param-col__body--table {
    padding: 6px;
  }

  .param-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 5px;
    font-size: 14px;
  }

  .param-row__label {
    flex: 0 0 64%;
    max-width: 64%;
    min-width: 0;
    font-size: 14px;
    color: var(--el-text-color-regular);
    line-height: 1.25;
  }

  .param-row__value {
    flex: 1 1 36%;
    max-width: 36%;
    min-width: 0;
    font-size: 14px;
  }

  :deep(.param-num.el-input-number) {
    width: 100%;
  }

  :deep(.param-select) {
    width: 100%;
  }

  .pre-on-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;

    th,
    td {
      border: 1px solid var(--el-border-color-lighter);
      padding: 2px 4px;
      text-align: center;
    }

    th {
      background: rgba(0, 234, 255, 0.08);
      font-weight: 600;
    }
  }

  .pre-on-table__idx {
    width: 36px;
    color: var(--el-text-color-secondary);
  }

  :deep(.pre-on-input.el-input-number) {
    width: 100%;
  }

  @media (max-width: 1600px) {
    .param-four-cols {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 900px) {
    .param-four-cols {
      grid-template-columns: 1fr;
    }
  }
}
</style>
