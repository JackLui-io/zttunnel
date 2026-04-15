<template>
  <div class="tunnel-param-form" v-loading="loading">
    <el-form :model="formData" class="param-form" :disabled="readOnly">
      <div class="param-four-cols">
        <!-- 列1：隧道基本参数 -->
        <div class="param-col">
          <div class="param-col__title">隧道基本参数</div>
          <div class="param-col__body">
            <div class="param-row">
              <span class="param-row__label">线路名称</span>
              <div class="param-row__value">
                <el-input v-model="formData.lineName" placeholder="请输入" disabled size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">线路编号</span>
              <div class="param-row__value">
                <el-input v-model="formData.lineId" placeholder="请输入" disabled size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">线路里程(km)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lineMileage" :precision="2" :step="0.1" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">隧道总里程(km)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lineMileageTunnel" :precision="2" :step="0.1" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">隧道名称</span>
              <div class="param-row__value">
                <el-input v-model="formData.tunnelName" placeholder="请输入" disabled size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">隧道编号</span>
              <div class="param-row__value">
                <el-input v-model="formData.tunnelId" placeholder="请输入" disabled size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">隧道方向</span>
              <div class="param-row__value">
                <el-input v-model="formData.direction" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">入口里程编号</span>
              <div class="param-row__value">
                <el-input v-model="formData.inMileageNum" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">出口里程编号</span>
              <div class="param-row__value">
                <el-input v-model="formData.outMileageNum" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">隧道里程(km)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.tunnelMileage" :precision="2" :step="0.1" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">调试模式(开1关0)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.numlamp" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">引道调光(正1反0)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.numlampPemergent" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">雷达信号不重发(s)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.numlampPbasic" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">洞外来车开灯控制器起始桩号</span>
              <div class="param-row__value">
                <el-input v-model="formData.basicStart" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">洞外来车开灯控制器结束桩号</span>
              <div class="param-row__value">
                <el-input v-model="formData.basicEnd" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">区段总数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.sectionNum" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计速度(km/h)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.designV" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">入库车流量限值(5min)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.entryTrafficLimit" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">入库车速限值(km/h)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.entrySpeedLimit" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div v-for="n in inBackupRange" :key="'b' + n" class="param-row">
              <span class="param-row__label">备用{{ n }}</span>
              <div class="param-row__value">
                <el-input v-model="formData['backup'+n]" placeholder="请输入" size="small" />
              </div>
            </div>
          </div>
        </div>

        <!-- 列2：计算参数 -->
        <div class="param-col">
          <div class="param-col__title">计算参数</div>
          <div class="param-col__body">
            <div class="param-row">
              <span class="param-row__label">物理区段</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.zone" :min="1" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">物理区段里程编号</span>
              <div class="param-row__value">
                <el-input v-model="formData.zoneNum" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">原照明系统回路设置</span>
              <div class="param-row__value">
                <el-input v-model="formData.loopNumber" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">原照明系统回路设计功率</span>
              <div class="param-row__value">
                <el-input v-model="formData.designOperatingPowerR1" placeholder="请输入" size="small" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">参与智慧照明的总回路设计功率</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.designOperatingPowerTotal" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">超亮系数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.coeffL" :precision="2" :step="0.1" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">中间段亮度(cd/m²)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lin" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">大车流限值(veh/5min)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.largeTraffic" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">停车预警速度限值(m/s)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.vMin" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">灯具正向调光(正1反0)(m)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lightDistance" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">调光比例打折(%)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.delay" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">异常慢速目标速度上限值</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.vMax" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">日间和夜间灯光的分隔点</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.tNight1" :min="0" :max="24" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">夜间和深夜灯光的分隔点</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.tNight2" :min="0" :max="24" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">深夜和日间灯光的分隔点</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.tNight3" :min="0" :max="24" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">折减系数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.kBrightnessreduction" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">通过亮度判断天黑、天亮的分界线</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.l20Day" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考的洞外亮度参数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.l20Design" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">日间预设亮度大值</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.l20DayPreMax" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">日间预设亮度小值</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.l20DayPreMin" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">灯具可调光的电压范围小值(V)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.vRange1" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">灯具可调光的电压范围大值(V)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.vRange2" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">过压限值(V)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.umax" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">失压限值(V)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.umin" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">与设计功率比较报警限值(%)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.pd" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">与之前正常功率比较报警限值(%)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.po" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考的紧急停车带亮度(cd/m²)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lemDesign" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考的夜间全洞亮度(cd/m²)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lnt1Design" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考的深夜全洞亮度(cd/m²)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lnt2Design" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考的路面亮度转换系数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.alphaRoadDesign" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">工作模式</span>
              <div class="param-row__value">
                <el-select v-model="formData.mode" placeholder="请选择" size="small" class="param-select">
                  <el-option label="固定功率" :value="1" />
                  <el-option label="无极调光" :value="2" />
                  <el-option label="智慧调光" :value="3" />
                </el-select>
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">当前洞外亮度(只读)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.l20" :min="0" disabled controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">车流上报重复次数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.numberOfRepeats" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
          </div>
        </div>

        <!-- 列3：灯具参数 -->
        <div class="param-col">
          <div class="param-col__title">灯具参数</div>
          <div class="param-col__body">
            <div v-for="item in lampPowerRows" :key="item.key" class="param-row">
              <span class="param-row__label">{{ item.label }}</span>
              <div class="param-row__value">
                <el-input-number v-model="formData[item.key]" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">最小亮灯比值</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lightRadioMin" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">最大亮灯比值</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.lightRadioMax" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div v-for="item in lampAreaRows" :key="item.key" class="param-row">
              <span class="param-row__label">{{ item.label }}</span>
              <div class="param-row__value">
                <el-input-number v-model="formData[item.key]" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">灯具异常时长</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.offlineTimeout" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div v-for="item in lampNumberRows" :key="item.key" class="param-row">
              <span class="param-row__label">{{ item.label }}</span>
              <div class="param-row__value">
                <el-input-number v-model="formData[item.key]" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考的灯具维护系数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.mDesign" :precision="2" :step="0.1" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div v-for="item in lampLmRows" :key="item.key" class="param-row">
              <span class="param-row__label">{{ item.label }}</span>
              <div class="param-row__value">
                <el-input-number v-model="formData[item.key]" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">隧道空间利用系数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.uSpaceRate" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">设计时参考隧道布灯方式取值</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.xLayoutDesign" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">碳排放因子</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.carbonEmissionFactor" :precision="4" :step="0.0001" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">等效种树常数</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.equivalentTreeConstant" :precision="2" :step="0.01" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
            <div class="param-row">
              <span class="param-row__label">减碳煤当量常数(kgce)</span>
              <div class="param-row__value">
                <el-input-number v-model="formData.carbonReductionCoalEquivalentConstant" :min="0" controls-position="right" size="small" class="param-num" />
              </div>
            </div>
          </div>
        </div>

        <!-- 列4：预亮灯配置控制 -->
        <div class="param-col param-col--preon">
          <div class="param-col__title">预亮灯配置控制</div>
          <div class="param-col__body param-col__body--table">
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
                    <el-input-number v-model="item.wait" :min="0" :max="255" controls-position="right" size="small" class="pre-on-input" />
                  </td>
                  <td>
                    <el-input-number v-model="item.duration" :min="0" :max="255" controls-position="right" size="small" class="pre-on-input" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getTunnelParamInfo, updateTunnelParamInfo } from '@/api/tunnelParam'

