-- =============================================================================
-- TaskForge 手工变更 SQL 记录（changelog）
-- =============================================================================
-- 用途：
--   记录相对基线库（如 sql/pmhub_20240305.sql）之后，本地/联调做过的增量修改。
--   之后凡菜单、权限、字典、补丁数据等 SQL 变更，都追加到本文件，不要再拆零散文件。
--
-- 约定：
--   1. 按时间倒序不强制；按「变更块」追加，块与块之间用分隔线分开。
--   2. 每个变更块必须写清：日期、Sprint/原因、影响表、是否幂等、如何回滚（可选）。
--   3. 尽量写成可重复执行（WHERE NOT EXISTS / INSERT ... SELECT）。
--   4. 基线全量脚本仍用 pmhub_20240305.sql / quartz.sql，勿往里塞临时补丁。
--
-- 库名：默认 `laigeoffer-pmhub`（与本地 application-dev.yml 一致）
-- =============================================================================

USE `laigeoffer-pmhub`;

-- -----------------------------------------------------------------------------
-- 2026-09-04 | Sprint E4 | 补齐「取消项目归档」菜单权限
-- 原因：Controller 使用 project:manage:cancelArchive，但 sys_menu 无对应按钮，
--       导致 Access Denied（表现为业务码 500）。
-- 影响表：sys_menu、sys_role_menu
-- 幂等：是（按 perms / role_menu 去重）
-- 回滚（可选）：
--   DELETE FROM sys_role_menu WHERE menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'project:manage:cancelArchive');
--   DELETE FROM sys_menu WHERE perms = 'project:manage:cancelArchive';
-- -----------------------------------------------------------------------------

-- 按钮权限：与「项目归档」同挂在父菜单 2002；order_num=12（归档=10、退出=11）
INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '取消项目归档', 2002, 12, '', NULL, NULL,
  1, 0, 'F', '0', '0', 'project:manage:cancelArchive', '#',
  'admin', NOW(), '', NULL, 'Sprint E4 补齐 cancelArchive'
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE perms = 'project:manage:cancelArchive'
);

SET @mid := (SELECT menu_id FROM sys_menu WHERE perms = 'project:manage:cancelArchive' LIMIT 1);

-- 赋给原先已有「项目归档」(menu_id=2039) 的角色，保持权限集合一致
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, @mid
FROM (SELECT 2 AS role_id UNION SELECT 102 UNION SELECT 107 UNION SELECT 109) r
WHERE @mid IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = @mid
  );

-- 说明：超级管理员 admin（user_id=1）走 selectPermsAll，菜单入库后重新登录即可；
--       普通角色依赖上面的 sys_role_menu；改权限后建议重新登录刷新 Redis 中的 LoginUser。

-- -----------------------------------------------------------------------------
-- （后续变更请追加在下方，保持同样注释格式）
-- -----------------------------------------------------------------------------
