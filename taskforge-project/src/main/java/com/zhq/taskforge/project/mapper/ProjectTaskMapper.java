package com.zhq.taskforge.project.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.domain.vo.task.TaskExportVO;
import com.zhq.taskforge.project.domain.vo.task.TaskReqVO;
import com.zhq.taskforge.project.domain.vo.task.TaskResVO;

public interface ProjectTaskMapper extends BaseMapper<ProjectTask> {

    /**
     * 任务详情（F2 再写 SQL）。
     */
    TaskResVO detail(@Param("taskId") String taskId);

    /**
     * 软删（F3 使用；不依赖 @TableLogic）。
     */
    int softDelete(@Param("id") String id,
            @Param("deletedTime") LocalDateTime deletedTime,
            @Param("updatedBy") String updatedBy,
            @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 查询列表
     */
    IPage<TaskResVO> selectTaskList(Page<TaskResVO> page, @Param("data") TaskReqVO data);

    /**
     * 按父任务 id 查子任务（F4）。
     */
    List<TaskResVO> queryChildTask(@Param("taskId") String parentTaskId);

    /** 按 id 列表导出（G4） */
    List<TaskExportVO> export(@Param("taskIdList") List<String> taskIdList);

    /** 当前用户作为任务成员参与的任务（G4） */
    List<TaskExportVO> exportAll(@Param("userId") Long userId);

    /** 导入用：按登录名查 user_id，没有则 null */
    Long selectUserIdByUsername(@Param("username") String username);
}