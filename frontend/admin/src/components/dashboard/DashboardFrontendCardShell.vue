<template>
  <div class="fe-card">
    <div class="fe-card-header">
      <span class="fe-card-header-title">{{ title }}</span>
    </div>
    <div class="fe-card-body">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true }
})
</script>

<style scoped>
/* 白底卡片：标题区与内容区均为白色 */
.fe-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  min-height: 0;
  margin-bottom: 20px;
  background: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.fe-card-header {
  position: relative;
  height: 49px;
  flex-shrink: 0;
  background: #ffffff;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  align-items: center;
}

.fe-card-header-title {
  padding: 0 20px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 16px;
  line-height: 22px;
  color: #303133;
}

.fe-card-body {
  flex: 1;
  min-height: 0;
  background: #ffffff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
