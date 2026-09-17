package com.zhq.taskforge.project.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.enums.LogTypeEnum;
import com.zhq.taskforge.common.enums.ProjectStatusEnum;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.ProjectMember;
import com.zhq.taskforge.project.domain.vo.ProjectVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberReqVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberResVO;
import com.zhq.taskforge.project.mapper.ProjectLogMapper;
import com.zhq.taskforge.project.mapper.ProjectMapper;
import com.zhq.taskforge.project.mapper.ProjectMemberMapper;
import com.zhq.taskforge.project.service.IProjectMemberService;

@Service
public class ProjectMemberServiceImpl implements IProjectMemberService {

    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ProjectLogMapper projectLogMapper;

    @Override
    public IPage<ProjectMemberResVO> list(ProjectMemberReqVO req) {
        String projectId = req.getProjectId();
        if (projectId == null || projectId.isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        long pageNum = req.getPageNum() == null ? 1L : req.getPageNum();
        long pageSize = req.getPageSize() == null ? 10L : req.getPageSize();
        return projectMemberMapper.selectMemberPage(new Page<>(pageNum, pageSize), req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ProjectVO vo) {
        // 1.校验 projectId,userIdList
        if (vo.getProjectId() == null || vo.getProjectId().isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        List<Long> userIdList = vo.getUserIdList();
        if (userIdList == null || userIdList.isEmpty()) {
            throw new ServiceException("请选择要邀请的队友");
        }
        // 2.项目是否存在校验
        Project project = projectMapper.selectById(vo.getProjectId());
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        // 3.对于每个userId,
        // 已经是成员则跳过
        // 否则insert ProjectMember 以及 saveLog
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : userIdList) {
            if (userId == null) {
                continue;
            }
            LambdaQueryWrapper<ProjectMember> qw = new LambdaQueryWrapper<>();
            qw.eq(ProjectMember::getPtId, vo.getProjectId())
                    .eq(ProjectMember::getType, ProjectStatusEnum.PROJECT.getStatusName())
                    .eq(ProjectMember::getUserId, userId);
            if (projectMemberMapper.selectCount(qw) > 0) {
                continue; // 已在本项目中，跳过
            }
            ProjectMember m = new ProjectMember();
            m.setPtId(vo.getProjectId());
            m.setUserId(userId);
            m.setType(ProjectStatusEnum.PROJECT.getStatusName()); // "project"
            m.setCreator(0);
            m.setJoinedTime(now);
            m.setCreatedBy(SecurityUtils.getUsername());
            m.setCreatedTime(now);
            m.setUpdatedBy(SecurityUtils.getUsername());
            m.setUpdatedTime(now);
            projectMemberMapper.insert(m);

            saveLog("inviteMember", vo.getProjectId(), userId);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(ProjectVO vo) {
        if (vo.getProjectId() == null || vo.getProjectId().isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        List<Long> userIds = vo.getUserIdList();
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("请选择要移除的用户");
        }
        Project project = projectMapper.selectById(vo.getProjectId());
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (userIds.contains(project.getUserId())) {
            throw new ServiceException("不能移除项目负责人");
        }
        // 先查出真正在项目中的人，避免「删 0 行仍写 removeMember 日志」
        LambdaQueryWrapper<ProjectMember> qw = new LambdaQueryWrapper<>();
        qw.eq(ProjectMember::getPtId, vo.getProjectId())
                .eq(ProjectMember::getType, ProjectStatusEnum.PROJECT.getStatusName())
                .in(ProjectMember::getUserId, userIds);
        List<ProjectMember> exists = projectMemberMapper.selectList(qw);
        if (exists == null || exists.isEmpty()) {
            return; // 没有可移除的，不写日志
        }
        projectMemberMapper.delete(qw);
        for (ProjectMember m : exists) {
            saveLog("removeMember", vo.getProjectId(), m.getUserId());
        }

    }

    private void saveLog(String operateType, String projectId, Long toUserId) {
        LocalDateTime now = LocalDateTime.now();
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
        log.setCreatedTime(now);
        log.setUpdatedBy(SecurityUtils.getUsername());
        log.setUpdatedTime(now);
        projectLogMapper.insert(log);
    }

}
