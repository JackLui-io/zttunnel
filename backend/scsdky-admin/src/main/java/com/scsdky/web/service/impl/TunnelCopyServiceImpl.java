package com.scsdky.web.service.impl;

import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.TunnelCopyRequestDto;
import com.scsdky.web.domain.vo.TunnelCopyResultVo;
import com.scsdky.web.service.TunnelCopyService;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.support.TunnelL4ReplicationSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 复制隧道群（level=3）：新 L3 + 新 L4，并逐表复制与 level-4 关联的设备与参数数据。
 * device_id 规则与 zttunnel-admin log.md「复制规则」一致：边缘/电能用 9916 十二位占位；灯具、洞外雷达、t_tunnel_device 保留源 device_id。
 * 不复制：tunnel_syscmd（保存指令副作用，由业务操作产生）。
 */
@Service
public class TunnelCopyServiceImpl implements TunnelCopyService {

    private static final int LEVEL_GROUP = 3;
    private static final int LEVEL_LINE = 4;

    @Resource
    private TunnelNameResultService tunnelNameResultService;
    @Resource
    private TunnelL4ReplicationSupport tunnelL4ReplicationSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TunnelCopyResultVo copyTunnelGroup(TunnelCopyRequestDto dto) {
        String newName = StringUtils.trim(dto.getNewTunnelName());
        if (StringUtils.isEmpty(newName)) {
            throw new ServiceException("新隧道名称不能为空");
        }

        TunnelNameResult source = tunnelNameResultService.getById(dto.getSourceTunnelGroupId());
        if (source == null) {
            throw new ServiceException("源隧道群不存在");
        }
        if (source.getLevel() == null || source.getLevel() != LEVEL_GROUP) {
            throw new ServiceException("只能复制隧道群（level=3）");
        }

        long sameName = tunnelNameResultService.lambdaQuery()
            .eq(TunnelNameResult::getParentId, source.getParentId())
            .eq(TunnelNameResult::getLevel, LEVEL_GROUP)
            .eq(TunnelNameResult::getTunnelName, newName)
            .count();
        if (sameName > 0) {
            throw new ServiceException("同级高速下已存在同名隧道群，请修改名称");
        }

        Date now = new Date();
        TunnelNameResult newGroup = TunnelNameResult.builder()
            .tunnelName(newName)
            .parentId(source.getParentId())
            .level(LEVEL_GROUP)
            .tunnelMileage(source.getTunnelMileage())
            .status(source.getStatus() != null ? source.getStatus() : 0)
            .createTime(now)
            .updateTime(now)
            .build();
        if (!tunnelNameResultService.save(newGroup)) {
            throw new ServiceException("保存新隧道群失败");
        }

        List<TunnelNameResult> children = tunnelNameResultService.lambdaQuery()
            .eq(TunnelNameResult::getParentId, source.getId())
            .eq(TunnelNameResult::getLevel, LEVEL_LINE)
            .orderByAsc(TunnelNameResult::getId)
            .list();

        List<Long> newLevel4Ids = new ArrayList<>();
        for (TunnelNameResult child : children) {
            TunnelNameResult nc = TunnelNameResult.builder()
                .tunnelName(child.getTunnelName())
                .parentId(newGroup.getId())
                .level(LEVEL_LINE)
                .tunnelMileage(child.getTunnelMileage())
                .status(child.getStatus() != null ? child.getStatus() : 0)
                .createTime(now)
                .updateTime(now)
                .build();
            if (!tunnelNameResultService.save(nc)) {
                throw new ServiceException("复制左右线节点失败");
            }
            newLevel4Ids.add(nc.getId());
            tunnelL4ReplicationSupport.replicateFromLive(child.getId(), nc.getId(), child.getTunnelName(), now);
        }

        return TunnelCopyResultVo.builder()
            .newTunnelGroupId(newGroup.getId())
            .newLevel4TunnelIds(newLevel4Ids)
            .firstLevel4TunnelId(newLevel4Ids.isEmpty() ? null : newLevel4Ids.get(0))
            .build();
    }
}
