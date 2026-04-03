<template>
  <div class="card">
    <div class="card-title">系统登录</div>
    <div class="card-body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.card {
  width: 500px;
  height: 412px;
  border-radius: 15px;
  background: var(--login-card-gradient);
  box-shadow: var(--login-card-shadow);
  backdrop-filter: blur(1px);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 37px 0 0;
  box-sizing: border-box;
}

.card-title {
  width: 129px;
  height: 34px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 26px;
  line-height: 34px;
  text-align: center;
  color: var(--login-title-color);
  margin-bottom: 36px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 34px;
  align-items: flex-start;
}
</style>
