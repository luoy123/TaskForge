package com.zhq.taskforge.generator.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 代码生成业务表 gen_table。
 * 不继承 BaseEntity：gen 表没有 deleted 列，继承后 MP 逻辑删除会拼 deleted = 0。
 */
@Data
@TableName("gen_table")
public class GenTable {

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long tableId;

    /** 表名称 */
    private String tableName;

    /** 表描述 */
    private String tableComment;

    /** 关联父表的表名（主子表用，暂不支持） */
    private String subTableName;

    /** 本表关联父表的外键名 */
    private String subTableFkName;

    /** 实体类名称（首字母大写） */
    private String className;

    /** 使用的模板（crud 单表，tree 树表，sub 主子表） */
    private String tplCategory;

    /** 生成包路径 */
    private String packageName;

    /** 生成模块名 */
    private String moduleName;

    /** 生成业务名 */
    private String businessName;

    /** 生成功能名 */
    private String functionName;

    /** 生成作者 */
    private String functionAuthor;

    /** 生成代码方式（0 zip 压缩包，1 自定义路径；TaskForge 只用 0） */
    private String genType;

    /** 生成路径 */
    private String genPath;

    /** 其它生成选项（JSON） */
    private String options;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private String remark;

    /** 主键列（O3 渲染用） */
    @TableField(exist = false)
    private GenTableColumn pkColumn;

    /** 全部列 */
    @TableField(exist = false)
    private List<GenTableColumn> columns;

    /** 前端回传的其它选项，PUT 时转成 JSON 存进 options */
    @TableField(exist = false)
    private Map<String, Object> params;
}
