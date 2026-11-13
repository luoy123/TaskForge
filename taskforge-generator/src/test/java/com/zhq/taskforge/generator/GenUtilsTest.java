package com.zhq.taskforge.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.zhq.taskforge.generator.config.GenConfig;
import com.zhq.taskforge.generator.constant.GenConstants;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.domain.GenTableColumn;
import com.zhq.taskforge.generator.util.GenUtils;

class GenUtilsTest {

    private GenConfig config(boolean autoRemovePre, String prefix) {
        GenConfig c = new GenConfig();
        c.setAuthor("zhq");
        c.setPackageName("com.zhq.taskforge.system");
        c.setAutoRemovePre(autoRemovePre);
        c.setTablePrefix(prefix);
        return c;
    }

    private GenTableColumn column(String name, String type, String isPk) {
        GenTableColumn c = new GenTableColumn();
        c.setColumnName(name);
        c.setColumnType(type);
        c.setIsPk(isPk);
        GenUtils.initColumnField(c, new GenTable());
        return c;
    }

    @Test
    void convertClassName() {
        assertEquals("SysNotice", GenUtils.convertClassName("sys_notice", config(false, "sys_")));
        assertEquals("Notice", GenUtils.convertClassName("sys_notice", config(true, "sys_,pmhub_")));
        assertEquals("ProjectTask", GenUtils.convertClassName("pmhub_project_task", config(true, "sys_, pmhub_")));
    }

    @Test
    void functionName() {
        assertEquals("通知公告", GenUtils.getFunctionName("通知公告表", "sys_notice"));
        assertEquals("流程表单信息", GenUtils.getFunctionName("流程表单信息表", "x"));
        assertEquals("pmhub_wf_model_deploy", GenUtils.getFunctionName("", "pmhub_wf_model_deploy"));
    }

    @Test
    void initTable() {
        GenTable t = new GenTable();
        t.setTableName("sys_notice");
        t.setTableComment("通知公告表");
        GenUtils.initTable(t, config(false, "sys_"), "tester");
        assertEquals("SysNotice", t.getClassName());
        assertEquals("system", t.getModuleName());
        assertEquals("notice", t.getBusinessName());
        assertEquals("通知公告", t.getFunctionName());
        assertEquals("zhq", t.getFunctionAuthor());
        assertEquals(GenConstants.TPL_CRUD, t.getTplCategory());
        assertNull(t.getUpdateTime());
    }

    @Test
    void javaTypes() {
        assertEquals("Long", column("id", "bigint unsigned", "1").getJavaType());
        assertEquals("Integer", column("age", "int", "0").getJavaType());
        assertEquals("BigDecimal", column("price", "decimal(10,2)", "0").getJavaType());
        assertEquals("BigDecimal", column("rate", "double", "0").getJavaType());
        assertEquals("LocalDateTime", column("create_time", "datetime", "0").getJavaType());
        assertEquals("String", column("notice_title", "varchar(50)", "0").getJavaType());
    }

    @Test
    void htmlTypesAndFlags() {
        GenTableColumn content = column("notice_content", "longblob", "0");
        assertEquals(GenConstants.HTML_EDITOR, content.getHtmlType());

        GenTableColumn remark = column("remark", "varchar(500)", "0");
        assertEquals(GenConstants.HTML_TEXTAREA, remark.getHtmlType());
        assertNull(remark.getIsQuery());

        GenTableColumn status = column("status", "char(1)", "0");
        assertEquals(GenConstants.HTML_RADIO, status.getHtmlType());

        GenTableColumn userName = column("user_name", "varchar(30)", "0");
        assertEquals(GenConstants.QUERY_LIKE, userName.getQueryType());
        assertEquals("userName", userName.getJavaField());

        GenTableColumn pk = column("notice_id", "int", "1");
        assertNull(pk.getIsEdit());
        assertNull(pk.getIsList());

        GenTableColumn deleted = column("deleted", "tinyint", "0");
        assertNull(deleted.getIsEdit());
        assertNull(deleted.getIsList());

        assertNull(column("update_time", "datetime", "0").getIsEdit());
        assertNull(column("update_by", "varchar(64)", "0").getIsEdit());
    }
}
