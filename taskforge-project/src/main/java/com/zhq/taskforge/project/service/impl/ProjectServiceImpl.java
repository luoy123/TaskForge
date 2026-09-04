package com.zhq.taskforge.project.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.enums.LogTypeEnum;
import com.zhq.taskforge.common.enums.ProjectStageEnum;
import com.zhq.taskforge.common.enums.ProjectStatusEnum;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.ProjectCollection;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.ProjectMember;
import com.zhq.taskforge.project.domain.ProjectStage;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;
import com.zhq.taskforge.project.mapper.ProjectCollectionMapper;
import com.zhq.taskforge.project.mapper.ProjectLogMapper;
import com.zhq.taskforge.project.mapper.ProjectMapper;
import com.zhq.taskforge.project.mapper.ProjectMemberMapper;
import com.zhq.taskforge.project.mapper.ProjectStageMapper;
import com.zhq.taskforge.project.service.IProjectService;

import cn.hutool.core.util.IdUtil;

@Service
public class ProjectServiceImpl implements IProjectService {

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ProjectStageMapper projectStageMapper;
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    @Autowired
    private ProjectLogMapper projectLogMapper;
    @Autowired
    private ProjectCollectionMapper projectCollectionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProject(Project project) {
        // TODO E2: 
        // 1. projectCode = "P" + 序列（可用 IdUtil / 自写 Seq）
        //雪花算法：分布式下唯一且大致有序的Long ID
        project.setProjectCode("p" + IdUtil.getSnowflakeNextIdStr());
        // 2. 填 userId、createdBy、createdTime、updated*；建议显式 deleted/status/archived/published/stageCode 默认值
        project.setUserId(SecurityUtils.getUserId());
        project.setCreatedBy(SecurityUtils.getUsername());
        project.setCreatedTime(LocalDateTime.now());
        project.setUpdatedBy(SecurityUtils.getUsername());
        project.setUpdatedTime(LocalDateTime.now());
        // 3. projectMapper.insert(project)  —— insert 后 project.getId() 有值
        projectMapper.insert(project);
        // 4. 遍历 ProjectStageEnum，insert 全部阶段,就是每个项目在创建后会有五个模板存放到数据库中。
        for(ProjectStageEnum stage : ProjectStageEnum.values()){
            ProjectStage  projectStage = new ProjectStage();
            projectStage.setProjectId(project.getId());//归属于那个项目
            projectStage.setStageCode(stage.getStatus());//阶段代码
            projectStage.setStageName(stage.getStatusName());//阶段名称

            projectStage.setCreatedBy(SecurityUtils.getUsername());
            projectStage.setCreatedTime(LocalDateTime.now());
            projectStage.setUpdatedBy(SecurityUtils.getUsername());
            projectStage.setUpdatedTime(LocalDateTime.now());
            projectStageMapper.insert(projectStage);
        }
        // 5. 查 STAGE_0，回写 projectStageId，updateById
        LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<ProjectStage>();
        qw.eq(ProjectStage::getProjectId,project.getId())
            .eq(ProjectStage::getStageCode,ProjectStageEnum.STAGE_0.getStatus());
            ProjectStage selectOne = projectStageMapper.selectOne(qw);
        project.setProjectStageId(selectOne.getId());
        project.setStageCode(selectOne.getStageCode());
        projectMapper.updateById(project);
        // 6. insert 创建者成员：ptId、type="project"、creator=1
        ProjectMember projectMember = new ProjectMember();
        projectMember.setPtId(project.getId());
        projectMember.setType(ProjectStatusEnum.PROJECT.getStatusName());
        projectMember.setCreator(1);
        projectMember.setUserId(SecurityUtils.getUserId());
        projectMember.setJoinedTime(LocalDateTime.now());
        projectMember.setCreatedBy(SecurityUtils.getUsername());
        projectMember.setCreatedTime(LocalDateTime.now());
        projectMember.setUpdatedBy(SecurityUtils.getUsername());
        projectMember.setUpdatedTime(LocalDateTime.now());
        projectMemberMapper.insert(projectMember);

        // 7. 写日志 create + inviteMember（可抽 private saveLog）
        saveLog("create", project.getId(), null);
        saveLog("inviteMember",project.getId(),SecurityUtils.getUserId());
    }

