package com.zhq.taskforge.security;

import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysUser;
import com.zhq.taskforge.model.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String name) throws  UsernameNotFoundException{
        SysUser userByName = sysUserService.getUserByName(name);
        if(userByName == null){
            throw new UsernameNotFoundException("用户不存在");
        }
        if(userByName.getStatus() != null && userByName.getStatus() == 1){
            throw  new BusinessException("用户已停用");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setName(userByName.getName());
        loginUser.setNickName(userByName.getNickName());
        loginUser.setPassword(userByName.getPassword());
        loginUser.setStatus(userByName.getStatus());
        loginUser.setUserId(userByName.getUserId());
        return loginUser;
    }


}
