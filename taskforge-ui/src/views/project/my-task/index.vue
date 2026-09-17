<template>
  <div class="page">
    <div class="head">
      <span class="title">我的任务</span>
    </div>

    <el-tabs v-model="query.type" @tab-change="handleTypeChange">
      <el-tab-pane label="我执行的" name="1" />
      <el-tab-pane label="我创建的" name="3" />
    </el-tabs>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="taskName" label="任务名称" min-width="160" />
      <el-table-column prop="projectName" label="所属项目" min-width="140" />
      <el-table-column prop="statusName" label="状态" width="100" />
      <el-table-column prop="priorityName" label="优先级" width="100" />
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.projectId"
            link
            type="primary"
            @click="goProjectTasks(row)"
          >进项目</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { queryMyTaskList } from '@/api/project/task'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  /** '1' 我执行 / '3' 我创建 */
  type: '1',
  pageNum: 1,
  pageSize: 10,
})

function handleTypeChange() {
  query.pageNum = 1
  getList()
}

function goProjectTasks(row) {
  router.push({ path: '/project/task', query: { projectId: row.projectId } })
}

/** 【学员填写 F04-7】queryMyTaskList({ type, pageNum, pageSize }) */
async function getList() {
  loading.value = true
  try {
    const { data: res } = await queryMyTaskList({
      type: Number(query.type),
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
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
.head .title {
  font-size: 18px;
  font-weight: 600;
}
.pager {
  display: flex;
  justify-content: flex-end;
}
</style>
