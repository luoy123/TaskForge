package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.common.core.domain.TreeSelect;
import com.zhq.taskforge.system.domain.vo.MetaVo;
import com.zhq.taskforge.system.domain.vo.RouterVo;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.core.domain.entity.SysMenu;
import com.zhq.taskforge.system.domain.SysRoleMenu;
import com.zhq.taskforge.system.mapper.SysMenuMapper;
import com.zhq.taskforge.system.mapper.SysRoleMenuMapper;
import com.zhq.taskforge.system.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl implements ISysMenuService {

    @Autowired
    SysMenuMapper sysMenuMapper;

    @Autowired
    SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> selectMenuList(SysMenu sysMenu) {
        LambdaQueryWrapper<SysMenu> qw = new LambdaQueryWrapper<>();
        if(sysMenu != null){
            if(StringUtils.hasText(sysMenu.getMenuName())){
                qw.like(SysMenu::getMenuName,sysMenu.getMenuName());
            }
            if(StringUtils.hasText(sysMenu.getVisible())){
                qw.eq(SysMenu::getVisible,sysMenu.getVisible());
            }
            if(StringUtils.hasText(sysMenu.getStatus())){
                qw.eq(SysMenu::getStatus,sysMenu.getStatus());
            }
        }

        qw.orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getOrderNum);

       return  sysMenuMapper.selectList(qw);
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> sysMenuList) {
        List<SysMenu> sysMenuList1 = buildMenuTree(sysMenuList);
        return sysMenuList1.stream()
                .map(TreeSelect::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        if(roleId == null){
            throw new ServiceException("roleId不能为空");
        }
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
        return sysRoleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    @Override
    public SysMenu selectMenuById(Long menuId) {
        if(menuId == null){
            throw new ServiceException("menuId不能为空");
        }
        SysMenu sysMenu = sysMenuMapper.selectById(menuId);
        if(sysMenu == null){
            throw new ServiceException("菜单项为空");
        }
        return sysMenu;
    }

    @Override
    public void addMenu(SysMenu sysMenu) {
        checkMenuNameUnique(sysMenu);

        checkExternalLink(sysMenu);

        sysMenu.setCreateTime(LocalDateTime.now());
        sysMenuMapper.insert(sysMenu);
    }

    @Override
    public void updateMenu(SysMenu sysMenu) {
        if(sysMenu.getMenuId() == null){
            throw new ServiceException("menuId不能为空");
        }

        checkMenuNameUnique(sysMenu);

        checkExternalLink(sysMenu);

        if(sysMenu.getMenuId().equals(sysMenu.getParentId())){
           throw new ServiceException("不能将父级设置成自己");
        }

        sysMenu.setUpdateTime(LocalDateTime.now());

        sysMenuMapper.updateById(sysMenu);

    }

    @Override
    public void deleteMenu(Long menuId) {
        if(menuId == null){
            throw  new ServiceException("menuId不能为空");
        }
        if(hasChildrenByMenuId(menuId)){
            throw  new ServiceException("存在子菜单，无法删除");
        }
        if(checkMenuExistRole(menuId)){
            throw  new ServiceException("该菜单分配给角色，无法删除");
        }
        sysMenuMapper.deleteById(menuId);
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {
        if(userId == null){
            throw new ServiceException("userId不能为空");
        }
        List<SysMenu> menus;
        if(Long.valueOf(1L).equals(userId)){
            menus = sysMenuMapper.selectMenuTreeAll();
        }else {
            menus = sysMenuMapper.selectMenuTreeByUserId(userId);
        }
    return buildMenuTree(menus);

    }

    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routerVoList = new ArrayList<>();

        for(SysMenu menu : menus){
            RouterVo routerVo = new RouterVo();
            routerVo.setName(getRouteName(menu));
            routerVo.setPath(getRoutePath(menu));
            routerVo.setComponent(getComponent(menu));
            routerVo.setHidden("1".equals(menu.getVisible()));
            routerVo.setQuery(menu.getQuery());
            routerVo.setMeta(
                    new MetaVo(
                            menu.getMenuName(),
                            menu.getIcon(),
                            Integer.valueOf(1).equals(menu.getIsCache())
                    )
            );
            List<SysMenu> children = menu.getChildren();
            if(children != null && !children.isEmpty() && "M".equals(menu.getMenuType())){
                routerVo.setAlwaysShow(true);
                routerVo.setRedirect("noRedirect");
                routerVo.setChildren(buildMenus(children));
            }
            routerVoList.add(routerVo);
        }
        return routerVoList;
    }

    private String getComponent(SysMenu menu){
        if("M".equals(menu.getMenuType())){
            return "Layout";
        }
        return menu.getComponent();
    }

    private String getRoutePath(SysMenu menu){
        if(menu.getParentId() != null &&  Long.valueOf(0L).equals(menu.getParentId())  && "M".equals(menu.getMenuType())){
            return "/" + menu.getPath();
        }
        return menu.getPath();
    }

    private String getRouteName(SysMenu menu){
        String path = menu.getPath();
        if(!StringUtils.hasText(path)){
            return "";
        }
        return path.substring(0,1).toUpperCase() + path.substring(1);
    }

    private boolean hasChildrenByMenuId(Long menuId){
        Long count = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, menuId)
        );

        return count > 0;
    }
    private boolean checkMenuExistRole(Long menuId){
        Long count = sysRoleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getMenuId, menuId)
        );

        return count > 0;
    }

    private void checkMenuNameUnique(SysMenu menu){
        Long menuId = menu.getMenuId() == null ? -1L : menu.getMenuId();
        Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();

        SysMenu sysMenu = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, menu.getMenuName())
                        .eq(SysMenu::getParentId, parentId)
                        .last("limit 1")
        );
        if(sysMenu != null && !sysMenu.getMenuId().equals(menuId)){
            throw new ServiceException("新增菜单"  + menu.getMenuName() + "失败，菜单已经存在");
        }
    }

    private void checkExternalLink(SysMenu menu){
        if(menu.getIsFrame() != null &&  menu.getIsFrame() == 0 && !isHttp(menu.getPath())){
            throw new ServiceException("新增菜单失败，外链必须是http或者https");
        }
    }

    private Boolean isHttp(String path){
        return StringUtils.hasText(path) && (path.startsWith("http://") || path.startsWith("https://"));
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> sysMenuList){
        List<SysMenu>  sysMenuList1 = new ArrayList<>();
        for(SysMenu menu : sysMenuList){
            if(menu.getParentId() == null || menu.getParentId() == 0){
                fn(sysMenuList,menu);
                sysMenuList1.add(menu);
            }
        }
        return sysMenuList1;
    }

    private  void fn (List<SysMenu> sysMenuList,SysMenu menu){
        List<SysMenu> childrenList  = getChildren(sysMenuList,menu);
        menu.setChildren(childrenList);
        for(SysMenu children : childrenList){
            if(!getChildren(sysMenuList,children).isEmpty()){
                fn(sysMenuList,children);
            }
        }
    }

    private List<SysMenu> getChildren(List<SysMenu> list, SysMenu parent){
        return list.stream()
                .filter(menu -> parent.getMenuId().equals(menu.getParentId()))
                .collect(Collectors.toList());
    }
}
