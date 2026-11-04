<template>
  <el-tag v-if="meta" :type="meta.type" size="small" effect="light">{{ meta.label }}</el-tag>
  <span v-else class="none">—</span>
</template>

<script setup>
import { computed } from 'vue'

/** 后端 approved 为字符串 '0' / '1' / '2'；未发起过审批为 null */
const APPROVAL_META = {
  0: { label: '审批中', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'danger' },
}

const props = defineProps({
  approved: { type: [String, Number], default: null },
})

const meta = computed(() =>
  props.approved === null || props.approved === undefined || props.approved === ''
    ? null
    : APPROVAL_META[props.approved],
)
</script>

<style scoped>
.none {
  color: #cbd5e1;
}
</style>
