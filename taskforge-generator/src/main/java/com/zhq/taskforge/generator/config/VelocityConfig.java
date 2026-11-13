package com.zhq.taskforge.generator.config;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Velocity 引擎：从 classpath 读取 vm/ 下的模板。
 */
@Configuration
public class VelocityConfig {

    private static final String LOADER_NAME = "classpath";

    @Bean
    public VelocityEngine velocityEngine() {
        //告诉Velocity引擎从哪里来获取.vm模板文件
        Properties p = new Properties();
        p.setProperty(RuntimeConstants.RESOURCE_LOADERS, LOADER_NAME);
        p.setProperty("resource.loader." + LOADER_NAME + ".class", ClasspathResourceLoader.class.getName());
        p.setProperty(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        VelocityEngine engine = new VelocityEngine(p);
        engine.init();
        return engine;
    }
}