    @Override
    public ProjectResVO detail(String projectId) {
        ProjectResVO vo = projectMapper.detail(projectId);
        if (vo == null) {
            throw new ServiceException("项目不存在");
        }

        // status → 中文名
        vo.setStatusName(ProjectStatusEnum.getStatusNameByStatus(vo.getStatus()));

        // 私有项目：非成员不可看
        if (vo.getProjectType() != null && vo.getProjectType() == 1) {
            LambdaQueryWrapper<ProjectMember> memberQw = new LambdaQueryWrapper<>();
            memberQw.eq(ProjectMember::getUserId, SecurityUtils.getUserId())
                    .eq(ProjectMember::getType, ProjectStatusEnum.PROJECT.getStatusName())
                    .eq(ProjectMember::getPtId, projectId);
            Long memberCount = projectMemberMapper.selectCount(memberQw);
            if (memberCount == null || memberCount == 0) {
                throw new ServiceException("该项目为私有项目，你不是项目成员无法查看");
            }
        }

        vo.setProjectTypeName(
                vo.getProjectType() != null && vo.getProjectType() == 1 ? "私有" : "公开");
        vo.setPublishedName(
                vo.getPublished() != null && vo.getPublished() == 1 ? "已发布" : "未发布");

        // 是否收藏：按「当前登录用户」查，不要用负责人 userId
        LambdaQueryWrapper<ProjectCollection> collectQw = new LambdaQueryWrapper<>();
        collectQw.eq(ProjectCollection::getUserId, SecurityUtils.getUserId())
                .eq(ProjectCollection::getProjectId, projectId);
        ProjectCollection collection = projectCollectionMapper.selectOne(collectQw);
        vo.setCollected(collection != null);

        // nickName 已在 ProjectMapper.xml join sys_user，直接返回
        return vo;
    }

    /**
     * 写项目动态日志。operateType 对齐 pmhub：create / inviteMember / …
     */
    private void saveLog(String operateType, String projectId, Long toUserId) {
        ProjectLog log = new ProjectLog();
        log.setLogType(LogTypeEnum.TRENDS.getStatus());
        log.setUserId(SecurityUtils.getUserId());
        log.setType(ProjectStatusEnum.PROJECT.getStatusName());
        log.setOperateType(operateType);
        log.setPtId(projectId);
        log.setProjectId(projectId);
        if (toUserId != null) {
            log.setToUserId(toUserId);
        }
        log.setCreatedBy(SecurityUtils.getUsername());
        log.setCreatedTime(LocalDateTime.now());
        log.setUpdatedBy(SecurityUtils.getUsername());
        log.setUpdatedTime(LocalDateTime.now());
        projectLogMapper.insert(log);
    }

    @Override
    public IPage<ProjectResVO> list(ProjectReqVO req) {
        long pageNum = req.getPageNum() == null ? 1L : req.getPageNum();
        long pageSize = req.getPageSize() == null ? 10L : req.getPageSize();
        Page<ProjectResVO> page = new Page<>(pageNum, pageSize);
        IPage<ProjectResVO> result = projectMapper.selectMyProjectList(page, SecurityUtils.getUserId(), req);

        for (ProjectResVO vo : result.getRecords()) {
            vo.setStatusName(ProjectStatusEnum.getStatusNameByStatus(vo.getStatus()));
            vo.setProjectTypeName(
                    vo.getProjectType() != null && vo.getProjectType() == 1 ? "私有" : "公开");
            vo.setPublishedName(
                    vo.getPublished() != null && vo.getPublished() == 1 ? "已发布" : "未发布");

            LambdaQueryWrapper<ProjectCollection> qw = new LambdaQueryWrapper<>();
            qw.eq(ProjectCollection::getUserId, SecurityUtils.getUserId())
                    .eq(ProjectCollection::getProjectId, vo.getProjectId());
            vo.setCollected(projectCollectionMapper.selectOne(qw) != null);
        }

        return result;
    }

