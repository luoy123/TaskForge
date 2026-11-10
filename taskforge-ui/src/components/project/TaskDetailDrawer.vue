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
            <el-radio-button value="1">动态</el-radio-button>
            <el-radio-button value="2">交付物</el-radio-button>
            <el-radio-button value="3">评论</el-radio-button>
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
              <el-tag size="small" :type="LOG_TAG_TYPE[item.logType] || 'info'">
                {{ item.logTypeName || '动态' }}
              </el-tag>
              <span class="who">{{ item.nickName || item.createdBy || item.userId }}</span>
              <span v-if="item.remark && item.logType !== 3" class="remark">{{ item.remark }}</span>
              <p class="content">{{ item.content || '—' }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!logLoading && !logs.length" description="暂无记录" />
      </el-tab-pane>

      <el-tab-pane label="工时" name="workTime">
        <div class="work-toolbar">
          <el-input-number
            v-model="workHours"
            :min="0.1"
            :max="24"
            :step="0.5"
            :precision="1"
            placeholder="小时"
            controls-position="right"
            style="width: 160px"
          />
          <span class="unit">小时</span>
          <el-button
            v-hasPermi="'project:task:edit'"
            type="primary"
            :loading="workAddLoading"
            :disabled="!workHours"
            @click="submitWorkTime"
          >登记工时</el-button>
          <el-button @click="loadWorkTimes">刷新</el-button>
        </div>
        <el-table v-loading="workListLoading" :data="workTimes" border size="small">
          <el-table-column label="登记人" min-width="110">
            <template #default="{ row }">{{ row.nickName || row.userId }}</template>
          </el-table-column>
          <el-table-column prop="workTime" label="小时" width="80" align="right" />
          <el-table-column prop="createdTime" label="登记时间" width="160" />
          <el-table-column label="操作" width="70" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="canDeleteWorkTime(row)"
                v-hasPermi="'project:task:edit'"
                link
                type="danger"
                @click="handleDeleteWorkTime(row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="work-footer">
          <span>合计 <b>{{ totalWorkTime }}</b> 小时</span>
          <el-pagination
            v-model:current-page="workQuery.pageNum"
            :page-size="workQuery.pageSize"
            :total="workTotal"
            layout="total, prev, pager, next"
            small
            @current-change="loadWorkTimes"
          />
        </div>
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
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addChildTask,
  addComment,
  addWorkTime,
  delWorkTime,
  listChildTask,
  listTaskLog,
  listWorkTime,
} from '@/api/project/task'
import { useUserStore } from '@/stores/user'

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

/** logType → el-tag 颜色：1 动态 / 2 交付物 / 3 评论 */
const LOG_TAG_TYPE = { 1: 'info', 2: 'warning', 3: 'success' }

const childName = ref('')
const childLoading = ref(false)
const childListLoading = ref(false)
const children = ref([])

const userStore = useUserStore()
const workHours = ref(undefined)
const workAddLoading = ref(false)
const workListLoading = ref(false)
const workTimes = ref([])
const workTotal = ref(0)
const totalWorkTime = ref(0)
const workQuery = reactive({ pageNum: 1, pageSize: 10 })

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

async function loadWorkTimes() {
  if (!props.taskId) {
    workTimes.value = []
    workTotal.value = 0
    totalWorkTime.value = 0
    return
  }
  workListLoading.value = true
  try {
    const { data: res } = await listWorkTime({
      taskId: props.taskId,
      pageNum: workQuery.pageNum,
      pageSize: workQuery.pageSize,
    })
    const page = res.data?.page || {}
    workTimes.value = page.records || []
    workTotal.value = page.total || 0
    totalWorkTime.value = Number(res.data?.totalWorkTime || 0)
  } finally {
    workListLoading.value = false
  }
}

async function submitWorkTime() {
  if (!workHours.value || workHours.value <= 0) {
    ElMessage.warning('工时必须大于 0')
    return
  }
  workAddLoading.value = true
  try {
    await addWorkTime({ taskId: props.taskId, workTime: workHours.value })
    ElMessage.success('已登记')
    workHours.value = undefined
    workQuery.pageNum = 1
    await loadWorkTimes()
  } finally {
    workAddLoading.value = false
  }
}

/** 后端规则：只能删自己的，超管（userId = 1）除外；前端只做显示控制 */
function canDeleteWorkTime(row) {
  const me = userStore.userId
  if (me == null) return false
  return me === 1 || String(row.userId) === String(me)
}

async function handleDeleteWorkTime(row) {
  await ElMessageBox.confirm(`确定删除这条 ${row.workTime} 小时的工时？`, '提示', { type: 'warning' })
  await delWorkTime({ workTimeId: row.workTimeId })
  ElMessage.success('已删除')
  await loadWorkTimes()
}

watch(
  () => [props.modelValue, props.taskId],
  ([open]) => {
    if (open && props.taskId) {
      activeTab.value = 'log'
      comment.value = ''
      childName.value = ''
      logFilter.value = 'all'
      workHours.value = undefined
      workQuery.pageNum = 1
      loadLogs()
      loadChildren()
      loadWorkTimes()
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
.log-item .remark {
  margin-left: 8px;
  color: #94a3b8;
  font-size: 12px;
}
.work-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.work-toolbar .unit {
  color: #64748b;
  font-size: 13px;
}
.work-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 13px;
  color: #334155;
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
