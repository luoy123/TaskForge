<template>
  <div class="page" v-loading="loading">
    <div class="toolbar">
      <el-button type="primary" :loading="loading" @click="getData">刷新</el-button>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip"
      title="这里是 Redis 概况和业务 key 的数量统计，不是在线用户列表；查看或强退在线用户请去「在线用户」页。"
    />

    <el-card shadow="never" class="card">
      <template #header>Redis 基本信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="版本">{{ show(info.redis_version) }}</el-descriptions-item>
        <el-descriptions-item label="已用内存">{{ show(info.used_memory_human) }}</el-descriptions-item>
        <el-descriptions-item label="客户端连接数">{{ show(info.connected_clients) }}</el-descriptions-item>
        <el-descriptions-item label="运行天数">{{ show(info.uptime_in_days) }}</el-descriptions-item>
        <el-descriptions-item label="Key 总数" :span="2">{{ show(dbSize) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="card">
      <template #header>业务前缀 Key 数量</template>
      <el-table :data="keyStats" border size="small">
        <el-table-column prop="name" label="用途" min-width="140" />
        <el-table-column prop="prefix" label="前缀" min-width="160">
          <template #default="{ row }">
            <code>{{ row.prefix }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="120" />
        <template #empty>
          <el-empty description="暂无数据" :image-size="60" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getCache } from '@/api/monitor/cache'

const loading = ref(false)
const info = ref({})
const dbSize = ref(null)
const keyStats = ref([])

function show(v) {
  return v == null || v === '' ? '—' : v
}

async function getData() {
  loading.value = true
  try {
    const { data: res } = await getCache()
    const d = res.data || {}
    info.value = d.info || {}
    dbSize.value = d.dbSize ?? null
    keyStats.value = d.keyStats || []
  } catch {
    /* 拦截器已提示错误，页面保持空态 */
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.page {
  padding: 8px;
}
.toolbar {
  margin-bottom: 12px;
}
.tip {
  margin-bottom: 12px;
}
.card {
  margin-bottom: 16px;
}
.card :deep(.el-descriptions__label) {
  white-space: nowrap;
}
</style>
