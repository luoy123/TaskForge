<template>
  <div class="page">
    <div class="head">
      <span class="title">已办任务</span>
      <el-button @click="getList">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="taskName" label="节点" min-width="120" />
      <el-table-column prop="businessKey" label="业务主键" min-width="160" show-overflow-tooltip />
      <el-table-column prop="procInsId" label="流程实例" min-width="160" show-overflow-tooltip />
      <el-table-column prop="assignee" label="办理人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="finishTime" label="完成时间" width="180" />
      <template #empty>
        <el-empty description="暂无已办" />
      </template>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { finishedList } from '@/api/workflow/process'

const loading = ref(false)
const tableData = ref([])

async function getList() {
  loading.value = true
  try {
    const { data: res } = await finishedList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
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
.head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
</style>
