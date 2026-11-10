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
        v-model="newName"
        clearable
        maxlength="30"
        placeholder="新阶段名称"
        style="flex: 1"
        @keyup.enter="submitAdd"
      />
      <el-button
        v-hasPermi="'project:stage:add'"
        type="success"
        :loading="addLoading"
        :disabled="!newName.trim()"
        @click="submitAdd"
      >新增阶段</el-button>
    </div>

    <el-table v-loading="loading" :data="stages" border size="small">
      <el-table-column label="顺序" width="70" align="center">
        <template #default="{ row }">{{ row.stageCode }}</template>
      </el-table-column>
      <el-table-column label="名称" min-width="160">
        <template #default="{ row }">
          <span>{{ row.stageName }}</span>
          <el-tag v-if="row.stageId === currentStageId" size="small" type="success" class="cur">当前</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="'project:manage:edit'"
            link
            type="success"
            :disabled="row.stageId === currentStageId"
            @click="handleSetCurrent(row)"
          >设为当前</el-button>
          <el-button
            v-hasPermi="'project:stage:edit'"
            link
            type="primary"
            @click="handleRename(row)"
          >改名</el-button>
          <el-button
            v-hasPermi="'project:stage:delete'"
            link
            type="danger"
            :disabled="row.stageId === currentStageId"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <p class="tip">当前阶段不能删除；阶段下还有任务时也删不掉。新建任务会落在当前阶段。</p>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addStage, delStage, editStage, listStage, setCurrentStage } from '@/api/project/stage'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  projectId: { type: String, default: '' },
  projectName: { type: String, default: '' },
  /** 项目列表行里的 stageCode，用来标出当前阶段 */
  currentStageCode: { type: Number, default: null },
})

const emit = defineEmits(['update:modelValue', 'changed'])

const drawerTitle = computed(() => (props.projectName ? `阶段 · ${props.projectName}` : '项目阶段'))

const loading = ref(false)
const stages = ref([])
const currentStageId = ref('')
const newName = ref('')
const addLoading = ref(false)

async function getList() {
  if (!props.projectId) {
    stages.value = []
    return
  }
  loading.value = true
  try {
    const { data: res } = await listStage({ projectId: props.projectId })
    stages.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function submitAdd() {
  const name = newName.value.trim()
  if (!name) return
  addLoading.value = true
  try {
    await addStage({ projectId: props.projectId, stageName: name })
    ElMessage.success('已新增')
    newName.value = ''
    await getList()
  } finally {
    addLoading.value = false
  }
}

async function handleRename(row) {
  const { value } = await ElMessageBox.prompt('新的阶段名称', '改名', {
    inputValue: row.stageName,
    inputValidator: (v) => (v && v.trim() ? true : '名称不能为空'),
  })
  const name = value.trim()
  if (name === row.stageName) return
  await editStage({ stageId: row.stageId, stageName: name })
  ElMessage.success('已改名')
  await getList()
  emit('changed')
}

async function handleSetCurrent(row) {
  await setCurrentStage({ stageId: row.stageId, projectId: props.projectId })
  currentStageId.value = row.stageId
  ElMessage.success(`当前阶段：${row.stageName}`)
  emit('changed')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除阶段「${row.stageName}」？`, '提示', { type: 'warning' })
  await delStage({ stageId: row.stageId })
  ElMessage.success('已删除')
  await getList()
  emit('changed')
}

watch(
  () => [props.modelValue, props.projectId],
  async ([open]) => {
    if (!open) return
    newName.value = ''
    currentStageId.value = ''
    await getList()
    const cur = stages.value.find((s) => s.stageCode === props.currentStageCode)
    currentStageId.value = cur ? cur.stageId : ''
  },
)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.cur {
  margin-left: 6px;
}
.tip {
  margin-top: 10px;
  font-size: 12px;
  color: #94a3b8;
}
</style>
