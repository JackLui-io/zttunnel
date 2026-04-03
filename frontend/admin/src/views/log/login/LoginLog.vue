<template>
  <div class="login-log">
    <div class="card">
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="用户名称">
            <el-input v-model="searchForm.userName" placeholder="请输入用户名称" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="登录IP">
            <el-input v-model="searchForm.ipaddr" placeholder="请输入登录IP" clearable style="width: 180px;" />
          </el-form-item>
          <el-form-item label="登录状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
              <el-option label="成功" value="0" />
              <el-option label="失败" value="1" />
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
          @selection-change="handleSelectionChange"
          border
          stripe
          :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: 'bold', padding: '12px 0' }"
          :cell-style="{ padding: '10px 8px' }"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="infoId" label="访问ID" width="80" align="center" />
          <el-table-column prop="userName" label="用户名称" min-width="100" align="center" show-overflow-tooltip />
          <el-table-column prop="ipaddr" label="登录IP" min-width="130" align="center" show-overflow-tooltip />
          <el-table-column prop="loginLocation" label="登录地点" min-width="100" align="center" show-overflow-tooltip />
          <el-table-column prop="browser" label="浏览器" min-width="90" align="center" show-overflow-tooltip />
          <el-table-column prop="os" label="操作系统" min-width="90" align="center" show-overflow-tooltip />
          <el-table-column prop="status" label="登录状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
                {{ scope.row.status === '0' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="msg" label="提示消息" min-width="100" align="center" show-overflow-tooltip />
          <el-table-column prop="loginTime" label="登录时间" width="170" align="center" />
          <el-table-column label="操作" width="80" align="center" fixed="right">
            <template #default="scope">
              <el-button type="danger" size="small" link @click="handleDelete(scope.row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { getLogininforList, deleteLogininfor, cleanLogininfor } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])

const searchForm = reactive({ userName: '', ipaddr: '', status: '' })

const pagination = reactive({ page: 1, size: 10, total: 0 })

const getList = async () => {
  loading.value = true
  try {
    const res = await getLogininforList({
      pageNum: pagination.page,
      pageSize: pagination.size,
      userName: searchForm.userName,
      ipaddr: searchForm.ipaddr,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.rows || []
      pagination.total = res.total || 0
    }
  } catch (error) {
    ElMessage.error('获取登录日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; getList() }
const resetSearch = () => { Object.assign(searchForm, { userName: '', ipaddr: '', status: '' }); handleSearch() }

const handleSelectionChange = (rows) => { selectedRows.value = rows }

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该条记录吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteLogininfor(row.infoId)
    if (res.code === 200) { ElMessage.success('删除成功'); getList() } else { ElMessage.error(res.msg || '删除失败') }
  }).catch(() => {})
}

const handleClean = () => {
  ElMessageBox.confirm('确定要清空所有登录日志吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await cleanLogininfor()
    if (res.code === 200) { ElMessage.success('清空成功'); getList() } else { ElMessage.error(res.msg || '清空失败') }
  }).catch(() => {})
}

const handleSizeChange = (size) => { pagination.size = size; getList() }
const handleCurrentChange = (page) => { pagination.page = page; getList() }

onMounted(() => getList())
</script>

<style lang="scss" scoped>
.login-log {
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
</style>
