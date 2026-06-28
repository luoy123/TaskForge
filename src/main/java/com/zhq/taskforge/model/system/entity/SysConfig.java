package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("sys_config")
@Data
public class SysConfig {

    @TableId(type = IdType.AUTO)
     private Integer configId;
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;

}
