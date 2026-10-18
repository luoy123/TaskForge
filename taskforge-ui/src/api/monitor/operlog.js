import request from '@/utils/request'

/** GET /monitor/operlog/list → R<Page<SysOperLog>> */
export function listOperlog(params) {
  return request.get('/monitor/operlog/list', { params })
}

/** GET /monitor/operlog/{operId} */
export function getOperlog(operId) {
  return request.get(`/monitor/operlog/${operId}`)
}

/** DELETE /monitor/operlog/{operIds} */
export function delOperlog(operIds) {
  return request.delete(`/monitor/operlog/${operIds}`)
}

/** DELETE /monitor/operlog/clean */
export function cleanOperlog() {
  return request.delete('/monitor/operlog/clean')
}
