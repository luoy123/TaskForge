import request from '@/utils/request'

/** GET /system/notice/list → R<Page<SysNotice>> */
export function listNotice(params) {
  return request.get('/system/notice/list', { params })
}

/** GET /system/notice/{noticeId} */
export function getNotice(noticeId) {
  return request.get(`/system/notice/${noticeId}`)
}

/** POST /system/notice */
export function addNotice(data) {
  return request.post('/system/notice', data)
}

/** PUT /system/notice */
export function updateNotice(data) {
  return request.put('/system/notice', data)
}

/** DELETE /system/notice/{noticeIds} */
export function delNotice(noticeIds) {
  return request.delete(`/system/notice/${noticeIds}`)
}
