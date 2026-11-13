<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="表名称">
        <el-input v-model="query.tableName" clearable placeholder="模糊查询" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="表描述">
        <el-input v-model="query.tableComment" clearable placeholder="模糊查询" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button v-hasPermi="'tool:gen:import'" type="success" @click="openImport">导入</el-button>
        <el-button
          v-hasPermi="'tool:gen:remove'"
          type="danger"
          :disabled="selection.length === 0"
          @click="handleDelete()"
        >
          删除
        </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border @selection-change="(rows) => (selection = rows)">
      <el-table-column type="selection" width="45" />
      <el-table-column prop="tableId" label="ID" width="70" />
      <el-table-column prop="tableName" label="表名称" min-width="170" show-overflow-tooltip />
      <el-table-column prop="tableComment" label="表描述" min-width="150" show-overflow-tooltip />
      <el-table-column prop="className" label="实体类" min-width="140" />
      <el-table-column prop="packageName" label="包路径" min-width="190" show-overflow-tooltip />
      <el-table-column prop="createTime" label="导入时间" width="170" />
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button v-hasPermi="'tool:gen:preview'" link type="primary" @click="handlePreview(row)">预览</el-button>
          <el-button v-hasPermi="'tool:gen:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-hasPermi="'tool:gen:code'" link type="success" @click="handleDownload(row)">下载代码</el-button>
          <el-button v-hasPermi="'tool:gen:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 导入表 -->
    <el-dialog v-model="importVisible" title="导入表" width="820px" destroy-on-close>
      <el-form :inline="true" :model="dbQuery" @submit.prevent>
        <el-form-item label="表名称">
          <el-input v-model="dbQuery.tableName" clearable @keyup.enter="handleDbQuery" />
        </el-form-item>
        <el-form-item label="表描述">
          <el-input v-model="dbQuery.tableComment" clearable @keyup.enter="handleDbQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleDbQuery">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table
        v-loading="dbLoading"
        :data="dbTableData"
        border
        height="360px"
        @selection-change="(rows) => (dbSelection = rows)"
      >
        <el-table-column type="selection" width="45" />
        <el-table-column prop="tableName" label="表名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="tableComment" label="表描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="建表时间" width="170" />
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="dbQuery.pageNum"
          v-model:page-size="dbQuery.pageSize"
          :total="dbTotal"
          layout="total, prev, pager, next"
          @current-change="getDbList"
        />
      </div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" :disabled="dbSelection.length === 0" @click="submitImport">
          导入（{{ dbSelection.length }}）
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑生成配置 -->
    <el-dialog v-model="editVisible" :title="`编辑生成配置：${editForm.tableName || ''}`" width="92%" top="4vh" destroy-on-close>
      <el-tabs v-model="editTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="basicFormRef" :model="editForm" :rules="basicRules" label-width="110px" class="edit-form">
            <el-form-item label="表名称">
              <el-input v-model="editForm.tableName" disabled />
            </el-form-item>
            <el-form-item label="表描述">
              <el-input v-model="editForm.tableComment" />
            </el-form-item>
            <el-form-item label="实体类名称" prop="className">
              <el-input v-model="editForm.className" />
            </el-form-item>
            <el-form-item label="作者">
              <el-input v-model="editForm.functionAuthor" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="editForm.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="字段信息" name="columns">
          <el-table :data="editForm.columns" border max-height="60vh" size="small">
            <el-table-column prop="sort" label="序号" width="55" />
            <el-table-column prop="columnName" label="字段列名" min-width="130" show-overflow-tooltip />
            <el-table-column label="字段描述" min-width="140">
              <template #default="{ row }">
                <el-input v-model="row.columnComment" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="columnType" label="物理类型" width="110" show-overflow-tooltip />
            <el-table-column label="Java 类型" width="140">
              <template #default="{ row }">
                <el-select v-model="row.javaType" size="small">
                  <el-option v-for="t in javaTypes" :key="t" :label="t" :value="t" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="Java 属性" min-width="130">
              <template #default="{ row }">
                <el-input v-model="row.javaField" size="small" />
              </template>
            </el-table-column>
            <el-table-column v-for="f in flagFields" :key="f.prop" :label="f.label" width="60" align="center">
              <template #default="{ row }">
                <el-checkbox v-model="row[f.prop]" true-value="1" false-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="查询方式" width="110">
              <template #default="{ row }">
                <el-select v-model="row.queryType" size="small">
                  <el-option v-for="q in queryTypes" :key="q.value" :label="q.label" :value="q.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="显示类型" width="130">
              <template #default="{ row }">
                <el-select v-model="row.htmlType" size="small">
                  <el-option v-for="h in htmlTypes" :key="h.value" :label="h.label" :value="h.value" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="字典类型" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.dictType" size="small" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="生成信息" name="gen">
          <el-form ref="genFormRef" :model="editForm" :rules="genRules" label-width="110px" class="edit-form">
            <el-form-item label="生成模板">
              <el-select v-model="editForm.tplCategory" disabled>
                <el-option label="单表（增删改查）" value="crud" />
              </el-select>
            </el-form-item>
            <el-form-item label="生成包路径" prop="packageName">
              <el-input v-model="editForm.packageName" placeholder="如 com.zhq.taskforge.system" />
            </el-form-item>
            <el-form-item label="生成模块名" prop="moduleName">
              <el-input v-model="editForm.moduleName" placeholder="Controller 路径和权限字第一段，如 system" />
            </el-form-item>
            <el-form-item label="生成业务名" prop="businessName">
              <el-input v-model="editForm.businessName" placeholder="Controller 路径和权限字第二段，如 notice" />
            </el-form-item>
            <el-form-item label="生成功能名" prop="functionName">
              <el-input v-model="editForm.functionName" placeholder="用于注释、Swagger、@Log 标题" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览 -->
    <el-dialog v-model="previewVisible" :title="`代码预览：${previewTitle}`" width="80%" top="4vh" destroy-on-close>
      <el-tabs v-model="previewTab">
        <el-tab-pane v-for="(code, path) in previewData" :key="path" :label="fileName(path)" :name="path">
          <div class="preview-head">
            <span class="preview-path">{{ path }}</span>
            <el-button size="small" @click="copyCode(code)">复制</el-button>
          </div>
          <pre class="preview-code">{{ code }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  delTable,
  downloadCode,
  getGenTable,
  importTable,
  listDbTable,
  listTable,
  previewTable,
  updateGenTable,
} from '@/api/tool/gen'
import { saveBlobResponse } from '@/utils/download'

