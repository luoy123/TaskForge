package com.zhq.taskforge.project.service.project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;

/**
 * 根据req.type 来选择对应的executor
 * spring把所有的QeuryAbstractExecurot 子类按照@Service("beanName") 注入到Map中。
 * QueryProjectFactory
 */
@Service
public class QueryProjectFactory {

    private static final Map<String, String> BENA_NAMES = new ConcurrentHashMap<>();

    static {
        for (QueryProjectEnum e : QueryProjectEnum.values()) {
            BENA_NAMES.put(e.getType(), e.getBeanName());
        }
    }

    @Autowired
    private Map<String, QueryAbstractExecutor> executorMap;

    public IPage<ProjectResVO> execute(Page<ProjectResVO> page, ProjectReqVO req) {
        String type = req.getType();
        type = (type == null || type.isBlank()) ? "my" : type;
        String beanName = BENA_NAMES.get(type);
        if (!StringUtils.hasText(beanName)) {
            throw new ServiceException("不支持的项目列表类型： " + type);
        }
        QueryAbstractExecutor executor = executorMap.get(beanName);
        if (executor == null) {
            throw new ServiceException("未找到执行器： " + beanName);
        }
        return executor.query(page, req);
    }

}
