package com.zhq.taskforge.generator.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.generator.domain.GenTable;

public interface IGenTableService {

    /** 已导入的表（分页） */
    Page<GenTable> selectGenTablePage(Long pageNum, Long pageSize, GenTable query);

    /** 数据库里还没导入的表（分页） */
    Page<GenTable> selectDbTablePage(Long pageNum, Long pageSize, GenTable query);

    /** 表配置 + 全部列（按 sort 排序） */
    GenTable selectGenTableById(Long tableId);

    /**
     * 导入表结构到 gen_table / gen_table_column。
     *
     * @param tableNames 表名
     * @param operName   操作人，由 Controller 从登录信息取
     */
    void importGenTable(List<String> tableNames, String operName);

    /** 保存表和列的生成配置 */
    void updateGenTable(GenTable genTable, String operName);

    /** 删除导入记录（连同列） */
    void deleteGenTableByIds(Long[] tableIds);

    /** 预览：key 为 zip 内文件路径，value 为渲染后的源码 */
    Map<String, String> previewCode(Long tableId);

    /** 生成 zip 字节 */
    byte[] downloadCode(Long tableId);
}
