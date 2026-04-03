<template>
  <div class="user-management">
    <div class="card">
      <!-- 搜索区域 -->
      <div class="search-form">
        <el-form :model="searchForm" :inline="true">
          <el-form-item label="用户名称">
            <el-input v-model="searchForm.userName" placeholder="请输入用户名称" clearable />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchForm.phonenumber" placeholder="请输入手机号" clearable />
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
            <el-button type="success" @click="handleAdd" v-permission="'system:user:add'">新增用户</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <div class="table-container">
        <el-table :data="tableData" style="width: 100%" v-loading="loading" @sort-change="handleSortChange">
          <el-table-column prop="userId" label="用户ID" width="80" sortable="custom" />
          <el-table-column prop="username" label="用户名称" min-width="110" />
          <el-table-column prop="nickName" label="用户昵称" min-width="100" />
          <el-table-column prop="unit" label="所属单位" min-width="120" show-overflow-tooltip />
          <el-table-column prop="roleName" label="角色" width="120">
            <template #default="scope">
              <el-tag
                :type="getRoleTagType(scope.row.roleName)"
                size="small"
                effect="light"
              >
                {{ scope.row.roleName || '暂无角色' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phonenumber" label="手机号码" width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
                {{ scope.row.status === '0' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-tooltip
                  :content="scope.row.userId === 1 || scope.row.username === 'admin' ? '超级管理员用户不可编辑' : '编辑用户信息'"
                  placement="top"
                >
                  <el-button
                    type="primary"
                    size="small"
                    @click="handleEdit(scope.row)"
                    class="action-btn"
                    v-permission="'system:user:edit'"
                    :disabled="scope.row.userId === 1 || scope.row.username === 'admin'"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  :content="scope.row.userId === 1 || scope.row.username === 'admin' ? '超级管理员用户密码不可重置' : '重置用户密码'"
                  placement="top"
                >
                  <el-button
                    type="warning"
                    size="small"
                    @click="resetPassword(scope.row)"
                    class="action-btn"
                    v-permission="'system:user:resetPwd'"
                    :disabled="scope.row.userId === 1 || scope.row.username === 'admin'"
                  >
                    <el-icon><Key /></el-icon>
                    重置密码
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  :content="scope.row.userId === 1 || scope.row.username === 'admin' ? '超级管理员用户不可删除' : '删除用户'"
                  placement="top"
                >
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleDelete(scope.row)"
                    :disabled="scope.row.userId === 1 || scope.row.username === 'admin'"
                    class="action-btn"
                    v-permission="'system:user:remove'"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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

    <!-- 用户表单：字段对齐 zt_tunnel_web admin/components/table2.vue -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      align-center
      destroy-on-close
      class="user-edit-dialog"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        class="user-dialog-form user-dialog-form--single-col"
        :model="userForm"
        :rules="userRules"
        label-width="120px"
        label-position="right"
      >
        <el-form-item label="用户名称" prop="userName">
          <el-input
            v-model="userForm.userName"
            maxlength="30"
            placeholder="请输入用户名称"
            clearable
            :disabled="isAdminRow"
          />
        </el-form-item>
        <el-form-item v-if="!userForm.userId" label="用户密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            maxlength="30"
            show-password
            placeholder="请输入用户密码"
            clearable
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName">
          <el-input
            v-model="userForm.nickName"
            maxlength="30"
            placeholder="请输入用户昵称"
            clearable
            :disabled="isAdminRow"
          />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input
            v-model="userForm.phonenumber"
            maxlength="11"
            placeholder="请输入手机号码"
            clearable
            :disabled="isAdminRow"
          />
        </el-form-item>
        <el-form-item label="所属单位" prop="unit">
          <el-input
            v-model="userForm.unit"
            maxlength="100"
            placeholder="请输入所属单位"
            clearable
            :disabled="isAdminRow"
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select
            v-model="userForm.roleId"
            placeholder="请选择角色"
            clearable
            filterable
            :disabled="isAdminRow"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属隧道" prop="tunnelPaths">
          <el-cascader
            v-model="userForm.tunnelPaths"
            :options="tunnelCascaderOptions"
            :props="tunnelCascaderProps"
            clearable
            filterable
            collapse-tags
            :show-all-levels="false"
            placeholder="请选择"
            :disabled="isAdminRow"
            @change="onTunnelCascaderChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList as getUserListApi, getUser, addUser, updateUser, deleteUser, resetUserPwd } from '@/api/system'
import { getTunnelTree, getAllTunnelTree } from '@/api/tunnel'

/** 隧道树 → el-cascader，与 zt_tunnel_web table2 一致（id / tunnelName / children） */
const tunnelCascaderProps = {
  multiple: true,
  expandTrigger: 'hover',
  value: 'id',
  label: 'tunnelName',
  children: 'children'
}

function findPathToTunnelNode(nodes, targetId, path = []) {
  if (!nodes?.length) return null
  const t = String(targetId)
  for (const n of nodes) {
    const cur = [...path, n.id]
    if (String(n.id) === t) return cur
    if (n.children?.length) {
      const sub = findPathToTunnelNode(n.children, targetId, cur)
      if (sub) return sub
    }
  }
  return null
}

function tunnelIdsToCascaderPaths(ids, tree) {
  if (!ids?.length || !tree?.length) return []
  const paths = []
  for (const id of ids) {
    const p = findPathToTunnelNode(tree, id)
    if (p) paths.push(p)
  }
  return paths
}

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userFormRef = ref()

// 搜索表单
const searchForm = reactive({
  userName: '',
  phonenumber: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 隧道级联选项
const tunnelCascaderOptions = ref([])

// 角色选项（GET /system/user/ 返回 roles）
const roleOptions = ref([])

// 用户表单（对齐 zt_tunnel_web admin/components/table2.vue）
const userForm = reactive({
  userId: null,
  userName: '',
  password: '',
  nickName: '',
  phonenumber: '',
  unit: '',
  roleId: null,
  tunnelPaths: [],
  tunnelIds: [],
  deptId: null,
  status: '0'
})

const isAdminRow = computed(
  () => userForm.userId === 1 || userForm.userName === 'admin'
)

// 表单校验（与 table2.vue rules 一致）
const userRules = computed(() => ({
  userName: [{ required: true, message: '请输入用户名称', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  phonenumber: [{ required: true, message: '请输入手机号码', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入所属单位', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  tunnelPaths: [
    {
      validator: (_rule, _v, cb) => {
        if (userForm.tunnelIds?.length) cb()
        else cb(new Error('请选择隧道'))
      },
      trigger: 'change'
    }
  ],
  password: userForm.userId
    ? []
    : [{ required: true, message: '请输入用户密码', trigger: 'blur' }]
}))

const loadTunnelTreeForDialog = async () => {
  try {
    const res = await getTunnelTree()
    if (res.code === 200 && res.data?.length) {
      tunnelCascaderOptions.value = res.data
      return
    }
  } catch (_) {
    /* 与 NoticeList 一致：权限树为空时再拉全量树 */
  }
  try {
    const res2 = await getAllTunnelTree()
    if (res2.code === 200 && res2.data) {
      tunnelCascaderOptions.value = res2.data
    } else {
      tunnelCascaderOptions.value = []
    }
  } catch (error) {
    console.error('获取隧道树失败:', error)
    tunnelCascaderOptions.value = []
  }
}

/** GET /system/user/ 或 /system/user/{id}，取 roles */
const loadRolesFromUserApi = async (userId) => {
  const res = await getUser(userId)
  if (res.code === 200) {
    roleOptions.value = res.roles || []
    return res
  }
  return null
}

const onTunnelCascaderChange = () => {
  const paths = userForm.tunnelPaths
  if (!paths || !Array.isArray(paths) || paths.length === 0) {
    userForm.tunnelIds = []
    return
  }
  const set = new Set()
  for (const p of paths) {
    if (Array.isArray(p)) {
      p.forEach((id) => {
        if (id !== undefined && id !== null && id !== '') set.add(id)
      })
    } else if (p !== undefined && p !== null && p !== '') {
      set.add(p)
    }
  }
  userForm.tunnelIds = [...set]
}



// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    const res = await getUserListApi({
      pageNum: pagination.page,
      pageSize: pagination.size,
      userName: searchForm.userName,
      phonenumber: searchForm.phonenumber,
      status: searchForm.status
    })
    if (res.code === 200) {
      // 处理用户数据，后端已经按ID升序排列
      const users = (res.rows || []).map(item => ({
        ...item,
        username: item.userName,
        unit: item.unit || '—',
        roleName: item.roleName || '暂无角色'
      }))

      tableData.value = users
      pagination.total = res.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  getUserList()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    userName: '',
    phonenumber: '',
    status: ''
  })
  handleSearch()
}

// 新增用户
const handleAdd = async () => {
  dialogTitle.value = '新增用户'
  resetForm()
  try {
    await loadTunnelTreeForDialog()
    const res = await loadRolesFromUserApi()
    if (!res) {
      ElMessage.error('加载角色失败')
      return
    }
    dialogVisible.value = true
  } catch (error) {
    console.error('打开新增失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 编辑用户
const handleEdit = async (row) => {
  if (row.userId === 1 || row.username === 'admin') {
    ElMessage.warning('超级管理员用户不可编辑')
    return
  }

  dialogTitle.value = '修改用户'
  resetForm()
  try {
    await loadTunnelTreeForDialog()
    const res = await loadRolesFromUserApi(row.userId)
    if (res && res.data) {
      const d = res.data
      const tunnelIdsRaw = d.tunnelIds
      const tunnelIds = Array.isArray(tunnelIdsRaw) ? [...tunnelIdsRaw] : []
      Object.assign(userForm, {
        userId: d.userId,
        userName: d.userName,
        nickName: d.nickName,
        phonenumber: d.phonenumber != null ? String(d.phonenumber) : '',
        unit: d.unit || '',
        roleId:
          Array.isArray(res.roleIds) && res.roleIds.length ? res.roleIds[0] : null,
        password: '',
        deptId: d.deptId ?? null,
        status: d.status != null ? String(d.status) : '0',
        tunnelIds,
        tunnelPaths: tunnelIdsToCascaderPaths(tunnelIds, tunnelCascaderOptions.value)
      })
    }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

// 删除用户
const handleDelete = (row) => {
  // 检查是否为超级管理员用户
  if (row.userId === 1 || row.username === 'admin') {
    ElMessage.warning('超级管理员用户不可删除')
    return
  }

  ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      console.log('删除用户 ID:', row.userId)
      const res = await deleteUser(row.userId)
      console.log('删除响应:', res)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getUserList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 重置密码
const resetPassword = (row) => {
  // 检查是否为超级管理员用户
  if (row.userId === 1 || row.username === 'admin') {
    ElMessage.warning('超级管理员用户密码不可重置')
    return
  }

  ElMessageBox.confirm(`确定要重置用户"${row.username}"的密码吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await resetUserPwd(row.userId, '123456')
      if (res.code === 200) {
        ElMessage.success('密码重置成功，新密码为：123456')
      } else {
        ElMessage.error(res.msg || '重置失败')
      }
    } catch (error) {
      ElMessage.error('密码重置失败')
    }
  }).catch(() => {})
}

// 提交表单（roleIds、tunnelIds 与 table2 submit 一致）
const handleSubmit = () => {
  onTunnelCascaderChange()
  userFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const data = {
        userId: userForm.userId,
        userName: userForm.userName,
        nickName: userForm.nickName,
        phonenumber: userForm.phonenumber || '',
        unit: userForm.unit || '',
        roleIds:
          userForm.roleId != null && userForm.roleId !== ''
            ? [userForm.roleId]
            : [],
        tunnelIds: userForm.tunnelIds || [],
        status: userForm.status || '0',
        sex: '0',
        postIds: []
      }
      if (userForm.deptId != null) {
        data.deptId = userForm.deptId
      }
      if (!userForm.userId) {
        data.password = userForm.password
      }

      let res
      if (userForm.userId) {
        res = await updateUser(data)
      } else {
        res = await addUser(data)
      }
      if (res.code === 200) {
        ElMessage.success(userForm.userId ? '修改操作成功。' : '新增操作成功。')
        dialogVisible.value = false
        getUserList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(userForm.userId ? '编辑失败' : '新增失败')
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    userId: null,
    userName: '',
    password: '',
    nickName: '',
    phonenumber: '',
    unit: '',
    roleId: null,
    tunnelPaths: [],
    tunnelIds: [],
    deptId: null,
    status: '0'
  })
  userFormRef.value?.resetFields()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  getUserList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  getUserList()
}

// 排序处理
const handleSortChange = ({ prop, order }) => {
  if (prop === 'userId') {
    // 根据排序方向重新排列数据
    if (order === 'ascending') {
      tableData.value.sort((a, b) => a.userId - b.userId)
    } else if (order === 'descending') {
      tableData.value.sort((a, b) => b.userId - a.userId)
    }
  }
}

// 获取角色标签类型
const getRoleTagType = (roleName) => {
  if (!roleName || roleName === '暂无角色') return 'info'
  if (roleName.includes('管理员') || roleName.includes('admin')) return 'danger'
  if (roleName.includes('系统')) return 'warning'
  return 'success'
}

// 初始化
onMounted(() => {
  getUserList()
})
</script>

<style lang="scss" scoped>
.user-management {
  .search-form {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
  }

  .table-container {
    .action-buttons {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
      justify-content: flex-start;
      align-items: center;

      .action-btn {
        margin: 0;
        padding: 6px 12px;
        font-size: 12px;
        border-radius: 4px;
        transition: all 0.3s ease;

        .el-icon {
          margin-right: 4px;
        }

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;

          &:hover {
            transform: none;
            box-shadow: none;
          }
        }
      }
    }

    .el-pagination {
      margin-top: 20px;
      text-align: right;
    }
  }
}

/* 新增/编辑用户弹窗：统一标签宽度，输入框/下拉与栅格列同宽 */
.user-dialog-form {
  width: 100%;
  box-sizing: border-box;

  :deep(.el-form-item) {
    margin-bottom: 14px;
  }

  :deep(.el-form-item__content) {
    flex: 1;
    min-width: 0;
  }

  :deep(.el-form-item__content > .el-input),
  :deep(.el-form-item__content > .el-select),
  :deep(.el-form-item__content > .el-tree-select),
  :deep(.el-form-item__content > .el-cascader) {
    width: 100%;
    max-width: 100%;
  }

  :deep(.el-select .el-select__wrapper) {
    width: 100%;
  }
}

.user-dialog-form--single-col {
  max-width: 100%;
}

:deep(.user-edit-dialog .el-dialog__body) {
  padding-top: 8px;
  padding-bottom: 4px;
}
</style>
