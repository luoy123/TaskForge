<template>
  <div class="building">
    <h2>{{ title }}</h2>
    <p>该菜单对应的前端页面尚未接入（component: {{ component }}）。</p>
    <p class="hint">Sprint 03：未知组件会落到本占位页，避免整站白屏。</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta?.title || '建设中')
const component = computed(() => route.meta?.component || '—')
</script>

<style scoped>
.building {
  padding: 24px;
  color: #334155;
}
.hint {
  color: #64748b;
  font-size: 13px;
}
</style>