const props = defineProps({
  tunnelId: {
    type: [Number, String],
    required: true
  },
  readOnly: { type: Boolean, default: false }
})

const inBackupRange = [1, 2, 3, 4, 5]

const lampPowerRows = [
  { key: 'p0Th1RatedPower', label: '入口段1使用的单灯额定功率' },
  { key: 'p0Th2RatedPower', label: '入口段2使用的单灯额定功率' },
  { key: 'p0Tr1RatedPower', label: '过渡段1使用的单灯额定功率' },
  { key: 'p0Tr2RatedPower', label: '过渡段2使用的单灯额定功率' },
  { key: 'p0MidRatedPower', label: '基本段使用的单灯额定功率' },
  { key: 'p0Ex1RatedPower', label: '出口段1使用的单灯额定功率' },
  { key: 'p0Ex2RatedPower', label: '出口段2使用的单灯额定功率' },
  { key: 'p0EmRatedPower', label: '紧急停车带单灯额定功率' }
]

const lampAreaRows = [
  { key: 's1Th1LightArea', label: '入口段1灯具照射面积' },
  { key: 's1Th2LightArea', label: '入口段2灯具照射面积' },
  { key: 's2Tr1LightArea', label: '过渡段1灯具照射面积' },
  { key: 's2Tr2LightArea', label: '过渡段2灯具照射面积' },
  { key: 's3MidLightArea', label: '基本段灯具照射面积' },
  { key: 's4Ex1LightArea', label: '出口段1灯具照射面积' },
  { key: 's4Ex2LightArea', label: '出口段2灯具照射面积' },
  { key: 's5EmLightArea', label: '紧急停车带灯具照射面积' }
]

