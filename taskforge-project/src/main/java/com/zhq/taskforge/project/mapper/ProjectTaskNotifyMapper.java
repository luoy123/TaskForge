package com.zhq.taskforge.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.project.domain.ProjectTaskNotify;
@Mapper 
public interface ProjectTaskNotifyMapper extends BaseMapper<ProjectTaskNotify>{

    public List<ProjectTaskNotify> selectDueSoonTasks(@Param("pauseStatus") Integer pauseStatus, @Param("finishedStatus") Integer finishedStatus);

}
