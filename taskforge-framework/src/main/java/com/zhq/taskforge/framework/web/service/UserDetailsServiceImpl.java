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

        // 1.查角色列表
        List<SysRole> selectRolesByUserId = sysRoleService.selectRolesByUserId(userByName.getUserId());

        // 2.给每个角色装填permissions
        for (SysRole role : selectRolesByUserId) {
            List<String> permissions = sysMenuMapper.selectMenuPermsByRoleId(role.getRoleId());
        }

        if (userByName == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (userByName.getStatus() != null && userByName.getStatus() == 1) {
            throw new ServiceException("用户已停用");
        }

        // 3. 该用户的所有菜单权限
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
        Set<String> permissionSet = new HashSet<>(permissions);
        loginUser.setPermissions(permissionSet);
        return loginUser;
    }

}
