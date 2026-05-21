package com.zhq.taskforge.model.system.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class SysRole {
    @TableId(type = IdType.AUTO)
    private Long roleId;

    private String roleName;
    private String roleKey;
    private Integer roleSort;
    private String dataScope;
    private Integer menuCheckStrictly;
    private Integer deptCheckStrictly;
    private String status;
    private String deleted;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private  String remark;

}
