package com.zhq.taskforge.auth.service.impl;


import com.zhq.taskforge.auth.dto.LoginRequest;
import com.zhq.taskforge.auth.dto.LoginResponse;
import com.zhq.taskforge.auth.service.AuthService;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysUser;
import com.zhq.taskforge.model.system.service.SysUserService;
import com.zhq.taskforge.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements  AuthService{

    @Autowired
    SysUserService sysUserService;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;



    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        //1.根据用户名查询用户
        SysUser sysUser = sysUserService.getUserByName(loginRequest.getUserName());

        //2.用户不存在，抛出BusinessException
        if(sysUser == null){
            throw  new BusinessException("用户不存在");
        }
        //3.用户状态不可用，抛出BusinessException
        if(sysUser.getStatus() == 1){
            throw  new BusinessException("用户不可用");
        }

        //4.用Bcrypt校验密码

        //5. 密码错误，抛出BusinessException异常
        if(!bCryptPasswordEncoder.matches(loginRequest.getPassword(), sysUser.getPassword())){
            throw  new BusinessException("密码错误");
        }

        //6.正确，生成token
        String token = JwtUtil.generateToken(sysUser.getUserId(), sysUser.getUserName());

        //7.更新用户loginIP，loginTime等， todo:
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setUserId(sysUser.getUserId());
        loginResponse.setNickName(sysUser.getNickName());
        loginResponse.setUserName(sysUser.getUserName());
        return  loginResponse;
    }
}
