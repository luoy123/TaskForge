package com.zhq.taskforge.framework.web.domain.server;

import lombok.Data;

/**
 * 服务器基本信息
 */
@Data
public class Sys {

    private String computerName;

    private String computerIp;

    /** 项目启动目录 */
    private String userDir;

    private String osName;

    private String osArch;
}
