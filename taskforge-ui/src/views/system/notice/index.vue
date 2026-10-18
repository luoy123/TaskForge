<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="标题">
        <el-input v-model="query.noticeTitle" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.noticeType" clearable placeholder="全部" style="width: 120px">
          <el-option label="通知" value="1" />
          <el-option label="公告" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="关闭" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'system:notice:add'" type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="noticeId" label="ID" width="80" />
      <el-table-column prop="noticeTitle" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          {{ row.noticeType === '1' ? '通知' : row.noticeType === '2' ? '公告' : row.noticeType }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'" size="small">
            {{ row.status === '0' ? '正常' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createBy" label="创建者" width="110" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'system:notice:edit'" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-hasPermi="'system:notice:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.noticeId ? '修改公告' : '新增公告'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" />
        </el-form-item>
        <el-form-item label="类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio value="1">通知</el-radio>
            <el-radio value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="noticeContent">
          <el-input v-model="form.noticeContent" type="textarea" :rows="5" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addNotice, delNotice, listNotice, updateNotice } from '@/api/system/notice'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  noticeTitle: '',
  noticeType: '',
  status: '',
})

const form = reactive({
  noticeId: undefined,
  noticeTitle: '',
  noticeType: '1',
  noticeContent: '',
  status: '0',
  remark: '',
})

const rules = {
  noticeTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

function resetForm() {
  Object.assign(form, {
    noticeId: undefined,
    noticeTitle: '',
    noticeType: '1',
    noticeContent: '',
    status: '0',
    remark: '',
  })
}

function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      noticeId: row.noticeId,
      noticeTitle: row.noticeTitle,
      noticeType: row.noticeType || '1',
      noticeContent: row.noticeContent || '',
      status: row.status ?? '0',
      remark: row.remark || '',
    })
  }
  dialogVisible.value = true
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.noticeTitle = ''
  query.noticeType = ''
  query.status = ''
  handleQuery()
}

/** ========== 【学员填写 F07】拉取列表 ========== */
async function getList() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.noticeTitle) params.noticeTitle = query.noticeTitle
    if (query.noticeType) params.noticeType = query.noticeType
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data: res } = await listNotice(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

/** ========== 【学员填写 F07】新增/编辑 ========== */
async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.noticeId) {
      await updateNotice({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addNotice({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

/** ========== 【学员填写 F07】删除 ========== */
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除「${row.noticeTitle}」？`, '提示', { type: 'warning' })
  await delNotice(row.noticeId)
  ElMessage.success('已删除')
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
