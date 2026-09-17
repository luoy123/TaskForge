<template>
  <div class="home">
    <h2>TaskForge 控制台</h2>
    <p class="sub">项目与任务概览</p>

    <el-row :gutter="16" class="stats">
      <el-col :xs="12" :sm="6" v-for="item in cards" :key="item.key">
        <div class="stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-descriptions :column="1" border class="meta">
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
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { projectStatistics } from '@/api/project/project'

const userStore = useUserStore()
const stats = ref({
  projectNum: 0,
  taskNum: 0,
  todayTaskNum: 0,
  overdueTaskNum: 0,
})

const cards = computed(() => [
  { key: 'project', label: '参与项目', value: stats.value.projectNum ?? 0 },
  { key: 'task', label: '任务总数', value: stats.value.taskNum ?? 0 },
  { key: 'today', label: '今日任务', value: stats.value.todayTaskNum ?? 0 },
  { key: 'overdue', label: '逾期任务', value: stats.value.overdueTaskNum ?? 0 },
])

/** 【学员填写 F04-8】projectStatistics() → 四个数字 */
async function loadStats() {
  try {
    const { data: res } = await projectStatistics()
    Object.assign(stats.value, res.data || {})
  } catch {
    /* 无权限时保持 0，不打断首页 */
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.home h2 {
  margin: 0 0 4px;
}
.sub {
  color: #64748b;
  margin: 0 0 20px;
}
.stats {
  margin-bottom: 24px;
}
.stat-card {
  background: linear-gradient(145deg, #f8fafc, #eef2ff);
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  margin-bottom: 12px;
}
.stat-label {
  font-size: 13px;
  color: #64748b;
}
.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}
.meta {
  max-width: 480px;
}
</style>
