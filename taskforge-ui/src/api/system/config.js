import request from '@/utils/request'

/** GET /system/config/list → R<Page<SysConfig>> */
export function listConfig(params) {
  return request.get('/system/config/list', { params })
}

/** GET /system/config/{configId} */
export function getConfig(configId) {
  return request.get(`/system/config/${configId}`)
}

/** GET /system/config/configKey/{configKey} */
export function getConfigKey(configKey) {
  return request.get(`/system/config/configKey/${configKey}`)
}

/** POST /system/config */
export function addConfig(data) {
  return request.post('/system/config', data)
}

/** PUT /system/config */
export function updateConfig(data) {
  return request.put('/system/config', data)
}

/** DELETE /system/config/{configIds} */
export function delConfig(configIds) {
  return request.delete(`/system/config/${configIds}`)
}

/** DELETE /system/config/refresheCache（后端路径拼写如此） */
export function refreshConfigCache() {
  return request.delete('/system/config/refresheCache')
}
