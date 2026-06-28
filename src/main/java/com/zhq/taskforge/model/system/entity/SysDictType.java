package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("sys_dict_type")
@Data
public class SysDictType {
    @TableId(type = IdType.AUTO)
    Long dictId;
    String dictName;
    String dictType;
    String status;
    String createBy;
    LocalDateTime createTime;
    String updateBy;
    LocalDateTime updateTime;
    String remark;

}
