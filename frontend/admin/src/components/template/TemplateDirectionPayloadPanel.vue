<template>
  <div class="template-payload-panel">
    <div class="section">
      <div class="section__head">
        <span class="section__title">边缘计算终端参数</span>
      </div>
      <TemplateEdgeTerminalPanel :payload="payload" :read-only="readOnly" />
    </div>

    <div class="section">
      <div class="section__head">
        <span class="section__title">设备列表（tunnel_devicelist）</span>
        <el-button v-if="!readOnly" type="primary" size="small" @click="addDeviceDialogVisible = true">
          添加设备
        </el-button>
      </div>
      <el-table :data="deviceTableRows" border size="small" stripe>
        <el-table-column label="关联类型" width="88">
          <template #default="{ row }">
            {{ row.linkType === 1 ? '边缘' : row.linkType === 2 ? '电能' : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="设备分类" width="88">
          <template #default="{ row }">{{ row.typeLabel }}</template>
        </el-table-column>
        <el-table-column prop="deviceId" label="设备号" width="140" />
        <el-table-column prop="nickName" label="终端名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="电表行" width="72">
          <template #default="{ row }">{{ row.powerCount }}</template>
        </el-table-column>
        <el-table-column label="灯具关联" width="88">
          <template #default="{ row }">{{ row.lampRelCount }}</template>
        </el-table-column>
        <el-table-column v-if="!readOnly" label="操作" width="72" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="onRemoveDevice(row.deviceId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section section--stats">
      <div class="section__title">快照其它数据（条数）</div>
      <el-descriptions :column="3" size="small" border>
        <el-descriptions-item label="设备参数">{{ (payload.deviceParams || []).length }}</el-descriptions-item>
        <el-descriptions-item label="灯具终端">{{ (payload.lampsTerminals || []).length }}</el-descriptions-item>
        <el-descriptions-item label="洞外雷达">{{ (payload.outOfRadars || []).length }}</el-descriptions-item>
        <el-descriptions-item label="隧道设备表">{{ (payload.tunnelDevices || []).length }}</el-descriptions-item>
        <el-descriptions-item label="引道灯">{{ (payload.approachLampsTerminals || []).length }}</el-descriptions-item>
        <el-descriptions-item label="经纬度">{{ (payload.longitudes || []).length }}</el-descriptions-item>
      </el-descriptions>
      <p class="stats-hint">
        灯具、电表明细、洞外设备等随「设备列表」中对应边缘/电能终端一并快照；删除边缘终端将同步移除其灯具与洞外雷达关联。若需改灯具/电表逐条数据，请使用专业工具或后续扩展编辑能力。
      </p>
    </div>

    <el-dialog v-model="addDeviceDialogVisible" title="添加设备" width="400px" destroy-on-close @closed="resetAddDev">
      <el-form label-width="100px">
        <el-form-item label="设备类型" required>
          <el-radio-group v-model="addDevForm.deviceTypeId">
            <el-radio :label="1">边缘计算终端</el-radio>
            <el-radio :label="2">电能表采集终端</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备号" required>
          <el-input-number v-model="addDevForm.deviceId" :min="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="终端名称">
          <el-input v-model="addDevForm.nickName" maxlength="64" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDeviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddDevice">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDevicelistToPayload, removeDevicelistFromPayload } from '@/utils/templatePayload'
import TemplateEdgeTerminalPanel from '@/components/template/TemplateEdgeTerminalPanel.vue'

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

const addDeviceDialogVisible = ref(false)
const addDevForm = reactive({
  deviceTypeId: 1,
  deviceId: undefined,
  nickName: ''
})

const linkByDevId = (deviceId) => {
  const id = Number(deviceId)
  const links = props.payload.devicelistTunnelinfos || []
  const hit = links.find((l) => l && Number(l.devicelistId) === id)
  return hit?.type
}

const deviceTableRows = computed(() => {
  const list = props.payload.devicelists || []
  return list
    .filter((d) => d != null)
    .map((d) => {
      const deviceId = d.deviceId
      const tid = d.deviceTypeId != null ? Number(d.deviceTypeId) : null
      let typeLabel = '其它'
      if (tid === 1) typeLabel = '边缘'
      else if (tid === 2) typeLabel = '电能'
      const powerCount = (props.payload.powerEdgeComputing || []).filter(
        (r) => r && Number(r.devicelistId) === Number(deviceId)
      ).length
      const lampRelCount = (props.payload.lampsEdgeComputings || []).filter(
        (r) => r && Number(r.devicelistId) === Number(deviceId)
      ).length
      return {
        deviceId,
        deviceTypeId: tid,
        typeLabel,
        linkType: linkByDevId(deviceId),
        nickName: d.nickName || '',
        powerCount,
        lampRelCount
      }
    })
})

const onRemoveDevice = async (deviceId) => {
  if (props.readOnly) return
  try {
    await ElMessageBox.confirm(
      `确定从模板快照中删除设备 ${deviceId}？将同步删除其电表行、参数及（若为边缘）灯具与洞外雷达关联。`,
      '删除设备',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  removeDevicelistFromPayload(props.payload, deviceId)
  ElMessage.success('已从快照中移除该设备')
}

const resetAddDev = () => {
  addDevForm.deviceTypeId = 1
  addDevForm.deviceId = undefined
  addDevForm.nickName = ''
}

const confirmAddDevice = () => {
  const id = addDevForm.deviceId
  if (id == null || !Number.isFinite(Number(id)) || Number(id) <= 0) {
    ElMessage.warning('请输入有效设备号')
    return
  }
  try {
    addDevicelistToPayload(props.payload, {
      deviceId: Number(id),
      deviceTypeId: addDevForm.deviceTypeId,
      nickName: (addDevForm.nickName || '').trim()
    })
    ElMessage.success('已添加设备（请保存本方向以写入模板）')
    addDeviceDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || '添加失败')
  }
}
</script>

<style lang="scss" scoped>
.template-payload-panel {
  .section {
    margin-bottom: 20px;
  }

  .section__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
 }

  .section__title {
    font-weight: 600;
    font-size: 14px;
    color: var(--el-text-color-primary);
  }

  .section--stats {
    .stats-hint {
      margin: 10px 0 0;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      line-height: 1.5;
    }
  }

}
</style>
