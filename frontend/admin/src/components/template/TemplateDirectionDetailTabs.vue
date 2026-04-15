<template>
  <div class="template-direction-detail-tabs">
    <div v-if="!readOnly" class="payload-toolbar">
      <el-button type="primary" size="small" @click="addDeviceDialogVisible = true">添加设备</el-button>
      <span class="payload-toolbar__hint">添加边缘或电能终端后可在对应页签查看；修改后请保存本方向。</span>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="detail-bind-tabs">
      <el-tab-pane label="隧道参数" name="param" class="tab-pane--param">
        <div class="param-tab-scroll">
          <TemplateEdgeTerminalPanel :payload="payload" :read-only="readOnly" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="边缘控制器" name="edge">
        <div class="table-pane">
          <div v-if="!readOnly" class="tab-pane-toolbar">
            <span class="tab-pane-toolbar__hint">可直接改单元格；设备号为快照主键请勿改。删除将同步清理关联灯具/电表等。</span>
          </div>
          <el-table
            v-if="edgeDevicelists.length"
            :data="edgeDevicelists"
            border
            stripe
            size="small"
            table-layout="auto"
            max-height="520"
          >
            <el-table-column prop="deviceId" label="设备号" min-width="112" show-overflow-tooltip />
            <el-table-column label="终端名称" min-width="128">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.nickName" size="small" maxlength="128" />
                <span v-else>{{ row.nickName ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="桩号" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceNum" size="small" maxlength="64" />
                <span v-else>{{ row.deviceNum ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="区段" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.zone" size="small" maxlength="64" />
                <span v-else>{{ row.zone ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="回路编号" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.loopNumber" size="small" maxlength="64" />
                <span v-else>{{ row.loopNumber ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="信号强度" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.csq" size="small" maxlength="32" />
                <span v-else>{{ row.csq ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="在线" min-width="80">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.online" size="small" maxlength="32" />
                <span v-else>{{ row.online != null ? row.online : '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="设备状态" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceStatus" size="small" maxlength="64" />
                <span v-else>{{ row.deviceStatus ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最后数据" min-width="156">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.lastUpdate" size="small" maxlength="64" />
                <span v-else>{{ row.lastUpdate ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="!readOnly" label="操作" width="72" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="onRemoveDevice(row.deviceId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无边缘终端设备" :image-size="64" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="电能终端" name="power">
        <div class="table-pane">
          <div v-if="!readOnly" class="tab-pane-toolbar">
            <span class="tab-pane-toolbar__hint">可直接改单元格；设备号请勿改。删除将同步移除挂在本终端下的电表行。</span>
          </div>
          <el-table
            v-if="powerDevicelists.length"
            :data="powerDevicelists"
            border
            stripe
            size="small"
            table-layout="auto"
            max-height="520"
          >
            <el-table-column prop="deviceId" label="设备号" min-width="112" show-overflow-tooltip />
            <el-table-column label="终端名称" min-width="128">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.nickName" size="small" maxlength="128" />
                <span v-else>{{ row.nickName ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="桩号" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceNum" size="small" maxlength="64" />
                <span v-else>{{ row.deviceNum ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="区段" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.zone" size="small" maxlength="64" />
                <span v-else>{{ row.zone ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="回路编号" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.loopNumber" size="small" maxlength="64" />
                <span v-else>{{ row.loopNumber ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="在线" min-width="80">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.online" size="small" maxlength="32" />
                <span v-else>{{ row.online != null ? row.online : '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="设备状态" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceStatus" size="small" maxlength="64" />
                <span v-else>{{ row.deviceStatus ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="!readOnly" label="操作" width="72" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="onRemoveDevice(row.deviceId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无电能终端设备" :image-size="64" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="灯具终端" name="lamps">
        <div class="table-pane">
          <div v-if="!readOnly" class="tab-pane-toolbar">
            <el-button type="primary" size="small" @click="openAddLamp">添加灯具行</el-button>
            <span class="tab-pane-toolbar__hint">唯一标识为关联主键，请勿随意修改；删除将同步清理 lampsEdgeComputings / nodes。</span>
          </div>
          <el-table
            v-if="lampsRows.length"
            :data="lampsRows"
            border
            stripe
            size="small"
            table-layout="auto"
            max-height="520"
          >
            <el-table-column label="唯一标识" min-width="120">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.uniqueId" size="small" maxlength="128" />
                <span v-else>{{ row.uniqueId ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="设备号" min-width="112">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceId" size="small" />
                <span v-else>{{ row.deviceId ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="终端名称" min-width="120">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceName" size="small" maxlength="128" />
                <span v-else>{{ row.deviceName ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="安装里程" min-width="104">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.deviceNum" size="small" maxlength="64" />
                <span v-else>{{ row.deviceNum ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="灯具序号" min-width="92">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.position" size="small" maxlength="64" />
                <span v-else>{{ row.position ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="回路编号" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.loopNumber" size="small" maxlength="64" />
                <span v-else>{{ row.loopNumber ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="区段" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.zone" size="small" maxlength="64" />
                <span v-else>{{ row.zone ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="区段2" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.zone2" size="small" maxlength="64" />
                <span v-else>{{ row.zone2 ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="蓝牙编号" min-width="108">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.bluetoothNum" size="small" maxlength="64" />
                <span v-else>{{ row.bluetoothNum ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="雷达设备号" min-width="116">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.ldDeviceId" size="small" maxlength="64" />
                <span v-else>{{ row.ldDeviceId ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="!readOnly" label="操作" width="72" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="onRemoveLamp(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无灯具终端快照" :image-size="64" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="引道灯控制器" name="approach">
        <div class="table-pane">
          <div v-if="!readOnly" class="tab-pane-toolbar">
            <el-button type="primary" size="small" @click="openAddApproach">添加引道灯行</el-button>
          </div>
          <el-table
            v-if="approachRows.length"
            :data="approachRows"
            border
            stripe
            size="small"
            table-layout="auto"
            max-height="520"
          >
            <el-table-column label="设备号" min-width="120">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.devNo" size="small" maxlength="64" />
                <span v-else>{{ row.devNo ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="安装里程" min-width="120">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.mileage" size="small" maxlength="64" />
                <span v-else>{{ row.mileage ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="区段" min-width="100">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.zoneName" size="small" maxlength="64" />
                <span v-else>{{ row.zoneName ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="蓝牙强度" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.bluetoothStrength" size="small" maxlength="32" />
                <span v-else>{{ row.bluetoothStrength ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="版本号" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.version" size="small" maxlength="64" />
                <span v-else>{{ row.version ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="88">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.status" size="small" maxlength="32" />
                <span v-else>{{ row.status ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最后数据" min-width="160">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.lastUpdate" size="small" maxlength="64" />
                <span v-else>{{ row.lastUpdate ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="!readOnly" label="操作" width="72" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="onRemoveApproach(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无引道灯控制器快照" :image-size="64" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="电表" name="meter">
        <div class="table-pane">
          <div v-if="!readOnly" class="tab-pane-toolbar">
            <el-button type="primary" size="small" :disabled="!powerDevicelists.length" @click="openAddMeter">
              添加电表行
            </el-button>
            <span v-if="!powerDevicelists.length" class="tab-pane-toolbar__hint">请先在「电能终端」添加快照终端后再新增电表。</span>
          </div>
          <el-table
            v-if="meterRows.length"
            :data="meterRows"
            border
            stripe
            size="small"
            table-layout="auto"
            max-height="520"
          >
            <el-table-column label="地址号" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.address" size="small" maxlength="32" />
                <span v-else>{{ row.address ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="电能终端设备号" min-width="148">
              <template #default="{ row }">
                <el-select
                  v-if="!readOnly"
                  :model-value="row.devicelistId != null && row.devicelistId !== '' ? Number(row.devicelistId) : undefined"
                  size="small"
                  filterable
                  style="width: 100%"
                  placeholder="选择"
                  @update:model-value="(v) => { row.devicelistId = v }"
                >
                  <el-option
                    v-for="p in powerDevicelists"
                    :key="p.deviceId"
                    :label="String(p.deviceId)"
                    :value="Number(p.deviceId)"
                  />
                </el-select>
                <span v-else>{{ row.devicelistId ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="电能终端名称" min-width="140">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.loopName" size="small" maxlength="128" />
                <span v-else>{{ row.loopName ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="方向" min-width="100">
              <template #default="{ row }">
                <el-select
                  v-if="!readOnly"
                  :model-value="meterDirectionVal(row)"
                  size="small"
                  clearable
                  placeholder="—"
                  style="width: 100%"
                  @update:model-value="(v) => setMeterDirection(row, v)"
                >
                  <el-option label="右线" :value="1" />
                  <el-option label="左线" :value="2" />
                </el-select>
                <span v-else>{{ row.direction === 1 ? '右线' : row.direction === 2 ? '左线' : row.direction ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="厂商 id" min-width="96">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.vendorId" size="small" maxlength="64" />
                <span v-else>{{ row.vendorId ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="启用" min-width="88">
              <template #default="{ row }">
                <el-select
                  v-if="!readOnly"
                  :model-value="meterEnabledVal(row)"
                  size="small"
                  style="width: 72px"
                  @update:model-value="(v) => setMeterEnabled(row, v)"
                >
                  <el-option label="是" :value="1" />
                  <el-option label="否" :value="0" />
                </el-select>
                <span v-else>
                  {{ row.isEnabled === 1 || row.isEnabled === true ? '是' : row.isEnabled === 0 || row.isEnabled === false ? '否' : '—' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="最后更新时间" min-width="160">
              <template #default="{ row }">
                <el-input v-if="!readOnly" v-model="row.lastTime" size="small" maxlength="64" />
                <span v-else>{{ row.lastTime ?? '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="!readOnly" label="操作" width="72" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="onRemoveMeter(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="无电表快照" :image-size="64" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="addDeviceDialogVisible" title="添加设备" width="400px" destroy-on-close @closed="resetAddDev">
      <el-form label-width="100px">
        <el-form-item label="设备类型" required>
          <el-radio-group v-model="addDevForm.deviceTypeId">
            <el-radio :label="1">边缘计算终端</el-radio>
            <el-radio :label="2">电能表采集终端</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备号" required>
          <el-input-number
            v-model="addDevForm.deviceId"
            :min="1"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
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

    <el-dialog v-model="addLampDialogVisible" title="添加灯具行" width="440px" destroy-on-close @closed="resetAddLamp">
      <el-form label-width="108px">
        <el-form-item label="唯一标识" required>
          <el-input v-model="addLampForm.uniqueId" maxlength="128" clearable placeholder="不可与现有行重复" />
        </el-form-item>
        <el-form-item label="设备号">
          <el-input-number v-model="addLampForm.deviceId" :min="1" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="终端名称">
          <el-input v-model="addLampForm.deviceName" maxlength="128" clearable />
        </el-form-item>
        <el-form-item label="安装里程">
          <el-input v-model="addLampForm.deviceNum" maxlength="64" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addLampDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddLamp">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addApproachDialogVisible" title="添加引道灯行" width="440px" destroy-on-close @closed="resetAddApproach">
      <el-form label-width="100px">
        <el-form-item label="设备号">
          <el-input v-model="addApproachForm.devNo" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="安装里程">
          <el-input v-model="addApproachForm.mileage" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="区段">
          <el-input v-model="addApproachForm.zoneName" maxlength="64" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addApproachDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddApproach">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addMeterDialogVisible" title="添加电表行" width="480px" destroy-on-close @closed="resetAddMeter">
      <el-form label-width="120px">
        <el-form-item label="电能终端" required>
          <el-select v-model="addMeterForm.devicelistId" filterable placeholder="选择设备号" style="width: 100%">
            <el-option
              v-for="p in powerDevicelists"
              :key="p.deviceId"
              :label="`${p.deviceId}${p.nickName ? ' · ' + p.nickName : ''}`"
              :value="Number(p.deviceId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="地址号">
          <el-input v-model="addMeterForm.address" maxlength="32" clearable />
        </el-form-item>
        <el-form-item label="电能终端名称">
          <el-input v-model="addMeterForm.loopName" maxlength="128" clearable />
        </el-form-item>
        <el-form-item label="方向">
          <el-select v-model="addMeterForm.direction" clearable placeholder="可选" style="width: 100%">
            <el-option label="右线" :value="1" />
            <el-option label="左线" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="厂商 id">
          <el-input v-model="addMeterForm.vendorId" maxlength="64" clearable />
        </el-form-item>
        <el-form-item label="启用">
          <el-radio-group v-model="addMeterForm.isEnabled">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMeterDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddMeter">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDevicelistToPayload,
  removeDevicelistFromPayload,
  removeLampTerminalFromPayload,
  removeApproachLampRow,
  removePowerMeterRow,
  addLampTerminalRow,
  addApproachLampRow,
  addPowerMeterRow
} from '@/utils/templatePayload'
import TemplateEdgeTerminalPanel from '@/components/template/TemplateEdgeTerminalPanel.vue'

const props = defineProps({
  payload: {
    type: Object,
    required: true
  },
  readOnly: {
    type: Boolean,
    default: true
  }
})

const activeTab = ref('param')
const addDeviceDialogVisible = ref(false)
const addDevForm = reactive({
  deviceTypeId: 1,
  deviceId: undefined,
  nickName: ''
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

const addLampDialogVisible = ref(false)
const addLampForm = reactive({
  uniqueId: '',
  deviceId: undefined,
  deviceName: '',
  deviceNum: ''
})

const openAddLamp = () => {
  resetAddLamp()
  addLampDialogVisible.value = true
}

const resetAddLamp = () => {
  addLampForm.uniqueId = ''
  addLampForm.deviceId = undefined
  addLampForm.deviceName = ''
  addLampForm.deviceNum = ''
}

const confirmAddLamp = () => {
  try {
    addLampTerminalRow(props.payload, {
      uniqueId: (addLampForm.uniqueId || '').trim(),
      deviceId: addLampForm.deviceId,
      deviceName: addLampForm.deviceName,
      deviceNum: addLampForm.deviceNum
    })
    ElMessage.success('已添加灯具行（请保存本方向）')
    addLampDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || '添加失败')
  }
}

const onRemoveLamp = async (row) => {
  if (props.readOnly || !row) return
  const uid = row.uniqueId != null ? String(row.uniqueId).trim() : ''
  if (!uid) return
  try {
    await ElMessageBox.confirm(`确定删除灯具「${uid}」及其关联关系？`, '删除灯具', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  removeLampTerminalFromPayload(props.payload, uid)
  ElMessage.success('已删除')
}

const addApproachDialogVisible = ref(false)
const addApproachForm = reactive({
  devNo: '',
  mileage: '',
  zoneName: ''
})

const openAddApproach = () => {
  resetAddApproach()
  addApproachDialogVisible.value = true
}

const resetAddApproach = () => {
  addApproachForm.devNo = ''
  addApproachForm.mileage = ''
  addApproachForm.zoneName = ''
}

const confirmAddApproach = () => {
  addApproachLampRow(props.payload, { ...addApproachForm })
  ElMessage.success('已添加引道灯行（请保存本方向）')
  addApproachDialogVisible.value = false
}

const onRemoveApproach = async (row) => {
  if (props.readOnly || !row) return
  try {
    await ElMessageBox.confirm('确定删除该引道灯快照行？', '删除引道灯', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  removeApproachLampRow(props.payload, row)
  ElMessage.success('已删除')
}

const addMeterDialogVisible = ref(false)
const addMeterForm = reactive({
  devicelistId: undefined,
  address: '',
  loopName: '',
  direction: undefined,
  vendorId: '',
  isEnabled: 1
})

const openAddMeter = () => {
  resetAddMeter()
  addMeterDialogVisible.value = true
}

const resetAddMeter = () => {
  addMeterForm.devicelistId = undefined
  addMeterForm.address = ''
  addMeterForm.loopName = ''
  addMeterForm.direction = undefined
  addMeterForm.vendorId = ''
  addMeterForm.isEnabled = 1
}

const confirmAddMeter = () => {
  try {
    addPowerMeterRow(props.payload, { ...addMeterForm })
    ElMessage.success('已添加电表行（请保存本方向）')
    addMeterDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e?.message || '添加失败')
  }
}

const onRemoveMeter = async (row) => {
  if (props.readOnly || !row) return
  try {
    await ElMessageBox.confirm('确定删除该电表快照行？', '删除电表', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  removePowerMeterRow(props.payload, row)
  ElMessage.success('已删除')
}

function meterDirectionVal(row) {
  const d = Number(row.direction)
  if (d === 1) return 1
  if (d === 2) return 2
  return undefined
}

function setMeterDirection(row, v) {
  row.direction = v == null || v === '' ? null : Number(v)
}

function meterEnabledVal(row) {
  const e = row.isEnabled
  if (e === true || e === 1 || e === '1') return 1
  if (e === false || e === 0 || e === '0') return 0
  return 1
}

function setMeterEnabled(row, v) {
  row.isEnabled = v
}

const devByType = (typeId) =>
  (props.payload.devicelists || []).filter((d) => d != null && Number(d.deviceTypeId) === typeId)

const edgeDevicelists = computed(() => devByType(1))
const powerDevicelists = computed(() => devByType(2))

const lampsRows = computed(() => (props.payload.lampsTerminals || []).filter((r) => r != null))

const approachRows = computed(() => (props.payload.approachLampsTerminals || []).filter((r) => r != null))

const meterRows = computed(() => (props.payload.powerEdgeComputing || []).filter((r) => r != null))
</script>

<style lang="scss" scoped>
.template-direction-detail-tabs {
  min-height: 360px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.payload-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.payload-toolbar__hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.detail-bind-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  :deep(.el-tab-pane) {
    min-height: 320px;
  }
}

.param-tab-scroll {
  padding: 8px 4px 12px;
}

.table-pane {
  padding: 4px 0 8px;
  min-height: 200px;
}

.tab-pane-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.tab-pane-toolbar__hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
</style>
