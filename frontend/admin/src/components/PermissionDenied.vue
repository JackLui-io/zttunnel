<template>
  <div class="permission-denied">
    <el-result
      icon="warning"
      title="权限不足"
      :sub-title="message || '抱歉，您没有访问此页面的权限，请联系管理员'"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">回到首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  message: {
    type: String,
    default: ''
  }
})

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.permission-denied {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
</style>
