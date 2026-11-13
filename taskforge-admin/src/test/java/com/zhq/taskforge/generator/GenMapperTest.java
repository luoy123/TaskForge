package com.zhq.taskforge.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.TaskForgeApplication;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.domain.GenTableColumn;
import com.zhq.taskforge.generator.mapper.GenTableColumnMapper;
import com.zhq.taskforge.generator.mapper.GenTableMapper;

/**
 * O2-1 自测：只读 information_schema 和 gen_table，不写库。
 */
@SpringBootTest(classes = TaskForgeApplication.class)
class GenMapperTest {

    @Autowired
    private GenTableMapper genTableMapper;

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    @Test
    void selectDbTableList() {
        Page<GenTable> page = genTableMapper.selectDbTableList(new Page<>(1, 10), new GenTable());
        System.out.println("可导入表总数 = " + page.getTotal());
        page.getRecords().forEach(t -> System.out.println("  " + t.getTableName() + " / " + t.getTableComment()));

        assertTrue(page.getTotal() > 0);
        assertEquals(10, page.getRecords().size());
        assertTrue(page.getRecords().stream().noneMatch(t -> t.getTableName().startsWith("ACT_")));
    }

    @Test
    void selectDbTableListByNames() {
        List<GenTable> list = genTableMapper.selectDbTableListByNames(List.of("sys_notice", "ACT_RU_TASK"));
        list.forEach(t -> System.out.println(t.getTableName() + " / " + t.getTableComment()));

        assertEquals(1, list.size(), "ACT_ 表应被排除");
        assertEquals("通知公告表", list.get(0).getTableComment());
    }

    @Test
    void selectDbTableColumnsByName() {
        List<GenTableColumn> columns = genTableColumnMapper.selectDbTableColumnsByName("sys_notice");
        columns.forEach(c -> System.out.printf("%-15s %-15s pk=%s inc=%s req=%s sort=%d%n",
                c.getColumnName(), c.getColumnType(), c.getIsPk(), c.getIsIncrement(), c.getIsRequired(), c.getSort()));

        GenTableColumn pk = columns.get(0);
        assertEquals("notice_id", pk.getColumnName());
        assertEquals("1", pk.getIsPk());
        assertEquals("1", pk.getIsIncrement());
    }

    @Test
    void genTableSelectWorksWithoutDeletedColumn() {
        Long count = genTableMapper.selectCount(null);
        System.out.println("gen_table 现有行数 = " + count);
    }
}