const javaTypes = ['Long', 'Integer', 'String', 'BigDecimal', 'Double', 'LocalDateTime']
const queryTypes = [
  { label: '=', value: 'EQ' },
  { label: '!=', value: 'NE' },
  { label: '>', value: 'GT' },
  { label: '>=', value: 'GTE' },
  { label: '<', value: 'LT' },
  { label: '<=', value: 'LTE' },
  { label: 'LIKE', value: 'LIKE' },
]
const htmlTypes = [
  { label: '文本框', value: 'input' },
  { label: '文本域', value: 'textarea' },
  { label: '下拉框', value: 'select' },
  { label: '单选框', value: 'radio' },
  { label: '复选框', value: 'checkbox' },
  { label: '日期控件', value: 'datetime' },
  { label: '图片上传', value: 'imageUpload' },
  { label: '文件上传', value: 'fileUpload' },
  { label: '富文本', value: 'editor' },
]
const flagFields = [
  { prop: 'isInsert', label: '插入' },
  { prop: 'isEdit', label: '编辑' },
  { prop: 'isList', label: '列表' },
  { prop: 'isQuery', label: '查询' },
  { prop: 'isRequired', label: '必填' },
]

// ---------- 已导入列表 ----------
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selection = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, tableName: '', tableComment: '' })

function buildParams(q) {
  const params = { pageNum: q.pageNum, pageSize: q.pageSize }
  if (q.tableName) params.tableName = q.tableName
  if (q.tableComment) params.tableComment = q.tableComment
  return params
}

