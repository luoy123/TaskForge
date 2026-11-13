<template>
  <div class="page" v-loading="loading">
    <div class="toolbar">
      <el-button type="primary" :loading="loading" @click="loadAll">刷新</el-button>
      <span class="hint">CPU 使用率需要采样 1 秒，刷新会稍慢</span>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="card">
          <template #header>CPU</template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="核心数">{{ show(cpu.cpuNum) }}</el-descriptions-item>
            <el-descriptions-item label="用户使用率">{{ percent(cpu.used) }}</el-descriptions-item>
            <el-descriptions-item label="系统使用率">{{ percent(cpu.sys) }}</el-descriptions-item>
            <el-descriptions-item label="IO 等待">{{ percent(cpu.wait) }}</el-descriptions-item>
            <el-descriptions-item label="空闲率" :span="2">{{ percent(cpu.free) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="card">
          <template #header>内存</template>
          <el-table :data="memRows" border size="small">
            <el-table-column prop="label" label="属性" width="90" />
            <el-table-column label="物理内存">
              <template #default="{ row }">{{ row.mem }}</template>
            </el-table-column>
            <el-table-column label="JVM">
              <template #default="{ row }">{{ row.jvm }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="card">
          <template #header>服务器</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="操作系统">{{ show(sys.osName) }} / {{ show(sys.osArch) }}</el-descriptions-item>
            <el-descriptions-item label="主机名">{{ show(sys.computerName) }}</el-descriptions-item>
            <el-descriptions-item label="IP">{{ show(sys.computerIp) }}</el-descriptions-item>
            <el-descriptions-item label="项目路径">{{ show(sys.userDir) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="card">
          <template #header>
            数据库连接池（Hikari）
          </template>
          <el-descriptions v-if="pool" :column="2" border>
            <el-descriptions-item label="活跃">{{ show(pool.active) }}</el-descriptions-item>
            <el-descriptions-item label="空闲">{{ show(pool.idle) }}</el-descriptions-item>
            <el-descriptions-item label="当前总数">{{ show(pool.total) }}</el-descriptions-item>
            <el-descriptions-item label="最大连接数">{{ show(pool.max) }}</el-descriptions-item>
            <el-descriptions-item label="排队等待" :span="2">
              <el-tag :type="pool.waiting > 0 ? 'danger' : 'success'" size="small">{{ show(pool.waiting) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无连接池数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="card">
      <template #header>JVM</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="名称">{{ show(jvm.name) }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ show(jvm.version) }}</el-descriptions-item>
        <el-descriptions-item label="启动时间">{{ show(jvm.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="运行时长">{{ show(jvm.runTime) }}</el-descriptions-item>
        <el-descriptions-item label="安装路径" :span="2">{{ show(jvm.home) }}</el-descriptions-item>
        <el-descriptions-item label="启动参数" :span="2">
          <span class="args">{{ show(jvm.inputArgs) }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="card">
      <template #header>磁盘</template>
      <el-table :data="sysFiles" border size="small">
        <el-table-column prop="dirName" label="盘符路径" min-width="120" />
        <el-table-column prop="sysTypeName" label="文件系统" width="100" />
        <el-table-column prop="typeName" label="名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="total" label="总大小" width="110" />
        <el-table-column prop="free" label="可用" width="110" />
        <el-table-column prop="used" label="已用" width="110" />
        <el-table-column label="使用率" width="200">
          <template #default="{ row }">
            <el-progress :percentage="clampPercent(row.usage)" :status="row.usage >= 90 ? 'exception' : ''" />
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无磁盘信息" :image-size="60" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getPool, getServer } from '@/api/monitor/server'

const loading = ref(false)
const server = ref({})
const pool = ref(null)

const cpu = computed(() => server.value.cpu || {})
const mem = computed(() => server.value.mem || {})
const jvm = computed(() => server.value.jvm || {})
const sys = computed(() => server.value.sys || {})
const sysFiles = computed(() => server.value.sysFiles || [])

function show(v) {
  return v == null || v === '' ? '—' : v
}

function percent(v) {
  return v == null || v === '' ? '—' : `${v}%`
}

function withUnit(v, unit) {
  return v == null || v === '' ? '—' : `${v} ${unit}`
}

function clampPercent(v) {
  const n = Number(v)
  if (Number.isNaN(n)) return 0
  return Math.min(100, Math.max(0, n))
}

/** 物理内存单位 GB，JVM 单位 MB（后端 getter 已换算） */
const memRows = computed(() => [
  { label: '总量', mem: withUnit(mem.value.total, 'GB'), jvm: withUnit(jvm.value.total, 'MB') },
  { label: '已用', mem: withUnit(mem.value.used, 'GB'), jvm: withUnit(jvm.value.used, 'MB') },
  { label: '剩余', mem: withUnit(mem.value.free, 'GB'), jvm: withUnit(jvm.value.free, 'MB') },
  { label: '使用率', mem: percent(mem.value.usage), jvm: percent(jvm.value.usage) },
])

async function loadAll() {
  loading.value = true
  try {
    const [serverRes, poolRes] = await Promise.allSettled([getServer(), getPool()])
    if (serverRes.status === 'fulfilled') {
      server.value = serverRes.value.data.data || {}
    }
    pool.value = poolRes.status === 'fulfilled' ? poolRes.value.data.data || null : null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.page {
  padding: 8px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.hint {
  color: #94a3b8;
  font-size: 13px;
}
.card {
  margin-bottom: 16px;
}
.card :deep(.el-descriptions__label) {
  white-space: nowrap;
}
.args {
  word-break: break-all;
  font-family: ui-monospace, Consolas, monospace;
  font-size: 12px;
}
</style>
