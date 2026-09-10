<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">TaskForge</div>
      <el-menu :default-active="active" router>
        <el-menu-item index="/home">
          <span>首页</span>
        </el-menu-item>

        <!-- F03：动态侧栏（填完 R1 + R4 后会出现 getRouters 菜单） -->
        <SidebarItem
          v-for="route in permissionStore.sidebarRouters"
          :key="route.path || route.name"
          :item="route"
        />

        <!-- F03 过渡：静态菜单兜底；动态侧栏 OK 后整段删除 -->
        <template v-if="!permissionStore.sidebarRouters.length">
          <el-sub-menu index="system">
            <template #title>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user"><span>用户管理</span></el-menu-item>
            <el-menu-item index="/system/role"><span>角色管理</span></el-menu-item>
            <el-menu-item index="/system/menu"><span>菜单管理</span></el-menu-item>
            <el-menu-item index="/system/dept"><span>部门管理</span></el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="project">
            <template #title>
              <span>项目管理</span>
            </template>
            <el-menu-item index="/project/list"><span>项目列表</span></el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ userStore.name || '未拉取用户信息' }}</span>
        <el-button link type="primary" @click="onLogout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const active = computed(() => route.path)

onMounted(async () => {
  if (userStore.token && !userStore.name) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // 401 时 request 会跳登录
    }
  }
})

function onLogout() {
  permissionStore.resetRoutes()
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout {
  min-height: 100vh;
}

.aside {
  background: #0f172a;
  color: #e2e8f0;

  .brand {
    height: 56px;
    display: grid;
    place-items: center;
    font-weight: 700;
    letter-spacing: 0.06em;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  :deep(.el-menu) {
    border-right: none;
    background: transparent;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    color: #cbd5e1;
  }

  :deep(.el-menu-item.is-active) {
    color: #fff;
    background: rgba(15, 118, 110, 0.35);
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
}
</style>
