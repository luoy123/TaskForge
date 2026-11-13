import request from '@/utils/request'

/** GET /monitor/server — CPU 要采样 1 秒，接口至少耗时 1 秒 */
export function getServer() {
  return request.get('/monitor/server')
}

/** GET /monitor/server/pool — Hikari 连接池 active / idle / total / waiting / max */
export function getPool() {
  return request.get('/monitor/server/pool')
}
