import request from '@/utils/request'

/** GET /system/dept/list → R<List<SysDept>> 已是树形 children */
export function listDept(params) {
  return request.get('/system/dept/list', { params })
}

export function getDept(deptId) {
  return request.get(`/system/dept/${deptId}`)
}

export function addDept(data) {
  return request.post('/system/dept', data)
}

export function updateDept(data) {
  return request.put('/system/dept', data)
}

export function delDept(deptId) {
  return request.delete(`/system/dept/${deptId}`)
}
