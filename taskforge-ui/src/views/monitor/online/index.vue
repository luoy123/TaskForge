<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="query.userName" clearable placeholder="精确匹配" />
      </el-form-item>
      <el-form-item label="登录 IP">
        <el-input v-model="query.ipaddr" clearable placeholder="精确匹配" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button @click="getList">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="tokenId" label="会话编号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="deptName" label="部门" width="120" show-overflow-tooltip />
      <el-table-column prop="ipaddr" label="IP" width="140" />
      <el-table-column prop="loginLocation" label="登录地点" min-width="120" show-overflow-tooltip />
      <el-table-column prop="browser" label="浏览器" width="120" show-overflow-tooltip />
      <el-table-column prop="os" label="系统" width="120" show-overflow-tooltip />
      <el-table-column label="登录时间" width="180">
        <template #default="{ row }">
          {{ formatLoginTime(row.loginTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="'monitor:online:forceLogout'"
            link
            type="danger"
            @click="handleForceLogout(row)"
          >强退</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无在线会话" />
      </template>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forceLogout, listOnline } from '@/api/monitor/online'

const loading = ref(false)
const tableData = ref([])
const query = reactive({
  ipaddr: '',
  userName: '',
})

function formatLoginTime(ms) {
  if (ms == null || ms === '') return '—'
  const n = Number(ms)
  if (Number.isNaN(n)) return String(ms)
  const d = new Date(n)
  const pad = (x) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function getList() {
  loading.value = true
  try {
    const params = {}
    if (query.ipaddr) params.ipaddr = query.ipaddr
    if (query.userName) params.userName = query.userName
    const { data: res } = await listOnline(params)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  getList()
}

function resetQuery() {
  query.ipaddr = ''
  query.userName = ''
  getList()
}

async function handleForceLogout(row) {
  if (!row?.tokenId) {
    ElMessage.warning('缺少会话编号 tokenId')
    return
  }
  await ElMessageBox.confirm(`确认强退用户「${row.userName || row.tokenId}」？`, '提示', {
    type: 'warning',
  })
  await forceLogout(row.tokenId)
  ElMessage.success('已强退')
  await getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.page {
  padding: 8px;
}
.toolbar {
  margin-bottom: 12px;
}
</style>
