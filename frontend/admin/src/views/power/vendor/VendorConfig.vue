<template>
  <div class="vendor-config">
    <div class="card">
      <div class="header">
        <h3>电能表厂商配置</h3>
        <el-button type="primary" @click="handleAdd">新增厂商</el-button>
      </div>

      <div class="table-container">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="vendorId" label="厂商ID" width="100" />
          <el-table-column prop="vendorName" label="厂商名称" />
          <el-table-column prop="description" label="厂商描述" />
          <el-table-column prop="createdTime" label="创建时间" width="170">
            <template #default="scope">
              {{ formatTime(scope.row.createdTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="厂商ID" prop="vendorId">
          <el-input-number v-model="form.vendorId" :min="0" :max="15" :disabled="dialogTitle === '编辑厂商'" />
          <span class="tip">（0-15）</span>
        </el-form-item>
        <el-form-item label="厂商名称" prop="vendorName">
          <el-input v-model="form.vendorName" placeholder="请输入厂商名称" />
        </el-form-item>
        <el-form-item label="厂商描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入厂商描述" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPowerVendorList, addPowerVendor, updatePowerVendor, deletePowerVendor } from '@/api/device'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const form = reactive({ vendorId: null, vendorName: '', description: '' })

const rules = {
  vendorId: [{ required: true, message: '请输入厂商ID', trigger: 'blur' }],
  vendorName: [{ required: true, message: '请输入厂商名称', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getPowerVendorList()
    if (res.code === 200) {
      tableData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '获取厂商列表失败')
    }
  } catch (error) {
    console.error('获取厂商列表失败:', error)
    ElMessage.error('获取厂商列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { vendorId: null, vendorName: '', description: '' })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增厂商'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, {
    vendorId: row.vendorId,
    vendorName: row.vendorName,
    description: row.description
  })
  dialogTitle.value = '编辑厂商'
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除厂商"${row.vendorName}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deletePowerVendor(row.vendorId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除厂商失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const isEdit = dialogTitle.value === '编辑厂商'
        const api = isEdit ? updatePowerVendor : addPowerVendor
        const res = await api(form)

        if (res.code === 200) {
          ElMessage.success(isEdit ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getList()
        } else {
          ElMessage.error(res.msg || (isEdit ? '修改失败' : '新增失败'))
        }
      } catch (error) {
        console.error('保存厂商失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const timeStr = String(time)
  return timeStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => getList())
</script>

<style lang="scss" scoped>
.vendor-config {
  .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
  .tip { margin-left: 10px; color: #909399; font-size: 12px; }
}
</style>
