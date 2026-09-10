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
