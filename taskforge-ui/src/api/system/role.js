import request from '@/utils/request'

export function listRole(params) {
  return request.get('/system/role/list', { params })
}

export function getRole(roleId) {
  return request.get(`/system/role/${roleId}`)
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function delRole(roleIds) {
  return request.delete(`/system/role/${roleIds}`)
}
