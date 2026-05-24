package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {
    private  Long roleId;
    private Long userId;
}
