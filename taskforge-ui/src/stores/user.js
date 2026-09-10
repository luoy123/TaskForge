import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getInfo, login as loginApi } from '@/api/auth'
import { usePermissionStore } from '@/stores/permission'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('Admin-Token') || '')
  const name = ref('')
  const roles = ref([])
  const permissions = ref([])

  async function login(username, password) {
    const { data: res } = await loginApi({ username, password })
    // ========== 【学员填写 ②】登录成功后保存 token ==========
    // res 是后端 R：{ code, message, data: { token: '...' } }
    // 需要：1) 赋给 token.value   2) 写入 localStorage 键名 Admin-Token
    // 提示：
    //   token.value = res.data.token
    //   localStorage.setItem('Admin-Token', token.value)
    //
    // 在下面写（大约 2 行）：
    token.value = res.data.token
    localStorage.setItem('Admin-Token', token.value)
    // ========== 填写结束 ==========
  }

  async function fetchUserInfo() {
    const { data: res } = await getInfo()
    const payload = res.data || {}
    name.value = payload.user?.userName || ''
    roles.value = payload.roles || []
    permissions.value = payload.permissions || []
  }

  function logout() {
    try {
      usePermissionStore().resetRoutes()
    } catch {
      // pinia 未就绪时忽略
    }
    token.value = ''
    name.value = ''
    roles.value = []
    permissions.value = []
    localStorage.removeItem('Admin-Token')
  }

  return { token, name, roles, permissions, login, fetchUserInfo, logout }
})
