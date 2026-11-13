package com.zhq.taskforge.framework.web.domain.server;

import lombok.Data;

/**
 * 磁盘（一个挂载点 / 盘符一条）
 */
@Data
public class SysFile {

    /** 盘符路径，如 C:\ 或 / */
    private String dirName;

    /** 文件系统类型，如 NTFS、ext4 */
    private String sysTypeName;

    /** 盘符名称 */
    private String typeName;

    /** 以下三个是已格式化的字符串，如 "120.5 GB" */
    private String total;

    private String free;

    private String used;

    /** 使用率 % */
    private double usage;
}