async function getList() {
  loading.value = true
  try {
    const { data: res } = await listTable(buildParams(query))
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.tableName = ''
  query.tableComment = ''
  handleQuery()
}

async function handleDelete(row) {
  const rows = row ? [row] : selection.value
  if (rows.length === 0) return
  const names = rows.map((r) => r.tableName).join('、')
  await ElMessageBox.confirm(`确认删除「${names}」的生成配置？不会删除数据库表本身。`, '提示', { type: 'warning' })
  await delTable(rows.map((r) => r.tableId).join(','))
  ElMessage.success('已删除')
  await getList()
}

// ---------- 导入 ----------
const importVisible = ref(false)
const dbLoading = ref(false)
const importLoading = ref(false)
const dbTableData = ref([])
const dbTotal = ref(0)
const dbSelection = ref([])
const dbQuery = reactive({ pageNum: 1, pageSize: 10, tableName: '', tableComment: '' })

function openImport() {
  Object.assign(dbQuery, { pageNum: 1, tableName: '', tableComment: '' })
  dbSelection.value = []
  importVisible.value = true
  getDbList()
}

async function getDbList() {
  dbLoading.value = true
  try {
    const { data: res } = await listDbTable(buildParams(dbQuery))
    dbTableData.value = res.data?.records || []
    dbTotal.value = res.data?.total || 0
  } finally {
    dbLoading.value = false
  }
}

function handleDbQuery() {
  dbQuery.pageNum = 1
  getDbList()
}

async function submitImport() {
  importLoading.value = true
  try {
    await importTable(dbSelection.value.map((r) => r.tableName))
    ElMessage.success(`已导入 ${dbSelection.value.length} 张表`)
    importVisible.value = false
    handleQuery()
  } finally {
    importLoading.value = false
  }
}

// ---------- 编辑 ----------
const editVisible = ref(false)
const editLoading = ref(false)
const editTab = ref('basic')
const basicFormRef = ref()
const genFormRef = ref()
const editForm = reactive({ columns: [] })

const basicRules = {
  className: [
    { required: true, message: '请输入实体类名称', trigger: 'blur' },
    { pattern: /^[A-Z][A-Za-z0-9]*$/, message: '大写字母开头，只含字母数字', trigger: 'blur' },
  ],
}
const genRules = {
  packageName: [
    { required: true, message: '请输入生成包路径', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$/, message: '如 com.zhq.taskforge.system', trigger: 'blur' },
  ],
  moduleName: [{ required: true, message: '请输入生成模块名', trigger: 'blur' }],
  businessName: [{ required: true, message: '请输入生成业务名', trigger: 'blur' }],
  functionName: [{ required: true, message: '请输入生成功能名', trigger: 'blur' }],
}

async function openEdit(row) {
  const { data: res } = await getGenTable(row.tableId)
  Object.keys(editForm).forEach((k) => delete editForm[k])
  Object.assign(editForm, res.data, { columns: res.data?.columns || [] })
  editTab.value = 'basic'
  editVisible.value = true
}

async function validateForm(formRef, tab) {
  if (!formRef.value) return true
  try {
    await formRef.value.validate()
    return true
  } catch {
    editTab.value = tab
    return false
  }
}

async function submitEdit() {
  // destroy-on-close + 懒渲染：没打开过的 tab 没有 formRef，后端还会再校验一次
  if (!(await validateForm(basicFormRef, 'basic'))) return
  if (!(await validateForm(genFormRef, 'gen'))) return
  editLoading.value = true
  try {
    await updateGenTable({ ...editForm })
    ElMessage.success('保存成功')
    editVisible.value = false
    await getList()
  } finally {
    editLoading.value = false
  }
}

// ---------- 预览 / 下载 ----------
const previewVisible = ref(false)
const previewTitle = ref('')
const previewData = ref({})
const previewTab = ref('')

function fileName(path) {
  return path.substring(path.lastIndexOf('/') + 1)
}

async function handlePreview(row) {
  const { data: res } = await previewTable(row.tableId)
  previewData.value = res.data || {}
  previewTab.value = Object.keys(previewData.value)[0] || ''
  previewTitle.value = row.tableName
  previewVisible.value = true
}

async function copyCode(code) {
  await navigator.clipboard.writeText(code)
  ElMessage.success('已复制')
}

async function handleDownload(row) {
  try {
    const response = await downloadCode(row.tableId)
    await saveBlobResponse(response, `${row.tableName}.zip`)
  } catch (e) {
    ElMessage.error(e.message || '下载失败')
  }
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
  margin-top: 8px;
}
.edit-form {
  max-width: 640px;
}
.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.preview-path {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.preview-code {
  max-height: 65vh;
  overflow: auto;
  margin: 0;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre;
}
</style>
