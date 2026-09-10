/**
 * 把扁平列表（含 id / parentId）转成树，供 el-table tree 使用。
 * @param {Array} list 原始数组
 * @param {string} idKey 主键字段，默认 id 对应 menuId/deptId 需传入
 * @param {string} parentKey 父 id 字段
 */
export function buildTree(list, idKey = 'id', parentKey = 'parentId') {
  const map = new Map()
  const roots = []
  list.forEach((item) => {
    map.set(item[idKey], { ...item, children: [] })
  })
  map.forEach((node) => {
    const pid = node[parentKey]
    if (pid && map.has(pid)) {
      map.get(pid).children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}
