package com.zhq.taskforge.system.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@TableName("sys_notice")
@Data
public class SysNotice {

    private final static Long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer noticeId;

    private String noticeTitle;

    private String noticeType;

    private String noticeContent;

    private String status;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    private String remark;

}
