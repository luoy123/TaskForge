package com.zhq.taskforge.project.service.project;

public enum QueryProjectEnum {

    MY("my", "queryMyProjectExecutor"),
    COLLECT("collect", "queryMyCollectProjectExecutor"),
    RECYCLE("recycle", "queryMyRecycleProjectExecutor");

    private final String type;
    private final String beanName;

    QueryProjectEnum(String type, String beanName) {
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
