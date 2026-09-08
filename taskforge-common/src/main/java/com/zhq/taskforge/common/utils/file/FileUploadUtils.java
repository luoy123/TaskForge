package com.zhq.taskforge.common.utils.file;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.zhq.taskforge.common.config.TaskForgeConfig;
import com.zhq.taskforge.common.exception.ServiceException;

/**
 * 文件上传工具：校验 → 唯一名 → transferTo → 返回绝对路径。
 */
public final class FileUploadUtils {

    public static final long DEFAULT_MAX_SIZE = 50L * 1024 * 1024;

    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    private FileUploadUtils() {
    }

    public static String uploadProjectFile(MultipartFile file) throws IOException {
        return upload(TaskForgeConfig.getProjectPath(), file);
    }

    public static String uploadTaskFile(MultipartFile file) throws IOException {
        return upload(TaskForgeConfig.getTaskPath(), file);
    }

    public static String uploadCoverFile(MultipartFile file) throws IOException {
        return upload(TaskForgeConfig.getCoverPath(), file);
    }

    public static String upload(String baseDir, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null
                || file.getOriginalFilename().isBlank()) {
            throw new ServiceException("文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.length() > DEFAULT_FILE_NAME_LENGTH) {
            throw new ServiceException("文件名长度不合法");
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0) {
            throw new ServiceException("文件名不合法，没有扩展名");
        }
        String extension = originalFilename.substring(index + 1).toLowerCase();
        boolean allowed = false;
        for (String ext : MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION) {
            if (ext.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new ServiceException("不允许的文件类型");
        }
        if (file.getSize() > DEFAULT_MAX_SIZE) {
            throw new ServiceException("文件大小不合法");
        }
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + UUID.randomUUID().toString().replace("-", "")
                + "." + extension;
        File absFile = getAbsoluteFile(baseDir, relativePath);
        file.transferTo(absFile);
        return absFile.getAbsolutePath();
    }

    public static File getAbsoluteFile(String baseDir, String relativeName) throws IOException {
        File desc = new File(baseDir + File.separator + relativeName);
        if (!desc.exists() && !desc.getParentFile().exists() && !desc.getParentFile().mkdirs()) {
            throw new IOException("创建目录失败: " + desc.getParent());
        }
        return desc;
    }

    /** 从原始文件名取扩展名（小写）；无后缀返回空串 */
    public static String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) {
            return "";
        }
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase();
    }
}
