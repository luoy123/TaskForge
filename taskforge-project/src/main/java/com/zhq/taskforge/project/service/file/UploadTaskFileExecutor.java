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
import com.zhq.taskforge.project.domain.ProjectFile;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.domain.vo.file.FileVO;
import com.zhq.taskforge.project.mapper.ProjectFileMapper;
import com.zhq.taskforge.project.mapper.ProjectLogMapper;
import com.zhq.taskforge.project.mapper.ProjectTaskMapper;

/**
 * 任务文件上传。业务逻辑请你实现（G2）。
 */
@Service("uploadTaskFileExecutor")
public class UploadTaskFileExecutor extends UploadAbstractExecutor {

    @Autowired
    private ProjectFileMapper projectFileMapper;
    @Autowired
    private ProjectLogMapper projectLogMapper;
    @Autowired
    private ProjectTaskMapper projectTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO upload(MultipartFile file, String id) throws Exception {
        // TODO G2 UploadTaskFileExecutor:
        // 1. 查任务 id，不存在/已删 → 抛异常；取出 projectId
        ProjectTask task = projectTaskMapper.selectById(id);
        if (task == null || task.getDeleted() == 1) {
            throw new ServiceException("任务不存在");
        }
        String projectId = task.getProjectId();
        // 2. pathName = FileUploadUtils.uploadTaskFile(file)
        String pathName = FileUploadUtils.uploadTaskFile(file);
        // 3. insert ProjectFile：type="task", ptId=任务id, projectId=上面查出的
        LocalDateTime now = LocalDateTime.now();
        ProjectFile projectFile = new ProjectFile();
        projectFile.setType("task");
        projectFile.setPtId(id);
        projectFile.setProjectId(projectId);
        projectFile.setFileName(file.getOriginalFilename());
        projectFile.setExtension(FileUploadUtils.getExtension(file));
        projectFile.setFileSize(new BigDecimal(file.getSize() / 1024).setScale(2, RoundingMode.HALF_UP));
        projectFile.setPathName(pathName);
        projectFile.setFileUrl(pathName);
        projectFile.setUserId(SecurityUtils.getUserId());
        projectFile.setCreatedBy(SecurityUtils.getUsername());
        projectFile.setCreatedTime(now);
        projectFile.setUpdatedBy(SecurityUtils.getUsername());
        projectFile.setUpdatedTime(now);
        projectFile.setDeleted(0);
        projectFile.setDeletedTime(null);
        projectFileMapper.insert(projectFile);
        // 4. 日志 operateType=uploadTaskFile, type=task, ptId=任务id, projectId=...
        LocalDateTime nowLog = LocalDateTime.now();
        ProjectLog projectLog = new ProjectLog();
        projectLog.setLogType(LogTypeEnum.DELIVERABLE.getStatus());
        projectLog.setOperateType("uploadTaskFile");
        projectLog.setType("task");
        projectLog.setUserId(SecurityUtils.getUserId());
        projectLog.setPtId(id);
        projectLog.setProjectId(projectId);
        projectLog.setContent(pathName);
        projectLog.setUserId(SecurityUtils.getUserId());
        projectLog.setCreatedBy(SecurityUtils.getUsername());
        projectLog.setCreatedTime(nowLog);
        projectLog.setUpdatedBy(SecurityUtils.getUsername());
        projectLog.setUpdatedTime(nowLog);
        projectLogMapper.insert(projectLog);
        // 5. return FileVO
        FileVO fileVO = new FileVO();
        fileVO.setFileId(projectFile.getId());
        fileVO.setFileName(projectFile.getFileName());
        fileVO.setFileUrl(pathName);
        return fileVO;
    }
}
