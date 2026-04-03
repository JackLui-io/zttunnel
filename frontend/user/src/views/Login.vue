<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import LoginCard from '../components/LoginCard.vue'
import InputField from '../components/InputField.vue'
import PrimaryButton from '../components/PrimaryButton.vue'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

interface LoginForm {
  username: string
  password: string
  remember: boolean
}

const form = ref<LoginForm>({
  username: '',
  password: '',
  remember: false
})

const errorMsg = ref('')
const loading = ref(false)

const updateScale = () => {
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
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateScale)
})

const handleLogin = async () => {
  errorMsg.value = ''
  if (!form.value.username.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!form.value.password) {
    errorMsg.value = '请输入密码'
    return
  }

  loading.value = true
  try {
    await login({
      username: form.value.username.trim(),
      password: form.value.password,
      code: '',
      uuid: ''
    })
    // 登录成功后先拉取用户信息再跳转，确保顶栏能正确显示用户名
    await authStore.fetchUserInfo()
    router.push('/dashboard')
  } catch (err: unknown) {
    errorMsg.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-stage">
      <div class="title-block">
        <div class="title-cn">隧道低碳智控平台</div>
        <div class="title-en">Tunnel Low-Carbon Intelligent Control Platform</div>
      </div>

      <LoginCard>
        <form class="form" @submit.prevent="handleLogin">
          <InputField v-model="form.username" placeholder="请输入用户名" autocomplete="username" icon="user"/>
          <InputField
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

          <PrimaryButton :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </PrimaryButton>
        </form>
      </LoginCard>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
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
  width: 472px;
  height: 80px;
  text-align: center;
  color: var(--login-title-color);
}

.title-cn {
  width: 460px;
  height: 54px;
  margin: 0 auto;
  font-family: 'QingNiaoHuaGuangJianDaBiaoSong', 'Microsoft YaHei', serif;
  font-weight: 400;
  font-size: 48px;
  line-height: 53px;
}

.title-en {
  margin-top: 5px;
  font-family: 'Hind', 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 18.5px;
  line-height: 30px;
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
