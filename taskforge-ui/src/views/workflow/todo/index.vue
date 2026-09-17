<template>
  <div class="page">
    <div class="head">
      <span class="title">待办任务</span>
      <el-button @click="getList">刷新</el-button>
      <el-button
        v-hasPermi="'workflow:process:deploy'"
        type="warning"
        plain
        :loading="deployLoading"
        @click="handleDeploy"
      >部署内置流程</el-button>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint"
      title="办理人须与发起时选的 approver（用户 id）一致；超级管理员也要选自己的 userId 才能在待办看到。"
    />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="taskName" label="节点" min-width="120" />
      <el-table-column prop="businessKey" label="业务主键" min-width="160" show-overflow-tooltip />
      <el-table-column prop="procInsId" label="流程实例" min-width="160" show-overflow-tooltip />
      <el-table-column prop="assignee" label="办理人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="'workflow:task:complete'"
            link
            type="success"
            @click="openHandle(row, 'complete')"
          >同意</el-button>
          <el-button
            v-hasPermi="'workflow:task:reject'"
            link
            type="danger"
            @click="openHandle(row, 'reject')"
          >驳回</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无待办。可从项目/任务列表发起审批后刷新。" />
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" destroy-on-close>
      <el-form label-width="70px">
        <el-form-item label="节点">{{ current?.taskName }}</el-form-item>
        <el-form-item label="业务">{{ current?.businessKey }}</el-form-item>
        <el-form-item label="意见">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="可选"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          :type="action === 'reject' ? 'danger' : 'primary'"
          :loading="submitLoading"
          @click="submitHandle"
        >确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deployBuiltin, todoList } from '@/api/workflow/process'
import { completeTask, rejectTask } from '@/api/workflow/task'

const loading = ref(false)
const deployLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const comment = ref('')
const action = ref('complete')
const current = ref(null)

async function getList() {
  loading.value = true
  try {
    const { data: res } = await todoList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleDeploy() {
  await ElMessageBox.confirm('部署内置 task_approve BPMN？可重复调用（会升版本）。', '提示', {
    type: 'warning',
  })
  deployLoading.value = true
  try {
    const { data: res } = await deployBuiltin()
    ElMessage.success(`部署成功：${res.data || ''}`)
  } finally {
    deployLoading.value = false
  }
}

function openHandle(row, act) {
  current.value = row
  action.value = act
  comment.value = ''
  dialogTitle.value = act === 'reject' ? '驳回' : '同意办理'
  dialogVisible.value = true
}

async function submitHandle() {
  if (!current.value?.taskId) return
  submitLoading.value = true
  try {
    const body = { taskId: current.value.taskId, comment: comment.value || undefined }
    if (action.value === 'reject') {
      await rejectTask(body)
      ElMessage.success('已驳回')
    } else {
      await completeTask(body)
      ElMessage.success('已同意')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
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
.head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.hint {
  margin-bottom: 0;
}
</style>
