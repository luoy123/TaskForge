import { authHasPermi } from '@/utils/auth'

/**
 * v-hasPermi="'system:user:add'"
 * v-hasPermi="['system:user:edit', 'system:user:add']"
 * 无权限则移除节点。
 */
export default {
  mounted(el, binding) {
    const { value } = binding
    if (value == null || value === '' || (Array.isArray(value) && !value.length)) {
      throw new Error(`v-hasPermi 需要权限字，例如 v-hasPermi="'system:user:add'"`)
    }
    if (!authHasPermi(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  },
}
