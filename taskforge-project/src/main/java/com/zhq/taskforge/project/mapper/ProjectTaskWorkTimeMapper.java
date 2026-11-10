package com.zhq.taskforge.project.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.project.domain.ProjectTaskWorkTime;
import com.zhq.taskforge.project.domain.vo.task.WorkTimeResVO;

@Mapper 
public interface ProjectTaskWorkTimeMapper extends BaseMapper<ProjectTaskWorkTime>{
    /**
     * 分页列表
     */
    IPage<WorkTimeResVO> selectWorkTimeList(Page<WorkTimeResVO> page, @Param("taskId") String taskId);

    @Select("select COALESCE(sum(work_time), 0) from pmhub_project_task_work_time where project_task_id = #{taskId}")
    BigDecimal selectWorkTimeSum(@Param ("taskId") String taskId);
}
 