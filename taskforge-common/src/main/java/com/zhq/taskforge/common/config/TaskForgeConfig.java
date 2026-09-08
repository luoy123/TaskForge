package com.zhq.taskforge.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取 taskforge.* 配置（上传根目录）。
 */
@Component
@ConfigurationProperties(prefix = "taskforge")
public class TaskForgeConfig {

    private static String profile;

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        TaskForgeConfig.profile = profile;
    }

    public static String getProjectPath() {
        return getProfile() + "/project";
    }

    public static String getTaskPath() {
        return getProfile() + "/task";
    }

    public static String getCoverPath() {
        return getProfile() + "/cover";
    }

    public static String getTemplatePath() {
        return getProfile() + "/template";
    }

    public static String getImportPath() {
        return getProfile() + "/import";
    }

    public static String getDownloadPath() {
        return getProfile() + "/download/";
    }
}
