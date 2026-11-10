package com.zhq.taskforge.project.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zhq.taskforge.common.enums.ProjectStatusEnum;
import com.zhq.taskforge.common.enums.ProjectTaskStatusEnum;
import com.zhq.taskforge.project.domain.ProjectTaskNotify;
import com.zhq.taskforge.project.mapper.ProjectTaskNotifyMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 值进行提醒，展示不发送企业微信
 * TaskNotifyJob
 */
@Component ("taskNotifyTask")
@Slf4j 
public class TaskNotifyJob {

    @Autowired 
    private ProjectTaskNotifyMapper projectTaskNotifyMapper;

    @Transactional (rollbackFor = Exception.class)
    public void notifyDueSoon(){
        //1.查询通知列表
        List<ProjectTaskNotify> selectDueSoonTasks = projectTaskNotifyMapper.selectDueSoonTasks(ProjectStatusEnum.PAUSE.getStatus(), ProjectTaskStatusEnum.FINISHED.getStatus());
        if(selectDueSoonTasks == null || selectDueSoonTasks.isEmpty()){
            log.info("待逾期提醒 无");
            return ;
        }
        //2.对于每一条提醒，都插入到表中。
        for(ProjectTaskNotify projectTaskNotify : selectDueSoonTasks){
            projectTaskNotify.setOverdue(0);
            projectTaskNotifyMapper.insert(projectTaskNotify);
        }
        log.info("待逾期提醒 任务id是{}，插入表中成功，共插入{}条", selectDueSoonTasks.get(0).getTaskId(),selectDueSoonTasks.size());
    }

}
