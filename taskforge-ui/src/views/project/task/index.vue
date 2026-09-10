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
          <el-button type="success" @click="openDialog()">新增任务</el-button>
          <el-button @click="getList">刷新</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="taskName" label="任务名称" min-width="160" />
        <el-table-column prop="statusName" label="状态" width="100" />
        <el-table-column prop="priorityName" label="优先级" width="100" />
        <el-table-column prop="nickName" label="执行人" width="120" />
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addTask, delTask, editTask, listTask } from '@/api/project/task'

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

const query = reactive({
  pageNum: 1,
  pageSize: 10,
})

const form = reactive({
  taskId: undefined,
  taskName: '',
  description: '',
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
}

function resetForm() {
  form.taskId = undefined
  form.taskName = ''
  form.description = ''
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑任务'
    form.taskId = row.taskId
    form.taskName = row.taskName
    form.description = row.description || ''
  } else {
    dialogTitle.value = '新增任务'
  }
  dialogVisible.value = true
}

/** ========== 【学员填写 T-C】任务列表 ==========
 * 若无 projectId 直接 return
 * await listTask({ projectId: projectId.value, pageNum, pageSize })
 * records / total 同用户页
 */
async function getList() {
  // TODO T-C
  if(!projectId.value){
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try{
   const { data: res } = await listTask({ projectId: projectId.value, pageNum: query.pageNum, pageSize: query.pageSize })
   tableData.value = res.data.records || []
   total.value = res.data.total || 0
  }finally{
    loading.value = false
  }
}

/** ========== 【学员填写 T-D】新增/编辑 ==========
 * 新增：addTask({ projectId: projectId.value, taskName, description })
 * 编辑：editTask({ taskId, taskName, description })
 */
async function submitForm() {
  // TODO T-D
  await formRef.value.validate()
  submitLoading.value = true
  try{
    if(form.taskId){
      await editTask({ taskId: form.taskId, taskName: form.taskName, description: form.description })
      ElMessage.success('修改成功')
    }else{
      await addTask({ projectId: projectId.value, taskName: form.taskName, description: form.description })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  }finally{
    submitLoading.value = false
  }
}

/** ========== 【学员填写 T-E】删除 ==========
 * delTask({ taskId: row.taskId })
 */
async function handleDelete(row) {
  // TODO T-E
  await ElMessageBox.confirm(`确认删除任务「${row.taskName}」？`, '提示', { type: 'warning' })
  await delTask({taskId: row.taskId})
  ElMessage.success('删除成功')
  getList()
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
</style>
