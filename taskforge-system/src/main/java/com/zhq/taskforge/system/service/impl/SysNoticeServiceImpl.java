package com.zhq.taskforge.system.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.domain.SysNotice;
import com.zhq.taskforge.system.mapper.SysNoticeMapper;
import com.zhq.taskforge.system.service.ISysNoticeService;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice>
        implements ISysNoticeService {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    @Override
    public Page<SysNotice> selectNoticelist(Long pageNum, Long pageSize, SysNotice sysNotice) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> qw = new LambdaQueryWrapper<SysNotice>();
        if (sysNotice != null) {
            if (StringUtils.hasText(sysNotice.getNoticeTitle())) {
                qw.like(SysNotice::getNoticeTitle, sysNotice.getNoticeTitle());
            }
            if (StringUtils.hasText(sysNotice.getNoticeType())) {
                qw.eq(SysNotice::getNoticeType, sysNotice.getNoticeType());
            }
            if (StringUtils.hasText(sysNotice.getStatus())) {
                qw.eq(SysNotice::getStatus, sysNotice.getStatus());
            }
        }
        qw.orderByDesc(SysNotice::getNoticeId);
        Page<SysNotice> selectPage = sysNoticeMapper.selectPage(page, qw);
        return selectPage;
    }

    @Override
    public SysNotice getById(Long noticeId) {
        SysNotice selectById = sysNoticeMapper.selectById(noticeId);
        return selectById;
    }

    @Override
    public void add(SysNotice sysNotice) {
        if (sysNotice != null) {
            sysNotice.setCreateBy(SecurityUtils.getUsername());
            sysNotice.setCreateTime(LocalDateTime.now());
            // 防止在knif4j测试的时候，使用的时“”，导致数据插入失败
            if (!StringUtils.hasText(sysNotice.getStatus())) {
                sysNotice.setStatus("0");
            }
        }
        sysNoticeMapper.insert(sysNotice);
        return;
    }

    @Override
    public void remove(Long[] noticeIds) {
        if (noticeIds != null && noticeIds.length > 0) {
            sysNoticeMapper.deleteBatchIds(Arrays.asList(noticeIds));
        }
        return;
    }

    @Override
    public void update(SysNotice sysNotice) {
        if (sysNotice == null || sysNotice.getNoticeId() == null) {
            return;
        }
        // 2. 只拷贝「有内容」的业务字段
        SysNotice update = new SysNotice();
        if (StringUtils.hasText(sysNotice.getNoticeTitle())) {
            update.setNoticeTitle(sysNotice.getNoticeTitle());
        }
        if (StringUtils.hasText(sysNotice.getNoticeType())) {
            update.setNoticeType(sysNotice.getNoticeType());
        }
        if (StringUtils.hasText(sysNotice.getNoticeContent())) {
            update.setNoticeContent(sysNotice.getNoticeContent());
        }
        if (StringUtils.hasText(sysNotice.getStatus())) {
            update.setStatus(sysNotice.getStatus());
        }
        if (StringUtils.hasText(sysNotice.getRemark())) {
            update.setRemark(sysNotice.getRemark());
        }

        // 3.更新审计字段，采用白名单对象更新。
        update.setNoticeId(sysNotice.getNoticeId());
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(LocalDateTime.now());
        sysNoticeMapper.updateById(update);
        return;
    }

}
