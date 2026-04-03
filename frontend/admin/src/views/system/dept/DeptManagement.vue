<template>
  <div class="dept-management">
    <div class="card">
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="部门名称">
            <el-input v-model="searchForm.deptName" placeholder="请输入部门名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="success" @click="handleAdd">新增部门</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table :data="tableData" style="width: 100%" v-loading="loading" row-key="deptId" :tree-props="{ children: 'children' }" default-expand-all>
          <el-table-column prop="deptName" label="部门名称" width="200" />
          <el-table-column prop="orderNum" label="排序" width="80" />
          <el-table-column prop="leader" label="负责人" />
          <el-table-column prop="phone" label="联系电话" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
                {{ scope.row.status === '0' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="success" size="small" @click="handleAddChild(scope.row)">新增</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select v-model="form.parentId" :data="deptOptions" :props="{ value: 'deptId', label: 'deptName', children: 'children' }" check-strictly placeholder="选择上级部门" clearable />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
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
import { getDeptList, addDept, updateDept, deleteDept } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const deptOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const searchForm = reactive({ deptName: '', status: '' })

const form = reactive({
  deptId: null, parentId: 0, deptName: '', orderNum: 0, leader: '', phone: '', email: '', status: '0'
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  orderNum: [{ required: true, message: '请输入显示排序', trigger: 'blur' }]
}

const getDepts = async () => {
  loading.value = true
  try {
    const res = await getDeptList({ deptName: searchForm.deptName, status: searchForm.status })
    if (res.code === 200) {
      tableData.value = res.data || []
      deptOptions.value = [{ deptId: 0, deptName: '主类目', children: res.data || [] }]
    }
  } catch (error) {
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => getDepts()
const resetSearch = () => { Object.assign(searchForm, { deptName: '', status: '' }); handleSearch() }

const resetForm = () => {
  Object.assign(form, { deptId: null, parentId: 0, deptName: '', orderNum: 0, leader: '', phone: '', email: '', status: '0' })
}

const handleAdd = () => { resetForm(); dialogTitle.value = '新增部门'; dialogVisible.value = true }
const handleAddChild = (row) => { resetForm(); form.parentId = row.deptId; dialogTitle.value = '新增部门'; dialogVisible.value = true }
const handleEdit = (row) => { resetForm(); Object.assign(form, row); dialogTitle.value = '编辑部门'; dialogVisible.value = true }

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除部门"${row.deptName}"吗？`, '提示', { type: 'warning' }).then(async () => {
    const res = await deleteDept(row.deptId)
    if (res.code === 200) { ElMessage.success('删除成功'); getDepts() } else { ElMessage.error(res.msg || '删除失败') }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 确保 orderNum 是字符串类型（后端要求）
        const submitData = {
          ...form,
          orderNum: String(form.orderNum)
        }
        console.log('提交部门数据:', submitData)
        const res = form.deptId ? await updateDept(submitData) : await addDept(submitData)
        if (res.code === 200) { 
          ElMessage.success(form.deptId ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getDepts() 
        } else { 
          ElMessage.error(res.msg || '操作失败') 
        }
      } catch (error) {
        console.error('部门操作失败:', error)
        // 错误已在request.js中处理
      }
    }
  })
}

onMounted(() => getDepts())
</script>

<style lang="scss" scoped>
.dept-management {
  .search-form { margin-bottom: 20px; padding: 20px; background: #f8f9fa; border-radius: 8px; }
}
</style>
