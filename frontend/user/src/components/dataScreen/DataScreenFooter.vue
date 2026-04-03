<template>
  <footer class="screen-footer">
    <div class="screen-footer-bg" />
    <div class="screen-footer-ex" />
  </footer>
</template>

<script setup lang="ts"></script>

<style scoped>
/* 底部装饰条：固定贴合视口底部，缩放时始终完全显示 */
.screen-footer {
  position: fixed;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 37px;
  z-index: 50;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  pointer-events: none;
}

@media (max-width: 768px) {
  .screen-footer {
    height: 28px;
  }
}

/* bottom.png 与 bottom-ex.png 叠放，contain 确保缩放时完整显示 */
.screen-footer-bg {
  position: absolute;
  width: min(745px, 100%);
  height: 37px;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  background: url('/dataScreen/images/bottom.png') no-repeat center bottom;
  background-size: contain;
}

.screen-footer-ex {
  position: absolute;
  width: min(745px, 100%);
  height: 32px;
  left: 50%;
  bottom: 2px;
  transform: translateX(-50%);
  background: url('/dataScreen/images/bottom-ex.png') no-repeat center bottom;
  background-size: contain;
  pointer-events: none;
}

@media (max-width: 768px) {
  .screen-footer-bg,
  .screen-footer-ex {
    height: 28px;
  }
}
</style>
