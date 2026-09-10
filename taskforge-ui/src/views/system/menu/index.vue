<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="菜单名称">
        <el-input v-model="query.menuName" clearable placeholder="模糊查询" />
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
      row-key="menuId"
      border
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="menuName" label="菜单名称" min-width="160" />
      <el-table-column prop="menuType" label="类型" width="80">
        <template #default="{ row }">
          {{ menuTypeLabel(row.menuType) }}
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由" min-width="140" />
      <el-table-column prop="perms" label="权限标识" min-width="160" />
      <el-table-column prop="orderNum" label="排序" width="80" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-input-number v-model="form.parentId" :min="0" controls-position="right" style="width: 100%" />
          <div class="hint">0 表示顶级菜单</div>
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路由地址">
          <el-input v-model="form.path" placeholder="如 system/user" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addMenu, delMenu, listMenu, updateMenu } from '@/api/system/menu'
import { buildTree } from '@/utils/tree'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref()

const query = reactive({
  menuName: '',
})

const form = reactive({
  menuId: undefined,
  parentId: 0,
  menuName: '',
  menuType: 'M',
  path: '',
  perms: '',
  orderNum: 0,
  status: '0',
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择类型', trigger: 'change' }],
}

function menuTypeLabel(type) {
  if (type === 'M') return '目录'
  if (type === 'C') return '菜单'
  if (type === 'F') return '按钮'
  return type
}

function resetForm() {
  form.menuId = undefined
  form.parentId = 0
  form.menuName = ''
  form.menuType = 'M'
  form.path = ''
  form.perms = ''
  form.orderNum = 0
  form.status = '0'
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑菜单'
    form.menuId = row.menuId
    form.parentId = row.parentId ?? 0
    form.menuName = row.menuName
    form.menuType = row.menuType || 'M'
    form.path = row.path || ''
    form.perms = row.perms || ''
    form.orderNum = row.orderNum ?? 0
    form.status = row.status ?? '0'
  } else {
    dialogTitle.value = '新增菜单'
  }
  dialogVisible.value = true
}

function resetQuery() {
  query.menuName = ''
  getList()
}

/** ========== 【学员填写 M-C】拉取菜单树 ==========
 * listMenu(query) → res.data 是扁平数组
 * tableData.value = buildTree(res.data || [], 'menuId', 'parentId')
 */
async function getList() {
  // TODO M-C
  loading.value = true
  try{
    const {data: res} = await listMenu(query)
    tableData.value = buildTree(res.data || [], 'menuId', 'parentId')
  }finally{
    loading.value = false
  }
}

/** ========== 【学员填写 M-D】新增/编辑 ========== */
async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.menuId) {
      await updateMenu(form)
    } else {
      await addMenu(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

/** ========== 【学员填写 M-E】删除 ========== */
async function handleDelete(row) {
  // TODO M-E
  await ElMessageBox.confirm('确定删除该菜单吗？','提示',{type: 'warning'})
  await delMenu(row.menuId)
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
