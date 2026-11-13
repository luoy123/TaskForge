package com.zhq.taskforge.generator.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.generator.domain.GenTableColumn;

public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {

    /**
     * 读某张表在 information_schema.columns 里的列结构。
     */
    List<GenTableColumn> selectDbTableColumnsByName(@Param("tableName") String tableName);
}
