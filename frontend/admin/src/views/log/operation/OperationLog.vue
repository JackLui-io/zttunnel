<template>
  <div class="operation-log">
    <div class="card">
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="系统模块">
            <el-input v-model="searchForm.title" placeholder="请输入系统模块" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="操作人员">
            <el-input v-model="searchForm.operName" placeholder="请输入操作人员" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="业务类型">
            <el-select v-model="searchForm.businessType" placeholder="请选择业务类型" clearable style="width: 150px;">
              <el-option label="其它" :value="0" />
              <el-option label="新增" :value="1" />
              <el-option label="修改" :value="2" />
              <el-option label="删除" :value="3" />
              <el-option label="授权" :value="4" />
              <el-option label="导出" :value="5" />
              <el-option label="导入" :value="6" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
              <el-option label="成功" :value="0" />
              <el-option label="失败" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
            <el-button type="danger" @click="handleClean">
              <el-icon><Delete /></el-icon>清空
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="loading"
          border
          stripe
          :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', padding: '12px 0' }"
          :cell-style="{ padding: '10px 8px' }"
        >
          <el-table-column prop="operId" label="日志ID" width="80" align="center" />
          <el-table-column prop="title" label="系统模块" width="100" align="center" show-overflow-tooltip />
          <el-table-column prop="businessType" label="业务类型" width="90" align="center">
            <template #default="scope">
              <el-tag :type="getBusinessTypeColor(scope.row.businessType)" size="small">
                {{ getBusinessTypeText(scope.row.businessType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="requestMethod" label="请求方式" width="90" align="center" />
          <el-table-column prop="operName" label="操作人员" min-width="90" align="center" show-overflow-tooltip />
          <el-table-column prop="operIp" label="操作IP" min-width="130" align="center" show-overflow-tooltip />
          <el-table-column prop="operLocation" label="操作地点" min-width="100" align="center" show-overflow-tooltip />
          <el-table-column prop="status" label="操作状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 0 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operTime" label="操作时间" width="170" align="center" />
          <el-table-column label="操作" width="130" align="center" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-button type="primary" size="small" link @click="handleDetail(scope.row)">
                  <el-icon><View /></el-icon>详情
                </el-button>
                <el-button type="danger" size="small" link @click="handleDelete(scope.row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detailData.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detailData.operIp }}</el-descriptions-item>
        <el-descriptions-item label="操作地点">{{ detailData.operLocation }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detailData.status === 0 ? 'success' : 'danger'" size="small">
            {{ detailData.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detailData.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detailData.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ detailData.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="param-content">{{ detailData.operParam }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <div class="param-content">{{ detailData.jsonResult }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="detailData.errorMsg">
          <div class="error-content">{{ detailData.errorMsg }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, View } from '@element-plus/icons-vue'
import { getOperLogList, deleteOperLog, cleanOperLog } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const detailData = ref({})

const searchForm = reactive({ title: '', operName: '', businessType: null, status: null })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const businessTypeMap = { 0: '其它', 1: '新增', 2: '修改', 3: '删除', 4: '授权', 5: '导出', 6: '导入' }
const businessTypeColorMap = { 0: 'info', 1: 'success', 2: 'primary', 3: 'danger', 4: 'warning', 5: '', 6: '' }

const getBusinessTypeText = (type) => businessTypeMap[type] || '未知'
const getBusinessTypeColor = (type) => businessTypeColorMap[type] || 'info'

const getList = async () => {
  loading.value = true
  try {
    const res = await getOperLogList({
      pageNum: pagination.page,
      pageSize: pagination.size,
      title: searchForm.title,
      operName: searchForm.operName,
      businessType: searchForm.businessType,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.rows || []
      pagination.total = res.total || 0
    }
  } catch (error) {
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; getList() }
const resetSearch = () => { Object.assign(searchForm, { title: '', operName: '', businessType: null, status: null }); handleSearch() }

const handleDetail = (row) => { detailData.value = row; detailVisible.value = true }

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该条记录吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteOperLog(row.operId)
    if (res.code === 200) { ElMessage.success('删除成功'); getList() } else { ElMessage.error(res.msg || '删除失败') }
  }).catch(() => {})
}

const handleClean = () => {
  ElMessageBox.confirm('确定要清空所有操作日志吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await cleanOperLog()
    if (res.code === 200) { ElMessage.success('清空成功'); getList() } else { ElMessage.error(res.msg || '清空失败') }
  }).catch(() => {})
}

const handleSizeChange = (size) => { pagination.size = size; getList() }
const handleCurrentChange = (page) => { pagination.page = page; getList() }

onMounted(() => getList())
</script>

<style lang="scss" scoped>
.operation-log {
  padding: 20px;

  .card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .search-form {
    margin-bottom: 20px;
    padding: 18px 20px;
    background: #f8f9fa;
    border-radius: 8px;

    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 16px;
    }

    :deep(.el-form-item__label) {
      font-weight: 500;
    }

    :deep(.el-button) {
      .el-icon {
        margin-right: 4px;
      }
    }
  }

  .table-container {
    :deep(.el-table) {
      border-radius: 8px;
      overflow: hidden;

      .el-table__header-wrapper {
        th {
          background: #f5f7fa !important;
        }
      }

      .el-table__row {
        &:hover > td {
          background-color: #f5f7fa;
        }
      }

      .el-button--small {
        padding: 4px 8px;
      }
    }

    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;

      :deep(.el-pagination) {
        .el-pagination__total {
          margin-right: 16px;
        }

        .el-pagination__sizes {
          margin-right: 16px;
        }
      }
    }
  }
}

.param-content {
  max-height: 150px;
  overflow-y: auto;
  word-break: break-all;
  white-space: pre-wrap;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}

.error-content {
  max-height: 150px;
  overflow-y: auto;
  word-break: break-all;
  white-space: pre-wrap;
  font-size: 12px;
  color: #f56c6c;
  background: #fef0f0;
  padding: 8px;
  border-radius: 4px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}
</style>
