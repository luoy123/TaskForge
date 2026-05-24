package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.common.TreeSelect;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysMenu;
import com.zhq.taskforge.model.system.entity.SysRoleMenu;
import com.zhq.taskforge.model.system.mapper.SysMenuMapper;
import com.zhq.taskforge.model.system.mapper.SysRoleMenuMapper;
import com.zhq.taskforge.model.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl implements SysMenuService {

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
                .orderByAsc(SysMenu::getMenuOrder);

       return  sysMenuMapper.selectList(qw);
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> sysMenuList) {
       //1.把平铺结构的Menu 变成父子结构的Menu
        List<SysMenu> sysMenuList1 = buildMenuTree(sysMenuList);
        //2.把父子结构的Menu，变成TreeSelect类型的父子结构
        return sysMenuList1.stream()
                .map(TreeSelect::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        if(roleId == null){
            throw new BusinessException("roleId不能为空");
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
            throw new BusinessException("menuId不能为空");
        }
        SysMenu sysMenu = sysMenuMapper.selectById(menuId);
        if(sysMenu == null){
            throw new BusinessException("菜单项为空");
        }
        return sysMenu;
    }

    @Override
    public void addMenu(SysMenu sysMenu) {
        //1.确保同一个父菜单下，菜单名称不会重复
        checkMenuNameUnique(sysMenu);
        //2.如果时外链，path必须是http://或者https://
        checkExternalLink(sysMenu);

        //3.插入数据
        sysMenu.setCreateTime(LocalDate.now());
        sysMenuMapper.insert(sysMenu);
    }

    @Override
    public void updateMenu(SysMenu sysMenu) {
        //判断menuId存在
        if(sysMenu.getMenuId() == null){
            throw new BusinessException("menuId不能为空");
        }
        //1.更新的菜单名称不能已经存在
        checkMenuNameUnique(sysMenu);

        //2.如果时外链，path必须是http://或者https://
        checkExternalLink(sysMenu);
        //3.不能把自己的父级设置成自己
        if(sysMenu.getMenuId().equals(sysMenu.getParentId())){
           throw new BusinessException("不能将父级设置成自己");
        }

        //4.更新时间
        sysMenu.setUpdateTime(LocalDate.now());
        //5.更新
        sysMenuMapper.updateById(sysMenu);

    }

    @Override
    public void deleteMenu(Long menuId) {
        if(menuId == null){
            throw  new BusinessException("menuId不能为空");
        }
        //1.当下菜单下不能存在子菜单项
        if(hasChildrenByMenuId(menuId)){
            throw  new BusinessException("存在子菜单，无法删除");
        }
        //2.当前菜单没有分配给某个角色
        if(checkMenuExistRole(menuId)){
            throw  new BusinessException("该菜单分配给角色，无法删除");
        }
        sysMenuMapper.deleteById(menuId);
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
        //1.获取MenuId和parentId
        Long menuId = menu.getMenuId() == null ? -1L : menu.getMenuId();
        Long parentId = menu.getParentId() == null ? 0L : menu.getParentId();

        //2.从数据库中返回查找到的数据
        SysMenu sysMenu = sysMenuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, menu.getMenuName())
                        .eq(SysMenu::getParentId, parentId)
                        .last("limit 1")
        );
        //3.若数据不为空 and 菜单id不等于当前菜单id，则说明以及存在了同名的菜单项，则无法进行添加。
        if(sysMenu != null && !sysMenu.getMenuId().equals(menuId)){
            throw new BusinessException("新增菜单"  + menu.getMenuName() + "失败，菜单已经存在");
        }
    }

    private void checkExternalLink(SysMenu menu){
        if(menu.getIsFrame() != null &&  menu.getIsFrame() == 0 && !isHttp(menu.getPath())){
            throw new BusinessException("新增菜单失败，外链必须是http或者https");
        }
    }

    private Boolean isHttp(String path){
        return StringUtils.hasText(path) && (path.startsWith("http://") || path.startsWith("https://"));
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> sysMenuList){
        List<SysMenu>  sysMenuList1 = new ArrayList<>();
        //1.找到parentId为null || parentId = 0
        for(SysMenu menu : sysMenuList){
            if(menu.getParentId() == null || menu.getParentId() == 0){
                //2.调用递归函数
                fn(sysMenuList,menu);
                sysMenuList1.add(menu);
            }
        }
        return sysMenuList1;
    }

    private  void fn (List<SysMenu> sysMenuList,SysMenu menu){
        //1.获取当前菜单项的子菜单项
        List<SysMenu> childrenList  = getChildren(sysMenuList,menu);
        //2.设置children
        menu.setChildren(childrenList);
        //3.遍历children列表，防止有多重菜单项的情况
        for(SysMenu children : childrenList){
            //4.存在子菜单项，则调用fn方法
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
