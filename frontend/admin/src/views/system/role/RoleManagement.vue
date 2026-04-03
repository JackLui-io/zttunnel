<template>
  <div class="role-management">
    <div class="card">
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="角色名称">
            <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
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
            <el-button type="success" @click="handleAdd">新增角色</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-container">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="roleId" label="角色ID" width="80" />
          <el-table-column prop="roleName" label="角色名称" />
          <el-table-column prop="roleKey" label="权限字符" />
          <el-table-column prop="roleSort" label="显示顺序" width="100" />
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
              <el-tooltip
                :content="scope.row.roleId === 1 || scope.row.roleKey === 'admin' ? '超级管理员角色不可编辑' : '编辑角色信息'"
                placement="top"
              >
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEdit(scope.row)"
                  :disabled="scope.row.roleId === 1 || scope.row.roleKey === 'admin'"
                >
                  编辑
                </el-button>
              </el-tooltip>
              <el-tooltip
                :content="scope.row.roleId === 1 || scope.row.roleKey === 'admin' ? '超级管理员角色拥有所有权限，不可修改' : '配置角色权限'"
                placement="top"
              >
                <el-button
                  type="info"
                  size="small"
                  @click="handlePermission(scope.row)"
                  :disabled="scope.row.roleId === 1 || scope.row.roleKey === 'admin'"
                >
                  权限
                </el-button>
              </el-tooltip>
              <el-tooltip
                :content="scope.row.roleId === 1 || scope.row.roleKey === 'admin' ? '超级管理员角色不可删除' : '删除角色'"
                placement="top"
              >
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(scope.row)"
                  :disabled="scope.row.roleId === 1 || scope.row.roleKey === 'admin'"
                >
                  删除
                </el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="请输入权限字符" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog v-model="permissionDialogVisible" :title="permissionDialogTitle" width="680px">
      <div class="permission-dialog-content">
        <div class="permission-actions">
          <el-button size="small" @click="toggleExpand">
            {{ menuExpand ? '折叠' : '展开' }}
          </el-button>
          <el-button size="small" @click="toggleSelectAll">
            {{ menuNodeAll ? '全不选' : '全选' }}
          </el-button>
          <span class="permission-tip">选择父级时，子级自动选择/取消</span>
        </div>

        <div class="permission-tree-container">
          <el-tree
            ref="permissionTreeRef"
            :data="menuTreeData"
            show-checkbox
            node-key="id"
            :default-expand-all="menuExpand"
            :props="defaultProps"
            :check-strictly="false"
            empty-text="暂无数据"
            @check="handleNodeCheck"
            :key="treeKey"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getMenuTree, getRoleMenus, assignRolePermissions } from '@/api/system'


const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

// 权限配置相关
const permissionDialogVisible = ref(false)
const permissionDialogTitle = ref('')
const permissionTreeRef = ref(null)
const currentRole = ref(null)
const menuTreeData = ref([])
const defaultProps = {
  children: 'children',
  label: 'label'
}
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const treeKey = ref(0) // 用于强制重新渲染树组件

const searchForm = reactive({
  roleName: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  roleId: null,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: ''
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }]
}

const getRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList({
      pageNum: pagination.page,
      pageSize: pagination.size,
      roleName: searchForm.roleName,
      status: searchForm.status
    })
    if (res.code === 200) {
      tableData.value = res.rows || []
      pagination.total = res.total || 0
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  getRoles()
}

const resetSearch = () => {
  Object.assign(searchForm, { roleName: '', status: '' })
  handleSearch()
}

const resetForm = () => {
  Object.assign(form, {
    roleId: null,
    roleName: '',
    roleKey: '',
    roleSort: 0,
    status: '0',
    remark: ''
  })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增角色'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  // 检查是否为超级管理员角色
  if (row.roleId === 1 || row.roleKey === 'admin') {
    ElMessage.warning('超级管理员角色不可编辑')
    return
  }

  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '编辑角色'
  dialogVisible.value = true
}

