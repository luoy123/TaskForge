package com.zhq.taskforge.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.TaskForgeApplication;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.domain.GenTableColumn;
import com.zhq.taskforge.generator.mapper.GenTableColumnMapper;
import com.zhq.taskforge.generator.mapper.GenTableMapper;
import com.zhq.taskforge.generator.service.IGenTableService;

/**
 * O2-2 自测：@Transactional 让每个用例结束后回滚，不在库里留数据。
 */
@SpringBootTest(classes = TaskForgeApplication.class)
@Transactional
class GenImportTest {

    @Autowired
    private IGenTableService genTableService;

    @Autowired
    private GenTableMapper genTableMapper;

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    @Test
    void importSysNotice() {
        genTableService.importGenTable(List.of("sys_notice"), "tester");

        GenTable table = genTableMapper.selectOne(
                new LambdaQueryWrapper<GenTable>().eq(GenTable::getTableName, "sys_notice"));
        assertNotNull(table);
        System.out.println(table);
        assertEquals("SysNotice", table.getClassName());
        assertEquals("通知公告", table.getFunctionName());
        assertEquals("zhq", table.getFunctionAuthor(), "GenConfig 应读到 application.yml 的 gen.author");
        assertEquals("tester", table.getCreateBy());

        List<GenTableColumn> columns = genTableColumnMapper.selectList(
                new LambdaQueryWrapper<GenTableColumn>()
                        .eq(GenTableColumn::getTableId, table.getTableId())
                        .orderByAsc(GenTableColumn::getSort));
        columns.forEach(c -> System.out.printf("%-15s %-14s %-14s %-9s edit=%s list=%s query=%s%n",
                c.getColumnName(), c.getJavaField(), c.getJavaType(), c.getHtmlType(),
                c.getIsEdit(), c.getIsList(), c.getIsQuery()));
        assertEquals(10, columns.size());
        assertEquals("noticeId", columns.get(0).getJavaField());
        assertEquals("Integer", columns.get(0).getJavaType());
    }

    @Test
    void duplicateImportRejected() {
        genTableService.importGenTable(List.of("sys_notice"), "tester");
        ServiceException e = assertThrows(ServiceException.class,
                () -> genTableService.importGenTable(List.of("sys_notice"), "tester"));
        assertTrue(e.getMessage().contains("已导入"));
    }

    @Test
    void excludedTableRejected() {
        ServiceException e = assertThrows(ServiceException.class,
                () -> genTableService.importGenTable(List.of("sys_post", "ACT_RU_TASK"), "tester"));
        assertTrue(e.getMessage().contains("ACT_RU_TASK"));
    }
}
