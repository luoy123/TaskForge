package com.zhq.taskforge.model.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type= IdType.AUTO)
    private Long menuId;
    private String menuName;
    private Long parentId;
    private Integer menuOrder;
    private String path;
    private String component;
    private String query;
    private Integer isFrame;
    private Integer isCache;
    private String menuType;
    private String visible;
    private String status;
    private String perms;
    private String icon;
    private String createBy;
    private String updateBy;
    private LocalDate createTime;
    private LocalDate updateTime;
    private String remark;
}
