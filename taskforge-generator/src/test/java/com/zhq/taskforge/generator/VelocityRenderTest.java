package com.zhq.taskforge.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import com.zhq.taskforge.generator.config.VelocityConfig;

class VelocityRenderTest {

    private final VelocityEngine engine = new VelocityConfig().velocityEngine();

    @Test
    void renderDomainTemplate() {
        Template template = engine.getTemplate("vm/java/domain.java.vm", StandardCharsets.UTF_8.name());
        VelocityContext context = new VelocityContext();
        context.put("packageName", "com.zhq.taskforge.system");

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        String code = writer.toString();
        System.out.println(code);

        assertEquals("package com.zhq.taskforge.system.domain;", code.lines().findFirst().orElse(""));
        assertTrue(code.contains("对象"), "模板中文注释应正常读取");
    }

    @Test
    void allTemplatesLoadable() {
        String[] templates = {
                "vm/java/domain.java.vm",
                "vm/java/mapper.java.vm",
                "vm/java/service.java.vm",
                "vm/java/serviceImpl.java.vm",
                "vm/java/controller.java.vm",
                "vm/xml/mapper.xml.vm"
        };
        for (String name : templates) {
            engine.getTemplate(name, StandardCharsets.UTF_8.name());
        }
    }
}
