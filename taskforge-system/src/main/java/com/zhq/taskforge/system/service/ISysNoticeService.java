package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysNotice;

public interface ISysNoticeService {

    Page<SysNotice> selectNoticelist(Long pageNum, Long pageSize, SysNotice sysNotice);

    SysNotice getById(Long noticeId);

    void add(SysNotice sysNotice);

    void remove(Long[] noticeIds);

    void update(SysNotice sysNotice);

}
