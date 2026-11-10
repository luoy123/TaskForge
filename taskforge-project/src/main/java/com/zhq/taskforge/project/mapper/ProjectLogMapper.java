package com.zhq.taskforge.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.vo.task.TaskLogVO;

public interface ProjectLogMapper extends BaseMapper<ProjectLog> {

    List<TaskLogVO> selectTaskLogList(@Param("taskId") String taskId, @Param("logType") Integer logType);
}
