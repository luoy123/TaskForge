package com.zhq.taskforge.project.service.file;

import org.springframework.web.multipart.MultipartFile;

import com.zhq.taskforge.project.domain.vo.file.FileVO;

/**
 * 上传执行器抽象（G2）。
 */
public abstract class UploadAbstractExecutor {

    /**
     * @param file 上传文件
     * @param id   project：项目 id；task：任务 id；cover：项目 id
     */
    public abstract FileVO upload(MultipartFile file, String id) throws Exception;
}
