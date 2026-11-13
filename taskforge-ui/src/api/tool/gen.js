import request from '@/utils/request'

/** GET /tool/gen/list → R<Page<GenTable>> 已导入的表 */
export function listTable(params) {
  return request.get('/tool/gen/list', { params })
}

/** GET /tool/gen/db/list → R<Page<GenTable>> 数据库中可导入的表 */
export function listDbTable(params) {
  return request.get('/tool/gen/db/list', { params })
}

/** GET /tool/gen/{tableId} → R<GenTable>（含 columns） */
export function getGenTable(tableId) {
  return request.get(`/tool/gen/${tableId}`)
}

/** POST /tool/gen/importTable  body: ["sys_notice", ...] */
export function importTable(tableNames) {
  return request.post('/tool/gen/importTable', tableNames)
}

/** PUT /tool/gen */
export function updateGenTable(data) {
  return request.put('/tool/gen', data)
}

/** DELETE /tool/gen/{tableIds} */
export function delTable(tableIds) {
  return request.delete(`/tool/gen/${tableIds}`)
}

/** GET /tool/gen/preview/{tableId} → R<Map<文件路径, 源码>> */
export function previewTable(tableId) {
  return request.get(`/tool/gen/preview/${tableId}`)
}

/** GET /tool/gen/download/{tableId} → zip */
export function downloadCode(tableId) {
  return request.get(`/tool/gen/download/${tableId}`, { responseType: 'blob' })
}
