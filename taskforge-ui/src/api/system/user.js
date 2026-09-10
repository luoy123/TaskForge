import request from '@/utils/request'

/** GET /system/user/list → R<Page<SysUser>>，Page 里常用 records / total */
export function listUser(params) {
  return request.get('/system/user/list', { params })
}

/** GET /system/user/{userId} */
export function getUser(userId) {
  return request.get(`/system/user/${userId}`)
}

/** POST /system/user */
export function addUser(data) {
  return request.post('/system/user', data)
}

/** PUT /system/user */
export function updateUser(data) {
  return request.put('/system/user', data)
}

/** DELETE /system/user/{userIds}  多个 id 用逗号拼接，如 1,2,3 */
export function delUser(userIds) {
  return request.delete(`/system/user/${userIds}`)
}
