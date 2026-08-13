package com.zhq.taskforge.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {
    private  Long roleId;
    private Long userId;
}
