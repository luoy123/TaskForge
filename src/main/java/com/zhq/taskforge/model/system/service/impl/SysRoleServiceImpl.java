package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysRole;
import com.zhq.taskforge.model.system.entity.SysRoleMenu;
import com.zhq.taskforge.model.system.entity.SysUserRole;
import com.zhq.taskforge.model.system.mapper.SysRoleMapper;
import com.zhq.taskforge.model.system.mapper.SysRoleMenuMapper;
import com.zhq.taskforge.model.system.mapper.SysUserRoleMapper;
import com.zhq.taskforge.model.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    SysUserRoleMapper sysUserRoleMapper;

    @Override
    public void addRole(SysRole role) {

        //1.验证角色名，角色权限没有重复
        checkRoleUnique(role);
        //2.填充默认值，插入角色

        role.setCreateTime(LocalDateTime.now());
        sysRoleMapper.insert(role);
        //3.插入sys_role_menu
       insertRoleMenu(role);
    }

    private void fillDefaultValue(SysRole role){
        //在mybatis-plus中默认会将非空字段插入sql,空字段不插入，此时数据库默认值生效
        if(role.getStatus() == null || role.getStatus().isBlank()){
            role.setStatus("0");
        }
        if(role.getDataScope() == null || role.getDataScope().isBlank()){
            role.setDataScope("1");
        }
        if(role.getMenuCheckStrictly() == null){
            role.setMenuCheckStrictly(1);
        }
        if(role.getDeptCheckStrictly() == null){
            role.setDeptCheckStrictly(1);
        }
        if(role.getDeleted() == null || role.getDeleted().isBlank()){
            role.setDeleted("0");
        }
    }

    private void insertRoleMenu(SysRole role){
        if(role.getMenuIds() != null && !role.getMenuIds().isEmpty()){
            for(Long menuId : role.getMenuIds()){
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setMenuId(menuId);
                sysRoleMenu.setRoleId(role.getRoleId());
                sysRoleMenuMapper.insert(sysRoleMenu);
            }
        }
    }


    @Override
    public void updateRole(SysRole sysRole) {
        //1.检查roleId不能为空
        if(sysRole.getRoleId() == null){
            throw new BusinessException("roleid不能为空");
        }

        //2.不能修改成超级管理员
        if(Long.valueOf(1L).equals(sysRole.getRoleId())){
            throw new BusinessException("用户角色不能修改成超级管理员");
        }
        //3.roleName和roleKey不能重复
        checkRoleUnique(sysRole);
        //4.更新sys_role
        sysRole.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(sysRole);
        //5.删除sys_role_menu中的roleId的旧数据
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId,sysRole.getRoleId())
        );
        //6.插入新的menuIds
        insertRoleMenu(sysRole);
    }

    @Override
    public void changeStatus(SysRole sysRole) {
      //1.roleId不能为空
        if(sysRole.getRoleId() == null){
            throw new BusinessException("roleId不能为空");
        }
        if(Long.valueOf(1L).equals(sysRole.getRoleId())){
            throw new BusinessException("无法修改超级管理员的状态");
        }
        sysRole.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(sysRole);
    }


    @Override
    public SysRole getDetailsById(Long roleId) {
        if(roleId == null ){
            throw new BusinessException("roleId不能为空，无法查询角色信息");
        }
        SysRole sysRole = sysRoleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleId, roleId)
        );

        if(sysRole == null){
            throw new BusinessException("角色不存在，无法查询角色信息");
        }

        //2.查询SysMenu项
        List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
        List<Long> collect = sysRoleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        sysRole.setMenuIds(collect);
        return sysRole;
    }

    @Override
    public void deleteRole(List<Long> roleIds) {
        //1.判断roleIds是否为空，是否存在数据
        if(roleIds == null || CollectionUtils.isEmpty(roleIds)){
            throw  new BusinessException("roleId为空，无法进行删除角色");
        }
        //2.不能删除超级管理员角色
        if(roleIds.contains(1L)){
            throw new BusinessException("无法删除超级管理员角色");
        }
        //3.验证角色是否分配给用户，分配了的无法进行删除。
        Long count = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId,roleIds)
        );
        if(count > 0){
            throw new BusinessException("角色已经分配用户，无法进行删除");
        }

        //4.删除sys_role_menu中的数据
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getRoleId,roleIds));
        //5.删除sys_role,逻辑删除,因为我在字段上设置了tablelogic,我是用delete，mybatis-plus也会变成逻辑删除。
        sysRoleMapper.delete(Wrappers.<SysRole>lambdaQuery()
                .in(SysRole::getRoleId,roleIds));
    }

    @Override
    public List<SysRole> optionSelect() {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        qw.eq(SysRole::getStatus,"0")
                .eq(SysRole::getDeleted,"0")
                .orderByAsc(SysRole::getRoleSort);
        return sysRoleMapper.selectList(qw);
    }

    @Override
    public Page<SysRole> list(Long PageNum, Long PageSizes, SysRole sysRole) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        qw.eq(SysRole::getDeleted,"0");
        if(sysRole != null){
            /**
             * hasText:
             * 1.不是null
             * 2.长度 > 0
             *3.不是空白字符(空格，制表符，)
             */
            if(StringUtils.hasText(sysRole.getRoleName())){
                qw.like(SysRole::getRoleName,sysRole.getRoleName());
            }
            if(StringUtils.hasText(sysRole.getRoleKey())){
                qw.like(SysRole::getRoleKey,sysRole.getRoleKey());
            }
            if(StringUtils.hasText(sysRole.getStatus())){
                qw.eq(SysRole::getStatus,sysRole.getStatus());
            }
        }
        qw.orderByAsc(SysRole::getRoleSort)
                .orderByDesc(SysRole::getCreateTime);
        return sysRoleMapper.selectPage(new Page<>(PageNum,PageSizes),qw);
    }


    private void checkRoleUnique(SysRole role){
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole sameNameRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, role.getRoleName())
                        .eq(SysRole::getDeleted, 0)
                        .last("limit 1")
        );
        if(sameNameRole != null && !sameNameRole.getRoleId().equals(roleId)){
            throw  new BusinessException( role.getRoleName() + "失败，角色名称存在");
        }

        SysRole sameKeyRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleKey, role.getRoleKey())
                        .eq(SysRole::getDeleted, 0)
                        .last("limit 1")
        );
        if(sameKeyRole != null && !sameKeyRole.getRoleId().equals(roleId)){
            throw  new BusinessException( role.getRoleName() + "失败，角色权限存在");
        }
    }


}
