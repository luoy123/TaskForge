package com.zhq.taskforge.model.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String name;

    private String nickName;

    private String email;

    private String phone;

    private String avatar;

    private String password;

    private Integer status;

    @TableLogic
    private Integer deleted;

    private String loginIp;


    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
