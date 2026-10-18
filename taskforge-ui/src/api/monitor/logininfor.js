import request from '@/utils/request'

/** GET /monitor/logininfor/list → R<Page<SysLogininfor>> */
export function listLogininfor(params) {
  return request.get('/monitor/logininfor/list', { params })
}

/** DELETE /monitor/logininfor/{infoIds} */
export function delLogininfor(infoIds) {
  return request.delete(`/monitor/logininfor/${infoIds}`)
}

/** DELETE /monitor/logininfor/clean */
export function cleanLogininfor() {
  return request.delete('/monitor/logininfor/clean')
}
