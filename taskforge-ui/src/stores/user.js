import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getInfo, login as loginApi } from '@/api/auth'
import { usePermissionStore } from '@/stores/permission'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('Admin-Token') || '')
  const name = ref('')
  const userId = ref(null)
  const roles = ref([])
  const permissions = ref([])

  /**
   * @param {{ username: string, password: string, code?: string, uuid?: string }} payload
   */
  async function login(payload) {
    const body = {
      username: payload.username,
      password: payload.password,
    }
    if (payload.uuid) {
      body.code = payload.code ?? ''
      body.uuid = payload.uuid
    }
    const { data: res } = await loginApi(body)
    token.value = res.data.token
    localStorage.setItem('Admin-Token', token.value)
  }

  async function fetchUserInfo() {
    const { data: res } = await getInfo()
    const payload = res.data || {}
    name.value = payload.user?.userName || ''
    userId.value = payload.user?.userId ?? null
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
    userId.value = null
    roles.value = []
    permissions.value = []
    localStorage.removeItem('Admin-Token')
  }

  return { token, name, userId, roles, permissions, login, fetchUserInfo, logout }
})