const lampNumberRows = [
  { key: 'n1Th1Number', label: '入口段1灯具数量' },
  { key: 'n1Th2Number', label: '入口段2灯具数量' },
  { key: 'n2Tr1Number', label: '过渡段1灯具数量' },
  { key: 'n2Tr2Number', label: '过渡段2灯具数量' },
  { key: 'n3MidNumber', label: '基本段灯具数量' },
  { key: 'n4Ex1Number', label: '出口段1灯具数量' },
  { key: 'n4Ex2Number', label: '出口段2灯具数量' },
  { key: 'n5EmNumber', label: '紧急停车带灯具数量' }
]

const lampLmRows = [
  { key: 'fai1Th1Lm', label: '入口段1灯具光通量' },
  { key: 'fai1Th2Lm', label: '入口段2灯具光通量' },
  { key: 'fai2Tr1Lm', label: '过渡段1灯具光通量' },
  { key: 'fai2Tr2Lm', label: '过渡段2灯具光通量' },
  { key: 'fai3MidLm', label: '基本段灯具光通量' },
  { key: 'fai4Ex1Lm', label: '出口段1灯具光通量' },
  { key: 'fai4Ex2Lm', label: '出口段2灯具光通量' },
  { key: 'fai5EmLm', label: '紧急停车带灯具光通量' }
]

const loading = ref(false)

/** 与旧版 table5 一致：空/非数字视为 0，界面始终用 0 占位展示 */
const emptyPreOnRows = () =>
  Array.from({ length: 20 }, () => ({ wait: 0, duration: 0 }))

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
  const cellStr = (v) => {
    const n = toPreOnCell(v)
    return String(n)
  }
  const waits = preOnConfigTable.value.map((item) => cellStr(item.wait))
  const durations = preOnConfigTable.value.map((item) => cellStr(item.duration))
  return [...waits, ...durations].join(',')
}

