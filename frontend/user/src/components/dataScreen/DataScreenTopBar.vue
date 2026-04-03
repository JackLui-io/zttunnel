<template>
  <header class="data-screen-top-bar">
    <img src="/page1/screen_head.png" alt="" class="data-screen-top-bar-img" />
  </header>
</template>

<script setup lang="ts"></script>

<style scoped>
.data-screen-top-bar {
  width: 100%;
  height: 100px;
  flex-shrink: 0;
}

.data-screen-top-bar-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: fill;
  object-position: center top;
}
</style>
