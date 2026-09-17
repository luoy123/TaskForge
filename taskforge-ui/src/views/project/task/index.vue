<template>
  <div class="page">
    <div class="head">
      <el-button link type="primary" @click="router.push('/project/list')">← 返回项目</el-button>
      <span class="title">任务列表</span>
      <span v-if="projectId" class="pid">projectId: {{ projectId }}</span>
    </div>

    <el-alert
      v-if="!projectId"
      type="warning"
      show-icon
      :closable="false"
      title="缺少 projectId。请从「项目列表」点某行的「任务」进入。"
    />

    <template v-else>
      <el-form :inline="true" class="toolbar" @submit.prevent>
        <el-form-item>
          <el-button v-hasPermi="'project:task:add'" type="success" @click="openDialog()">新增任务</el-button>
          <el-button @click="getList">刷新</el-button>
          <el-button
            v-hasPermi="'project:task:list'"
            :disabled="!selectedIds.length"
            @click="handleExportSelected"
          >导出选中</el-button>
          <el-button v-hasPermi="'project:task:list'" @click="handleExportAll">导出全部</el-button>
          <el-upload
            v-hasPermi="'project:task:import'"
            :show-file-list="false"
            :http-request="handleImport"
            accept=".xlsx,.xls"
            style="display: inline-block; margin-left: 8px"
          >
            <el-button type="warning">导入 Excel</el-button>
          </el-upload>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column prop="taskName" label="任务名称" min-width="160" />
        <el-table-column prop="statusName" label="状态" width="100" />
        <el-table-column prop="priorityName" label="优先级" width="100" />
        <el-table-column prop="nickName" label="执行人" width="120" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-hasPermi="'project:task:approve'"
              link
              type="success"
              @click="openApprove(row)"
            >发起审批</el-button>
            <el-button
              v-hasPermi="'project:file:queryFileList'"
              link
              type="primary"
              @click="openFiles(row)"
            >附件</el-button>
            <el-button
              v-hasPermi="'project:task:edit'"
              link
              type="primary"
              @click="openDialog(row)"
            >编辑</el-button>
            <el-button
              v-hasPermi="'project:task:delete'"
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
    </template>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item v-if="form.taskId" label="状态">
          <el-select v-model="form.status" clearable placeholder="不改则留空" style="width: 100%">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已逾期" :value="3" />
          </el-select>
          <div class="field-tip">审批中（approved=0）时改状态会被后端拒绝。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <FilePanel v-model="fileVisible" :biz-id="activeTaskId" file-type="task" />
    <TaskDetailDrawer
      v-model="detailVisible"
      :task-id="detailTaskId"
      :project-id="projectId"
      :task-name="detailTaskName"
    />
    <ApproveStartDialog
      v-model="approveVisible"
      kind="task"
      :biz-id="approveBizId"
      :biz-label="approveBizLabel"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addTask,
  delTask,
  editTask,
  exportAllTasks,
  exportTasks,
  importTasks,
  listTask,
} from '@/api/project/task'
import FilePanel from '@/components/project/FilePanel.vue'
import TaskDetailDrawer from '@/components/project/TaskDetailDrawer.vue'
import ApproveStartDialog from '@/components/workflow/ApproveStartDialog.vue'
import { saveBlobResponse } from '@/utils/download'

const route = useRoute()
const router = useRouter()

const projectId = computed(() => route.query.projectId)

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增任务')
const formRef = ref()
const selectedIds = ref([])
const fileVisible = ref(false)
const activeTaskId = ref('')
const detailVisible = ref(false)
const detailTaskId = ref('')
const detailTaskName = ref('')
const approveVisible = ref(false)
const approveBizId = ref('')
const approveBizLabel = ref('')

const query = reactive({
  pageNum: 1,
  pageSize: 10,
})

const form = reactive({
  taskId: undefined,
  taskName: '',
  description: '',
  status: undefined,
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
}

function resetForm() {
  form.taskId = undefined
  form.taskName = ''
  form.description = ''
  form.status = undefined
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑任务'
    form.taskId = row.taskId
    form.taskName = row.taskName
    form.description = row.description || ''
    form.status = row.status
  } else {
    dialogTitle.value = '新增任务'
  }
  dialogVisible.value = true
}

function openFiles(row) {
  activeTaskId.value = row.taskId
  fileVisible.value = true
}

function openDetail(row) {
  detailTaskId.value = row.taskId
  detailTaskName.value = row.taskName || ''
  detailVisible.value = true
}

function openApprove(row) {
  approveBizId.value = row.taskId
  approveBizLabel.value = row.taskName || row.taskId
  approveVisible.value = true
}

function onSelectionChange(rows) {
  selectedIds.value = (rows || []).map((r) => r.taskId)
}

async function getList() {
  if (!projectId.value) {
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const { data: res } = await listTask({
      projectId: projectId.value,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.taskId) {
      const payload = {
        taskId: form.taskId,
        taskName: form.taskName,
        description: form.description,
      }
      if (form.status !== undefined && form.status !== null && form.status !== '') {
        payload.status = form.status
      }
      await editTask(payload)
      ElMessage.success('修改成功')
    } else {
      await addTask({
        projectId: projectId.value,
        taskName: form.taskName,
        description: form.description,
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除任务「${row.taskName}」？`, '提示', { type: 'warning' })
  await delTask({ taskId: row.taskId })
  ElMessage.success('删除成功')
  getList()
}

/** 【学员填写 F04-6】exportTasks({ taskIds }) → saveBlobResponse */
async function handleExportSelected() {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先勾选任务')
    return
  }
  try {
    const response = await exportTasks({ taskIds: selectedIds.value })
    await saveBlobResponse(response, '任务导出.xlsx')
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error(e.message || '导出失败')
  }
}

/** 【学员填写 F04-6】exportAllTasks() */
async function handleExportAll() {
  try {
    const response = await exportAllTasks()
    await saveBlobResponse(response, '我的任务.xlsx')
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error(e.message || '导出失败')
  }
}

/** 【学员填写 F04-6】FormData + importTasks；成功后刷新列表 */
async function handleImport(option) {
  const formData = new FormData()
  formData.append('file', option.file)
  try {
    const { data: res } = await importTasks(formData)
    ElMessage.success(res.data || '导入成功')
    option.onSuccess?.()
    getList()
  } catch (e) {
    option.onError?.(e)
  }
}

watch(projectId, () => {
  query.pageNum = 1
  getList()
})

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
.head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.pid {
  color: #94a3b8;
  font-size: 13px;
}
.toolbar {
  margin-bottom: 0;
}
.pager {
  display: flex;
  justify-content: flex-end;
}
.field-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}
</style>
