<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="任务名称">
        <el-input v-model="query.jobName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="任务组">
        <el-input v-model="query.jobGroup" clearable placeholder="精确匹配" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="暂停" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'monitor:job:add'" type="success" @click="openDialog()">新增</el-button>
        <el-button v-hasPermi="'monitor:job:query'" @click="openLog()">调度日志</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="jobId" label="ID" width="70" />
      <el-table-column prop="jobName" label="任务名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="jobGroup" label="任务组" width="110" />
      <el-table-column prop="invokeTarget" label="调用目标" min-width="220" show-overflow-tooltip />
      <el-table-column prop="cronExpression" label="cron" min-width="140" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status"
            active-value="0"
            inactive-value="1"
            :disabled="!canChangeStatus"
            @change="(val) => handleStatus(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'monitor:job:changeStatus'" link type="primary" @click="handleRun(row)">执行</el-button>
          <el-button v-hasPermi="'monitor:job:edit'" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-hasPermi="'monitor:job:query'" link @click="openLog(row)">日志</el-button>
          <el-button v-hasPermi="'monitor:job:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无定时任务" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.jobId ? '修改任务' : '新增任务'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" />
        </el-form-item>
        <el-form-item label="任务组" prop="jobGroup">
          <el-input v-model="form.jobGroup" placeholder="DEFAULT" />
        </el-form-item>
        <el-form-item label="调用目标" prop="invokeTarget">
          <el-input v-model="form.invokeTarget" placeholder="beanName.methodName()" />
        </el-form-item>
        <el-form-item label="cron" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="0 0 1 * * ?" />
        </el-form-item>
        <el-form-item label="错过策略" prop="misfirePolicy">
          <el-select v-model="form.misfirePolicy" style="width: 100%">
            <el-option label="默认" value="0" />
            <el-option label="立即执行" value="1" />
            <el-option label="执行一次" value="2" />
            <el-option label="放弃执行" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="并发" prop="concurrent">
          <el-radio-group v-model="form.concurrent">
            <el-radio value="0">允许</el-radio>
            <el-radio value="1">禁止</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="logVisible" :title="logTitle" size="70%">
      <div class="toolbar">
        <el-button v-hasPermi="'monitor:job:remove'" type="danger" @click="handleCleanLog">清空</el-button>
        <el-button @click="loadLogs">刷新</el-button>
      </div>
      <el-table v-loading="logLoading" :data="logData" border>
        <el-table-column prop="jobLogId" label="日志ID" width="90" />
        <el-table-column prop="jobName" label="任务名称" min-width="120" />
        <el-table-column prop="jobGroup" label="任务组" width="100" />
        <el-table-column prop="invokeTarget" label="调用目标" min-width="180" show-overflow-tooltip />
        <el-table-column prop="jobMessage" label="日志" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="'monitor:job:remove'" link type="danger" @click="handleDeleteLog(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addJob, changeJobStatus, delJob, listJob, runJob, updateJob } from '@/api/monitor/job'
import { cleanJobLog, delJobLog, listJobLog } from '@/api/monitor/jobLog'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const canChangeStatus = computed(() => {
  const perms = userStore.permissions || []
  return perms.includes('*:*:*') || perms.includes('monitor:job:changeStatus')
})

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const query = reactive({ jobName: '', jobGroup: '', status: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  jobId: undefined,
  jobName: '',
  jobGroup: 'DEFAULT',
  invokeTarget: '',
  cronExpression: '',
  misfirePolicy: '3',
  concurrent: '1',
  remark: '',
})
const rules = {
  jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  jobGroup: [{ required: true, message: '请输入任务组', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入 cron', trigger: 'blur' }],
}

const logVisible = ref(false)
const logLoading = ref(false)
const logData = ref([])
const logQuery = reactive({ jobName: '', jobGroup: '' })
const logTitle = computed(() => (logQuery.jobName ? `调度日志 · ${logQuery.jobName}` : '调度日志'))

async function getList() {
  loading.value = true
  try {
    const params = {}
    if (query.jobName) params.jobName = query.jobName
    if (query.jobGroup) params.jobGroup = query.jobGroup
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data: res } = await listJob(params)
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  getList()
}

function resetQuery() {
  query.jobName = ''
  query.jobGroup = ''
  query.status = ''
  getList()
}

function resetForm() {
  Object.assign(form, {
    jobId: undefined,
    jobName: '',
    jobGroup: 'DEFAULT',
    invokeTarget: '',
    cronExpression: '',
    misfirePolicy: '3',
    concurrent: '1',
    remark: '',
  })
}

function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      jobId: row.jobId,
      jobName: row.jobName,
      jobGroup: row.jobGroup,
      invokeTarget: row.invokeTarget,
      cronExpression: row.cronExpression,
      misfirePolicy: row.misfirePolicy || '3',
      concurrent: row.concurrent || '1',
      remark: row.remark || '',
    })
  }
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.jobId) {
      await updateJob({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addJob({ ...form })
      ElMessage.success('新增成功（默认暂停）')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

async function handleStatus(row, status) {
  const text = status === '0' ? '启用' : '暂停'
  try {
    await ElMessageBox.confirm(`确认${text}任务「${row.jobName}」？`, '提示', { type: 'warning' })
    await changeJobStatus({ jobId: row.jobId, status })
    ElMessage.success(`${text}成功`)
    await getList()
  } catch {
    /* 取消或失败时保持原开关 */
  }
}

async function handleRun(row) {
  await ElMessageBox.confirm(`确认立即执行「${row.jobName}」？`, '提示', { type: 'warning' })
  await runJob({ jobId: row.jobId })
  ElMessage.success('已提交执行，稍后可在日志中查看结果')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除任务「${row.jobName}」？`, '提示', { type: 'warning' })
  await delJob(row.jobId)
  ElMessage.success('已删除')
  await getList()
}

async function loadLogs() {
  logLoading.value = true
  try {
    const params = {}
    if (logQuery.jobName) params.jobName = logQuery.jobName
    if (logQuery.jobGroup) params.jobGroup = logQuery.jobGroup
    const { data: res } = await listJobLog(params)
    logData.value = res.data || []
  } finally {
    logLoading.value = false
  }
}

function openLog(row) {
  logQuery.jobName = row?.jobName || ''
  logQuery.jobGroup = row?.jobGroup || ''
  logVisible.value = true
  loadLogs()
}

async function handleDeleteLog(row) {
  await ElMessageBox.confirm('确认删除这条日志？', '提示', { type: 'warning' })
  await delJobLog(row.jobLogId)
  ElMessage.success('已删除')
  await loadLogs()
}

async function handleCleanLog() {
  await ElMessageBox.confirm('确认清空全部调度日志？', '提示', { type: 'warning' })
  await cleanJobLog()
  ElMessage.success('已清空')
  await loadLogs()
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
