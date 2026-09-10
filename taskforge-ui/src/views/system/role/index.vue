<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="角色名">
        <el-input v-model="query.roleName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="roleId" label="ID" width="80" />
      <el-table-column prop="roleName" label="角色名称" min-width="140" />
      <el-table-column prop="roleKey" label="权限字符" min-width="140" />
      <el-table-column prop="roleSort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" :disabled="row.roleId === 1" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="form.roleKey" :disabled="!!form.roleId" placeholder="如 common" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" controls-position="right" />
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
import { addRole, delRole, listRole, updateRole } from '@/api/system/role'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
})

const form = reactive({
  roleId: undefined,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: '',
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }],
}

function resetForm() {
  form.roleId = undefined
  form.roleName = ''
  form.roleKey = ''
  form.roleSort = 0
  form.status = '0'
  form.remark = ''
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑角色'
    form.roleId = row.roleId
    form.roleName = row.roleName
    form.roleKey = row.roleKey
    form.roleSort = row.roleSort ?? 0
    form.status = row.status ?? '0'
    form.remark = row.remark || ''
  } else {
    dialogTitle.value = '新增角色'
  }
  dialogVisible.value = true
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.roleName = ''
  handleQuery()
}

/** ========== 【学员填写 R-C】拉取角色列表（与用户页 getList 同套路） ==========
 * listRole(query) → res.data.records / res.data.total
 */
async function getList() {
  loading.value = true
  try {
    const { data: res } = await listRole(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

/** ========== 【学员填写 R-D】新增/编辑 ==========
 * form.roleId 有值 → updateRole(form)，否则 addRole(form)
 */
async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.roleId) {
      await updateRole(form)
      ElMessage.success('修改成功')
    } else {
      await addRole(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

/** ========== 【学员填写 R-E】删除 ==========
 * 勿删 roleId===1；delRole(row.roleId)
 */
async function handleDelete(row) {
  if (row.roleId === 1) {
    ElMessage.warning('不能删除超级管理员角色')
    return
  }
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await delRole(row.roleId)
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
