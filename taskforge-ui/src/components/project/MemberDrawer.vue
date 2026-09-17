<template>
  <el-drawer
    :model-value="modelValue"
    :title="drawerTitle"
    size="560px"
    destroy-on-close
    @close="emit('update:modelValue', false)"
  >
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="昵称 / 邮箱"
        style="width: 180px"
        @keyup.enter="handleQuery"
      />
      <el-button type="primary" @click="handleQuery">搜索</el-button>
      <el-button
        v-hasPermi="'project:member:inviteMemberList'"
        type="success"
        @click="openInvite"
      >邀请</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border size="small">
      <el-table-column prop="nickName" label="昵称" width="100" />
      <el-table-column prop="userName" label="账号" width="110" />
      <el-table-column prop="deptName" label="部门" min-width="100" />
      <el-table-column prop="roleName" label="角色" width="100" />
      <el-table-column label="创建者" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.creator === 1" size="small" type="success">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="joinedTime" label="加入时间" width="150" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="'project:member:removeMemberList'"
            link
            type="danger"
            :disabled="row.creator === 1"
            @click="handleRemove(row)"
          >移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20]"
        layout="total, prev, pager, next"
        small
        @current-change="getList"
        @size-change="getList"
      />
    </div>

    <el-dialog v-model="inviteVisible" title="邀请成员" width="420px" append-to-body destroy-on-close>
      <el-select
        v-model="inviteUserIds"
        multiple
        filterable
        remote
        clearable
        placeholder="搜索用户"
        :remote-method="searchUsers"
        :loading="userLoading"
        style="width: 100%"
      >
        <el-option
          v-for="u in userOptions"
          :key="u.userId"
          :label="`${u.nickName || u.userName} (${u.userName})`"
          :value="u.userId"
        />
      </el-select>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" :loading="inviteLoading" @click="submitInvite">确定</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addMember, listMember, removeMember } from '@/api/project/member'
import { listUser } from '@/api/system/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  projectId: { type: String, default: '' },
  projectName: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const drawerTitle = computed(() =>
  props.projectName ? `成员 · ${props.projectName}` : '项目成员',
)

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 10,
})

const inviteVisible = ref(false)
const inviteLoading = ref(false)
const inviteUserIds = ref([])
const userLoading = ref(false)
const userOptions = ref([])

function handleQuery() {
  query.pageNum = 1
  getList()
}

/** 【学员填写 F04-4】listMember({ projectId, keyword, pageNum, pageSize }) */
async function getList() {
  if (!props.projectId) {
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const { data: res } = await listMember({
      projectId: props.projectId,
      keyword: query.keyword,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function openInvite() {
  inviteUserIds.value = []
  userOptions.value = []
  inviteVisible.value = true
  searchUsers('')
}

async function searchUsers(keyword) {
  userLoading.value = true
  try {
    const { data: res } = await listUser({
      pageNum: 1,
      pageSize: 20,
      userName: keyword || undefined,
      nickName: keyword || undefined,
    })
    userOptions.value = res.data.records || []
  } finally {
    userLoading.value = false
  }
}

/** 【学员填写 F04-4】addMember({ projectId, userIdList }) */
async function submitInvite() {
  if (!inviteUserIds.value.length) {
    ElMessage.warning('请选择用户')
    return
  }
  inviteLoading.value = true
  try {
    await addMember({
      projectId: props.projectId,
      userIdList: inviteUserIds.value,
    })
    ElMessage.success('邀请成功')
    inviteVisible.value = false
    getList()
  } finally {
    inviteLoading.value = false
  }
}

/** 【学员填写 F04-4】removeMember({ projectId, userIdList: [row.userId] }) */
async function handleRemove(row) {
  await ElMessageBox.confirm(`确定移除「${row.nickName || row.userName}」？`, '提示', {
    type: 'warning',
  })
  await removeMember({
    projectId: props.projectId,
    userIdList: [row.userId],
  })
  ElMessage.success('已移除')
  getList()
}

watch(
  () => [props.modelValue, props.projectId],
  ([open]) => {
    if (open) {
      query.pageNum = 1
      query.keyword = ''
      getList()
    }
  },
)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
