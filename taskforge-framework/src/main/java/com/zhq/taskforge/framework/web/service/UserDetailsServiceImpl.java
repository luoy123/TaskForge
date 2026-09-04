package com.zhq.taskforge.framework.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zhq.taskforge.common.core.domain.entity.SysRole;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.system.mapper.SysMenuMapper;
import com.zhq.taskforge.system.service.ISysRoleService;
import com.zhq.taskforge.system.service.ISysUserService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ISysUserService sysUserService;
    @Autowired
    ISysRoleService sysRoleService;
    @Autowired
    SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        SysUser userByName = sysUserService.getUserByName(name);
        if (userByName == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (userByName.getStatus() != null && userByName.getStatus() == 1) {
            throw new ServiceException("用户已停用");
        }

        // 1.查角色列表并给每个角色装填 permissions
        List<SysRole> roles = sysRoleService.selectRolesByUserId(userByName.getUserId());
        for (SysRole role : roles) {
            List<String> rolePerms = sysMenuMapper.selectMenuPermsByRoleId(role.getRoleId());
            role.setPermissions(rolePerms == null ? new HashSet<>() : new HashSet<>(rolePerms));
        }
        userByName.setRoles(roles);

        // 2.该用户的所有菜单权限（并集）
        List<String> permissions;
        if (Long.valueOf(1L).equals(userByName.getUserId())) {
            permissions = sysMenuMapper.selectPermsAll();
        } else {
            permissions = sysMenuMapper.selectPermsByUserId(userByName.getUserId());
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userByName.getUserId());
        loginUser.setUser(userByName);
        loginUser.setDeptId(userByName.getDeptId());
        loginUser.setPermissions(new HashSet<>(permissions == null ? List.of() : permissions));
        return loginUser;
    }

}
