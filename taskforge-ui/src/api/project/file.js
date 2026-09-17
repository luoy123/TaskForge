import request from '@/utils/request'

/**
 * 上传：multipart
 * form: file, id（项目或任务 id）, type=project|task|cover
 */
export function uploadFile(formData) {
  return request.post('/project/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** POST /project/file/list  body: { id, type?, pageNum, pageSize } */
export function listFile(data) {
  return request.post('/project/file/list', data)
}

/** POST /project/file/rename  body: { fileId, fileName } */
export function renameFile(data) {
  return request.post('/project/file/rename', data)
}

/** POST /project/file/delete  body: { fileIds: string[] } */
export function deleteFile(data) {
  return request.post('/project/file/delete', data)
}

/** GET /project/file/download?fileId=  → blob */
export function downloadFile(fileId) {
  return request.get('/project/file/download', {
    params: { fileId },
    responseType: 'blob',
  })
}
