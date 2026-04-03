package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.utils.SecurityUtils;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.TunnelNotice;
import com.scsdky.web.domain.dto.NoticeDto;
import com.scsdky.web.domain.vo.NoticeDataVo;
import com.scsdky.web.domain.vo.NoticeNumVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMessageCategoryVo;
import com.scsdky.web.domain.vo.dashboard.DashboardMessageItemVo;
import com.scsdky.web.domain.vo.dashboard.DashboardPendingCountsVo;
import com.scsdky.web.enums.NoticeTypeEnum;
import com.scsdky.web.mapper.TTunnelNoticeMapper;
import com.scsdky.web.service.TunnelNoticeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tubo
 */
@Service
public class TunnelNoticeServiceImpl extends ServiceImpl<TTunnelNoticeMapper, TunnelNotice> implements TunnelNoticeService {

    @Override
    public List<TunnelNotice> getNoticeListByPage(NoticeDto noticeDto) {
        LambdaQueryWrapper<TunnelNotice> queryWrapper = new LambdaQueryWrapper<TunnelNotice>()
                .eq(noticeDto.getType() != null, TunnelNotice::getType, noticeDto.getType())
                .eq(noticeDto.getTunnelId() != null, TunnelNotice::getTunnelId, noticeDto.getTunnelId())
                .eq(noticeDto.getProcess() != null, TunnelNotice::getProcess, noticeDto.getProcess())
                .between(noticeDto.getStartTime() != null || noticeDto.getEndTime() != null, TunnelNotice::getCreateTime, noticeDto.getStartTime(), noticeDto.getEndTime())
                .like(StringUtils.isNotBlank(noticeDto.getContent()), TunnelNotice::getContent, noticeDto.getContent());
        queryWrapper.orderByDesc(TunnelNotice::getCreateTime);
        List<TunnelNotice> list = list(queryWrapper);

        list.forEach(tunnelNotice -> {
            tunnelNotice.setTypeName(NoticeTypeEnum.getEnumValue(tunnelNotice.getType()));
        });
        return list;
    }

    @Override
    public List<NoticeNumVo> countByTunnel(NoticeDto noticeDto) {
        Long userId = SecurityUtils.getUserId();
        noticeDto.setUserId(userId);
        return this.getBaseMapper().countByTunnel(noticeDto);
    }

    @Override
    public List<NoticeDataVo> countTypeByTunnelId(Integer tunnelId) {
        List<NoticeDataVo> noticeDataVos = new ArrayList<>();
        LambdaQueryWrapper<TunnelNotice> queryWrapper = new LambdaQueryWrapper<TunnelNotice>()
                .eq(TunnelNotice::getTunnelId,tunnelId)
                .eq(TunnelNotice::getProcess,0);
        queryWrapper.and(wrapper -> wrapper.eq(TunnelNotice::getType,2).or().eq(TunnelNotice::getType,3).or().eq(TunnelNotice::getType,4));
        List<TunnelNotice> tunnelNotices = list(queryWrapper);

        Map<Integer, List<TunnelNotice>> tunnelNoticesByType = tunnelNotices.stream().collect(Collectors.groupingBy(TunnelNotice::getType, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<TunnelNotice>> integerListEntry : tunnelNoticesByType.entrySet()) {
            NoticeDataVo noticeDataVo = new NoticeDataVo();
            noticeDataVo.setType(integerListEntry.getKey());
            noticeDataVo.setTypeName(NoticeTypeEnum.getEnumValue(integerListEntry.getKey()));
            noticeDataVo.setTunnelNoticeList(integerListEntry.getValue());
            noticeDataVo.setTypeNum(integerListEntry.getValue().size());
            noticeDataVos.add(noticeDataVo);
        }
        return noticeDataVos;
    }

    @Override
    public DashboardPendingCountsVo pendingCounts() {
        Long userId = SecurityUtils.getUserId();
        DashboardPendingCountsVo vo = this.getBaseMapper().countPendingByType(userId);
        if (vo == null) {
            vo = new DashboardPendingCountsVo();
            vo.setSystemNotice(0);
            vo.setOperationNotice(0);
            vo.setRealtimeAlarm(0);
            vo.setDeviceAlarm(0);
        }
        return vo;
    }

    @Override
    public List<DashboardMessageCategoryVo> messageNotifications(Integer limitPerCategory) {
        int limit = limitPerCategory != null && limitPerCategory > 0 ? limitPerCategory : 10;
        Long userId = SecurityUtils.getUserId();

        List<DashboardMessageCategoryVo> result = new ArrayList<>();
        String[] names = {"系统通知", "操作通知", "实时警报", "设备告警"};
        for (int i = 1; i <= 4; i++) {
            DashboardMessageCategoryVo cat = new DashboardMessageCategoryVo();
            cat.setName(names[i - 1]);
            cat.setCount(this.getBaseMapper().countNoticeByType(userId, i));
            cat.setItems(this.getBaseMapper().selectNoticeListByType(userId, i, limit));
            result.add(cat);
        }
        return result;
    }
}