    @Override
    public void edit(Project project) {
        if (project.getProjectId() == null || project.getProjectId().isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        project.setId(project.getProjectId());

        if (project.getProjectType() != null) {
            project.setType(project.getProjectType());
        }

        // 阶段写回
        if (project.getStageCode() != null) {
            LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<>();
            qw.eq(ProjectStage::getProjectId, project.getId())
                    .eq(ProjectStage::getStageCode, project.getStageCode());
            ProjectStage stage = projectStageMapper.selectOne(qw);
            if (stage == null) {
                throw new ServiceException("阶段不存在");
            }
            project.setProjectStageId(stage.getId());
        }

        // 仅已发布项目可归档（状态改为已归档时）
        if (Objects.equals(project.getStatus(), ProjectStatusEnum.ARCHIVED.getStatus())) {
            Project db = projectMapper.selectById(project.getId());
            if (db == null) {
                throw new ServiceException("项目不存在");
            }
            Integer published = project.getPublished() != null ? project.getPublished() : db.getPublished();
            if (published == null || published == 0) {
                throw new ServiceException("项目只有发布了才能进行归档");
            }
            project.setArchived(1);
            project.setArchivedTime(LocalDateTime.now());
        }

        project.setUpdatedBy(SecurityUtils.getUsername());
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
        saveLog("edit", project.getId(), null);
    }

    @Override
    public void delete(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }

        // TODO: 接入任务模块后，有任务则禁止删除

        LocalDateTime now = LocalDateTime.now();
        int rows = projectMapper.softDelete(projectId, now, SecurityUtils.getUsername(), now);
        if (rows == 0) {
            throw new ServiceException("项目不存在或已删除");
        }
        saveLog("delete", projectId, null);
    }

    @Override
    public void archived(String projectId) {
        Project project = projectMapper.selectById(projectId);
        if(project == null){
            throw new ServiceException("项目不存在");
        }
        if (project.getPublished() == null || project.getPublished() == 0) {
            throw new ServiceException("项目未发布，无法进行归档");
        }

        project.setArchived(1);
        project.setArchivedTime(LocalDateTime.now());
        project.setStatus(ProjectStatusEnum.ARCHIVED.getStatus());
        project.setUpdatedBy(SecurityUtils.getUsername());
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);

        saveLog("archive", projectId, null);
    }

    @Override
    public void unarchived(String projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        LambdaUpdateWrapper<Project> uw = new LambdaUpdateWrapper<>();
        uw.eq(Project::getId, projectId)
                .set(Project::getArchived, 0)
                .set(Project::getArchivedTime, null)
                .set(Project::getStatus, ProjectStatusEnum.DOING.getStatus())
                .set(Project::getUpdatedBy, SecurityUtils.getUsername())
                .set(Project::getUpdatedTime, LocalDateTime.now());
        projectMapper.update(null, uw);

        saveLog("cancelArchive", projectId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quit(String projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (SecurityUtils.getUserId().equals(project.getUserId())) {
            throw new ServiceException("项目负责人不能退出项目");
        }
        LambdaQueryWrapper<ProjectMember> qw = new LambdaQueryWrapper<>();
        qw.eq(ProjectMember::getPtId, projectId)
                .eq(ProjectMember::getUserId, SecurityUtils.getUserId())
                .eq(ProjectMember::getType, ProjectStatusEnum.PROJECT.getStatusName());
        projectMemberMapper.delete(qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collect(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        LambdaQueryWrapper<ProjectCollection> qw = new LambdaQueryWrapper<>();
        qw.eq(ProjectCollection::getProjectId, projectId)
                .eq(ProjectCollection::getUserId, SecurityUtils.getUserId());
        if (projectCollectionMapper.selectCount(qw) > 0) {
            return;
        }

        ProjectCollection collection = new ProjectCollection();
        collection.setProjectId(projectId);
        collection.setUserId(SecurityUtils.getUserId());
        collection.setCreatedBy(SecurityUtils.getUsername());
        collection.setCreatedTime(LocalDateTime.now());
        collection.setUpdatedBy(SecurityUtils.getUsername());
        collection.setUpdatedTime(LocalDateTime.now());
        projectCollectionMapper.insert(collection);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollect(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        LambdaQueryWrapper<ProjectCollection> qw = new LambdaQueryWrapper<>();
        qw.eq(ProjectCollection::getProjectId, projectId)
                .eq(ProjectCollection::getUserId, SecurityUtils.getUserId());
        projectCollectionMapper.delete(qw);
    }
}
