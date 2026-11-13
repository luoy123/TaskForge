package com.zhq.taskforge.generator.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zhq.taskforge.generator.constant.GenConstants;

import lombok.Data;

/**
 * 代码生成业务字段表 gen_table_column。
 * is* 系列沿用若依约定：String，"1" 表示是，其余表示否。
 */
@Data
@TableName("gen_table_column")
public class GenTableColumn {

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long columnId;

    /** 归属表编号（库里是 varchar(64)，MySQL 隐式转换） */
    private Long tableId;

    /** 列名称 */
    private String columnName;

    /** 列描述 */
    private String columnComment;

    /** 列类型，如 varchar(50) */
    private String columnType;

    /** Java 类型 */
    private String javaType;

    /** Java 字段名 */
    private String javaField;

    /** 是否主键（1 是） */
    private String isPk;

    /** 是否自增（1 是） */
    private String isIncrement;

    /** 是否必填（1 是） */
    private String isRequired;

    /** 是否为插入字段（1 是） */
    private String isInsert;

    /** 是否编辑字段（1 是） */
    private String isEdit;

    /** 是否列表字段（1 是） */
    private String isList;

    /** 是否查询字段（1 是） */
    private String isQuery;

    /** 查询方式（EQ、NE、GT、LT、LIKE、BETWEEN） */
    private String queryType;

    /** 显示类型（input、textarea、select、radio、datetime 等） */
    private String htmlType;

    /** 字典类型 */
    private String dictType;

    /** 排序 */
    private Integer sort;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 不加 @JsonIgnore 会被 Jackson 当成属性 pk 多输出一项 */
    @JsonIgnore
    public boolean isPk() {
        return GenConstants.REQUIRE.equals(isPk);
    }

    @JsonIgnore
    public boolean isIncrement() {
        return GenConstants.REQUIRE.equals(isIncrement);
    }

    /** 模板里拼 getter / setter 用：noticeId → NoticeId */
    @JsonIgnore
    public String getCapJavaField() {
        return javaField == null || javaField.isEmpty() ? javaField
                : Character.toUpperCase(javaField.charAt(0)) + javaField.substring(1);
    }
}
