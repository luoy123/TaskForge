package com.zhq.taskforge.auth.vo;

import lombok.Data;

@Data
public class MetaVo {

    private String title;
    private String icon;
    private boolean noCache;
    private String link;

    public MetaVo() {
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

    public MetaVo(String title, String icon, boolean noCache, String link) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        this.link = link;
    }
}