package com.zhq.taskforge.auth.dto;

import com.zhq.taskforge.auth.vo.RouterVo;
import lombok.Data;

import java.util.List;

/**
 * 返回给前端的数据
 */
@Data
public class LoginResponse {

    private String token;

    private Long userId;

    private String userName;

    private String nickName;

    //返回权限
    private List<String> permi;

    //返回路由
    private List<RouterVo>  menus;
}
