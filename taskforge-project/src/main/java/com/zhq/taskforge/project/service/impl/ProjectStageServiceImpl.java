package com.zhq.taskforge.project.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.ProjectStage;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.domain.vo.ProjectStageVO;
import com.zhq.taskforge.project.mapper.ProjectMapper;
import com.zhq.taskforge.project.mapper.ProjectStageMapper;
import com.zhq.taskforge.project.mapper.ProjectTaskMapper;
import com.zhq.taskforge.project.service.IProjectStageService;

@Service("projectStageService")
public class ProjectStageServiceImpl implements IProjectStageService{

    @Autowired 
    private ProjectStageMapper projectStageMapper;

    @Autowired 
    private ProjectMapper projectMapper;

    @Autowired 
    private ProjectTaskMapper projectTaskMapper;

    @Override
    public List<ProjectStageVO> list(String projectId) {
        //1.校验id是否存在
       if(projectId== null || projectId.isBlank()){
        throw new ServiceException("项目ID不能为空");
    }
    //2.等值查询
    LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<ProjectStage>().eq(ProjectStage::getProjectId,projectId)
    .orderByAsc(ProjectStage::getStageCode);
    List<ProjectStage> projectStagelist = projectStageMapper.selectList(qw);
    //3.列表为空，则返回空列表
    if(projectStagelist.isEmpty()){
        return Collections.emptyList();
    }
    //4. 转传成Vo对象
    List<ProjectStageVO> projectStageVOList = new ArrayList<>();
    for(ProjectStage ps : projectStagelist){
        ProjectStageVO projectStageVO = new ProjectStageVO();
        projectStageVO.setStageId(ps.getId());
        projectStageVO.setStageName(ps.getStageName());
        projectStageVO.setStageCode(ps.getStageCode());
        projectStageVO.setProjectId(ps.getProjectId());
        projectStageVOList.add(projectStageVO);
    }
    return projectStageVOList;
}

    @Override
    @Transactional (rollbackFor = Exception.class)
    public void add(ProjectStageVO projectStageVO) {
        //1.projectStageVO是否为空
        if(projectStageVO == null){
            throw new ServiceException("项目阶段VO不能为空");
        }
        //2.projectId,StageId是否为空
        if(projectStageVO.getProjectId() == null || projectStageVO.getProjectId().isBlank()){
            throw new ServiceException("项目ID不能为空");
        }
        //3.阶段名称是否为空
        if(projectStageVO.getStageName() == null || projectStageVO.getStageName().isBlank()){
            throw new ServiceException("阶段名称不能为空");
        }
        //4.项目是否存在
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
        .eq(Project::getId,projectStageVO.getProjectId());
        Project selectOne = projectMapper.selectOne(wrapper);
        if(selectOne == null){
            throw new ServiceException("项目不存在");
        }

        //5.阶段名称查重:同一个项目下存在相同的名称则抛出异常
        LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<ProjectStage>()
        .eq(ProjectStage::getProjectId,projectStageVO.getProjectId())
        .eq(ProjectStage::getStageName,projectStageVO.getStageName());

        Long selectCount = projectStageMapper.selectCount(qw);
        if(selectCount > 0){
            throw new ServiceException("阶段名称已存在");
        }
        //6.计算stagecode
        LambdaQueryWrapper<ProjectStage> qw1 = new LambdaQueryWrapper<ProjectStage>()
        .eq(ProjectStage::getProjectId,projectStageVO.getProjectId())
        .orderByDesc(ProjectStage::getStageCode)
        .last("limit 1");
        ProjectStage selectProject = projectStageMapper.selectOne(qw1);
        int nextcode = 0;
        if(selectProject == null){
            nextcode = 0;
        }else{
            nextcode = selectProject.getStageCode() + 1;
        }
        //7.封装ProjectStage对象
        LocalDateTime now = LocalDateTime.now();
        ProjectStage projectStage = new ProjectStage();
        projectStage.setStageName(projectStageVO.getStageName());
        projectStage.setStageCode(nextcode);
        projectStage.setProjectId(projectStageVO.getProjectId());
        projectStage.setCreatedBy(SecurityUtils.getUsername());
        projectStage.setCreatedTime(now);
        projectStage.setUpdatedBy(SecurityUtils.getUsername());
        projectStage.setUpdatedTime(now);
        //8.插入数据
        projectStageMapper.insert(projectStage);
        
    }

