import request from '@/utils/request'

/** POST /login → R<{ token }>；开启验证码时 body 带 code + uuid */
export function login(data) {
  return request.post('/login', data)
}

/** GET /captchaImage → R<{ captchaEnabled, uuid?, img? }>（无需 Token） */
export function getCaptchaImage() {
  return request.get('/captchaImage')
}

/** GET /getInfo */
export function getInfo() {
  return request.get('/getInfo')
}

/** GET /getRouters */
export function getRouters() {
  return request.get('/getRouters')
}
