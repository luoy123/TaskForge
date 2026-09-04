package com.zhq.taskforge.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.core.domain.entity.SysRole;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.system.domain.SysRoleDept;
import com.zhq.taskforge.system.domain.SysRoleMenu;
import com.zhq.taskforge.system.domain.SysUserRole;
import com.zhq.taskforge.system.mapper.SysRoleDeptMapper;
import com.zhq.taskforge.system.mapper.SysRoleMapper;
import com.zhq.taskforge.system.mapper.SysRoleMenuMapper;
import com.zhq.taskforge.system.mapper.SysUserRoleMapper;
import com.zhq.taskforge.system.service.ISysRoleService;

@Service
@Transactional
public class SysRoleServiceImpl implements ISysRoleService {

    @Autowired
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired
    SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    SysRoleDeptMapper sysRoleDeptMapper;
    @Override
    public void addRole(SysRole role) {

        checkRoleUnique(role);

        role.setCreateTime(LocalDateTime.now());
        sysRoleMapper.insert(role);
        insertRoleMenu(role);
    }

    private void fillDefaultValue(SysRole role) {
        if (role.getStatus() == null || role.getStatus().isBlank()) {
            role.setStatus("0");
        }
        if (role.getDataScope() == null || role.getDataScope().isBlank()) {
            role.setDataScope("1");
        }
        if (role.getMenuCheckStrictly() == null) {
            role.setMenuCheckStrictly(1);
        }
        if (role.getDeptCheckStrictly() == null) {
            role.setDeptCheckStrictly(1);
        }
        if (role.getDelFlag() == null || role.getDelFlag().isBlank()) {
            role.setDelFlag("0");
        }
    }

    private void insertRoleMenu(SysRole role) {
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            for (Long menuId : role.getMenuIds()) {
                SysRoleMenu sysRoleMenu = new SysRoleMenu();
                sysRoleMenu.setMenuId(menuId);
                sysRoleMenu.setRoleId(role.getRoleId());
                sysRoleMenuMapper.insert(sysRoleMenu);
            }
        }
    }

    @Override
    public void updateRole(SysRole sysRole) {
        if (sysRole.getRoleId() == null) {
            throw new ServiceException("roleid不能为空");
        }

        if (Long.valueOf(1L).equals(sysRole.getRoleId())) {
            throw new ServiceException("用户角色不能修改成超级管理员");
        }
        checkRoleUnique(sysRole);
        sysRole.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(sysRole);
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, sysRole.getRoleId()));
        insertRoleMenu(sysRole);
    }

    @Override
    public void changeStatus(SysRole sysRole) {
        if (sysRole.getRoleId() == null) {
            throw new ServiceException("roleId不能为空");
        }
        if (Long.valueOf(1L).equals(sysRole.getRoleId())) {
            throw new ServiceException("无法修改超级管理员的状态");
        }
        sysRole.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(sysRole);
    }

    @Override
    public SysRole getDetailsById(Long roleId) {
        if (roleId == null) {
            throw new ServiceException("roleId不能为空，无法查询角色信息");
        }
        SysRole sysRole = sysRoleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleId, roleId));

        if (sysRole == null) {
            throw new ServiceException("角色不存在，无法查询角色信息");
        }

        List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId));
        List<Long> collect = sysRoleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        
                //已勾选的部门
        List<SysRoleDept> list =  sysRoleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDept>()
                                    .eq(SysRoleDept::getRoleId,roleId));
        List<Long> deptIdList = list.stream().map(SysRoleDept::getDeptId).collect(Collectors.toList());
        sysRole.setMenuIds(collect);
        sysRole.setDeptIds(deptIdList);
        return sysRole;
    }

    @Override
    public void deleteRole(List<Long> roleIds) {
        if (roleIds == null || CollectionUtils.isEmpty(roleIds)) {
            throw new ServiceException("roleId为空，无法进行删除角色");
        }
        if (roleIds.contains(1L)) {
            throw new ServiceException("无法删除超级管理员角色");
        }
        Long count = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, roleIds));
        if (count > 0) {
            throw new ServiceException("角色已经分配用户，无法进行删除");
        }

        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery()
                .in(SysRoleMenu::getRoleId, roleIds));
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .in(SysRoleDept::getRoleId, roleIds));
        sysRoleMapper.delete(Wrappers.<SysRole>lambdaQuery()
                .in(SysRole::getRoleId, roleIds));
    }

    @Override
    public List<SysRole> optionSelect() {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        qw.eq(SysRole::getStatus, "0")
                .eq(SysRole::getDelFlag, "0")
                .orderByAsc(SysRole::getRoleSort);
        return sysRoleMapper.selectList(qw);
    }

    @Override
    public Page<SysRole> list(Long PageNum, Long PageSizes, SysRole sysRole) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<>();
        qw.eq(SysRole::getDelFlag, "0");
        if (sysRole != null) {
            if (StringUtils.hasText(sysRole.getRoleName())) {
                qw.like(SysRole::getRoleName, sysRole.getRoleName());
            }
            if (StringUtils.hasText(sysRole.getRoleKey())) {
                qw.like(SysRole::getRoleKey, sysRole.getRoleKey());
            }
            if (StringUtils.hasText(sysRole.getStatus())) {
                qw.eq(SysRole::getStatus, sysRole.getStatus());
            }
        }
        qw.orderByAsc(SysRole::getRoleSort)
                .orderByDesc(SysRole::getCreateTime);
        return sysRoleMapper.selectPage(new Page<>(PageNum, PageSizes), qw);
    }

    private void checkRoleUnique(SysRole role) {
        Long roleId = role.getRoleId() == null ? -1L : role.getRoleId();
        SysRole sameNameRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, role.getRoleName())
                        .eq(SysRole::getDelFlag, 0)
                        .last("limit 1"));
        if (sameNameRole != null && !sameNameRole.getRoleId().equals(roleId)) {
            throw new ServiceException(role.getRoleName() + "失败，角色名称存在");
        }

        SysRole sameKeyRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleKey, role.getRoleKey())
                        .eq(SysRole::getDelFlag, 0)
                        .last("limit 1"));
        if (sameKeyRole != null && !sameKeyRole.getRoleId().equals(roleId)) {
            throw new ServiceException(role.getRoleName() + "失败，角色权限存在");
        }
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        if (userId == null) {
            throw new ServiceException("userId不能为空");
        }
        return sysRoleMapper.selectRolesByUserId(userId);
    }

    @Override
    public void authDataScope(SysRole role) {
        if (role.getRoleId() == null) {
            throw new ServiceException("roleId不能为空");
        }
        if (Long.valueOf(1L).equals(role.getRoleId())) {
            throw new ServiceException("无法修改超级管理员的数据范围");
        }

        // 1. 只更新 data_scope，不要整角色 update
        SysRole update = new SysRole();
        update.setRoleId(role.getRoleId());
        update.setDataScope(role.getDataScope());
        update.setUpdateBy(role.getUpdateBy());
        update.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(update);

        // 2. 先清空旧关联
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, role.getRoleId()));

        // 3. data_scope=2 且带了 deptIds → 重新插入
        if ("2".equals(role.getDataScope())
                && role.getDeptIds() != null
                && !role.getDeptIds().isEmpty()) {
            for (Long deptId : role.getDeptIds()) {
                SysRoleDept rd = new SysRoleDept();
                rd.setDeptId(deptId);
                rd.setRoleId(role.getRoleId());
                sysRoleDeptMapper.insert(rd);
            }
        }
    }

}
