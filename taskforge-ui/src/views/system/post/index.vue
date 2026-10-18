<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="岗位编码">
        <el-input v-model="query.postCode" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="岗位名称">
        <el-input v-model="query.postName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'system:post:add'" type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="postId" label="ID" width="80" />
      <el-table-column prop="postCode" label="岗位编码" min-width="120" />
      <el-table-column prop="postName" label="岗位名称" min-width="140" />
      <el-table-column prop="postSort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'system:post:edit'" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-hasPermi="'system:post:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.postId ? '修改岗位' : '新增岗位'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="form.postCode" />
        </el-form-item>
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="form.postName" />
        </el-form-item>
        <el-form-item label="排序" prop="postSort">
          <el-input-number v-model="form.postSort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addPost, delPost, listPost, updatePost } from '@/api/system/post'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  postCode: '',
  postName: '',
  status: '',
})

const form = reactive({
  postId: undefined,
  postCode: '',
  postName: '',
  postSort: 0,
  status: '0',
  remark: '',
})

const rules = {
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(form, {
    postId: undefined,
    postCode: '',
    postName: '',
    postSort: 0,
    status: '0',
    remark: '',
  })
}

function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      postId: row.postId,
      postCode: row.postCode,
      postName: row.postName,
      postSort: row.postSort ?? 0,
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
  query.postCode = ''
  query.postName = ''
  query.status = ''
  handleQuery()
}

/** ========== 【学员填写 F07】拉取列表 ==========
 * listPost(query) → res.data.records / total
 * 空字符串不要带给后端（status='' 会被当成精确匹配）
 */
async function getList() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.postCode) params.postCode = query.postCode
    if (query.postName) params.postName = query.postName
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data: res } = await listPost(params)
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
    if (form.postId) {
      await updatePost({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addPost({ ...form })
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
  await ElMessageBox.confirm(`确认删除岗位「${row.postName}」？`, '提示', { type: 'warning' })
  await delPost(row.postId)
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
