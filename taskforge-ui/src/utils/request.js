import axios from 'axios'
import { ElMessage } from 'element-plus'

/** 对齐后端 R<T>：{ code, message, data }，成功 code === 200 */
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 15000,
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('Admin-Token')
    // ========== 【学员填写 ①】有 token 时带上请求头 ==========
    // 后端 JwtFilter 要：Authorization: Bearer <token>
    // 提示：if (token) { config.headers.Authorization = `Bearer ${token}` }
    //
    // 在下面写（大约 2～3 行）：
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // ========== 填写结束 ==========
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('Admin-Token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  },
)

export default service
