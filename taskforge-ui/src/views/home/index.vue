<template>
  <div class="home">
    <h2>TaskForge 控制台</h2>
    <p>前端骨架已就绪。下一步按 Sprint H 接菜单动态路由、系统管理页、项目任务页。</p>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="角色">
        {{ userStore.roles.join(', ') || '—' }}
      </el-descriptions-item>
      <el-descriptions-item label="权限数">
        {{ userStore.permissions.length }}
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.home h2 {
  margin-top: 0;
}
.home p {
  color: #64748b;
}
</style>
