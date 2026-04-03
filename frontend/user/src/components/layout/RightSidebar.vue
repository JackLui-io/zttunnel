<template>
  <aside class="right-sidebar">
    <!-- 待处理 -->
    <div class="card card-pending">
      <div class="card-pending-header">
        <img src="/page1/wait.png" alt="" class="card-pending-header-bg" width="445" height="47.77" />
        <span class="card-pending-header-title">待处理</span>
      </div>
      <div class="card-pending-body">
        <div class="card-pending-grid">
          <div
            v-for="(item, i) in pendingData"
            :key="i"
            class="card-pending-item"
          >
            <div class="card-pending-item-left">
              <span class="card-pending-item-label">{{ item.label }}</span>
              <span class="card-pending-item-num" :class="{ 'card-pending-item-num--alert': item.isAlert }">
                {{ item.num }}
              </span>
            </div>
            <div class="card-pending-item-icon-wrap">
              <img
                :src="item.isAlert ? '/page1/alert-icon.svg' : '/page1/sound.svg'"
                alt=""
                class="card-pending-item-icon"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 消息通知 -->
    <div class="card card-message">
      <div class="card-message-header">
        <img src="/page1/news.png" alt="" class="card-message-header-bg" width="445" height="47.77" />
        <span class="card-message-header-title">消息通知</span>
      </div>
      <div class="card-message-body">
        <div class="card-message-inner">
          <div
            v-for="(cat, ci) in messageCategories"
            :key="ci"
            class="card-message-category-block"
            :class="{ 'is-collapsed': cat.collapsed }"
          >
            <div
              class="card-message-category-bar"
              :class="{ 'is-collapsed': cat.collapsed }"
              role="button"
              tabindex="0"
              @click="toggleCategory(ci)"
              @keydown.enter.space.prevent="toggleCategory(ci)"
            >
              {{ cat.name }}({{ cat.count }})
            </div>
            <div v-show="!cat.collapsed" class="card-message-category-items">
              <div
                v-for="(msg, mi) in (cat.items || [])"
                :key="mi"
                class="card-message-item"
              >
                <div class="card-message-item-time">{{ msg.time }}</div>
                <div class="card-message-item-text">{{ msg.text }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPendingCounts, getMessageNotifications } from '@/api/analyze'
import type { DashboardPendingCountsVo, DashboardMessageCategoryVo } from '@/api/analyze'

/** 待处理项（label, num, isAlert） */
interface PendingItem {
  label: string
  num: number
  isAlert: boolean
}

const pendingData = ref<PendingItem[]>([
  { label: '系统通知', num: 0, isAlert: false },
  { label: '操作通知', num: 0, isAlert: false },
  { label: '实时警报', num: 0, isAlert: true },
  { label: '设备告警', num: 0, isAlert: true },
])

const messageCategories = ref<Array<DashboardMessageCategoryVo & { collapsed?: boolean }>>([])

function toggleCategory(idx: number) {
  const cat = messageCategories.value[idx]
  if (cat) cat.collapsed = !cat.collapsed
}

async function fetchData() {
  try {
    const [countsRes, msgRes] = await Promise.all([
      getPendingCounts(),
      getMessageNotifications(10),
    ])
    const counts = countsRes ?? {}
    pendingData.value = [
      { label: '系统通知', num: counts.systemNotice ?? 0, isAlert: false },
      { label: '操作通知', num: counts.operationNotice ?? 0, isAlert: false },
      { label: '实时警报', num: counts.realtimeAlarm ?? 0, isAlert: true },
      { label: '设备告警', num: counts.deviceAlarm ?? 0, isAlert: true },
    ]
    messageCategories.value = (Array.isArray(msgRes) ? msgRes : []).map((c) => ({
      ...c,
      name: c.name ?? '',
      count: c.count ?? 0,
      items: c.items ?? [],
      collapsed: true,
    }))
  } catch (e) {
    console.error('[RightSidebar] 获取待处理/消息通知失败:', e)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.right-sidebar {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 445px;
  flex-shrink: 0;
}

.card-pending,
.card-message {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: visible;
  background: transparent;
  border: none;
}

.card-pending-header,
.card-message-header {
  position: relative;
  width: 445px;
  height: 47.77px;
  flex-shrink: 0;
}
.card-pending-header-bg,
.card-message-header-bg {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
}
.card-pending-header-title,
.card-message-header-title {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 800;
  font-size: 16px;
  line-height: 22px;
  color: #0f7f62;
}

.card-pending-body,
.card-message-body {
  background: linear-gradient(180deg, #f0fbef 36.54%, #e0e9da 100%);
  box-shadow: 0 2px 4px rgba(85, 160, 92, 0.6);
  border-radius: 0 0 15px 15px;
  overflow: hidden;
}

.card-pending-body {
  width: 445px;
  height: 239px;
  display: flex;
  flex-direction: column;
}

.card-pending-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 24px;
  padding: 24px;
  flex: 1;
  min-height: 0;
}

.card-pending-item {
  min-height: 0;
  background: linear-gradient(
    180deg,
    rgba(145, 202, 167, 0.4) 0%,
    rgba(187, 223, 159, 0.4) 50%,
    rgba(222, 240, 214, 0.4) 100%
  );
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}
.card-pending-item-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.card-pending-item-label {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #5f646b;
}
.card-pending-item-num {
  font-size: 20px;
  font-weight: 700;
  color: #37474f;
}
.card-pending-item-num--alert {
  color: #e65100;
}
.card-pending-item-icon-wrap {
  flex-shrink: 0;
  width: 35px;
  height: 35px;
  background: #53aa7d;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-pending-item-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.card-message-body {
  width: 445px;
  height: 513px;
  overflow-y: auto;
}

.card-message-inner {
  padding: 19px 30px 20px;
}

.card-message-category-block {
  margin-top: 16px;
}
.card-message-category-block:first-child {
  margin-top: 0;
}

.card-message-category-bar {
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 18px;
  color: #757985;
  margin-bottom: 8px;
  cursor: pointer;
}
.card-message-category-bar.is-collapsed {
  box-sizing: border-box;
  width: 386px;
  height: 42px;
  margin-bottom: 0;
  margin-top: 16px;
  padding: 0;
  background: #def0d6;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #0f7f62;
}
.card-message-category-block:first-child .card-message-category-bar.is-collapsed {
  margin-top: 0;
}
.card-message-category-block.is-collapsed .card-message-category-items {
  display: none;
}

.card-message-item {
  box-sizing: border-box;
  width: 386px;
  min-height: 75px;
  padding: 10px 12px;
  border: 1px solid #b8e0c8;
  border-radius: 10px;
  margin-bottom: 8px;
}
.card-message-item-time {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  line-height: 18px;
  color: #757985;
  margin-bottom: 4px;
}
.card-message-item-text {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  line-height: 20px;
  color: #37474f;
}

@media (max-width: 768px) {
  .right-sidebar {
    width: 100%;
    gap: 12px;
  }

  .card-pending-header,
  .card-message-header {
    width: 100%;
    height: 44px;
  }

  .card-pending-body {
    width: 100%;
    height: auto;
    min-height: 180px;
  }

  .card-pending-grid {
    gap: 12px;
    padding: 12px;
  }

  .card-pending-item {
    min-height: 70px;
  }

  .card-message-body {
    width: 100%;
    height: auto;
    max-height: 400px;
  }

  .card-message-item {
    width: 100%;
    max-width: 100%;
  }

  .card-message-category-bar.is-collapsed {
    width: 100%;
    max-width: 100%;
  }
}
</style>
