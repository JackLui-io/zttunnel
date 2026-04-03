<script setup lang="ts">
interface Props {
  modelValue: string
  type?: string
  placeholder?: string
  autocomplete?: string
  icon?: 'user' | 'lock'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  autocomplete: ''
})

const emit = defineEmits<{ (e: 'update:modelValue', value: string): void }>()

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}
</script>

<template>
  <label class="field">
    <span v-if="props.icon" class="icon" aria-hidden="true">
      <svg v-if="props.icon === 'user'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
          d="M12 12.5C9.238 12.5 7 10.262 7 7.5C7 4.738 9.238 2.5 12 2.5C14.762 2.5 17 4.738 17 7.5C17 10.262 14.762 12.5 12 12.5Z"
          stroke="currentColor"
          stroke-width="1.6"
        />
        <path
          d="M4.5 20.5C4.5 17.463 7.187 15 12 15C16.813 15 19.5 17.463 19.5 20.5"
          stroke="currentColor"
          stroke-width="1.6"
          stroke-linecap="round"
        />
      </svg>
      <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path
          d="M6.5 10.5V8.5C6.5 5.462 8.962 3 12 3C15.038 3 17.5 5.462 17.5 8.5V10.5"
          stroke="currentColor"
          stroke-width="1.6"
        />
        <rect x="4.5" y="10.5" width="15" height="10" rx="2" stroke="currentColor" stroke-width="1.6" />
        <circle cx="12" cy="15.5" r="1.6" fill="currentColor" />
      </svg>
    </span>
    <input
      class="input"
      :type="props.type"
      :placeholder="props.placeholder"
      :autocomplete="props.autocomplete"
      :value="props.modelValue"
      @input="handleInput"
      required
    />
  </label>
</template>

<style scoped>
.field {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 379px;
  height: 46px;
  background: var(--login-input-bg);
  border-radius: 5px;
  padding-left: 16px;
  box-sizing: border-box;
}

.input {
  border: none;
  outline: none;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 400;
  font-size: 16px;
  line-height: 20px;
  width: calc(100% - 32px);
  color: #333333;
  background: transparent;
}

.input::placeholder {
  font-size: 16px;
  color: var(--login-input-placeholder);
}

.icon {
  width: 16px;
  height: 16px;
  color: var(--login-icon-color);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.icon svg {
  width: 100%;
  height: 100%;
}
</style>
