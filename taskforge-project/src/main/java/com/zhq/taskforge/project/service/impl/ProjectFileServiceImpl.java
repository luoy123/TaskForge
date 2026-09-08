package com.zhq.taskforge.project.service.impl;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.ProjectFile;
import com.zhq.taskforge.project.domain.vo.file.FileVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileIdsVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileReqVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileResVO;
import com.zhq.taskforge.project.mapper.ProjectFileMapper;
import com.zhq.taskforge.project.service.IProjectFileService;
import com.zhq.taskforge.project.service.file.UploadFileFactory;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件服务：上传走工厂；list/rename/delete/download 请你实现（G3）。
 */
@Service
public class ProjectFileServiceImpl implements IProjectFileService {

    @Autowired
    private ProjectFileMapper projectFileMapper;

    @Autowired
    private UploadFileFactory uploadFileFactory;

    @Override
    public FileVO upload(MultipartFile file, String id, String type) throws Exception {
        return uploadFileFactory.execute(type, file, id);
    }

    @Override
    public IPage<ProjectFileResVO> list(ProjectFileReqVO req) {
        // TODO G3 list:
        // 1. req.id 非空，否则 ServiceException
        if (req.getId() == null) {
            throw new ServiceException("id 不能为空");
        }

        // 2. pageNum/pageSize 默认 1 / 10
        Long pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        Long pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
        // 3. return projectFileMapper.queryFileList(new Page<>(pageNum, pageSize),
        // req);
        // （筛选 SQL 已在 XML，不必再写 Wrapper）
        return projectFileMapper.queryFileList(new Page<>(pageNum, pageSize), req);
    }

    @Override
    public void rename(ProjectFileReqVO req) {
        // TODO G3 rename:
        // 1. fileId、fileName 非空
        if (req.getFileId() == null || req.getFileName() == null || req.getFileName().isBlank()) {
            throw new ServiceException("fileId 和 fileName 不能为空且 fileName 不能为空字符串");
        }
        // 2. selectById；不存在或 deleted=1 → 抛异常
        ProjectFile projectFile = projectFileMapper.selectById(req.getFileId());
        if (projectFile == null || projectFile.getDeleted() == 1) {
            throw new ServiceException("文件不存在或已删除");
        }
        // 3. 第一版：只改库字段 fileName（+ updatedBy/Time），不必改磁盘文件名
        projectFile.setFileName(req.getFileName());
        projectFile.setUpdatedBy(SecurityUtils.getUsername());
        projectFile.setUpdatedTime(LocalDateTime.now());
        // 4. updateById
        projectFileMapper.updateById(projectFile);
    }

    @Override
    public void delete(ProjectFileIdsVO req) {
        // TODO G3 delete:
        // 1. fileIds 非空
        if (req.getFileIds() == null || req.getFileIds().isEmpty()) {
            throw new ServiceException("fileIds 不能为空");
        }
        // 2. for 每个 id：projectFileMapper.softDelete(id, now, username, now)
        for (String fileId : req.getFileIds()) {
            {
                if (fileId == null || projectFileMapper.selectById(fileId) == null
                        || projectFileMapper.selectById(fileId).getDeleted() == 1) {
                    throw new ServiceException("文件不存在或已删除");
                }
                projectFileMapper.softDelete(fileId, LocalDateTime.now(), SecurityUtils.getUsername(),
                        LocalDateTime.now());
            }
        }
        // 3. 磁盘文件第一版可保留（计划允许）；想删盘可用 new File(pathName).delete()
        // 4. 不要用 deleteById（会绕过软删字段）
    }

    @Override
    public void download(String fileId, HttpServletResponse response) throws Exception {
        // TODO G3 download:
        // 1. fileId 非空；查出记录，不存在/已删 → 抛异常
        if (fileId == null) {
            throw new ServiceException("fileId 不能为空");
        }
        ProjectFile projectFile = projectFileMapper.selectById(fileId);
        if (projectFile == null || projectFile.getDeleted() == 1) {
            throw new ServiceException("文件不存在或已删除");
        }
        // 2. pathName 对应磁盘 File 必须 exists
        File file = new File(projectFile.getPathName());
        if (!file.exists()) {
            throw new ServiceException("文件不存在");
        }
        // 3. response.setContentType("application/octet-stream");
        response.setContentType("application/octet-stream");
        // 4. response.setHeader("Content-Disposition",
        // "attachment; filename=" + URLEncoder.encode(fileName, UTF_8));
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(projectFile.getFileName(), StandardCharsets.UTF_8));
        // 5. 用 Files.copy(path, response.getOutputStream()) 或 IO 流拷贝
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
