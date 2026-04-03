package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelNotice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.NoticeDto;
import com.scsdky.web.domain.vo.NoticeNumVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMessageItemVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPendingCountsVo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TTunnelNotice
 */
public interface TTunnelNoticeMapper extends BaseMapper<TunnelNotice> {

    /**
     * 通知类型隧道
     * @param noticeDto
     * @return
     */
    List<NoticeNumVo> countByTunnel(NoticeDto noticeDto);

    /**
     * Dashboard 待处理统计：4 类 process=0 数量，按用户隧道过滤
     */
    DashboardPendingCountsVo countPendingByType(Long userId);

    /**
     * Dashboard 消息通知：按 type 查询，最近7天 process=0，按用户隧道过滤，最多 limit 条
     */
    List<DashboardMessageItemVo> selectNoticeListByType(@Param("userId") Long userId, @Param("type") Integer type, @Param("limit") Integer limit);

    /**
     * Dashboard 消息通知：按 type 统计，最近7天 process=0
     */
    int countNoticeByType(@Param("userId") Long userId, @Param("type") Integer type);
}




