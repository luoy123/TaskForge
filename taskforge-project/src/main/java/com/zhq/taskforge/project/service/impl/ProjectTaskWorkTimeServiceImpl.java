package com.zhq.taskforge.project.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.domain.ProjectTaskWorkTime;
import com.zhq.taskforge.project.domain.vo.task.WorkTimePageVO;
import com.zhq.taskforge.project.domain.vo.task.WorkTimeReqVO;
import com.zhq.taskforge.project.domain.vo.task.WorkTimeResVO;
import com.zhq.taskforge.project.mapper.ProjectTaskMapper;
import com.zhq.taskforge.project.mapper.ProjectTaskWorkTimeMapper;
import com.zhq.taskforge.project.service.IProjectTaskWorkTimeService;

@Service 
public class ProjectTaskWorkTimeServiceImpl implements IProjectTaskWorkTimeService{
    private static final BigDecimal MAX_WORK_TIME = new BigDecimal("24");
    private static final BigDecimal MIN_WORK_TIME = new BigDecimal("0");

    @Autowired 
    private ProjectTaskMapper projectTaskMapper;

    @Autowired 
    private ProjectTaskWorkTimeMapper projectTaskWorkTimeMapper;

    @Override
    @Transactional (rollbackFor = Exception.class)
    public void add(WorkTimeReqVO req) {
        //1.taskId是否为空
        if(req.getTaskId() == null || req.getTaskId().isBlank()){
            throw new ServiceException("任务id不能为空");
        }
        //2.workTime是否为空
        if(req.getWorkTime() == null){
            throw new ServiceException("工作时间不能为空");
        }
        //3.范围检查，workTime的范围是0~24小时，通过compareTo来判断
         BigDecimal workTime = req.getWorkTime();
         if(workTime.compareTo(MIN_WORK_TIME) <= 0 || workTime.compareTo(MAX_WORK_TIME) > 0){
            throw new ServiceException("工作时间范围错误，请输入0~24小时之间的值");
         }
        //4.最多一位小数
        if(workTime.stripTrailingZeros().scale() > 1){
            throw new ServiceException("工作时间最多一位小数");
        }
        //5.查任务
        ProjectTask task = getTask(req.getTaskId());
        //6.组装实体
        LocalDateTime now = LocalDateTime.now();
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        ProjectTaskWorkTime projectTaskWorkTime = new ProjectTaskWorkTime();
        projectTaskWorkTime.setProjectTaskId(task.getId());
        projectTaskWorkTime.setWorkTime(workTime);
        projectTaskWorkTime.setProjectId(task.getProjectId());
        projectTaskWorkTime.setUserId(userId);
        projectTaskWorkTime.setCreatedTime(now);
        projectTaskWorkTime.setUpdatedTime(now);
        projectTaskWorkTime.setCreatedBy(username);
        projectTaskWorkTime.setUpdatedBy(username);
        //7.插入数据
        projectTaskWorkTimeMapper.insert(projectTaskWorkTime);

    }
      

    @Override
    public void delete(String workTimeId) {
        //1.workTimeId是否为空
        if(workTimeId == null || workTimeId.isBlank()){
            throw new ServiceException("工作时间Id不能为空");
        }
        //2.工时记录是否存在
        ProjectTaskWorkTime selectById = projectTaskWorkTimeMapper.selectById(workTimeId);
        if(selectById == null){
            throw new ServiceException("工时记录不存在");
        }
        //3.只能删除自己登录的，管理员除外
        Long userId = SecurityUtils.getUserId();
        if(! userId.equals(selectById.getUserId()) && !SecurityUtils.isAdmin(userId)){
            throw new ServiceException("无权删除该工时");
        }
        //4.删除数据，物理删除
        projectTaskWorkTimeMapper.deleteById(workTimeId);
    }


    @Override
    public WorkTimePageVO list(WorkTimeReqVO req) {
       //1.任务id是否存在
       if(req.getTaskId() == null || req.getTaskId().isBlank()){
        throw new ServiceException("任务id不能为空");
       }
       //2.查询任务
       ProjectTask task = getTask(req.getTaskId());
       //3.设置分页参数
       Long  pageNum = req.getPageNum() == null ?1L : req.getPageNum();
       Long pageSize = req.getPageSize() == null ?10L : req.getPageSize();
       Page<WorkTimeResVO> page = new Page<WorkTimeResVO>(pageNum, pageSize);
       //4.查询列表
       IPage<WorkTimeResVO> list = projectTaskWorkTimeMapper.selectWorkTimeList(page,req.getTaskId());
       //5.查询任务总时长
       BigDecimal totalWorkTime = projectTaskWorkTimeMapper.selectWorkTimeSum(req.getTaskId());
       //6.组装返回结果
      WorkTimePageVO workTimePageVO = new WorkTimePageVO();
      workTimePageVO.setPage(list);
      workTimePageVO.setTotalWorkTime(totalWorkTime);
      return workTimePageVO;
    }


    private ProjectTask getTask(String taskId){
         ProjectTask task = projectTaskMapper.selectById(taskId);
        if(task == null || Integer.valueOf(1).equals(task.getDeleted())){
            throw new ServiceException("任务不存在");
        }
        return task;
    }

}
