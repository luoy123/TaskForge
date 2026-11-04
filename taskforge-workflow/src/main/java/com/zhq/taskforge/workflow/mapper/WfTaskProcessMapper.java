package com.zhq.taskforge.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.workflow.domain.WfTaskProcess;

/**
 * 业务-流程关联表。
 */
@Mapper
public interface WfTaskProcessMapper extends BaseMapper<WfTaskProcess> {

    /**
     * 查询业务是否处于审批中（approved = '0'）。
     * 供 project 模块禁改状态使用时可再抽 common 接口；MVP 也可在 project 侧自写同款 SQL。
     */
    @Select("select approved from pmhub_project_task_process where extra_id = #{extraId} and type = #{type} limit 1")
    String selectApproved(@Param("extraId") String extraId, @Param("type") String type);


    @Select("select status from pmhub_project_task where id = #{taskId} and deleted = 0 limit 1")
    Integer selectTaskIdIfExists(@Param("taskId") String taskId);

    @Select("select status from pmhub_project where id = #{projectId} and deleted = 0 limit 1")
    Integer selectProjectIdIfExists(@Param("projectId") String projectId);

    @Update("update pmhub_project set status = #{status} where id = #{taskId}")
    Integer upateProjectStatus(@Param("taskId") String taskId, @Param("status") Integer status);

    @Update("update pmhub_project_task set status = #{status} where id = #{taskId}")
    Integer updateTaskStatus(@Param("taskId") String taskId, @Param("status") Integer status);
}
