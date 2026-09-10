<template>
  <!-- 【学员填写 R4】结构已给：有可见 children → el-sub-menu，否则 el-menu-item；请完成 resolvePath -->
  <template v-if="!item.hidden">
    <el-sub-menu v-if="hasChildren" :index="resolvePath(item.path)">
      <template #title>
        <span>{{ item.meta?.title || item.name }}</span>
      </template>
      <SidebarItem
        v-for="child in visibleChildren"
        :key="child.name || child.path"
        :item="child"
        :base-path="resolvePath(item.path)"
      />
    </el-sub-menu>
    <el-menu-item v-else :index="resolvePath(item.path)">
      <span>{{ item.meta?.title || item.name }}</span>
    </el-menu-item>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import SidebarItem from './SidebarItem.vue'

const props = defineProps({
  item: { type: Object, required: true },
  /** 父级已拼好的绝对 path，如 /system */
  basePath: { type: String, default: '' },
})

const visibleChildren = computed(() => (props.item.children || []).filter((c) => !c.hidden))
const hasChildren = computed(() => visibleChildren.value.length > 0)

/**
 * 【学员填写 R4-path】拼 el-menu 的 index（必须等于路由完整 path）
 * - path 以 / 开头 → 直接返回
 * - 否则：basePath + '/' + path（注意去掉重复斜杠）
 */
function resolvePath(routePath) {
  // ========== 【学员填写 R4-path】开始 ==========
  // 在下面写：
  void routePath
  void props
  return props.basePath || '/'
  // ========== 【学员填写 R4-path】结束 ==========
}
</script>
