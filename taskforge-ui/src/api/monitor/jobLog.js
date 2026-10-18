import request from '@/utils/request'

export function listJobLog(params) {
  return request.get('/monitor/jobLog/list', { params })
}

export function delJobLog(jobLogIds) {
  return request.delete(`/monitor/jobLog/${jobLogIds}`)
}

export function cleanJobLog() {
  return request.delete('/monitor/jobLog/clean')
}
