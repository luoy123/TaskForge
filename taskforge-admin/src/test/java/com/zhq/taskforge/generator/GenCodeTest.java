package com.zhq.taskforge.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.TaskForgeApplication;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.mapper.GenTableMapper;
import com.zhq.taskforge.generator.service.IGenTableService;

/**
 * O3 自测：导入 sys_notice → 改包名和类名 → 预览 / 下载。
 * 渲染结果写到 target/gen-out，供编译检查用；@Transactional 回滚，不在库里留数据。
 */
@SpringBootTest(classes = TaskForgeApplication.class)
@Transactional
class GenCodeTest {

    @Autowired
    private IGenTableService genTableService;

    @Autowired
    private GenTableMapper genTableMapper;

    private Long importNotice() {
        genTableService.importGenTable(List.of("sys_notice"), "tester");
        GenTable table = genTableService.selectGenTableById(genTableMapper.selectOne(
                new LambdaQueryWrapper<GenTable>().eq(GenTable::getTableName, "sys_notice")).getTableId());
        table.setPackageName("com.zhq.taskforge.gentest");
        table.setModuleName("gentest");
        table.setClassName("GenNotice");
        genTableService.updateGenTable(table, "tester");
        return table.getTableId();
    }

    @Test
    void previewAndWriteFiles() throws IOException {
        Long tableId = importNotice();
        Map<String, String> files = genTableService.previewCode(tableId);
        files.keySet().forEach(System.out::println);

        assertEquals(6, files.size());
        assertTrue(files.containsKey("main/java/com/zhq/taskforge/gentest/domain/GenNotice.java"));
        assertTrue(files.containsKey("main/java/com/zhq/taskforge/web/controller/gentest/GenNoticeController.java"));
        assertTrue(files.containsKey("main/resources/mapper/gentest/GenNoticeMapper.xml"));
        for (Map.Entry<String, String> f : files.entrySet()) {
            String code = f.getValue();
            assertFalse(code.contains("AjaxResult"), f.getKey());
            assertFalse(code.contains("TableDataInfo"), f.getKey());
            assertFalse(code.contains("javax."), f.getKey());
            assertFalse(code.contains("laigeoffer"), f.getKey());
            assertFalse(code.contains("${"), "存在未渲染的变量：" + f.getKey());
        }
        String controller = files.get("main/java/com/zhq/taskforge/web/controller/gentest/GenNoticeController.java");
        assertTrue(controller.contains("R<Page<GenNotice>>"));
        assertTrue(controller.contains("hasAnyAuthority('gentest:notice:list')"));
        System.out.println(files.get("main/java/com/zhq/taskforge/gentest/service/impl/GenNoticeServiceImpl.java"));

        Path out = Path.of("target", "gen-out");
        for (Map.Entry<String, String> f : files.entrySet()) {
            Path p = out.resolve(f.getKey());
            Files.createDirectories(p.getParent());
            Files.writeString(p, f.getValue(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void downloadZip() throws IOException {
        Long tableId = importNotice();
        byte[] zip = genTableService.downloadCode(tableId);
        List<String> entries = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                entries.add(e.getName());
            }
        }
        System.out.println(entries);
        assertEquals(6, entries.size());
        assertTrue(entries.stream().allMatch(n -> n.startsWith("main/")));
    }
}
