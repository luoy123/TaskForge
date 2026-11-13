import request from '@/utils/request'

/** GET /monitor/cache → { info, dbSize, keyStats[] } */
export function getCache() {
  return request.get('/monitor/cache')
}
