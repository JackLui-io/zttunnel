import request from '@/utils/request'

// ==================== 用户管理 ====================

// 获取用户列表 - 对接zttunnel后端 /system/user/list
export function getUserList(params) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}

// 获取用户详情；不传 userId 时请求 /system/user/，返回全部岗位与角色（与 RuoYi 新增/编辑前置一致）
export function getUser(userId) {
  const hasId =
    userId !== undefined && userId !== null && userId !== ''
  return request({
    url: hasId ? `/system/user/${userId}` : '/system/user/',
    method: 'get'
  })
}

// 新增用户 - 对接zttunnel后端 /system/user
export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

// 修改用户 - 对接zttunnel后端 /system/user
export function updateUser(data) {
  return request({
    url: '/system/user',
    method: 'put',
    data
  })
}

// 删除用户 - 对接zttunnel后端 /system/user/{userId}
export function deleteUser(userId) {
  return request({
    url: `/system/user/${userId}`,
    method: 'delete'
  })
}

// 重置密码 - 对接zttunnel后端 /system/user/resetPwd
export function resetUserPwd(userId, password) {
  return request({
    url: '/system/user/resetPwd',
    method: 'put',
    data: { userId, password }
  })
}

// 修改用户状态 - 对接zttunnel后端 /system/user/changeStatus
export function changeUserStatus(userId, status) {
  return request({
    url: '/system/user/changeStatus',
    method: 'put',
    data: { userId, status }
  })
}

// ==================== 角色管理 ====================

// 获取角色列表 - 对接zttunnel后端 /system/role/list
export function getRoleList(params) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params
  })
}

// 获取角色详情 - 对接zttunnel后端 /system/role/{roleId}
export function getRole(roleId) {
  return request({
    url: `/system/role/${roleId}`,
    method: 'get'
  })
}

// 新增角色 - 对接zttunnel后端 /system/role
export function addRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

// 修改角色 - 对接zttunnel后端 /system/role
export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

// 删除角色 - 对接zttunnel后端 /system/role/{roleId}
export function deleteRole(roleId) {
  return request({
    url: `/system/role/${roleId}`,
    method: 'delete'
  })
}

// 获取角色权限菜单 - 使用新的tunnel_menu表
export function getRoleMenus(roleId) {
  return request({
    url: `/tunnel/menu/roleMenuTreeselect/${roleId}`,
    method: 'get'
  })
}

// 分配角色菜单权限 - 使用新的tunnel_menu表
export function assignRolePermissions(data) {
  return request({
    url: '/tunnel/menu/saveRoleMenus',
    method: 'post',
    data: {
      roleId: data.roleId,
      menuIds: data.menuIds
    }
  })
}

// 获取权限菜单树 - 使用新的tunnel_menu表
export function getMenuTree() {
  return request({
    url: '/tunnel/menu/treeselect',
    method: 'get'
  })
}

// ==================== 部门管理 ====================

// 获取部门列表 - 对接zttunnel后端 /system/dept/list
export function getDeptList(params) {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params
  })
}

// 获取部门树形列表 - 对接zttunnel后端 /system/dept/treeselect
export function getDeptTreeSelect() {
  return request({
    url: '/system/dept/treeselect',
    method: 'get'
  })
}

// 获取部门详情 - 对接zttunnel后端 /system/dept/{deptId}
export function getDept(deptId) {
  return request({
    url: `/system/dept/${deptId}`,
    method: 'get'
  })
}

// 新增部门 - 对接zttunnel后端 /system/dept
export function addDept(data) {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

// 修改部门 - 对接zttunnel后端 /system/dept
export function updateDept(data) {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

// 删除部门 - 对接zttunnel后端 /system/dept/{deptId}
export function deleteDept(deptId) {
  return request({
    url: `/system/dept/${deptId}`,
    method: 'delete'
  })
}

// ==================== 登录日志 ====================

// 获取登录日志列表 - 对接zttunnel后端 /monitor/logininfor/list
export function getLogininforList(params) {
  return request({
    url: '/monitor/logininfor/list',
    method: 'get',
    params
  })
}

// 删除登录日志 - 对接zttunnel后端 /monitor/logininfor/{infoIds}
export function deleteLogininfor(infoIds) {
  return request({
    url: `/monitor/logininfor/${infoIds}`,
    method: 'delete'
  })
}

// 清空登录日志 - 对接zttunnel后端 /monitor/logininfor/clean
export function cleanLogininfor() {
  return request({
    url: '/monitor/logininfor/clean',
    method: 'delete'
  })
}

// ==================== 操作日志 ====================

// 获取操作日志列表 - 对接zttunnel后端 /monitor/operlog/list
export function getOperLogList(params) {
  return request({
    url: '/monitor/operlog/list',
    method: 'get',
    params
  })
}

// 删除操作日志 - 对接zttunnel后端 /monitor/operlog/{operIds}
export function deleteOperLog(operIds) {
  return request({
    url: `/monitor/operlog/${operIds}`,
    method: 'delete'
  })
}

// 清空操作日志 - 对接zttunnel后端 /monitor/operlog/clean
export function cleanOperLog() {
  return request({
    url: '/monitor/operlog/clean',
    method: 'delete'
  })
}

// ==================== 岗位管理 ====================

// 获取岗位列表 - 对接zttunnel后端 /system/post/list
export function getPostList(params) {
  return request({
    url: '/system/post/list',
    method: 'get',
    params
  })
}

// 获取岗位详情 - 对接zttunnel后端 /system/post/{postId}
export function getPost(postId) {
  return request({
    url: `/system/post/${postId}`,
    method: 'get'
  })
}

// 新增岗位 - 对接zttunnel后端 /system/post
export function addPost(data) {
  return request({
    url: '/system/post',
    method: 'post',
    data
  })
}

// 修改岗位 - 对接zttunnel后端 /system/post
export function updatePost(data) {
  return request({
    url: '/system/post',
    method: 'put',
    data
  })
}

// 删除岗位 - 对接zttunnel后端 /system/post/{postId}
export function deletePost(postId) {
  return request({
    url: `/system/post/${postId}`,
    method: 'delete'
  })
}

// ==================== 个人中心 ====================

// 修改密码 - 对接zttunnel后端 /system/user/profile/updatePwd
export function changePassword(oldPassword, newPassword) {
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    params: { oldPassword, newPassword }
  })
}

// 获取个人信息 - 对接zttunnel后端 /system/user/profile
export function getUserProfile() {
  return request({
    url: '/system/user/profile',
    method: 'get'
  })
}

// 修改个人信息 - 对接zttunnel后端 /system/user/profile
export function updateUserProfile(data) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}
