package com.zhq.taskforge.generator.util;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.generator.config.GenConfig;
import com.zhq.taskforge.generator.constant.GenConstants;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.domain.GenTableColumn;

/**
 * 代码生成器工具类：根据 information_schema 的原始信息推算 gen_table / gen_table_column 的默认值。
 */
public class GenUtils {

    private GenUtils() {
    }

    /**
     * 初始化表信息。
     */
    public static void initTable(GenTable table, GenConfig config, String operName) {
        String tableName = table.getTableName();
        table.setClassName(convertClassName(tableName, config));
        table.setPackageName(config.getPackageName());
        table.setModuleName(getModuleName(config.getPackageName()));
        table.setBusinessName(getBusinessName(tableName));
        table.setFunctionName(getFunctionName(table.getTableComment(), tableName));
        table.setFunctionAuthor(config.getAuthor());
        table.setTplCategory(GenConstants.TPL_CRUD);
        table.setGenType(GenConstants.GEN_TYPE_ZIP);
        table.setCreateBy(operName);
        // 从 information_schema 读出来的是建表时间，不能当导入时间
        table.setCreateTime(LocalDateTime.now());
        table.setUpdateTime(null);
    }

    /**
     * 初始化列属性字段。
     */
    public static void initColumnField(GenTableColumn column, GenTable table) {
        String dataType = getDbType(column.getColumnType());
        String columnName = column.getColumnName();
        column.setTableId(table.getTableId());
        column.setCreateBy(table.getCreateBy());
        column.setCreateTime(LocalDateTime.now());
        column.setJavaField(StringUtils.toCamelCase(columnName));
        column.setJavaType(GenConstants.TYPE_STRING);
        column.setQueryType(GenConstants.QUERY_EQ);
        column.setHtmlType(GenConstants.HTML_INPUT);

        if (arraysContains(GenConstants.COLUMNTYPE_STR, dataType)
                || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType)) {
            // 字符串长度超过 500 或文本类型用文本域
            boolean longText = getColumnLength(column.getColumnType()) >= 500
                    || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType);
            column.setHtmlType(longText ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT);
        } else if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType)) {
            column.setJavaType(GenConstants.TYPE_LOCAL_DATE_TIME);
            column.setHtmlType(GenConstants.HTML_DATETIME);
        } else if (arraysContains(GenConstants.COLUMNTYPE_NUMBER, dataType)) {
            column.setJavaType(getNumberJavaType(dataType));
        }

        // 插入字段（默认所有字段都需要插入）
        column.setIsInsert(GenConstants.REQUIRE);
        // 编辑 / 列表 / 查询字段：主键和审计字段除外
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName) && !column.isPk()) {
            column.setIsEdit(GenConstants.REQUIRE);
        }
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName) && !column.isPk()) {
            column.setIsList(GenConstants.REQUIRE);
        }
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !column.isPk()) {
            column.setIsQuery(GenConstants.REQUIRE);
        }

        String lowerName = columnName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith("name")) {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }
        if (lowerName.endsWith("status")) {
            column.setHtmlType(GenConstants.HTML_RADIO);
        } else if (lowerName.endsWith("type") || lowerName.endsWith("sex")) {
            column.setHtmlType(GenConstants.HTML_SELECT);
        } else if (lowerName.endsWith("image")) {
            column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
        } else if (lowerName.endsWith("file")) {
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
        } else if (lowerName.endsWith("content")) {
            column.setHtmlType(GenConstants.HTML_EDITOR);
        }
    }

    /**
     * 表名转 Java 类名：sys_notice → SysNotice；开启 autoRemovePre 时先去掉前缀。
     */
    public static String convertClassName(String tableName, GenConfig config) {
        if (config.isAutoRemovePre() && StringUtils.isNotEmpty(config.getTablePrefix())) {
            for (String prefix : config.getTablePrefix().split(",")) {
                String p = prefix.trim();
                if (!p.isEmpty() && tableName.startsWith(p)) {
                    tableName = tableName.substring(p.length());
                    break;
                }
            }
        }
        return StringUtils.convertToCamelCase(tableName);
    }

    /**
     * 模块名取包名最后一段：com.zhq.taskforge.system → system。
     */
    public static String getModuleName(String packageName) {
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * 业务名取表名最后一段：sys_notice → notice。
     */
    public static String getBusinessName(String tableName) {
        return tableName.substring(tableName.lastIndexOf('_') + 1);
    }

    /**
     * 功能名：去掉表注释末尾的「表」；注释为空时用表名。
     */
    public static String getFunctionName(String tableComment, String tableName) {
        if (StringUtils.isEmpty(tableComment) || tableComment.isBlank()) {
            return tableName;
        }
        String name = tableComment.trim();
        if (name.length() > 1 && name.endsWith("表")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * 列类型去掉长度和修饰：varchar(50) → varchar，bigint unsigned → bigint。
     */
    public static String getDbType(String columnType) {
        String type = columnType.toLowerCase(Locale.ROOT).trim();
        int paren = type.indexOf('(');
        if (paren > 0) {
            type = type.substring(0, paren);
        }
        int space = type.indexOf(' ');
        if (space > 0) {
            type = type.substring(0, space);
        }
        return type;
    }

    /**
     * 字符串长度：varchar(50) → 50，无长度返回 0。
     */
    public static int getColumnLength(String columnType) {
        int start = columnType.indexOf('(');
        int end = columnType.indexOf(')');
        if (start < 0 || end <= start) {
            return 0;
        }
        try {
            return Integer.parseInt(columnType.substring(start + 1, end).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 数字类型按类型名判断，不看括号里的显示宽度（MySQL 8 已不显示 int(11)）。
     */
    private static String getNumberJavaType(String dataType) {
        if (arraysContains(GenConstants.COLUMNTYPE_FLOAT, dataType)) {
            return GenConstants.TYPE_BIGDECIMAL;
        }
        if ("bigint".equals(dataType)) {
            return GenConstants.TYPE_LONG;
        }
        return GenConstants.TYPE_INTEGER;
    }

    public static boolean arraysContains(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }
}