    @Override
    @Transactional (rollbackFor = Exception.class)
    public void edit(ProjectStageVO projectStageVO) {
        //1.stageId不能为空，projectId我们从后端数据库中进行获取。
        if(projectStageVO == null){
            throw new ServiceException("项目阶段VO不能为空");
        }
        if(projectStageVO.getStageId() == null || projectStageVO.getStageId().isBlank()){
            throw new ServiceException("阶段ID不能为空");
        }
        //2.阶段名称是否为空
        if(projectStageVO.getStageName() == null || projectStageVO.getStageName().isBlank()){
            throw new ServiceException("阶段名称不能为空");
     }
        //3.阶段是否存在
        LambdaQueryWrapper<ProjectStage> wrapper = new LambdaQueryWrapper<ProjectStage>()
        .eq(ProjectStage::getId,projectStageVO.getStageId());
        ProjectStage selectOne = projectStageMapper.selectOne(wrapper);
        if(selectOne == null){
            throw new ServiceException("阶段不存在");
        }
        //4. 阶段重名查询:同一目录下，同名，且id不等于自己
     LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<ProjectStage>()
        .eq(ProjectStage::getProjectId,selectOne.getProjectId())
        .ne(ProjectStage::getId,projectStageVO.getStageId())
        .eq(ProjectStage::getStageName,projectStageVO.getStageName());

        Long selectCount = projectStageMapper.selectCount(qw);
        if(selectCount > 0){
            throw new ServiceException("阶段名称已存在");
        }
        //5.更新数据
        ProjectStage projectStage = new ProjectStage();
        projectStage.setStageName(projectStageVO.getStageName());
        projectStage.setId(projectStageVO.getStageId());
        projectStage.setUpdatedBy(SecurityUtils.getUsername());
        projectStage.setUpdatedTime(LocalDateTime.now());
        projectStageMapper.updateById(projectStage);
}

    @Override
    @Transactional (rollbackFor = Exception.class)
    public void delete(String stageId) {
        //1.stageId是否为空
        if(stageId == null || stageId.isBlank()){
            throw new ServiceException("阶段ID不能为空");
        }
        //2.阶段是否存在
        LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<ProjectStage>()
        .eq(ProjectStage::getId,stageId);
        ProjectStage selectOne = projectStageMapper.selectOne(qw);
        if(selectOne == null){
            throw new ServiceException("阶段不存在");
        }
        //3.不能删除项目当前阶段：
        //eg:一个需求处于开发状态，我们不能直接丢弃改状态，应该改成其他的状态，待处理，然后再讲开发状态进行删除
        //3.2根据项目id查询改项目的当前阶段id 是否等于 stageId
        LambdaQueryWrapper<Project> qw2 = new LambdaQueryWrapper<Project>()
        .eq(Project::getId,selectOne.getProjectId())
        .eq(Project::getProjectStageId,stageId);
        Long count = projectMapper.selectCount(qw2);
        //3.3 如果count大于0，则不能删除
        if(count > 0){
            throw new ServiceException("不能删除项目当前阶段");
        }
        //4.阶段下还有任务不能进行删除
        LambdaQueryWrapper<ProjectTask> qw3 = new LambdaQueryWrapper<ProjectTask>()
        .eq(ProjectTask::getProjectStageId,stageId)
        .eq(ProjectTask::getDeleted,0);
        Long count1 = projectTaskMapper.selectCount(qw3);
        if(count1 > 0){
            throw new ServiceException("阶段下还有任务不能进行删除");
        }
        //5.删除阶段
        projectStageMapper.deleteById(stageId);
    }

    @Override
    @Transactional (rollbackFor = Exception.class)
    public void setCurrent(String stageId, String projectId) {
        //1.参数不能为空
        if(stageId == null || stageId.isBlank()){
            throw new ServiceException("阶段ID不能为空");
        }
        if(projectId == null || projectId.isBlank()){
            throw new ServiceException("项目ID不能为空");
        }
        //2.阶段是否存在
        LambdaQueryWrapper<ProjectStage> qw = new LambdaQueryWrapper<ProjectStage>()
        .eq(ProjectStage::getId,stageId);
        ProjectStage selectOne = projectStageMapper.selectOne(qw);
        if(selectOne == null){
            throw new ServiceException("阶段不存在");
        }
        //3.检验阶段是否属于这个项目，stage.getProjectId = projectId
        if(!selectOne.getProjectId().equals(projectId)){
            throw new ServiceException("阶段不属于当前项目");
        }
        //3.项目是否存在
        LambdaQueryWrapper<Project> qw1 = new LambdaQueryWrapper<Project>()
        .eq(Project::getId,projectId);
        Project selectOne1 = projectMapper.selectOne(qw1);
        if(selectOne1 == null){
            throw new ServiceException("项目不存在");
        }
        //已经是当前阶段则直接返回。
        if(stageId.equals(selectOne1.getProjectStageId())){
            return;
        }
        //4.更新项目当前阶段
        Project project = new Project();
        project.setId(projectId);
        project.setStageCode(selectOne.getStageCode());
        project.setProjectStageId(selectOne.getId());
        project.setUpdatedBy(SecurityUtils.getUsername());
        project.setUpdatedTime(LocalDateTime.now());
        projectMapper.updateById(project);
    }

}
