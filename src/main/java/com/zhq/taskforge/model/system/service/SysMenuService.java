package com.zhq.taskforge.model.system.service;

import com.zhq.taskforge.auth.vo.RouterVo;
import com.zhq.taskforge.common.TreeSelect;
import com.zhq.taskforge.model.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService {
    List<SysMenu> selectMenuList(SysMenu sysMenu);

    List<TreeSelect> buildMenuTreeSelect(List<SysMenu> sysMenuList);

    List<Long> selectMenuListByRoleId(Long roleId);

    SysMenu selectMenuById(Long menuId);

    void addMenu(SysMenu sysMenu);

    void updateMenu(SysMenu sysMenu);

    void deleteMenu(Long menuId);

    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 返回前端路由需要的对象
     * @param menus
     * @return
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);
}
