<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="query.userName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="登录 IP">
        <el-input v-model="query.ipaddr" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button
          v-hasPermi="'monitor:logininfor:remove'"
          type="danger"
          :disabled="!selectedIds.length"
          @click="handleDelete()"
        >删除</el-button>
        <el-button v-hasPermi="'monitor:logininfor:remove'" type="warning" @click="handleClean">清空</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border @selection-change="onSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="infoId" label="ID" width="80" />
      <el-table-column prop="userName" label="用户名" width="120" />
      <el-table-column prop="ipaddr" label="IP" width="140" />
      <el-table-column prop="loginLocation" label="地点" min-width="120" show-overflow-tooltip />
      <el-table-column prop="browser" label="浏览器" width="110" show-overflow-tooltip />
      <el-table-column prop="os" label="系统" width="110" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="msg" label="消息" min-width="140" show-overflow-tooltip />
      <el-table-column prop="loginTime" label="登录时间" width="170" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="'monitor:logininfor:remove'"
            link
            type="danger"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="getList"
        @current-change="getList"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanLogininfor, delLogininfor, listLogininfor } from '@/api/monitor/logininfor'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  ipaddr: '',
  status: '',
})

function onSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.infoId)
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.userName = ''
  query.ipaddr = ''
  query.status = ''
  handleQuery()
}

/** ========== 【学员填写 F07】登录日志列表 ========== */
async function getList() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.userName) params.userName = query.userName
    if (query.ipaddr) params.ipaddr = query.ipaddr
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data: res } = await listLogininfor(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

/** ========== 【学员填写 F07】删除选中 / 单行 ========== */
async function handleDelete(row) {
  const ids = row ? [row.infoId] : selectedIds.value
  if (!ids.length) {
    ElMessage.warning('请先选择记录')
    return
  }
  await ElMessageBox.confirm(`确认删除 ${ids.length} 条登录日志？`, '提示', { type: 'warning' })
  await delLogininfor(ids.join(','))
  ElMessage.success('已删除')
  await getList()
}

/** ========== 【学员填写 F07】清空 ========== */
async function handleClean() {
  await ElMessageBox.confirm('确认清空全部登录日志？此操作不可恢复', '提示', { type: 'warning' })
  await cleanLogininfor()
  ElMessage.success('已清空')
  await getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
}
</style>
