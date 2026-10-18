<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="系统模块">
        <el-input v-model="query.title" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="操作人员">
        <el-input v-model="query.operName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="正常" :value="0" />
          <el-option label="异常" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button
          v-hasPermi="'monitor:operlog:remove'"
          type="danger"
          :disabled="!selectedIds.length"
          @click="handleDelete()"
        >删除</el-button>
        <el-button v-hasPermi="'monitor:operlog:remove'" type="warning" @click="handleClean">清空</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border @selection-change="onSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="operId" label="ID" width="80" />
      <el-table-column prop="title" label="系统模块" min-width="120" show-overflow-tooltip />
      <el-table-column label="业务类型" width="100">
        <template #default="{ row }">
          {{ businessTypeLabel(row.businessType) }}
        </template>
      </el-table-column>
      <el-table-column prop="operName" label="操作人员" width="110" />
      <el-table-column prop="operIp" label="操作 IP" width="130" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '异常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operTime" label="操作时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'monitor:operlog:query'" link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-hasPermi="'monitor:operlog:remove'"
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

    <el-dialog v-model="detailVisible" title="操作日志详情" width="640px" destroy-on-close>
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="模块">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ businessTypeLabel(detail.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="请求 URL">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作 IP">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === 0 ? '正常' : '异常' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="pre">{{ detail.operParam || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回参数">
          <pre class="pre">{{ detail.jsonResult || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="错误信息">
          <pre class="pre error">{{ detail.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanOperlog, delOperlog, getOperlog, listOperlog } from '@/api/monitor/operlog'

const BUSINESS_TYPE_MAP = {
  0: '其它',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '授权',
  5: '导出',
  6: '导入',
  7: '强退',
  8: '生成代码',
  9: '清空数据',
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const detailVisible = ref(false)
const detail = ref(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  operName: '',
  status: undefined,
})

function businessTypeLabel(t) {
  return BUSINESS_TYPE_MAP[t] ?? t
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.operId)
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.title = ''
  query.operName = ''
  query.status = undefined
  handleQuery()
}

/** ========== 【学员填写 F07】操作日志列表 ========== */
async function getList() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.title) params.title = query.title
    if (query.operName) params.operName = query.operName
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data: res } = await listOperlog(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  const { data: res } = await getOperlog(row.operId)
  detail.value = res.data || row
  detailVisible.value = true
}

/** ========== 【学员填写 F07】删除 ========== */
async function handleDelete(row) {
  const ids = row ? [row.operId] : selectedIds.value
  if (!ids.length) {
    ElMessage.warning('请先选择记录')
    return
  }
  await ElMessageBox.confirm(`确认删除 ${ids.length} 条操作日志？`, '提示', { type: 'warning' })
  await delOperlog(ids.join(','))
  ElMessage.success('已删除')
  await getList()
}

/** ========== 【学员填写 F07】清空 ========== */
async function handleClean() {
  await ElMessageBox.confirm('确认清空全部操作日志？此操作不可恢复', '提示', { type: 'warning' })
  await cleanOperlog()
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
.pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  max-height: 180px;
  overflow: auto;
}
.pre.error {
  color: var(--el-color-danger);
}
</style>
