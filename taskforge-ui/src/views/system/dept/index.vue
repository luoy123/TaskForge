<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="部门名称">
        <el-input v-model="query.deptName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="tableData"
      row-key="deptId"
      border
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="deptName" label="部门名称" min-width="180" />
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="电话" width="130" />
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
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门">
          <el-input-number v-model="form.parentId" :min="0" controls-position="right" style="width: 100%" />
          <div class="hint">0 表示顶级部门</div>
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
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
import { onMounted, reactive, ref, resolveDirective } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDept, delDept, listDept, updateDept } from '@/api/system/dept'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref()

const query = reactive({
  deptName: '',
})

const form = reactive({
  deptId: undefined,
  parentId: 0,
  deptName: '',
  orderNum: 0,
  leader: '',
  phone: '',
  status: '0',
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

function resetForm() {
  form.deptId = undefined
  form.parentId = 0
  form.deptName = ''
  form.orderNum = 0
  form.leader = ''
  form.phone = ''
  form.status = '0'
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑部门'
    form.deptId = row.deptId
    form.parentId = row.parentId ?? 0
    form.deptName = row.deptName
    form.orderNum = row.orderNum ?? 0
    form.leader = row.leader || ''
    form.phone = row.phone || ''
    form.status = row.status ?? '0'
  } else {
    dialogTitle.value = '新增部门'
  }
  dialogVisible.value = true
}

function resetQuery() {
  query.deptName = ''
  getList()
}

/** ========== 【学员填写 D-C】拉取部门树 ==========
 * listDept(query) → res.data 已是带 children 的树，直接赋 tableData
 * 无分页
 */
async function getList() {
  // TODO D-C
  loading.value = true
  try{
    const {data: res} = await listDept(query)
    tableData.value = res.data || []
  }finally{
    loading.value = false
  }
}

/** ========== 【学员填写 D-D】新增/编辑 ========== */
async function submitForm() {
  // TODO D-D
  await formRef.value.validate()
  submitLoading.value = true
  try{
    if(form.deptId){
      await updateDept(form)
    }else{
      await addDept(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getList()
  }finally{
    submitLoading.value = false
  }
}

/** ========== 【学员填写 D-E】删除 ========== */
async function handleDelete(row) {
  // TODO D-E
  await ElMessageBox.confirm('确定删除该部门吗？','提示',{type: 'warning'})
  await delDept(row.deptId)
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
.hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
