<template>
  <div class="tunnel-company-page">
    <div class="card">
      <div class="toolbar">
        <el-button
          v-if="canAdd"
          type="primary"
          @click="openAddDialog"
        >
          新增
        </el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>

      <div class="table-container">
        <el-table
          :data="tableData"
          v-loading="loading"
          style="width: 100%"
          row-key="id"
        >
          <el-table-column prop="tunnelName" label="公司名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 0 ? '有效' : '失效' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="170">
            <template #default="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column v-if="canEdit" label="操作" width="112" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" plain @click="openEditDialog(scope.row)">
                编辑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="companyDialogTitle"
      width="440px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="公司名称" prop="tunnelName">
          <el-input
            v-model="form.tunnelName"
            placeholder="对应一级隧道名称（管理单位）"
            maxlength="128"
            show-word-limit
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCompanyForm">
          {{ isEditMode ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTunnelCompanyList, addTunnelCompany, updateTunnel } from '@/api/tunnel'
import { hasPermission } from '@/utils/permission'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  tunnelName: '',
  status: 0,
  tunnelMileage: null
})

const isEditMode = computed(() => form.id != null)
const companyDialogTitle = computed(() => (isEditMode.value ? '编辑公司' : '新增公司'))

const rules = {
  tunnelName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' }
  ]
}

const router = useRouter()

const canAdd = computed(() => hasPermission('system:tunnel:update'))
const canEdit = computed(() => hasPermission('system:tunnel:update'))

const formatTime = (val) => {
  if (!val) return '-'
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getTunnelCompanyList()
    if (res.code === 200 && Array.isArray(res.data)) {
      tableData.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  form.id = null
  form.tunnelName = ''
  form.status = 0
  form.tunnelMileage = null
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  if (!row?.id) return
  form.id = row.id
  form.tunnelName = row.tunnelName || ''
  form.status = row.status != null ? row.status : 0
  form.tunnelMileage = row.tunnelMileage ?? null
  dialogVisible.value = true
}

const resetForm = () => {
  form.id = null
  form.tunnelName = ''
  form.status = 0
  form.tunnelMileage = null
  formRef.value?.resetFields?.()
}

const submitCompanyForm = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const name = form.tunnelName.trim()
  if (!name) {
    ElMessage.warning('请输入公司名称')
    return
  }
  submitting.value = true
  try {
    if (isEditMode.value) {
      const res = await updateTunnel({
        id: form.id,
        tunnelName: name,
        parentId: 0,
        level: 1,
        tunnelMileage: form.tunnelMileage,
        status: form.status
      })
      if (res.code === 200) {
        ElMessage.success(res.msg || '保存成功')
        dialogVisible.value = false
        await loadList()
      }
      return
    }

    const res = await addTunnelCompany({ tunnelName: name })
    if (res.code === 200) {
      const newCompanyId = res.data != null ? res.data : null
      ElMessage.success('新增成功')
      dialogVisible.value = false
      await loadList()
      try {
        await ElMessageBox.confirm(
          '是否前往隧道列表，在该管理单位下添加高速公路？',
          '下一步',
          {
            type: 'success',
            confirmButtonText: '去隧道列表',
            cancelButtonText: '稍后',
            closeOnClickModal: false
          }
        )
        const q = newCompanyId != null ? { focusCompanyId: String(newCompanyId) } : {}
        router.push({ path: '/tunnel/list', query: q })
      } catch {
        /* 取消 */
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.tunnel-company-page {
  .card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }

  .toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-bottom: 16px;
  }

  .table-container {
    width: 100%;
  }
}
</style>
