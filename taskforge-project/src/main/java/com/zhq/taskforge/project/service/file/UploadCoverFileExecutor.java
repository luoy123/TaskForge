package com.zhq.taskforge.project.service.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.file.FileUploadUtils;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.vo.file.FileVO;
import com.zhq.taskforge.project.mapper.ProjectMapper;

/**
 * 项目封面上传。业务逻辑请你实现（G2）。
 * <p>
 * 对照 pmhub：可不插 ProjectFile，只更新 Project.cover。
 */
@Service("uploadCoverFileExecutor")
public class UploadCoverFileExecutor extends UploadAbstractExecutor {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO upload(MultipartFile file, String id) throws Exception {
        // TODO G2 UploadCoverFileExecutor:
        // 1. 查项目 id，不存在 → 抛异常
        Project project = projectMapper.selectById(id);
        if (project == null || project.getDeleted() == 1) {
            throw new ServiceException("项目不存在");
        }
        // 2. path = FileUploadUtils.uploadCoverFile(file)
        String path = FileUploadUtils.uploadCoverFile(file);
        // 3. project.setCover(path); projectMapper.updateById(project)
        project.setCover(path);
        projectMapper.updateById(project);
        // 4. return FileVO（fileName + fileUrl 即可，fileId 可空）
        FileVO fileVO = new FileVO();
        fileVO.setFileName(file.getOriginalFilename());
        fileVO.setFileUrl(path);
        return fileVO;
    }
}
