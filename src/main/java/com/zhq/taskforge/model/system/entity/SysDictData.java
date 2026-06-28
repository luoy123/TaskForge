package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("sys_dict_data")
@Data
public class SysDictData {

    @TableId(type = IdType.AUTO)
    Long dictCode;
    Integer dictSort;
    String dictLabel;
    String dictValue;
    String dictType;
    String cssClass;
    String listClass;
    String isDefault;
    String status;
    String createBy;
    LocalDateTime createTime;
    String updateBy;
    LocalDateTime updateTime;
    String remark;
}
