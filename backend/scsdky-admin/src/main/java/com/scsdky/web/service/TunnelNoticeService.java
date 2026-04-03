package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.NoticeDto;
import com.scsdky.web.domain.vo.NoticeDataVo;
import com.scsdky.web.domain.vo.NoticeNumVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMessageCategoryVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPendingCountsVo;

import java.util.List;

/**
 * @author tubo
 */
public interface TunnelNoticeService extends IService<TunnelNotice> {

    /**
     * 获取通知公告列表
     * @param noticeDto
     * @return
     */
    List<TunnelNotice> getNoticeListByPage(NoticeDto noticeDto);

    /**
     * 通知公告 按类型区分
     * @param noticeDto
     * @return
     */
    List<NoticeNumVo> countByTunnel(NoticeDto noticeDto);

    /**
     * 隧道id获取通知类型个数及通知类型
     * @param tunnelId
     * @return
     */
    List<NoticeDataVo> countTypeByTunnelId(Integer tunnelId);

    /**
     * Dashboard 待处理统计（RightSidebar 待处理卡片）
     */
    DashboardPendingCountsVo pendingCounts();

    /**
     * Dashboard 消息通知列表（RightSidebar 消息通知卡片）
     * @param limitPerCategory 每类最多条数，默认 10
     */
    List<DashboardMessageCategoryVo> messageNotifications(Integer limitPerCategory);
}
