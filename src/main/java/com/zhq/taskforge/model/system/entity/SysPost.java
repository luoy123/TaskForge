package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_post")
public class SysPost {

    @TableId(type = IdType.AUTO)
    private Long postId;

    private String postCode;

    private String postName;

    private Integer postSort;

    private String status;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;
}