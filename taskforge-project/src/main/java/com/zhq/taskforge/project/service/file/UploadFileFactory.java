package com.zhq.taskforge.project.service.file;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.project.domain.vo.file.FileVO;

/**
 * 按 type 分流到对应 Executor（对照 pmhub UploadFileFactory）。
 */
@Service
public class UploadFileFactory {

    private static final Map<String, String> TYPE_TO_BEAN = new ConcurrentHashMap<>();

    static {
        for (UploadTypeEnum e : UploadTypeEnum.values()) {
            TYPE_TO_BEAN.put(e.getType(), e.getBeanName());
        }
    }

    /** key = Spring bean 名，如 uploadProjectFileExecutor */
    @Autowired
    private Map<String, UploadAbstractExecutor> executorMap;

    public FileVO execute(String type, MultipartFile file, String id) throws Exception {
        if (!StringUtils.hasText(type)) {
            throw new ServiceException("上传类型不能为空");
        }
        if (!StringUtils.hasText(id)) {
            throw new ServiceException("业务 id 不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }
        String beanName = TYPE_TO_BEAN.get(type);
        if (beanName == null) {
            throw new ServiceException("不支持的上传类型: " + type);
        }
        UploadAbstractExecutor executor = executorMap.get(beanName);
        if (executor == null) {
            throw new ServiceException("上传执行器未注册: " + beanName);
        }
        return executor.upload(file, id);
    }
}
