import request from '@/utils/request'

/** GET /system/post/list → R<Page<SysPost>> */
export function listPost(params) {
  return request.get('/system/post/list', { params })
}

/** GET /system/post/{postId} */
export function getPost(postId) {
  return request.get(`/system/post/${postId}`)
}

/** POST /system/post */
export function addPost(data) {
  return request.post('/system/post', data)
}

/** PUT /system/post */
export function updatePost(data) {
  return request.put('/system/post', data)
}

/** DELETE /system/post/{postIds} */
export function delPost(postIds) {
  return request.delete(`/system/post/${postIds}`)
}
