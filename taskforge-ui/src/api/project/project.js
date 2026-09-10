import request from '@/utils/request'

/** POST /project/list → R<Page> */
export function listProject(data) {
  return request.post('/project/list', data)
}

/** POST /project/add */
export function addProject(data) {
  return request.post('/project/add', data)
}

/** POST /project/edit */
export function editProject(data) {
  return request.post('/project/edit', data)
}

/** DELETE /project/delete  body: { projectId } */
export function delProject(data) {
  return request.delete('/project/delete', { data })
}

/** POST /project/detail  body: { projectId } */
export function getProject(data) {
  return request.post('/project/detail', data)
}
