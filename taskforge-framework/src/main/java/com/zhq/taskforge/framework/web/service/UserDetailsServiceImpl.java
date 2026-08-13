package com.zhq.taskforge.framework.web.service;

import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.system.mapper.SysMenuMapper;
import com.zhq.taskforge.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ISysUserService sysUserService;
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
        List<String> permissions;
        if (Long.valueOf(1L).equals(userByName.getUserId())) {
            permissions = sysMenuMapper.selectPermsAll();
        } else {
            permissions = sysMenuMapper.selectPermsByUserId(userByName.getUserId());
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userByName.getUserId());
        loginUser.setUser(userByName);
        Set<String> permissionSet = new HashSet<>(permissions);
        loginUser.setPermissions(permissionSet);
        return loginUser;
    }

}
