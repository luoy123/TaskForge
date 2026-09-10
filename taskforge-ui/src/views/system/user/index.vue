<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="toolbar" @submit.prevent>
      <el-form-item label="用户名">
        <el-input v-model="query.userName" clearable placeholder="模糊查询" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openDialog()">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="userId" label="ID" width="80" />
      <el-table-column prop="userName" label="用户名" min-width="120" />
      <el-table-column prop="nickName" label="昵称" min-width="120" />
      <el-table-column prop="phonenumber" label="手机" min-width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '停用' }}
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" :disabled="!!form.userId" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item v-if="!form.userId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="手机" prop="phonenumber">
          <el-input v-model="form.phonenumber" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
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
import { addUser, delUser, listUser, updateUser } from '@/api/system/user'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref()

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
})

const form = reactive({
  userId: undefined,
  userName: '',
  nickName: '',
  password: '',
  phonenumber: '',
  status: 0,
})

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function resetForm() {
  form.userId = undefined
  form.userName = ''
  form.nickName = ''
  form.password = ''
  form.phonenumber = ''
  form.status = 0
}

function openDialog(row) {
  resetForm()
  if (row) {
    dialogTitle.value = '编辑用户'
    form.userId = row.userId
    form.userName = row.userName
    form.nickName = row.nickName
    form.phonenumber = row.phonenumber || ''
    form.status = row.status
  } else {
    dialogTitle.value = '新增用户'
  }
  dialogVisible.value = true
}

function handleQuery() {
  query.pageNum = 1
  getList()
}

function resetQuery() {
  query.userName = ''
  handleQuery()
}

/** ========== 【学员填写 C】拉取列表 ==========
 * 调用 listUser(query)，响应结构：
 *   const { data: res } = await listUser(query)
 *   // res = R，res.data = MyBatis-Plus Page
 *   // Page.records → 表格行；Page.total → 总数
 * 记得 loading 开关；catch 里也可关掉 loading
 * 在下面实现 getList：
 */
async function getList() {
  // TODO C
  loading.value = true
  try{

    const {data: res} = await listUser(query)
    tableData.value =res.data.records || []
    total.value = res.data.total || 0
  }finally{
    loading.value = false
  }
  
}

/** ========== 【学员填写 D】提交新增/编辑 ==========
 * 1. await formRef.value.validate()
 * 2. 有 form.userId → updateUser(form)；否则 → addUser(form)
 * 3. 成功：ElMessage.success、关对话框、getList()
 * 提示：const { data: res } 不必再用，拦截器已保证 code===200 才会继续
 * 在下面实现 submitForm：
 */
async function submitForm() {
  // TODO D
  await formRef.value.validate()
  submitLoading.value = true
  try{
    if(form.userId){
      await updateUser(form)
      ElMessage.success('修改成功')
    }else{
      await addUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  }finally{
    submitLoading.value = false
  }
}

/** ========== 【学员填写 E】删除 ==========
 * 1. ElMessageBox.confirm 确认
 * 2. await delUser(row.userId)   // 单个 id 即可
 * 3. 成功提示 + getList()
 * 在下面实现 handleDelete：
 */
async function handleDelete(row) {
  // TODO E
  await ElMessageBox.confirm(`确定删除该用户${row.userName}?`,'提示',{type: 'warning'})
  await delUser(row.userId)
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
