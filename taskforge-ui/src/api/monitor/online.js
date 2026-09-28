import request from '@/utils/request'

/** GET /monitor/online/list?ipaddr=&userName= */
export function listOnline(params) {
  return request.get('/monitor/online/list', { params })
}

/** DELETE /monitor/online/{tokenId} — tokenId 是会话 uuid，不是整段 JWT */
export function forceLogout(tokenId) {
  return request.delete(`/monitor/online/${tokenId}`)
}
