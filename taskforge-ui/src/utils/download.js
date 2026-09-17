/**
 * 触发浏览器下载 blob。
 * 若后端错误时返回 JSON blob，调用方需自行解析（见 saveBlobResponse）。
 */
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'download'
  a.click()
  window.URL.revokeObjectURL(url)
}

/**
 * axios blob 响应：若 Content-Type 是 json，当作业务错误抛出。
 * 【学员填写】可在此完善错误 JSON 解析。
 */
export async function saveBlobResponse(response, filename) {
  const blob = response.data
  const type = response.headers?.['content-type'] || ''
  if (type.includes('application/json')) {
    const text = await blob.text()
    const json = JSON.parse(text)
    throw new Error(json.message || '下载失败')
  }
  downloadBlob(blob, filename)
}
