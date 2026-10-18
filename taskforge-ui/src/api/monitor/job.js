import request from '@/utils/request'

export function listJob(params) {
  return request.get('/monitor/job/list', { params })
}

export function getJob(jobId) {
  return request.get(`/monitor/job/${jobId}`)
}

export function addJob(data) {
  return request.post('/monitor/job', data)
}

export function updateJob(data) {
  return request.put('/monitor/job', data)
}

export function changeJobStatus(data) {
  return request.put('/monitor/job/changeStatus', data)
}

export function runJob(data) {
  return request.put('/monitor/job/run', data)
}

export function delJob(jobIds) {
  return request.delete(`/monitor/job/${jobIds}`)
}
