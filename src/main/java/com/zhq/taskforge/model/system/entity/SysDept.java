package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("sys_dept")
public class SysDept {

    @TableId(type = IdType.AUTO)
    private Long deptId;

    private Long parentId;

    private String ancestors;

    private String deptName;

    private Integer orderNum;

    private String leader;

    private String phone;

    private String email;

    private String status;

    @TableLogic(value = "0", delval = "1")
    private String delFlag;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    /** 子部门列表（非数据库字段，用于树形结构） */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}