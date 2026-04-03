<template>
  <div class="user-profile">
    <div class="profile-header">
      <h2>个人中心</h2>
    </div>

    <el-row :gutter="20">
      <!-- 左侧头像区域 -->
      <el-col :span="8">
        <div class="avatar-section card">
          <div class="avatar-container">
            <el-avatar :size="120" :src="userInfo.avatar" class="user-avatar">
              <el-icon size="60"><User /></el-icon>
            </el-avatar>
            <div class="avatar-upload">
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="uploadAvatar"
                accept="image/*"
              >
                <el-button type="primary" size="small">
                  <el-icon><Upload /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
            </div>
          </div>
          <div class="user-basic-info">
            <h3>{{ userInfo.nickName || userInfo.userName }}</h3>
            <p class="user-role">{{ userInfo.roleName || '普通用户' }}</p>
            <p class="user-dept">{{ userInfo.deptName || '暂无部门' }}</p>
            <p class="user-post">{{ userInfo.postName || '暂无岗位' }}</p>
          </div>
        </div>
      </el-col>

      <!-- 右侧信息区域 -->
      <el-col :span="16">
        <div class="info-section card">
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="用户昵称" prop="nickName">
                      <el-input v-model="userForm.nickName" placeholder="请输入用户昵称" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号码" prop="phonenumber">
                      <el-input v-model="userForm.phonenumber" placeholder="请输入手机号码" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别" prop="sex">
                      <el-radio-group v-model="userForm.sex">
                        <el-radio value="0">男</el-radio>
                        <el-radio value="1">女</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item>
                  <el-button type="primary" @click="updateProfile">保存修改</el-button>
                  <el-button @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updatePassword">修改密码</el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserProfile, updateUserProfile, changePassword } from '@/api/system'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const activeTab = ref('basic')
const userFormRef = ref(null)
const passwordFormRef = ref(null)

const userInfo = ref({
  userId: null,
  userName: '',
  nickName: '',
  email: '',
  phonenumber: '',
  sex: '0',
  avatar: '',
  roleName: '',
  deptName: '',
  postName: ''
})

const userForm = reactive({
  userId: null,
  nickName: '',
  email: '',
  phonenumber: '',
  sex: '0'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phonenumber: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const loadUserInfo = async () => {
  try {
    // 使用个人中心专用接口，无需权限
    const res = await getUserProfile()
    if (res.code === 200 && res.data) {
      // 更新本地用户信息
      Object.assign(userInfo.value, res.data)
      Object.assign(userForm, {
        userId: res.data.userId,
        nickName: res.data.nickName,
        email: res.data.email,
        phonenumber: res.data.phonenumber,
        sex: res.data.sex || '0'
      })
      // 更新store
      userStore.setUserInfo(res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt500K = file.size / 1024 < 500 // 限制为500KB以内

  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return false
  }
  if (!isLt500K) {
    ElMessage.error('上传头像图片大小不能超过 500KB!')
    return false
  }
  return true
}

const uploadAvatar = async (options) => {
  const { file } = options

  // 压缩图片并转换为base64
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const img = new Image()

  img.onload = () => {
    // 设置压缩后的尺寸
    const maxWidth = 200
    const maxHeight = 200
    let { width, height } = img

    if (width > height) {
      if (width > maxWidth) {
        height = (height * maxWidth) / width
        width = maxWidth
      }
    } else {
      if (height > maxHeight) {
        width = (width * maxHeight) / height
        height = maxHeight
      }
    }

    canvas.width = width
    canvas.height = height

    // 绘制压缩后的图片
    ctx.drawImage(img, 0, 0, width, height)

    // 转换为base64，质量设为0.5以减小文件大小
    let compressedBase64 = canvas.toDataURL('image/jpeg', 0.5)

    // 检查压缩后的大小，如果仍然太大则进一步压缩
    if (compressedBase64.length > 60000) { // 约60KB
      compressedBase64 = canvas.toDataURL('image/jpeg', 0.3)
    }

    // 最终检查，如果还是太大则拒绝
    if (compressedBase64.length > 65000) { // 约65KB，确保在数据库字段限制内
      ElMessage.error('头像文件压缩后仍然过大，请选择更小的图片')
      return
    }

    userInfo.value.avatar = compressedBase64
    userStore.setUserInfo({ avatar: compressedBase64 })
    ElMessage.success(`头像上传成功（大小：${Math.round(compressedBase64.length/1024)}KB），点击保存修改将永久保存`)
  }

  img.src = URL.createObjectURL(file)
}

const updateProfile = async () => {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 使用个人中心专用接口，无需权限
        const updateData = { ...userForm }

        // 包含头像字段进行保存
        if (userInfo.value.avatar) {
          updateData.avatar = userInfo.value.avatar
        }

        const res = await updateUserProfile(updateData)
        if (res.code === 200) {
          ElMessage.success('个人信息修改成功')
          // 更新store中的用户信息
          userStore.setUserInfo({
            ...userForm,
            avatar: userInfo.value.avatar
          })
          loadUserInfo()
        } else {
          ElMessage.error(res.msg || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败')
      }
    }
  })
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
        if (res.code === 200) {
          ElMessage.success('密码修改成功')
          resetPasswordForm()
        } else {
          ElMessage.error(res.msg || '密码修改失败')
        }
      } catch (error) {
        ElMessage.error('密码修改失败')
      }
    }
  })
}

const resetForm = () => {
  loadUserInfo()
}

const resetPasswordForm = () => {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordFormRef.value?.resetFields()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.user-profile {
  .profile-header {
    margin-bottom: 20px;
    h2 { margin: 0; color: #303133; }
  }

  .card {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .avatar-section {
    text-align: center;

    .avatar-container {
      margin-bottom: 20px;

      .user-avatar {
        margin-bottom: 16px;
        border: 3px solid #f0f0f0;
      }
    }

    .user-basic-info {
      h3 { margin: 0 0 8px 0; color: #303133; }
      .user-role { color: #409EFF; margin: 4px 0; }
      .user-dept { color: #909399; margin: 4px 0; }
      .user-post { color: #67C23A; margin: 4px 0; }
    }
  }

  .info-section {
    min-height: 400px;
  }
}
</style>
