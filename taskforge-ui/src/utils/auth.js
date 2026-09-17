import { useUserStore } from '@/stores/user'

/**
 * 是否具备权限字（超管 *:*:* 放行）。
 * @param {string|string[]} value
 */
export function authHasPermi(value) {
  const userStore = useUserStore()
  const all = userStore.permissions || []
  if (!all.length) {
    return false
  }
  if (all.includes('*:*:*')) {
    return true
  }
  const need = Array.isArray(value) ? value : [value]
  return need.some((p) => p && all.includes(p))
}
