<template>
  <header class="top-bar">
    <div class="top-bar-bg"></div>
    <button
      type="button"
      class="top-bar-logo-btn"
      aria-label="切换首页"
      @click="toggleHomePage"
    ></button>
    <button
      type="button"
      class="top-bar-menu-btn"
      aria-label="菜单"
      @click="$emit('toggleMenu')"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M3 12h18M3 6h18M3 18h18" stroke-linecap="round" />
      </svg>
    </button>
    <div class="top-bar-date">
      <span class="top-bar-date-str">{{ dateStr }}</span>
      <span class="top-bar-week">{{ weekStr }}</span>
      <span class="top-bar-time">{{ timeStr }}</span>
    </div>
    <button type="button" class="top-bar-btn" aria-label="数据大屏" @click="goDataScreen">
      <img src="/page1/Group106.svg" alt="" class="top-bar-btn-svg" width="144" height="42" />
    </button>
    <div class="top-bar-user" @click="toggleDropdown" ref="userRef">
      <div class="top-bar-user-icon" aria-hidden="true"></div>
      <span class="top-bar-username">{{ username }}</span>
      <div class="top-bar-dropdown" aria-hidden="true">
        <svg width="16" height="8" viewBox="0 0 16 8" fill="none">
          <path d="M1 1L8 7L15 1" stroke="#0F7F62" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </div>
      <Transition name="dropdown">
        <div v-show="showDropdown" class="top-bar-user-menu">
          <button type="button" class="top-bar-user-menu-item" @click.stop="handleLogout">退出登录</button>
        </div>
      </Transition>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineEmits<{ toggleMenu: [] }>()

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

function toggleHomePage() {
  if (route.path === '/dashboard') return
  router.push('/dashboard')
}

function goDataScreen() {
  if (route.path === '/dataScreen') return
  router.push('/dataScreen')
}
const dateStr = ref('')
const weekStr = ref('')
const timeStr = ref('')
const username = computed(() => authStore.user?.userName ?? authStore.user?.nickName ?? '用户')
const showDropdown = ref(false)
const userRef = ref<HTMLElement | null>(null)

function handleClickOutside(e: MouseEvent) {
  if (userRef.value && !userRef.value.contains(e.target as Node)) {
    showDropdown.value = false
  }
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

async function handleLogout() {
  showDropdown.value = false
  await authStore.clearAuth()
  router.push('/login')
}

const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

function pad(n: number) {
  return String(n).padStart(2, '0')
}

function tick() {
  const d = new Date()
  dateStr.value = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  weekStr.value = weekDays[d.getDay()] ?? '星期日'
  timeStr.value = `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

let timer: ReturnType<typeof setInterval>
onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  clearInterval(timer)
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.top-bar {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100px;
  z-index: 100;
  pointer-events: none;
}
.top-bar > * {
  pointer-events: auto;
}

.top-bar-menu-btn {
  display: none;
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  z-index: 10;
  color: rgb(15, 127, 98);
}

.top-bar-bg {
  position: absolute;
  width: 100%;
  height: 100px;
  left: 0;
  top: 0;
  background-image: url('/page1/head.png');
  background-size: 100% auto;
  background-repeat: no-repeat;
  background-position: center top;
  z-index: 0;
  pointer-events: none;
}

.top-bar-logo-btn {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  z-index: 1;
}

.top-bar-date {
  position: absolute;
  left: 26px;
  top: 43px;
  height: 21px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 16px;
  line-height: 21px;
  color: rgb(15, 127, 98);
}
.top-bar-date-str,
.top-bar-week,
.top-bar-time {
  font-weight: 700;
}

.top-bar-btn {
  position: absolute;
  right: 166px;
  top: 32px;
  width: 144px;
  height: 42px;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  z-index: 10;
}
.top-bar-btn-svg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.top-bar-user {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  z-index: 10;
}

.top-bar-user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  min-width: 120px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
}

.top-bar-user-menu-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: none;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  color: #37474f;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
}
.top-bar-user-menu-item:hover {
  background: #f5f5f5;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.top-bar-user-icon {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  background: rgb(15, 127, 98);
  border-radius: 50%;
  position: relative;
}
.top-bar-user-icon::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 4px;
  width: 6px;
  height: 6px;
  background: #edf2ea;
  border-radius: 50%;
}

.top-bar-username {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 14px;
  line-height: 18px;
  color: rgb(15, 127, 98);
  white-space: nowrap;
}

.top-bar-dropdown {
  flex-shrink: 0;
  width: 16px;
  height: 8px;
  color: rgb(15, 127, 98);
}
.top-bar-dropdown svg {
  display: block;
  width: 100%;
  height: 100%;
}

@media (max-width: 768px) {
  .top-bar {
    position: relative;
    height: 56px;
    display: flex;
    align-items: center;
    padding: 0 12px;
    background: linear-gradient(180deg, #f0fbef 0%, #e0e9da 100%);
    box-shadow: 0 2px 4px rgba(85, 160, 92, 0.3);
  }

  .top-bar-bg {
    display: none;
  }

  .top-bar-logo-btn {
    display: none;
  }

  .top-bar-menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .top-bar-date {
    position: relative;
    left: 0;
    top: 0;
    flex: 1;
    justify-content: center;
    font-size: 14px;
    gap: 6px;
  }

  .top-bar-week {
    display: none;
  }

  .top-bar-btn {
    display: none;
  }

  .top-bar-user {
    position: relative;
    right: 0;
    top: 0;
    transform: none;
  }
}
</style>
