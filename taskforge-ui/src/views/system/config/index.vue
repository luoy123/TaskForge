<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="参数名称">
        <el-input v-model="query.configName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="参数键名">
        <el-input v-model="query.configKey" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="系统内置">
        <el-select v-model="query.configType" clearable placeholder="全部" style="width: 120px">
          <el-option label="是" value="Y" />
          <el-option label="否" value="N" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'system:config:add'" type="success" @click="openDialog()">新增</el-button>
        <el-button v-hasPermi="'system:config:remove'" type="warning" @click="handleRefreshCache">刷新缓存</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="configId" label="ID" width="80" />
      <el-table-column prop="configName" label="参数名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="configKey" label="键名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="configValue" label="键值" min-width="120" show-overflow-tooltip />
      <el-table-column label="系统内置" width="100">
        <template #default="{ row }">
          {{ row.configType === 'Y' ? '是' : '否' }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'system:config:edit'" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button v-hasPermi="'system:config:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.configId ? '修改参数' : '新增参数'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" :disabled="!!form.configId" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
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
import { addConfig, delConfig, listConfig, refreshConfigCache, updateConfig } from '@/api/system/config'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  configName: '',
  configKey: '',
  configType: '',
})

const form = reactive({
  configId: undefined,
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'N',
  remark: '',
})

const rules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }],
}

function resetForm() {
  Object.assign(form, {
    configId: undefined,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'N',
    remark: '',
  })
}

function openDialog(row) {
  resetForm()
  if (row) {
    Object.assign(form, {
      configId: row.configId,
      configName: row.configName,
      configKey: row.configKey,
      configValue: row.configValue,
      configType: row.configType || 'N',
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
  query.configName = ''
  query.configKey = ''
  query.configType = ''
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
    if (query.configName) params.configName = query.configName
    if (query.configKey) params.configKey = query.configKey
    if (query.configType) params.configType = query.configType
    const { data: res } = await listConfig(params)
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
    if (form.configId) {
      await updateConfig({ ...form })
      ElMessage.success('修改成功')
    } else {
      await addConfig({ ...form })
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
  await ElMessageBox.confirm(`确认删除参数「${row.configKey}」？`, '提示', { type: 'warning' })
  await delConfig(row.configId)
  ElMessage.success('已删除')
  await getList()
}

async function handleRefreshCache() {
  await ElMessageBox.confirm('确认刷新参数缓存？', '提示', { type: 'warning' })
  await refreshConfigCache()
  ElMessage.success('缓存已刷新')
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
