import request from '@/utils/request'

/** GET /workflow/process/todoList */
export function todoList() {
  return request.get('/workflow/process/todoList')
}

/** GET /workflow/process/finishedList */
export function finishedList() {
  return request.get('/workflow/process/finishedList')
}

/** POST /workflow/process/deployBuiltin */
export function deployBuiltin() {
  return request.post('/workflow/process/deployBuiltin')
}

/** GET /workflow/process/definition/{processKey} */
export function latestDefinition(processKey) {
  return request.get(`/workflow/process/definition/${processKey}`)
}
