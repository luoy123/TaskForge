package com.zhq.taskforge.common.utils.file;

/**
 * 允许的上传后缀。
 */
public final class MimeTypeUtils {

    private MimeTypeUtils() {
    }

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            "bmp", "gif", "jpg", "jpeg", "png",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            "rar", "zip", "gz", "bz2",
            "mp4", "avi", "rmvb", "pdf"
    };
}
