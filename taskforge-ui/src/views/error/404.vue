<template>
  <div class="not-found">
    <el-result icon="warning" title="页面不存在或没有访问权限" :sub-title="route.fullPath">
      <template #extra>
        <el-button type="primary" @click="router.replace('/home')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
</script>

<style scoped>
.not-found {
  padding: 24px;
}
</style>
