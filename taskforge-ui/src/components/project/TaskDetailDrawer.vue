<template>
  <el-drawer
    :model-value="modelValue"
    :title="drawerTitle"
    size="640px"
    destroy-on-close
    @close="emit('update:modelValue', false)"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="评论 / 动态" name="log">
        <div class="comment-box">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            placeholder="写一条评论…"
            maxlength="500"
            show-word-limit
          />
          <el-button
            v-hasPermi="'project:task:addComment'"
            type="primary"
            class="mt"
            :loading="commentLoading"
            :disabled="!comment.trim()"
            @click="submitComment"
          >发表评论</el-button>
        </div>

        <div class="filter">
          <el-radio-group v-model="logFilter" size="small" @change="loadLogs">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="3">评论</el-radio-button>
            <el-radio-button value="1">动态</el-radio-button>
          </el-radio-group>
          <el-button link type="primary" @click="loadLogs">刷新</el-button>
        </div>

        <el-timeline v-loading="logLoading">
          <el-timeline-item
            v-for="item in logs"
            :key="item.id"
            :timestamp="item.createdTime"
            placement="top"
          >
            <div class="log-item">
              <el-tag size="small" :type="item.logType === 3 ? 'success' : 'info'">
                {{ item.logType === 3 ? '评论' : item.operateType || '动态' }}
              </el-tag>
              <span class="who">{{ item.createdBy || item.userId }}</span>
              <p class="content">{{ item.content || item.remark || '—' }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!logLoading && !logs.length" description="暂无记录" />
      </el-tab-pane>

      <el-tab-pane label="子任务" name="child">
        <div class="child-toolbar">
          <el-input v-model="childName" placeholder="子任务名称" clearable style="flex: 1" />
          <el-button
            v-hasPermi="'project:task:addChildTask'"
            type="success"
            :loading="childLoading"
            :disabled="!childName.trim()"
            @click="submitChild"
          >新增子任务</el-button>
          <el-button @click="loadChildren">刷新</el-button>
        </div>
        <el-table v-loading="childListLoading" :data="children" border size="small">
          <el-table-column prop="taskName" label="名称" min-width="140" />
          <el-table-column prop="statusName" label="状态" width="90" />
          <el-table-column prop="nickName" label="执行人" width="100" />
          <el-table-column prop="createdTime" label="创建时间" width="150" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addChildTask, addComment, listChildTask, listTaskLog } from '@/api/project/task'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  taskId: { type: String, default: '' },
  projectId: { type: String, default: '' },
  taskName: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const drawerTitle = computed(() => (props.taskName ? `任务 · ${props.taskName}` : '任务详情'))

const activeTab = ref('log')
const comment = ref('')
const commentLoading = ref(false)
const logLoading = ref(false)
const logs = ref([])
const logFilter = ref('all')

const childName = ref('')
const childLoading = ref(false)
const childListLoading = ref(false)
const children = ref([])

/** 【学员填写 F04-7】listTaskLog({ taskId, logType? }) */
async function loadLogs() {
  if (!props.taskId) {
    logs.value = []
    return
  }
  logLoading.value = true
  try {
    const body = { taskId: props.taskId }
    if (logFilter.value !== 'all') body.logType = Number(logFilter.value)
    const { data: res } = await listTaskLog(body)
    logs.value = res.data || []
  } finally {
    logLoading.value = false
  }
}

/** 【学员填写 F04-7】addComment({ taskId, comment, projectId }) */
async function submitComment() {
  const text = comment.value.trim()
  if (!text) return
  commentLoading.value = true
  try {
    await addComment({
      taskId: props.taskId,
      projectId: props.projectId,
      comment: text,
    })
    ElMessage.success('已发表')
    comment.value = ''
    await loadLogs()
  } finally {
    commentLoading.value = false
  }
}

/** 【学员填写 F04-7】listChildTask({ taskId }) */
async function loadChildren() {
  if (!props.taskId) {
    children.value = []
    return
  }
  childListLoading.value = true
  try {
    const { data: res } = await listChildTask({ taskId: props.taskId })
    children.value = res.data || []
  } finally {
    childListLoading.value = false
  }
}

/** 【学员填写 F04-7】addChildTask：父任务用 taskId（后端会转成 taskPid） */
async function submitChild() {
  const name = childName.value.trim()
  if (!name) return
  childLoading.value = true
  try {
    await addChildTask({
      taskId: props.taskId,
      projectId: props.projectId,
      taskName: name,
    })
    ElMessage.success('子任务已创建')
    childName.value = ''
    await loadChildren()
  } finally {
    childLoading.value = false
  }
}

watch(
  () => [props.modelValue, props.taskId],
  ([open]) => {
    if (open && props.taskId) {
      activeTab.value = 'log'
      comment.value = ''
      childName.value = ''
      logFilter.value = 'all'
      loadLogs()
      loadChildren()
    }
  },
)
</script>

<style scoped>
.comment-box {
  margin-bottom: 12px;
}
.mt {
  margin-top: 8px;
}
.filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.log-item .who {
  margin-left: 8px;
  color: #64748b;
  font-size: 13px;
}
.log-item .content {
  margin: 6px 0 0;
  white-space: pre-wrap;
}
.child-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
