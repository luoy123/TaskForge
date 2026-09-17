import request from '@/utils/request'

/** POST /workflow/task/complete  body: { taskId, comment?, variables? } */
export function completeTask(data) {
  return request.post('/workflow/task/complete', data)
}

/** POST /workflow/task/reject  body: { taskId, comment? } */
export function rejectTask(data) {
  return request.post('/workflow/task/reject', data)
}

/** POST /workflow/task/startTaskApprove  body: { taskId, approver } */
export function startTaskApprove(data) {
  return request.post('/workflow/task/startTaskApprove', data)
}

/** POST /workflow/task/startProjectApprove  body: { projectId, approver } */
export function startProjectApprove(data) {
  return request.post('/workflow/task/startProjectApprove', data)
}
