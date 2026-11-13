package com.zhq.taskforge.generator.util;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

import org.apache.velocity.VelocityContext;

import com.zhq.taskforge.generator.constant.GenConstants;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.domain.GenTableColumn;

/**
 * 模板处理工具类（对照 pmhub VelocityUtils；只支持单表 crud）。
 */
public class VelocityUtils {

    private static final String JAVA_PATH = "main/java";

    private static final String MAPPER_XML_PATH = "main/resources/mapper";

    public static final String TPL_DOMAIN = "vm/java/domain.java.vm";
    public static final String TPL_MAPPER = "vm/java/mapper.java.vm";
    public static final String TPL_SERVICE = "vm/java/service.java.vm";
    public static final String TPL_SERVICE_IMPL = "vm/java/serviceImpl.java.vm";
    public static final String TPL_CONTROLLER = "vm/java/controller.java.vm";
    public static final String TPL_MAPPER_XML = "vm/xml/mapper.xml.vm";

    private static final List<String> TEMPLATES = List.of(
            TPL_DOMAIN, TPL_MAPPER, TPL_SERVICE, TPL_SERVICE_IMPL, TPL_CONTROLLER, TPL_MAPPER_XML);

    private VelocityUtils() {
    }

    public static List<String> getTemplateList() {
        return TEMPLATES;
    }

    /**
     * 设置模板变量。调用前需已设置 table.columns 和 table.pkColumn。
     */
    public static VelocityContext prepareContext(GenTable table) {
        String packageName = table.getPackageName();
        String functionName = table.getFunctionName();
        GenTableColumn pk = table.getPkColumn();

        VelocityContext context = new VelocityContext();
        context.put("tableName", table.getTableName());
        context.put("functionName", functionName == null || functionName.isBlank() ? "【请填写功能名称】" : functionName);
        context.put("ClassName", table.getClassName());
        context.put("className", uncapitalize(table.getClassName()));
        context.put("moduleName", table.getModuleName());
        context.put("businessName", table.getBusinessName());
        context.put("basePackage", getPackagePrefix(packageName));
        context.put("packageName", packageName);
        context.put("author", table.getFunctionAuthor());
        context.put("datetime", LocalDate.now().toString());
        context.put("pkColumn", pk);
        context.put("pkIdType", getPkIdType(pk));
        context.put("importList", getImportList(table));
        context.put("permissionPrefix", table.getModuleName() + ":" + table.getBusinessName());
        context.put("columns", table.getColumns());
        context.put("table", table);
        context.put("hasCreateBy", hasColumn(table, "createBy", GenConstants.TYPE_STRING));
        context.put("hasCreateTime", hasColumn(table, "createTime", GenConstants.TYPE_LOCAL_DATE_TIME));
        context.put("hasUpdateBy", hasColumn(table, "updateBy", GenConstants.TYPE_STRING));
        context.put("hasUpdateTime", hasColumn(table, "updateTime", GenConstants.TYPE_LOCAL_DATE_TIME));
        return context;
    }

    /**
     * zip 内路径。Controller 按 TaskForge 习惯放 admin 的 web.controller.{moduleName} 包。
     */
    public static String getFileName(String template, GenTable table) {
        String className = table.getClassName();
        String javaPath = JAVA_PATH + "/" + table.getPackageName().replace('.', '/');
        return switch (template) {
            case TPL_DOMAIN -> javaPath + "/domain/" + className + ".java";
            case TPL_MAPPER -> javaPath + "/mapper/" + className + "Mapper.java";
            case TPL_SERVICE -> javaPath + "/service/I" + className + "Service.java";
            case TPL_SERVICE_IMPL -> javaPath + "/service/impl/" + className + "ServiceImpl.java";
            case TPL_CONTROLLER -> JAVA_PATH + "/" + getPackagePrefix(table.getPackageName()).replace('.', '/')
                    + "/web/controller/" + table.getModuleName() + "/" + className + "Controller.java";
            case TPL_MAPPER_XML -> MAPPER_XML_PATH + "/" + table.getModuleName() + "/" + className + "Mapper.xml";
            default -> throw new IllegalArgumentException("未知模板：" + template);
        };
    }

    /**
     * com.zhq.taskforge.system → com.zhq.taskforge
     */
    public static String getPackagePrefix(String packageName) {
        int lastIndex = packageName.lastIndexOf('.');
        return lastIndex < 0 ? packageName : packageName.substring(0, lastIndex);
    }

    /**
     * 实体需要额外 import 的类，排好序保证生成结果稳定。
     */
    public static TreeSet<String> getImportList(GenTable table) {
        TreeSet<String> imports = new TreeSet<>();
        for (GenTableColumn column : table.getColumns()) {
            if (GenConstants.TYPE_LOCAL_DATE_TIME.equals(column.getJavaType())) {
                imports.add("java.time.LocalDateTime");
                imports.add("com.fasterxml.jackson.annotation.JsonFormat");
            } else if (GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                imports.add("java.math.BigDecimal");
            } else if (GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                imports.add("java.util.Date");
                imports.add("com.fasterxml.jackson.annotation.JsonFormat");
            }
        }
        return imports;
    }

    /**
     * 自增主键用 AUTO；非自增 Long 用雪花 ASSIGN_ID；其余由调用方赋值 INPUT。
     */
    private static String getPkIdType(GenTableColumn pk) {
        if (pk == null) {
            return "AUTO";
        }
        if (pk.isIncrement()) {
            return "AUTO";
        }
        return GenConstants.TYPE_LONG.equals(pk.getJavaType()) ? "ASSIGN_ID" : "INPUT";
    }

    private static boolean hasColumn(GenTable table, String javaField, String javaType) {
        return table.getColumns().stream()
                .anyMatch(c -> javaField.equals(c.getJavaField()) && javaType.equals(c.getJavaType()));
    }

    private static String uncapitalize(String s) {
        return s == null || s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
