<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="角色名">
        <el-input v-model="query.roleName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'system:role:add'" type="success" @click="openDialog()">新增</el-button>
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
          <el-button v-hasPermi="'system:role:edit'" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button
            v-hasPermi="'system:role:remove'"
            link
            type="danger"
            :disabled="row.roleId === 1"
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
        <el-form-item label="菜单权限">
          <div class="tree-toolbar">
            <el-checkbox v-model="menuExpand" @change="handleExpandChange">展开/折叠</el-checkbox>
            <el-checkbox v-model="menuNodeAll" @change="handleCheckAllChange">全选/全不选</el-checkbox>
            <el-checkbox v-model="menuCheckStrictly">父子联动</el-checkbox>
          </div>
          <div v-loading="menuLoading" class="tree-box">
            <el-tree
              ref="menuTreeRef"
              :data="menuOptions"
              show-checkbox
              node-key="id"
              :check-strictly="!menuCheckStrictly"
              :props="treeProps"
              empty-text="暂无菜单"
            />
          </div>
          <div class="field-tip">保存后写入 sys_role_menu；用户需重新登录才会刷新权限缓存。</div>
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
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleMenuTreeselect, treeselect } from '@/api/system/menu'
import { addRole, delRole, listRole, updateRole } from '@/api/system/role'

const loading = ref(false)
const submitLoading = ref(false)
const menuLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref()
const menuTreeRef = ref()
const menuOptions = ref([])
/** 打开编辑弹窗后待回填的已勾选 menuId */
const pendingCheckedKeys = ref([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
/** true = 父子联动（对应后端 menuCheckStrictly=1） */
const menuCheckStrictly = ref(true)

const treeProps = {
  label: 'label',
  children: 'children',
}

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
  menuOptions.value = []
  pendingCheckedKeys.value = []
  menuExpand.value = false
  menuNodeAll.value = false
  menuCheckStrictly.value = true
}

function collectAllNodeIds(nodes, out = []) {
  for (const n of nodes || []) {
    out.push(n.id)
    if (n.children?.length) {
      collectAllNodeIds(n.children, out)
    }
  }
  return out
}

function setTreeExpand(expand) {
  const tree = menuTreeRef.value
  if (!tree?.store?.nodesMap) return
  for (const node of Object.values(tree.store.nodesMap)) {
    node.expanded = expand
  }
}

function handleExpandChange(val) {
  setTreeExpand(!!val)
}

function handleCheckAllChange(val) {
  const tree = menuTreeRef.value
  if (!tree) return
  if (val) {
    tree.setCheckedKeys(collectAllNodeIds(menuOptions.value))
  } else {
    tree.setCheckedKeys([])
  }
}

/** 勾选 + 半选（父子联动时父节点常为半选，必须一并提交） */
function getMenuAllCheckedKeys() {
  const tree = menuTreeRef.value
  if (!tree) return []
  const checked = tree.getCheckedKeys() || []
  const half = tree.getHalfCheckedKeys() || []
  return [...new Set([...checked, ...half])]
}

async function loadMenuTreeForCreate() {
  menuLoading.value = true
  try {
    const { data: res } = await treeselect()
    menuOptions.value = res.data || []
    pendingCheckedKeys.value = []
  } finally {
    menuLoading.value = false
  }
}

async function loadMenuTreeForEdit(roleId) {
  menuLoading.value = true
  try {
    const { data: res } = await roleMenuTreeselect(roleId)
    const payload = res.data || {}
    menuOptions.value = payload.menus || []
    pendingCheckedKeys.value = payload.checkedKeys || []
  } finally {
    menuLoading.value = false
  }
}

async function applyCheckedKeys() {
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(pendingCheckedKeys.value || [])
}

async function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑角色'
    form.roleId = row.roleId
    form.roleName = row.roleName
    form.roleKey = row.roleKey
    form.roleSort = row.roleSort ?? 0
    form.status = row.status ?? '0'
    form.remark = row.remark || ''
    // 后端 Integer：1 联动 / 0 不联动
    menuCheckStrictly.value = row.menuCheckStrictly == null || Number(row.menuCheckStrictly) === 1
    await loadMenuTreeForEdit(row.roleId)
    dialogVisible.value = true
    await applyCheckedKeys()
  } else {
    dialogTitle.value = '新增角色'
    await loadMenuTreeForCreate()
    dialogVisible.value = true
    await applyCheckedKeys()
  }
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.roleName = ''
  handleQuery()
}

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

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = {
      roleId: form.roleId,
      roleName: form.roleName,
      roleKey: form.roleKey,
      roleSort: form.roleSort,
      status: form.status,
      remark: form.remark,
      menuCheckStrictly: menuCheckStrictly.value ? 1 : 0,
      menuIds: getMenuAllCheckedKeys(),
    }
    if (form.roleId) {
      await updateRole(payload)
      ElMessage.success('修改成功，相关用户请重新登录以刷新权限')
    } else {
      await addRole(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

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
.tree-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}
.tree-box {
  width: 100%;
  max-height: 320px;
  overflow: auto;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 8px;
}
.field-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}
</style>
