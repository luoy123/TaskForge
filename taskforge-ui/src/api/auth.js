import request from '@/utils/request'

/** POST /login → R<{ token }> */
export function login(data) {
  return request.post('/login', data)
}

/** GET /getInfo */
export function getInfo() {
  return request.get('/getInfo')
}

/** GET /getRouters */
export function getRouters() {
  return request.get('/getRouters')
}
