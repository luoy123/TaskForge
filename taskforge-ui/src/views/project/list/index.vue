<template>
  <div class="page">
    <el-tabs
      v-if="!menuFixedType"
      v-model="query.type"
      @tab-change="handleTypeChange"
    >
      <el-tab-pane label="我的项目" name="my" />
      <el-tab-pane label="收藏" name="collect" />
      <el-tab-pane label="回收站" name="recycle" />
    </el-tabs>

    <el-alert
      v-if="query.type === 'collect'"
      type="info"
      :closable="false"
      show-icon
      title="收藏：只显示你点过「收藏」的项目（不是归档）。"
      class="hint"
    />
    <el-alert
      v-else-if="query.type === 'recycle'"
      type="info"
      :closable="false"
      show-icon
      title="回收站：只显示「删除」过的项目。归档仍在「我的项目」里。"
      class="hint"
    />

    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" clearable placeholder="项目名称" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'project:manage:add'" type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="projectName" label="项目名称" min-width="160" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="stageName" label="阶段" width="120" />
      <el-table-column prop="nickName" label="负责人" width="120" />
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="520" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goTasks(row)">任务</el-button>
          <el-button
            v-hasPermi="'project:member:list'"
            link
            type="primary"
            @click="openMembers(row)"
          >成员</el-button>
          <el-button
            v-hasPermi="'project:file:queryFileList'"
            link
            type="primary"
            @click="openFiles(row)"
          >附件</el-button>
          <el-button
            v-hasPermi="'project:manage:edit'"
            link
            type="primary"
            @click="openDialog(row)"
          >编辑</el-button>
          <el-button
            v-if="!row.collected"
            v-hasPermi="'project:manage:collect'"
            link
            type="warning"
            @click="handleCollect(row)"
          >收藏</el-button>
          <el-button
            v-else
            v-hasPermi="'project:manage:cancelCollect'"
            link
            type="info"
            @click="handleCancelCollect(row)"
          >取消收藏</el-button>
          <el-button
            v-if="query.type !== 'recycle'"
            v-hasPermi="'project:manage:approve'"
            link
            type="success"
            @click="openApprove(row)"
          >发起审批</el-button>
          <el-button
            v-if="query.type !== 'recycle'"
            v-hasPermi="'project:manage:archive'"
            link
            type="warning"
            @click="handleArchive(row)"
          >归档</el-button>
          <el-button
            v-if="query.type !== 'recycle'"
            v-hasPermi="'project:manage:delete'"
            link
            type="danger"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty
          :description="
            query.type === 'collect'
              ? '暂无收藏。去「我的项目」点「收藏」即可出现在这里。'
              : query.type === 'recycle'
                ? '回收站为空。只有点「删除」的项目会进来（归档不会）。'
                : '暂无项目'
          "
        />
      </template>
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

    <MemberDrawer
      v-model="memberVisible"
      :project-id="activeProjectId"
      :project-name="activeProjectName"
    />
    <FilePanel
      v-model="fileVisible"
      :biz-id="activeProjectId"
      file-type="project"
    />
    <ApproveStartDialog
      v-model="approveVisible"
      kind="project"
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
  addProject,
  archiveProject,
  cancelCollectProject,
  collectProject,
  delProject,
  editProject,
  listProject,
} from '@/api/project/project'
import FilePanel from '@/components/project/FilePanel.vue'
import MemberDrawer from '@/components/project/MemberDrawer.vue'
import ApproveStartDialog from '@/components/workflow/ApproveStartDialog.vue'
import { PROJECT_LIST_TYPE_BY_COMPONENT } from '@/utils/permission'

const route = useRoute()
const router = useRouter()

/** 侧栏「我的收藏 / 回收站」等独立菜单：按 component 定死 type，不再显示 Tab */
const menuFixedType = computed(() => {
  const c = route.meta?.component
  return (c && PROJECT_LIST_TYPE_BY_COMPONENT[c]) || ''
})

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const formRef = ref()
const memberVisible = ref(false)
const fileVisible = ref(false)
const activeProjectId = ref('')
const activeProjectName = ref('')
const approveVisible = ref(false)
const approveBizId = ref('')
const approveBizLabel = ref('')

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  /** I1：my | collect | recycle */
  type: 'my',
})

function syncTypeFromMenu() {
  if (menuFixedType.value) {
    query.type = menuFixedType.value
  }
}

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

function handleTypeChange(name) {
  // 用 tab-change 回传的 name，避免偶发读到旧 type
  if (name) query.type = name
  query.pageNum = 1
  getList()
}

function goTasks(row) {
  router.push({ path: '/project/task', query: { projectId: row.projectId } })
}

function openMembers(row) {
  activeProjectId.value = row.projectId
  activeProjectName.value = row.projectName || ''
  memberVisible.value = true
}

function openFiles(row) {
  activeProjectId.value = row.projectId
  fileVisible.value = true
}

function openApprove(row) {
  approveBizId.value = row.projectId
  approveBizLabel.value = row.projectName || row.projectId
  approveVisible.value = true
}

async function getList() {
  loading.value = true
  try {
    const { data: res } = await listProject(query)
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
    if (form.projectId) {
      await editProject({
        projectId: form.projectId,
        projectName: form.projectName,
        description: form.description,
      })
      ElMessage.success('修改成功')
    } else {
      await addProject({ projectName: form.projectName, description: form.description })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该项目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await delProject({ projectId: row.projectId })
  ElMessage.success('删除成功')
  getList()
}

/** 【学员填写 F04-3】二次确认后调 archiveProject({ projectId }) */
async function handleArchive(row) {
  await ElMessageBox.confirm(`确定归档「${row.projectName}」吗？`, '提示', { type: 'warning' })
  await archiveProject({ projectId: row.projectId })
  ElMessage.success('已归档')
  getList()
}

/** 【学员填写 F04-3】collectProject({ projectId }) */
async function handleCollect(row) {
  await collectProject({ projectId: row.projectId })
  ElMessage.success('已收藏')
  getList()
}

/** 【学员填写 F04-3】cancelCollectProject({ projectId }) */
async function handleCancelCollect(row) {
  await cancelCollectProject({ projectId: row.projectId })
  ElMessage.success('已取消收藏')
  getList()
}

onMounted(() => {
  syncTypeFromMenu()
  getList()
})

/** 同一 list 组件被多个菜单复用，侧栏切换时要改 type 并重新拉列表 */
watch(
  () => route.meta?.component,
  () => {
    syncTypeFromMenu()
    query.pageNum = 1
    getList()
  },
)
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
.hint {
  margin-bottom: 0;
}
.pager {
  display: flex;
  justify-content: flex-end;
}
</style>
