package com.zhq.taskforge.system.service;

import com.zhq.taskforge.common.core.domain.TreeSelect;
import com.zhq.taskforge.system.domain.vo.RouterVo;
import com.zhq.taskforge.common.core.domain.entity.SysMenu;

import java.util.List;

public interface ISysMenuService {
    List<SysMenu> selectMenuList(SysMenu sysMenu);

    List<TreeSelect> buildMenuTreeSelect(List<SysMenu> sysMenuList);

    List<Long> selectMenuListByRoleId(Long roleId);

    SysMenu selectMenuById(Long menuId);

    void addMenu(SysMenu sysMenu);

    void updateMenu(SysMenu sysMenu);

    void deleteMenu(Long menuId);

    List<SysMenu> selectMenuTreeByUserId(Long userId);

    List<RouterVo> buildMenus(List<SysMenu> menus);
}
