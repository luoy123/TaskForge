package com.zhq.taskforge.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 读取 gen.* 配置（代码生成默认作者、包名、表前缀）。
 * 由 Service 注入后作为参数传给 GenUtils，不走静态字段。
 */
@Data
@Component
@ConfigurationProperties(prefix = "gen")
public class GenConfig {

    /** 作者 */
    private String author;

    /** 默认生成包路径，如 com.zhq.taskforge.system */
    private String packageName;

    /** 生成类名时是否去掉表前缀 */
    private boolean autoRemovePre;

    /** 表前缀，多个用逗号分隔 */
    private String tablePrefix;
}
