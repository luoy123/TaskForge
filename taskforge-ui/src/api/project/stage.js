import request from '@/utils/request'

/** POST /project/stage/list  body: { projectId } → 按 stageCode 升序 */
export function listStage(data) {
  return request.post('/project/stage/list', data)
}

/** POST /project/stage/add  body: { projectId, stageName } */
export function addStage(data) {
  return request.post('/project/stage/add', data)
}

/** POST /project/stage/edit  body: { stageId, stageName }（只改名称） */
export function editStage(data) {
  return request.post('/project/stage/edit', data)
}

/** DELETE /project/stage/delete  body: { stageId } */
export function delStage(data) {
  return request.delete('/project/stage/delete', { data })
}

/** POST /project/stage/setCurrent  body: { stageId, projectId } */
export function setCurrentStage(data) {
  return request.post('/project/stage/setCurrent', data)
}