const handlePermission = async (row) => {
  // 检查是否为超级管理员角色（roleId为1或roleKey为admin）
  if (row.roleId === 1 || row.roleKey === 'admin') {
    ElMessage.warning('超级管理员角色拥有所有权限，不可修改')
    return
  }

  currentRole.value = row
  permissionDialogTitle.value = `配置权限：${row.roleName}`

  // 重置状态
  menuExpand.value = false
  menuNodeAll.value = false

  try {
    // 获取角色菜单权限（包含菜单树和已选中的菜单）
    const roleMenuRes = await getRoleMenus(row.roleId)

    console.log('角色菜单响应:', roleMenuRes)

    if (roleMenuRes.code === 200) {
      // 后端返回的数据结构：{ menus: [...], checkedKeys: [...] }
      menuTreeData.value = roleMenuRes.data.menus || []
      const checkedKeys = roleMenuRes.data.checkedKeys || []
      
      console.log('菜单树数据:', menuTreeData.value)
      console.log('已选中的菜单ID:', checkedKeys)
      
      // 设置选中的节点
      nextTick(() => {
        if (permissionTreeRef.value) {
          permissionTreeRef.value.setCheckedKeys(checkedKeys, false)
        }
      })
    }

    permissionDialogVisible.value = true
  } catch (error) {
    console.error('获取权限数据失败:', error)
    ElMessage.error('获取权限数据失败')
  }
}

const handleDelete = (row) => {
  // 检查是否为超级管理员角色
  if (row.roleId === 1 || row.roleKey === 'admin') {
    ElMessage.warning('超级管理员角色不可删除')
    return
  }

  ElMessageBox.confirm(`确定要删除角色"${row.roleName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteRole(row.roleId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getRoles()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = form.roleId ? await updateRole(form) : await addRole(form)
        if (res.code === 200) {
          ElMessage.success(form.roleId ? '修改成功' : '新增成功')
          dialogVisible.value = false
          getRoles()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleSizeChange = (size) => {
  pagination.size = size
  getRoles()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getRoles()
}

// 切换展开/折叠
const toggleExpand = () => {
  menuExpand.value = !menuExpand.value

  // 保存当前选中状态
  const currentCheckedKeys = permissionTreeRef.value?.getCheckedKeys() || []
  const currentHalfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []

  // 强制重新渲染树组件以应用新的展开状态
  treeKey.value++

  // 恢复选中状态
  nextTick(() => {
    if (permissionTreeRef.value && (currentCheckedKeys.length > 0 || currentHalfCheckedKeys.length > 0)) {
      permissionTreeRef.value.setCheckedKeys([...currentCheckedKeys, ...currentHalfCheckedKeys])
    }
  })
}

// 切换全选/全不选
const toggleSelectAll = () => {
  menuNodeAll.value = !menuNodeAll.value

  nextTick(() => {
    if (!permissionTreeRef.value) {
      console.warn('权限树引用不存在')
      return
    }

    if (menuNodeAll.value) {
      // 全选：获取所有节点ID
      const allKeys = []
      const traverse = (nodes) => {
        nodes.forEach(node => {
          allKeys.push(node.id)
          if (node.children && node.children.length > 0) {
            traverse(node.children)
          }
        })
      }
      traverse(menuTreeData.value)
      permissionTreeRef.value.setCheckedKeys(allKeys)
    } else {
      // 全不选：清空所有选中
      permissionTreeRef.value.setCheckedKeys([])
    }
  })
}

// 节点选中状态改变时的处理
const handleNodeCheck = (data, checked) => {
  console.log('节点选中状态改变:', data, checked)
}

// 提交权限配置
const submitPermission = async () => {
  if (!currentRole.value) return

  try {
    const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []

    console.log('选中的菜单ID:', checkedKeys)
    console.log('半选中的菜单ID:', halfCheckedKeys)

    // 构建完整的角色数据，包含菜单权限
    // 注意：roleSort 需要转换为字符串类型
    const roleData = {
      roleId: currentRole.value.roleId,
      roleName: currentRole.value.roleName,
      roleKey: currentRole.value.roleKey,
      roleSort: String(currentRole.value.roleSort || 0),
      status: currentRole.value.status,
      menuIds: [...checkedKeys, ...halfCheckedKeys]
    }

    console.log('提交的角色数据:', roleData)

    const res = await assignRolePermissions(roleData)

    if (res.code === 200) {
      ElMessage.success('权限配置成功')
      permissionDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '权限配置失败')
    }
  } catch (error) {
    console.error('权限配置失败:', error)
    ElMessage.error('权限配置失败')
  }
}

onMounted(() => {
  getRoles()
})
</script>

<style lang="scss" scoped>
.role-management {
  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .table-container {
    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

.permission-dialog-content {
  .permission-actions {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;

    .permission-tip {
      color: #909399;
      font-size: 12px;
      margin-left: auto;
    }
  }

  .permission-tree-container {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 15px;

    :deep(.el-tree-node__content) {
      height: 32px;
      line-height: 32px;
    }

    :deep(.el-tree-node__label) {
      font-size: 14px;
    }
  }
}
</style>
