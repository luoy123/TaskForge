package com.zhq.taskforge.project.service.file;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zhq.taskforge.common.enums.LogTypeEnum;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.common.utils.file.FileUploadUtils;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.ProjectFile;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.vo.file.FileVO;
import com.zhq.taskforge.project.mapper.ProjectFileMapper;
import com.zhq.taskforge.project.mapper.ProjectLogMapper;
import com.zhq.taskforge.project.mapper.ProjectMapper;

/**
 * 项目文件上传。业务逻辑请你实现（G2）。
 */
@Service("uploadProjectFileExecutor")
public class UploadProjectFileExecutor extends UploadAbstractExecutor {

    @Autowired
    private ProjectFileMapper projectFileMapper;
    @Autowired
    private ProjectLogMapper projectLogMapper;
    @Autowired
    private ProjectMapper projectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO upload(MultipartFile file, String id) throws Exception {
        // TODO G2 UploadProjectFileExecutor:
        // 1. projectMapper.selectById(id)，不存在则抛异常
        Project project = projectMapper.selectById(id);
        if (project == null || project.getDeleted() == 1) {
            throw new ServiceException("项目不存在");
        }
        // 2. pathName = FileUploadUtils.uploadProjectFile(file)
        String pathName = FileUploadUtils.uploadProjectFile(file);
        // 3. insert ProjectFile：
        // type="project", ptId=id, projectId=id
        // fileName=原始名, extension=FileUploadUtils.getExtension(file)
        // fileSize = size/1024 (BigDecimal, 2 位)
        // pathName / fileUrl = pathName（G1 返回绝对路径，可先两者相同）
        // userId / 审计 / deleted=0
        LocalDateTime now = LocalDateTime.now();
        ProjectFile projectFile = new ProjectFile();
        projectFile.setType("project");
        projectFile.setPtId(id);
        projectFile.setProjectId(id);
        projectFile.setFileName(file.getOriginalFilename());
        projectFile.setExtension(FileUploadUtils.getExtension(file));
        projectFile.setFileSize(new BigDecimal(file.getSize() / 1024).setScale(2, RoundingMode.HALF_UP));
        projectFile.setPathName(pathName);
        projectFile.setFileUrl(pathName);
        projectFile.setUserId(SecurityUtils.getUserId());
        projectFile.setCreatedBy(SecurityUtils.getUsername());
        projectFile.setCreatedTime(LocalDateTime.now());
        projectFile.setUpdatedBy(SecurityUtils.getUsername());
        projectFile.setUpdatedTime(now);
        projectFile.setDeleted(0);
        projectFile.setDeletedTime(null);
        projectFileMapper.insert(projectFile);
        // 4. insert ProjectLog：logType=DELIVERABLE(2), operateType=uploadProjectFile,
        // type=project, ptId=id, projectId=id, content=fileUrl
        ProjectLog projectLog = new ProjectLog();
        projectLog.setLogType(LogTypeEnum.DELIVERABLE.getStatus());
        projectLog.setOperateType("uploadProjectFile");
        projectLog.setType("project");
        projectLog.setPtId(id);
        projectLog.setProjectId(id);
        projectLog.setContent(pathName);
        projectLog.setUserId(SecurityUtils.getUserId());
        projectLog.setCreatedBy(SecurityUtils.getUsername());
        projectLog.setCreatedTime(now);
        projectLog.setUpdatedBy(SecurityUtils.getUsername());
        projectLog.setUpdatedTime(now);
        projectLogMapper.insert(projectLog);
        // 5. return FileVO(fileId, fileName, fileUrl)
        FileVO fileVO = new FileVO();
        fileVO.setFileId(projectFile.getId());
        fileVO.setFileName(projectFile.getFileName());
        fileVO.setFileUrl(pathName);
        return fileVO;
    }
}
