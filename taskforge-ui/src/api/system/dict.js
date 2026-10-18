import request from '@/utils/request'

/** ---------- 字典类型 ---------- */

/** GET /system/dict/type/list → R<Page<SysDictType>> */
export function listDictType(params) {
  return request.get('/system/dict/type/list', { params })
}

/** GET /system/dict/type/{dictId} */
export function getDictType(dictId) {
  return request.get(`/system/dict/type/${dictId}`)
}

/** POST /system/dict/type */
export function addDictType(data) {
  return request.post('/system/dict/type', data)
}

/** PUT /system/dict/type */
export function updateDictType(data) {
  return request.put('/system/dict/type', data)
}

/** DELETE /system/dict/type/{dictIds} */
export function delDictType(dictIds) {
  return request.delete(`/system/dict/type/${dictIds}`)
}

/** ---------- 字典数据 ---------- */

/** GET /system/dict/data/list → R<Page<SysDictData>> */
export function listDictData(params) {
  return request.get('/system/dict/data/list', { params })
}

/** GET /system/dict/data/{dictCode} */
export function getDictData(dictCode) {
  return request.get(`/system/dict/data/${dictCode}`)
}

/** GET /system/dict/data/type/{dictType} → R<List> */
export function getDicts(dictType) {
  return request.get(`/system/dict/data/type/${dictType}`)
}

/** POST /system/dict/data */
export function addDictData(data) {
  return request.post('/system/dict/data', data)
}

/** PUT /system/dict/data */
export function updateDictData(data) {
  return request.put('/system/dict/data', data)
}

/** DELETE /system/dict/data/{dictCodes} */
export function delDictData(dictCodes) {
  return request.delete(`/system/dict/data/${dictCodes}`)
}