const formData = ref({
  lineName: '',
  tunnelName: '',
  direction: '',
  lineMileage: null,
  tunnelMileage: null,
  lineMileageTunnel: null,
  lineId: null,
  tunnelId: null,
  backup1: '',
  backup2: '',
  backup3: '',
  backup4: '',
  backup5: '',
  zone: null,
  zoneNum: '',
  inMileageNum: '',
  outMileageNum: '',
  basicStart: '',
  basicEnd: '',
  sectionNum: null,
  loopNumber: '',
  designOperatingPowerR1: '',
  designOperatingPowerTotal: null,
  lin: null,
  coeffL: null,
  kBrightnessreduction: null,
  designV: null,
  xLayoutDesign: null,
  mDesign: null,
  largeTraffic: null,
  vMin: null,
  vMax: null,
  lightDistance: null,
  delay: null,
  offlineTimeout: null,
  numberOfRepeats: null,
  entryTrafficLimit: null,
  entrySpeedLimit: null,
  tNight1: null,
  tNight2: null,
  tNight3: null,
  l20Day: null,
  l20Design: null,
  l20DayPreMax: null,
  l20DayPreMin: null,
  l20: null,
  mode: null,
  lemDesign: null,
  lnt1Design: null,
  lnt2Design: null,
  alphaRoadDesign: null,
  uSpaceRate: null,
  vRange1: null,
  vRange2: null,
  umax: null,
  umin: null,
  pd: null,
  po: null,
  lightRadioMin: null,
  lightRadioMax: null,
  p0Th1RatedPower: null,
  p0Th2RatedPower: null,
  p0Tr1RatedPower: null,
  p0Tr2RatedPower: null,
  p0MidRatedPower: null,
  p0Ex1RatedPower: null,
  p0Ex2RatedPower: null,
  p0EmRatedPower: null,
  n1Th1Number: null,
  n1Th2Number: null,
  n2Tr1Number: null,
  n2Tr2Number: null,
  n3MidNumber: null,
  n4Ex1Number: null,
  n4Ex2Number: null,
  n5EmNumber: null,
  fai1Th1Lm: null,
  fai1Th2Lm: null,
  fai2Tr1Lm: null,
  fai2Tr2Lm: null,
  fai3MidLm: null,
  fai4Ex1Lm: null,
  fai4Ex2Lm: null,
  fai5EmLm: null,
  s1Th1LightArea: null,
  s1Th2LightArea: null,
  s2Tr1LightArea: null,
  s2Tr2LightArea: null,
  s3MidLightArea: null,
  s4Ex1LightArea: null,
  s4Ex2LightArea: null,
  s5EmLightArea: null,
  numlamp: null,
  numlampPemergent: null,
  numlampPbasic: null,
  carbonEmissionFactor: 0.573,
  equivalentTreeConstant: null,
  carbonReductionCoalEquivalentConstant: 29270
})

const loadTunnelParam = async () => {
  if (!props.tunnelId) return

  loading.value = true
  try {
    const res = await getTunnelParamInfo(props.tunnelId)
    if (res.code === 200 && res.data) {
      const tunnelData = res.data.tunnelNameResultVo || res.data
      Object.keys(formData.value).forEach((key) => {
        if (tunnelData[key] !== undefined && tunnelData[key] !== null) {
          formData.value[key] = tunnelData[key]
        }
      })
      parsePreOnConfig(tunnelData.preOnConfig)
    }
  } catch (error) {
    console.error('获取隧道参数失败:', error)
    ElMessage.error('获取隧道参数失败')
  } finally {
    loading.value = false
  }
}

const saveParams = async () => {
  if (props.readOnly) {
    ElMessage.warning('当前为查看模式，无法保存')
    return
  }
  // 后端 TunnelInfoAndDeviceDto 要求 tunnelNameResultVo 嵌套（与 zt_tunnel_web table5 一致），
  // 勿将字段铺在 JSON 根上，否则 tunnelNameResultVo 为 null 导致服务端 NPE → code 500。
  const data = {
    tunnelNameResultVo: {
      ...formData.value,
      id: props.tunnelId,
      preOnConfig: serializePreOnConfig()
    }
  }
  const res = await updateTunnelParamInfo(data)
  if (res.code !== 200) {
    throw new Error(res.msg || '保存失败')
  }
  return res
}

watch(
  () => props.tunnelId,
  (newVal) => {
    if (newVal) {
      loadTunnelParam()
    }
  },
  { immediate: true }
)

defineExpose({
  saveParams
})
</script>

<style lang="scss" scoped>
.tunnel-param-form {
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

    :deep(.el-input__inner),
    :deep(.el-input__wrapper),
    :deep(.el-input__wrapper input) {
      font-size: 14px;
    }

    :deep(.el-input-number .el-input__inner),
    :deep(.el-input-number .el-input__wrapper),
    :deep(.el-input-number .el-input__wrapper input) {
      font-size: 14px;
    }

    :deep(.el-select .el-input__inner),
    :deep(.el-select .el-input__wrapper),
    :deep(.el-select .el-input__wrapper input),
    :deep(.el-select__selected-item) {
      font-size: 14px;
    }
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

  :deep(.pre-on-input .el-input__inner),
  :deep(.pre-on-input .el-input__wrapper input),
  :deep(.pre-on-input .el-input__wrapper) {
    font-size: 14px;
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
