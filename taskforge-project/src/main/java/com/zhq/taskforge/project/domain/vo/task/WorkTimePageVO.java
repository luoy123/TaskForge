package com.zhq.taskforge.project.domain.vo.task;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.Data;

@Data
public class WorkTimePageVO {
    //分页结果
    private IPage<WorkTimeResVO> page;
    //总工时
    private BigDecimal totalWorkTime;
}
