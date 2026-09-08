package com.zhq.taskforge.project.service.file;

/**
 * 上传类型 → Spring Bean 名（必须与 @Service("...") 一致）。
 */
public enum UploadTypeEnum {

    PROJECT("project", "uploadProjectFileExecutor"),
    TASK("task", "uploadTaskFileExecutor"),
    COVER("cover", "uploadCoverFileExecutor"),
    TEMPLATE("template", "uploadTemplateFileExecutor");

    private final String type;
    private final String beanName;

    UploadTypeEnum(String type, String beanName) {
        this.type = type;
        this.beanName = beanName;
    }

    public String getType() {
        return type;
    }

    public String getBeanName() {
        return beanName;
    }
}
