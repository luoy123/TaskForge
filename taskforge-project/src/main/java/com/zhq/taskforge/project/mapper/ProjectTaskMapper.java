package com.zhq.taskforge.project.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.domain.vo.ProjectVO;
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

        /**
         * 查任务是否审批中（复用 pmhub_project_task_process，避免 project 依赖 workflow）。
         * approved = '0' 表示审批中。
         */
        @Select("select approved from pmhub_project_task_process where extra_id = #{extraId} and type = #{type} limit 1")
        String selectApproved(@Param("extraId") String extraId, @Param("type") String type);

        /**
         * 查询当前用户执行的任务刘表
         */
        IPage<TaskResVO> queryMyExecutedTaskList(Page<TaskResVO> page,
                        @Param("projectId") String projectId,
                        @Param("userId") Long userId);

        /**
         * 查询当前用户创建的任务列表
         */
        IPage<TaskResVO> queryMyCreatedTaskList(Page<TaskResVO> page,
                        @Param("projectId") String projectId,
                        @Param("userName") String userName);

        /**
         * 进行中的任务
         * 
         * @param userId
         * @return
         */
        List<ProjectVO> selectDoingProjectList(@Param("userId") Long userId);

        /**
         * 我参与的项目
         * 
         * @param userId
         * @return
         */
        List<ProjectVO> selectMyProjectOptions(@Param("userId") Long userId);
}