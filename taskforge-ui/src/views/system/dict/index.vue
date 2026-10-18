<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="字典名称">
        <el-input v-model="query.dictName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item label="字典类型">
        <el-input v-model="query.dictType" clearable placeholder="模糊查询" />
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
        <el-button v-hasPermi="'system:dict:add'" type="success" @click="openTypeDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="dictId" label="ID" width="80" />
      <el-table-column prop="dictName" label="字典名称" min-width="140" />
      <el-table-column prop="dictType" label="字典类型" min-width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDataDrawer(row)">{{ row.dictType }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'system:dict:query'" link @click="openDataDrawer(row)">数据</el-button>
          <el-button v-hasPermi="'system:dict:edit'" link type="primary" @click="openTypeDialog(row)">编辑</el-button>
          <el-button v-hasPermi="'system:dict:remove'" link type="danger" @click="handleDeleteType(row)">删除</el-button>
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

    <el-dialog v-model="typeDialogVisible" :title="typeForm.dictId ? '修改字典类型' : '新增字典类型'" width="480px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" :disabled="!!typeForm.dictId" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="submitTypeForm">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="dataVisible" :title="`字典数据 · ${currentDictType}`" size="65%">
      <div class="toolbar">
        <el-button v-hasPermi="'system:dict:add'" type="success" @click="openDataDialog()">新增数据</el-button>
        <el-button @click="loadDataList">刷新</el-button>
      </div>
      <el-table v-loading="dataLoading" :data="dataTable" border>
        <el-table-column prop="dictCode" label="编码" width="80" />
        <el-table-column prop="dictLabel" label="标签" min-width="120" />
        <el-table-column prop="dictValue" label="键值" min-width="120" />
        <el-table-column prop="dictSort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-hasPermi="'system:dict:edit'" link type="primary" @click="openDataDialog(row)">编辑</el-button>
            <el-button v-hasPermi="'system:dict:remove'" link type="danger" @click="handleDeleteData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="dataQuery.pageNum"
          v-model:page-size="dataQuery.pageSize"
          :total="dataTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadDataList"
          @current-change="loadDataList"
        />
      </div>
    </el-drawer>

    <el-dialog v-model="dataDialogVisible" :title="dataForm.dictCode ? '修改字典数据' : '新增字典数据'" width="480px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典类型">
          <el-input v-model="dataForm.dictType" disabled />
        </el-form-item>
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitLoading" @click="submitDataForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDictData,
  addDictType,
  delDictData,
  delDictType,
  listDictData,
  listDictType,
  updateDictData,
  updateDictType,
} from '@/api/system/dict'

const loading = ref(false)
const typeSubmitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const typeDialogVisible = ref(false)
const typeFormRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  dictName: '',
  dictType: '',
  status: '',
})

const typeForm = reactive({
  dictId: undefined,
  dictName: '',
  dictType: '',
  status: '0',
  remark: '',
})

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
}

const dataVisible = ref(false)
const dataLoading = ref(false)
const dataSubmitLoading = ref(false)
const dataTable = ref([])
const dataTotal = ref(0)
const currentDictType = ref('')
const dataDialogVisible = ref(false)
const dataFormRef = ref()
const dataQuery = reactive({ pageNum: 1, pageSize: 10, dictType: '' })
const dataForm = reactive({
  dictCode: undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: '0',
  remark: '',
})
const dataRules = {
  dictLabel: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入键值', trigger: 'blur' }],
}

function resetTypeForm() {
  Object.assign(typeForm, {
    dictId: undefined,
    dictName: '',
    dictType: '',
    status: '0',
    remark: '',
  })
}

function openTypeDialog(row) {
  resetTypeForm()
  if (row) {
    Object.assign(typeForm, {
      dictId: row.dictId,
      dictName: row.dictName,
      dictType: row.dictType,
      status: row.status ?? '0',
      remark: row.remark || '',
    })
  }
  typeDialogVisible.value = true
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.dictName = ''
  query.dictType = ''
  query.status = ''
  handleQuery()
}

/** ========== 【学员填写 F07】字典类型列表 ========== */
async function getList() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.dictName) params.dictName = query.dictName
    if (query.dictType) params.dictType = query.dictType
    if (query.status !== '' && query.status != null) params.status = query.status
    const { data: res } = await listDictType(params)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function submitTypeForm() {
  await typeFormRef.value.validate()
  typeSubmitLoading.value = true
  try {
    if (typeForm.dictId) {
      await updateDictType({ ...typeForm })
      ElMessage.success('修改成功')
    } else {
      await addDictType({ ...typeForm })
      ElMessage.success('新增成功')
    }
    typeDialogVisible.value = false
    await getList()
  } finally {
    typeSubmitLoading.value = false
  }
}

async function handleDeleteType(row) {
  await ElMessageBox.confirm(`确认删除字典类型「${row.dictType}」？`, '提示', { type: 'warning' })
  await delDictType(row.dictId)
  ElMessage.success('已删除')
  await getList()
}

function openDataDrawer(row) {
  currentDictType.value = row.dictType
  dataQuery.dictType = row.dictType
  dataQuery.pageNum = 1
  dataVisible.value = true
  loadDataList()
}

/** ========== 【学员填写 F07】字典数据列表（按 dictType） ========== */
async function loadDataList() {
  dataLoading.value = true
  try {
    const { data: res } = await listDictData(dataQuery)
    dataTable.value = res.data?.records || []
    dataTotal.value = res.data?.total || 0
  } finally {
    dataLoading.value = false
  }
}

function resetDataForm() {
  Object.assign(dataForm, {
    dictCode: undefined,
    dictType: currentDictType.value,
    dictLabel: '',
    dictValue: '',
    dictSort: 0,
    status: '0',
    remark: '',
  })
}

function openDataDialog(row) {
  resetDataForm()
  if (row) {
    Object.assign(dataForm, {
      dictCode: row.dictCode,
      dictType: row.dictType,
      dictLabel: row.dictLabel,
      dictValue: row.dictValue,
      dictSort: row.dictSort ?? 0,
      status: row.status ?? '0',
      remark: row.remark || '',
    })
  }
  dataDialogVisible.value = true
}

async function submitDataForm() {
  await dataFormRef.value.validate()
  dataSubmitLoading.value = true
  try {
    if (dataForm.dictCode) {
      await updateDictData({ ...dataForm })
      ElMessage.success('修改成功')
    } else {
      await addDictData({ ...dataForm })
      ElMessage.success('新增成功')
    }
    dataDialogVisible.value = false
    await loadDataList()
  } finally {
    dataSubmitLoading.value = false
  }
}

async function handleDeleteData(row) {
  await ElMessageBox.confirm(`确认删除「${row.dictLabel}」？`, '提示', { type: 'warning' })
  await delDictData(row.dictCode)
  ElMessage.success('已删除')
  await loadDataList()
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
  margin-bottom: 12px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
