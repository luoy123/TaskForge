package com.zhq.taskforge.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从securitycontext中获取当前对象
 */
public class SecurityUtils {

    public  static String getUserName(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null ||  authentication.getPrincipal() == null){
            return "";
        }
        Object principal = authentication.getPrincipal();
        if(principal instanceof  LoginUser loginUser){
            return loginUser.getUsername();
        }
        //返回兜底数据
        return authentication.getName();
    }
}
