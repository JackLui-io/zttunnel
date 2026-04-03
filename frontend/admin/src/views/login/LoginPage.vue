<template>
  <div class="login-page">
    <div class="login-stage">
      <div class="title-block">
        <div class="title-cn">隧道低碳后台管理系统</div>
      </div>

      <LoginCard>
        <form class="form" @submit.prevent="handleLogin">
          <LoginInputField
            v-model="form.username"
            placeholder="请输入用户名"
            autocomplete="username"
            icon="user"
          />
          <LoginInputField
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            icon="lock"
          />

          <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

          <label class="remember">
            <input v-model="form.remember" class="checkbox" type="checkbox" />
            <span class="remember-text">记住密码</span>
          </label>

          <LoginPrimaryButton :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </LoginPrimaryButton>
        </form>
      </LoginCard>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import LoginCard from '@/components/login/LoginCard.vue'
import LoginInputField from '@/components/login/LoginInputField.vue'
import LoginPrimaryButton from '@/components/login/LoginPrimaryButton.vue'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const errorMsg = ref('')
const loading = ref(false)

function updateScale() {
  const designWidth = 1920
  const designHeight = 1080
  const baseScale = Math.min(window.innerWidth / designWidth, window.innerHeight / designHeight)
  const minScale = window.innerWidth < 768 ? 0.4 : 0.7
  const base = Math.max(minScale, baseScale)
  const scale = Math.min(1, base * 1.1)
  document.documentElement.style.setProperty('--login-scale', String(scale))
}

onMounted(() => {
  updateScale()
  window.addEventListener('resize', updateScale)
  if (localStorage.getItem('token')) {
    router.push('/')
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateScale)
})

async function handleLogin() {
  errorMsg.value = ''
  if (!form.username.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!form.password) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    userStore.removeToken()
    userStore.clearUserInfo()
    await userStore.login({
      username: form.username.trim(),
      password: form.password
    })
    ElMessage.success('登录成功')
    window.location.href = '/'
  } catch (err) {
    const msg = err?.message || '登录失败，请检查用户名和密码'
    errorMsg.value = msg
  } finally {
    loading.value = false
  }
}
</script>

<style>
/* 登录页主标题：青鸟华光简大标宋（public/fonts） */
@font-face {
  font-family: 'QingNiaoHuaGuangJianDaBiaoSong';
  src: url('/fonts/QingNiaoHuaGuangJianDaBiaoSong-2.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
  font-display: swap;
}
</style>

<style scoped>
.login-page {
  --login-bg-image: url('/bg5.svg');
  --login-bg-color: #0f7f62;
  --login-title-color: #ffffff;
  --login-card-shadow: 0 0 10px rgba(188, 188, 188, 0.3);
  --login-input-bg: #ffffff;
  --login-input-placeholder: #898989;
  --login-icon-color: #949494;
  --login-button-bg: #167428;
  --login-button-text: #ffffff;
  --login-card-gradient: linear-gradient(127.07deg, rgba(64, 114, 58, 0.65) 4.71%, rgba(255, 255, 255, 0.65) 97.14%);

  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  background: var(--login-bg-color) var(--login-bg-image) center center no-repeat;
  background-size: cover;
}

.login-stage {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 1920px;
  height: 1080px;
  transform: translate(-50%, -50%) scale(var(--login-scale, 1));
  transform-origin: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 38px;
}

.title-block {
  min-width: 320px;
  padding: 0 16px;
  text-align: center;
  color: var(--login-title-color);
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.25);
}

.title-cn {
  margin: 0 auto;
  font-family: 'QingNiaoHuaGuangJianDaBiaoSong', 'Microsoft YaHei', serif;
  font-weight: 400;
  font-size: 48px;
  line-height: 1.15;
  white-space: nowrap;
}

.form {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 34px;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  cursor: pointer;
  user-select: none;
}

.checkbox {
  width: 17px;
  height: 16px;
  background: var(--login-input-bg);
  border-radius: 4px;
  display: inline-block;
  margin: 0;
  accent-color: var(--login-input-bg);
}

.remember-text {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 16px;
  line-height: 18px;
  color: var(--login-title-color);
}

.error-msg {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: #e65100;
  margin-top: -10px;
}
</style>
