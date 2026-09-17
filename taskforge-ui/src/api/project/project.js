import request from '@/utils/request'

/** POST /project/list → R<Page>；可带 type: my | collect | recycle */
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

/** POST /project/archive */
export function archiveProject(data) {
  return request.post('/project/archive', data)
}

/** POST /project/cancelArchive */
export function cancelArchiveProject(data) {
  return request.post('/project/cancelArchive', data)
}

/** POST /project/collect */
export function collectProject(data) {
  return request.post('/project/collect', data)
}

/** POST /project/cancelCollect */
export function cancelCollectProject(data) {
  return request.post('/project/cancelCollect', data)
}

/** GET /project/statistics */
export function projectStatistics() {
  return request.get('/project/statistics')
}

/** GET /project/doing */
export function projectDoing() {
  return request.get('/project/doing')
}

/** GET /project/select */
export function projectSelect() {
  return request.get('/project/select')
}
