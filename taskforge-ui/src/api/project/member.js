import request from '@/utils/request'

/** POST /project/member/list */
export function listMember(data) {
  return request.post('/project/member/list', data)
}

/** POST /project/member/add  body: { projectId, userIdList: number[] } */
export function addMember(data) {
  return request.post('/project/member/add', data)
}

/** POST /project/member/remove  body: { projectId, userIdList: number[] } */
export function removeMember(data) {
  return request.post('/project/member/remove', data)
}
