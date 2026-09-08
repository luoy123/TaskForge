package com.zhq.taskforge.project.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.vo.task.TaskExcelVO;
import com.zhq.taskforge.project.domain.vo.task.TaskExportVO;
import com.zhq.taskforge.project.domain.vo.task.TaskReqVO;
import com.zhq.taskforge.project.domain.vo.task.TaskResVO;

/**
 * 项目任务（Sprint F / G4）。
 */
public interface IProjectTaskService {

    String add(TaskReqVO req);

    TaskResVO detail(String taskId);

    IPage<TaskResVO> list(TaskReqVO req);

    void edit(TaskReqVO req);

    void delete(String taskId);

    String addChildTask(TaskReqVO req);

    List<TaskResVO> queryChildTask(String parentTaskId);

    void addComment(TaskReqVO req);

    List<ProjectLog> queryTaskLogList(TaskReqVO req);

    /** 按任务 id 列表导出数据（G4） */
    List<TaskExportVO> export(List<String> taskIds);

    /** 导出当前用户参与的任务（G4） */
    List<TaskExportVO> exportAll();

    /** 导入任务（G4）；返回如「成功 x 条，跳过 y 条」 */
    String importTask(List<TaskExcelVO> rows);
}
