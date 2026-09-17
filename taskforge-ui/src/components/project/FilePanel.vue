<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    size="520px"
    destroy-on-close
    @close="emit('update:modelValue', false)"
  >
    <div class="toolbar">
      <el-upload
        v-hasPermi="'project:file:upload'"
        :show-file-list="false"
        :http-request="handleUpload"
        :disabled="!bizId"
      >
        <el-button type="primary" :disabled="!bizId">上传附件</el-button>
      </el-upload>
      <el-button @click="getList" :disabled="!bizId">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border size="small">
      <el-table-column prop="fileName" label="文件名" min-width="140" show-overflow-tooltip />
      <el-table-column prop="extension" label="类型" width="70" />
      <el-table-column prop="nickName" label="上传人" width="90" />
      <el-table-column prop="createdTime" label="时间" width="150" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
          <el-button
            v-hasPermi="'project:file:rename'"
            link
            type="primary"
            @click="handleRename(row)"
          >重命名</el-button>
          <el-button
            v-hasPermi="'project:file:delete'"
            link
            type="danger"
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
        :page-sizes="[10, 20]"
        layout="total, prev, pager, next"
        small
        @current-change="getList"
        @size-change="getList"
      />
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteFile, downloadFile, listFile, renameFile, uploadFile } from '@/api/project/file'
import { saveBlobResponse } from '@/utils/download'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 项目 id 或任务 id */
  bizId: { type: String, default: '' },
  /** project | task */
  fileType: { type: String, default: 'project' },
})

const emit = defineEmits(['update:modelValue'])

const title = computed(() => (props.fileType === 'task' ? '任务附件' : '项目附件'))

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

/** 【学员填写 F04-5】listFile({ id: bizId, type: fileType, pageNum, pageSize }) */
async function getList() {
  if (!props.bizId) {
    tableData.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const { data: res } = await listFile({
      id: props.bizId,
      type: props.fileType,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 【学员填写 F04-5】multipart：
 * form.append('file', option.file)
 * form.append('id', bizId)
 * form.append('type', fileType)
 */
async function handleUpload(option) {
  const form = new FormData()
  form.append('file', option.file)
  form.append('id', props.bizId)
  form.append('type', props.fileType)
  try {
    await uploadFile(form)
    ElMessage.success('上传成功')
    option.onSuccess?.()
    getList()
  } catch (e) {
    option.onError?.(e)
  }
}

async function handleDownload(row) {
  try {
    const response = await downloadFile(row.fileId)
    await saveBlobResponse(response, row.fileName || 'download')
  } catch (e) {
    ElMessage.error(e.message || '下载失败')
  }
}

async function handleRename(row) {
  const { value } = await ElMessageBox.prompt('新文件名', '重命名', {
    inputValue: row.fileName,
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
  await renameFile({ fileId: row.fileId, fileName: value })
  ElMessage.success('已重命名')
  getList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.fileName}」？`, '提示', { type: 'warning' })
  await deleteFile({ fileIds: [row.fileId] })
  ElMessage.success('已删除')
  getList()
}

watch(
  () => [props.modelValue, props.bizId, props.fileType],
  ([open]) => {
    if (open) {
      query.pageNum = 1
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
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
