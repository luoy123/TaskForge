package com.zhq.taskforge.project.service;

import com.zhq.taskforge.project.domain.vo.task.WorkTimePageVO;
import com.zhq.taskforge.project.domain.vo.task.WorkTimeReqVO;

public interface IProjectTaskWorkTimeService {

    void add(WorkTimeReqVO workTimeReqVO);

    void delete(String workTimeId);

    /** 分页列表展示 */
    WorkTimePageVO list(WorkTimeReqVO workTimeReqVO);
    
}
