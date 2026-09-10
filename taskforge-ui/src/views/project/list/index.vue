<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="项目名称" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="projectName" label="项目名称" min-width="160" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="stageName" label="阶段" width="120" />
      <el-table-column prop="nickName" label="负责人" width="120" />
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goTasks(row)">任务</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" />
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addProject, delProject, editProject, listProject } from '@/api/project/project'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const formRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
})

/** 编辑时用 projectId；提交给后端时 edit 用 id 字段（与实体一致） */
const form = reactive({
  projectId: undefined,
  projectName: '',
  description: '',
})

const rules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
}

function resetForm() {
  form.projectId = undefined
  form.projectName = ''
  form.description = ''
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑项目'
    form.projectId = row.projectId
    form.projectName = row.projectName
    form.description = row.description || ''
  } else {
    dialogTitle.value = '新增项目'
  }
  dialogVisible.value = true
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.keyword = ''
  handleQuery()
}

function goTasks(row) {
  router.push({ path: '/project/task', query: { projectId: row.projectId } })
}

/** ========== 【学员填写 P-C】项目列表 ==========
 * 注意：listProject 是 POST，参数直接传对象，不是 { params }
 * const { data: res } = await listProject(query)
 * tableData = res.data.records；total = res.data.total
 */
async function getList() {
  // TODO P-C
  loading.value = true
  try{
    const { data: res } = await listProject(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }finally{
    loading.value = false
  }
}

/** ========== 【学员填写 P-D】新增/编辑 ==========
 * 有 form.projectId → editProject({ projectId, projectName, description })
 * 否则 → addProject({ projectName, description })
 * （后端 edit 会用 projectId 填到实体 id）
 */
async function submitForm() {
  // TODO P-D
  await formRef.value.validate()
  submitLoading.value = true
  try{
    if(form.projectId){
      await editProject({ projectId: form.projectId, projectName: form.projectName, description: form.description })
      ElMessage.success('修改成功')
    }else{
      await addProject({ projectName: form.projectName, description: form.description })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  }finally{
    submitLoading.value = false
  }
}

/** ========== 【学员填写 P-E】删除 ==========
 * await delProject({ projectId: row.projectId })
 * axios delete 带 body：api 里已写成 { data }
 */
async function handleDelete(row) {
  // TODO P-E
  await ElMessageBox.confirm('确定删除该项目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await delProject({ projectId: row.projectId })
  ElMessage.success('删除成功')
  getList()
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
.toolbar {
  margin-bottom: 0;
}
.pager {
  display: flex;
  justify-content: flex-end;
}
</style>
