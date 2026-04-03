<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// 应用根组件 - 不使用缩放，始终充满屏幕
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #050a15 0%, #0a1628 50%, #051020 100%);
}

#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}
</style>
