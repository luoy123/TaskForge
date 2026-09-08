package com.zhq.taskforge.project.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.project.domain.vo.file.FileVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileIdsVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileReqVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileResVO;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目文件。
 */
public interface IProjectFileService {

    FileVO upload(MultipartFile file, String id, String type) throws Exception;

    /** 分页列表（G3） */
    IPage<ProjectFileResVO> list(ProjectFileReqVO req);

    /** 重命名（G3） */
    void rename(ProjectFileReqVO req);

    /** 批量软删（G3） */
    void delete(ProjectFileIdsVO req);

    /** 单文件下载（G3） */
    void download(String fileId, HttpServletResponse response) throws Exception;
}
