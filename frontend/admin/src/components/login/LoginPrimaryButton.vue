<script setup>
defineProps({
  disabled: { type: Boolean, default: false }
})
</script>

<template>
  <button class="btn" type="submit" :disabled="disabled">
    <slot />
  </button>
</template>

<style scoped>
.btn {
  width: 379.17px;
  height: 47px;
  border: none;
  border-radius: 5px;
  background: var(--login-button-bg);
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  font-size: 16px;
  line-height: 21px;
  color: var(--login-button-text);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.btn:active:not(:disabled) {
  transform: translateY(1px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
