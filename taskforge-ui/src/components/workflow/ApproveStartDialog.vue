<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="420px"
    destroy-on-close
    @close="emit('update:modelValue', false)"
  >
    <el-form label-width="80px" @submit.prevent>
      <el-form-item label="业务">
        <span>{{ bizLabel || bizId }}</span>
      </el-form-item>
      <el-form-item label="审批人" required>
        <el-select
          v-model="approver"
          filterable
          remote
          clearable
          placeholder="搜索用户（userId 字符串）"
          :remote-method="searchUsers"
          :loading="userLoading"
          style="width: 100%"
        >
          <el-option
            v-for="u in userOptions"
            :key="u.userId"
            :label="`${u.nickName || u.userName} (id=${u.userId})`"
            :value="String(u.userId)"
          />
        </el-select>
      </el-form-item>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="发起后 approved=0，任务改状态会被后端拒绝，直到审批通过/驳回。"
      />
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">发起</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listUser } from '@/api/system/user'
import { startProjectApprove, startTaskApprove } from '@/api/workflow/task'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** task | project */
  kind: { type: String, default: 'task' },
  bizId: { type: String, default: '' },
  bizLabel: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'success'])

const title = computed(() => (props.kind === 'project' ? '发起项目审批' : '发起任务审批'))
const loading = ref(false)
const approver = ref('')
const userLoading = ref(false)
const userOptions = ref([])

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

async function submit() {
  if (!props.bizId) {
    ElMessage.warning('缺少业务 id')
    return
  }
  if (!approver.value) {
    ElMessage.warning('请选择审批人')
    return
  }
  loading.value = true
  try {
    if (props.kind === 'project') {
      await startProjectApprove({ projectId: props.bizId, approver: approver.value })
    } else {
      await startTaskApprove({ taskId: props.bizId, approver: approver.value })
    }
    ElMessage.success('已发起审批')
    emit('update:modelValue', false)
    emit('success')
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      approver.value = ''
      userOptions.value = []
      searchUsers('')
    }
  },
)
</script>
