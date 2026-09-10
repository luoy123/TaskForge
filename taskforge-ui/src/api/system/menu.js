import request from '@/utils/request'

/** GET /system/menu/list → R<List<SysMenu>> 扁平列表 */
export function listMenu(params) {
  return request.get('/system/menu/list', { params })
}

export function getMenu(menuId) {
  return request.get(`/system/menu/${menuId}`)
}

export function addMenu(data) {
  return request.post('/system/menu', data)
}

export function updateMenu(data) {
  return request.put('/system/menu', data)
}

export function delMenu(menuId) {
  return request.delete(`/system/menu/${menuId}`)
}
