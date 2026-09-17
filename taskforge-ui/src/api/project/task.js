import request from '@/utils/request'

/** POST /project/task/list */
export function listTask(data) {
  return request.post('/project/task/list', data)
}

/** POST /project/task/add */
export function addTask(data) {
  return request.post('/project/task/add', data)
}

/** POST /project/task/edit */
export function editTask(data) {
  return request.post('/project/task/edit', data)
}

/** POST /project/task/delete  body: { taskId } */
export function delTask(data) {
  return request.post('/project/task/delete', data)
}

/** POST /project/task/addComment  body: { taskId, comment, projectId? } */
export function addComment(data) {
  return request.post('/project/task/addComment', data)
}

/** POST /project/task/log/list  body: { taskId, logType? } */
export function listTaskLog(data) {
  return request.post('/project/task/log/list', data)
}

/** POST /project/task/addChildTask */
export function addChildTask(data) {
  return request.post('/project/task/addChildTask', data)
}

/** POST /project/task/queryChildTask  body: { taskId } */
export function listChildTask(data) {
  return request.post('/project/task/queryChildTask', data)
}

/** POST /project/task/queryMyTaskList  type: 1 我执行 / 3 我创建 */
export function queryMyTaskList(data) {
  return request.post('/project/task/queryMyTaskList', data)
}

/** POST /project/task/situation  body: { projectId } */
export function taskSituation(data) {
  return request.post('/project/task/situation', data)
}

/** POST /project/task/export  body: { taskIds: string[] } → blob */
export function exportTasks(data) {
  return request.post('/project/task/export', data, { responseType: 'blob' })
}

/** POST /project/task/exportAll → blob */
export function exportAllTasks() {
  return request.post('/project/task/exportAll', {}, { responseType: 'blob' })
}

/** POST /project/task/import  multipart file */
export function importTasks(formData) {
  return request.post('/project/task/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
