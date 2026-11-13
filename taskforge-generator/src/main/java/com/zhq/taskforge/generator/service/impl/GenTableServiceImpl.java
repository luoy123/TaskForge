package com.zhq.taskforge.generator.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.generator.config.GenConfig;
import com.zhq.taskforge.generator.constant.GenConstants;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.domain.GenTableColumn;
import com.zhq.taskforge.generator.mapper.GenTableColumnMapper;
import com.zhq.taskforge.generator.mapper.GenTableMapper;
import com.zhq.taskforge.generator.service.IGenTableService;
import com.zhq.taskforge.generator.util.GenUtils;
import com.zhq.taskforge.generator.util.VelocityUtils;

@Service
public class GenTableServiceImpl implements IGenTableService {

    @Autowired
    private GenTableMapper genTableMapper;

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    @Autowired
    private GenConfig genConfig;

    @Autowired
    private VelocityEngine velocityEngine;

    @Override
    public Page<GenTable> selectGenTablePage(Long pageNum, Long pageSize, GenTable query) {
        LambdaQueryWrapper<GenTable> qw = new LambdaQueryWrapper<>();
        if (query != null) {
            qw.like(StringUtils.isNotEmpty(query.getTableName()), GenTable::getTableName, query.getTableName());
            qw.like(StringUtils.isNotEmpty(query.getTableComment()), GenTable::getTableComment, query.getTableComment());
        }
        qw.orderByDesc(GenTable::getTableId);
        return genTableMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    @Override
    public Page<GenTable> selectDbTablePage(Long pageNum, Long pageSize, GenTable query) {
        return genTableMapper.selectDbTableList(new Page<>(pageNum, pageSize), query);
    }

    @Override
    public GenTable selectGenTableById(Long tableId) {
        GenTable table = genTableMapper.selectById(tableId);
        if (table == null) {
            throw new ServiceException("生成配置不存在：" + tableId);
        }
        table.setColumns(selectColumns(tableId));
        return table;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importGenTable(List<String> tableNames, String operName) {
        List<String> names = tableNames == null ? List.of()
                : tableNames.stream()
                        .filter(StringUtils::isNotEmpty)
                        .map(String::trim)
                        .distinct()
                        .toList();
        if (names.isEmpty()) {
            throw new ServiceException("请选择要导入的表");
        }

        // 1. 已导入的不允许重复导入
        List<GenTable> existed = genTableMapper.selectList(
                new LambdaQueryWrapper<GenTable>().in(GenTable::getTableName, names));
        if (!existed.isEmpty()) {
            String existedNames = existed.stream().map(GenTable::getTableName).collect(Collectors.joining(", "));
            throw new ServiceException("以下表已导入：" + existedNames);
        }

        // 2. 只导入 information_schema 里存在且不在排除名单里的表
        List<GenTable> tables = genTableMapper.selectDbTableListByNames(names);
        Set<String> found = tables.stream()
                .map(t -> t.getTableName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        List<String> missing = names.stream()
                .filter(n -> !found.contains(n.toLowerCase(Locale.ROOT)))
                .toList();
        if (!missing.isEmpty()) {
            throw new ServiceException("表不存在或不允许导入：" + String.join(", ", missing));
        }

        // 3. 先写 gen_table 拿到 tableId，再写该表的列
        for (GenTable table : tables) {
            GenUtils.initTable(table, genConfig, operName);
            genTableMapper.insert(table);

            List<GenTableColumn> columns = genTableColumnMapper.selectDbTableColumnsByName(table.getTableName());
            for (GenTableColumn column : columns) {
                GenUtils.initColumnField(column, table);
                genTableColumnMapper.insert(column);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGenTable(GenTable genTable, String operName) {
        validateEdit(genTable);
        GenTable db = genTableMapper.selectById(genTable.getTableId());
        if (db == null) {
            throw new ServiceException("生成配置不存在：" + genTable.getTableId());
        }

        // 只允许改生成配置，表名 / 生成方式等不让前端改
        GenTable update = new GenTable();
        update.setTableId(db.getTableId());
        update.setTableComment(genTable.getTableComment());
        update.setClassName(genTable.getClassName().trim());
        update.setPackageName(genTable.getPackageName().trim());
        update.setModuleName(genTable.getModuleName().trim());
        update.setBusinessName(genTable.getBusinessName().trim());
        update.setFunctionName(genTable.getFunctionName().trim());
        update.setFunctionAuthor(genTable.getFunctionAuthor());
        update.setRemark(genTable.getRemark());
        if (genTable.getParams() != null && !genTable.getParams().isEmpty()) {
            update.setOptions(JSON.toJSONString(genTable.getParams()));
        }
        update.setUpdateBy(operName);
        update.setUpdateTime(LocalDateTime.now());
        genTableMapper.updateById(update);

        if (genTable.getColumns() == null) {
            return;
        }
        Set<Long> ownColumnIds = selectColumns(db.getTableId()).stream()
                .map(GenTableColumn::getColumnId)
                .collect(Collectors.toSet());
        for (GenTableColumn column : genTable.getColumns()) {
            if (!ownColumnIds.contains(column.getColumnId())) {
                throw new ServiceException("列不属于该表：" + column.getColumnId());
            }
            genTableColumnMapper.updateById(toColumnUpdate(column, operName));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGenTableByIds(Long[] tableIds) {
        if (tableIds == null || tableIds.length == 0) {
            throw new ServiceException("删除主键不能为空");
        }
        List<Long> ids = Arrays.asList(tableIds);
        genTableMapper.deleteByIds(ids);
        genTableColumnMapper.delete(new LambdaQueryWrapper<GenTableColumn>().in(GenTableColumn::getTableId, ids));
    }

    @Override
    public Map<String, String> previewCode(Long tableId) {
        GenTable table = prepareTable(tableId);
        VelocityContext context = VelocityUtils.prepareContext(table);
        Map<String, String> result = new LinkedHashMap<>();
        for (String template : VelocityUtils.getTemplateList()) {
            result.put(VelocityUtils.getFileName(template, table), render(template, context));
        }
        return result;
    }

    @Override
    public byte[] downloadCode(Long tableId) {
        Map<String, String> files = previewCode(tableId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new ServiceException("生成 zip 失败：" + e.getMessage());
        }
        return out.toByteArray();
    }

    private List<GenTableColumn> selectColumns(Long tableId) {
        return genTableColumnMapper.selectList(new LambdaQueryWrapper<GenTableColumn>()
                .eq(GenTableColumn::getTableId, tableId)
                .orderByAsc(GenTableColumn::getSort));
    }

    /**
     * 查表和列并设置主键列；没有主键时用第一列兜底（同 pmhub）。
     */
    private GenTable prepareTable(Long tableId) {
        GenTable table = selectGenTableById(tableId);
        if (table.getColumns().isEmpty()) {
            throw new ServiceException("表没有列信息，请删除后重新导入：" + table.getTableName());
        }
        GenTableColumn pk = table.getColumns().stream()
                .filter(GenTableColumn::isPk)
                .findFirst()
                .orElse(table.getColumns().get(0));
        table.setPkColumn(pk);
        return table;
    }

    private String render(String template, VelocityContext context) {
        Template tpl = velocityEngine.getTemplate(template, StandardCharsets.UTF_8.name());
        StringWriter sw = new StringWriter();
        tpl.merge(context, sw);
        return sw.toString();
    }

    private void validateEdit(GenTable genTable) {
        if (genTable == null || genTable.getTableId() == null) {
            throw new ServiceException("tableId 不能为空");
        }
        requireText(genTable.getClassName(), "实体类名称不能为空");
        requireText(genTable.getPackageName(), "生成包路径不能为空");
        requireText(genTable.getModuleName(), "生成模块名不能为空");
        requireText(genTable.getBusinessName(), "生成业务名不能为空");
        requireText(genTable.getFunctionName(), "生成功能名不能为空");
        if (!genTable.getClassName().trim().matches("[A-Z][A-Za-z0-9]*")) {
            throw new ServiceException("实体类名称需大写字母开头，只含字母数字");
        }
        if (!genTable.getPackageName().trim().matches("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+")) {
            throw new ServiceException("生成包路径格式不正确，如 com.zhq.taskforge.system");
        }
        if (genTable.getTplCategory() != null && !GenConstants.TPL_CRUD.equals(genTable.getTplCategory())) {
            throw new ServiceException("TaskForge 只支持单表（crud）生成");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ServiceException(message);
        }
    }

    /**
     * 列只更新生成相关字段；勾选项统一成 "1" / "0"，避免前端传 null 时 updateById 跳过不改。
     */
    private GenTableColumn toColumnUpdate(GenTableColumn column, String operName) {
        requireText(column.getJavaField(), "Java 属性不能为空：" + column.getColumnName());
        requireText(column.getJavaType(), "Java 类型不能为空：" + column.getColumnName());
        GenTableColumn update = new GenTableColumn();
        update.setColumnId(column.getColumnId());
        update.setColumnComment(column.getColumnComment());
        update.setJavaType(column.getJavaType());
        update.setJavaField(column.getJavaField().trim());
        update.setIsInsert(flag(column.getIsInsert()));
        update.setIsEdit(flag(column.getIsEdit()));
        update.setIsList(flag(column.getIsList()));
        update.setIsQuery(flag(column.getIsQuery()));
        update.setIsRequired(flag(column.getIsRequired()));
        update.setQueryType(column.getQueryType());
        update.setHtmlType(column.getHtmlType());
        update.setDictType(column.getDictType() == null ? "" : column.getDictType());
        update.setUpdateBy(operName);
        update.setUpdateTime(LocalDateTime.now());
        return update;
    }

    private String flag(String value) {
        return GenConstants.REQUIRE.equals(value) ? GenConstants.REQUIRE : "0";
    }
}
