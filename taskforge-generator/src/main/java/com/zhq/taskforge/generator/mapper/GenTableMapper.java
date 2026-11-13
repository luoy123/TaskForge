package com.zhq.taskforge.generator.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.generator.domain.GenTable;

public interface GenTableMapper extends BaseMapper<GenTable> {

    /**
     * 库里还没导入 gen_table 的表（分页），对应 /tool/gen/db/list。
     */
    Page<GenTable> selectDbTableList(Page<GenTable> page, @Param("t") GenTable query);

    /**
     * 按表名查 information_schema.tables，importTable 用。
     */
    List<GenTable> selectDbTableListByNames(@Param("tableNames") List<String> tableNames);
}
