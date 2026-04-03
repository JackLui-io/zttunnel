package com.scsdky.web.service.impl;

import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.vo.PlaceholderEdgeTunnelVo;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.TunnelPlaceholderBindService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从 {@code tunnel_devicelist_tunnelinfo} 筛选 {@code devicelist_id} 十进制以 9916 开头的记录，
 * 按 {@code tunnel_id} 去重后列出四级隧道（与复制占位号习惯一致）。
 */
@Service
public class TunnelPlaceholderBindServiceImpl implements TunnelPlaceholderBindService {

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Override
    public List<Long> listTunnelIdsWith9916PrefixedDevicelist() {
        List<Long> tunnelIds = tunnelDevicelistTunnelinfoService.listDistinctTunnelIdsByDevicelist9916CharPrefix();
        if (tunnelIds == null || tunnelIds.isEmpty()) {
            return Collections.emptyList();
        }
        tunnelIds.sort(Long::compareTo);
        return tunnelIds;
    }

    @Override
    public List<PlaceholderEdgeTunnelVo> listPlaceholderEdgeBindings() {
        List<Long> tunnelIds = listTunnelIdsWith9916PrefixedDevicelist();
        if (tunnelIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, TunnelNameResult> byId = loadTunnelNameTreeForLevel4Ids(tunnelIds);

        List<PlaceholderEdgeTunnelVo> out = new ArrayList<>();
        for (Long tunnelId : tunnelIds) {
            TunnelNameResult l4 = byId.get(tunnelId);
            if (l4 == null || l4.getLevel() == null || l4.getLevel() != 4) {
                continue;
            }
            TunnelNameResult l3 = l4.getParentId() != null ? byId.get(l4.getParentId()) : null;
            TunnelNameResult l2 = (l3 != null && l3.getParentId() != null)
                    ? byId.get(l3.getParentId())
                    : null;

            String lineName = l2 != null ? l2.getTunnelName() : null;
            String tunnelGroupName = l3 != null ? l3.getTunnelName() : null;
            String direction = l4.getTunnelName();

            out.add(PlaceholderEdgeTunnelVo.builder()
                    .tunnelLevel4Id(tunnelId)
                    .lineName(lineName != null ? lineName : "")
                    .tunnelName(tunnelGroupName != null ? tunnelGroupName : "")
                    .direction(direction != null ? direction : "")
                    .createTime(l4.getCreateTime())
                    .build());
        }
        out.sort(Comparator.comparing(PlaceholderEdgeTunnelVo::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return out;
    }

    /**
     * 4、3、2 层各批量查询一次，避免对每个 tunnelId 连续 getById（N+1）。
     */
    private Map<Long, TunnelNameResult> loadTunnelNameTreeForLevel4Ids(List<Long> level4Ids) {
        if (level4Ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, TunnelNameResult> byId = new HashMap<>((level4Ids.size() * 3 + 1) * 4 / 3);
        List<TunnelNameResult> l4 = tunnelNameResultService.listByIds(level4Ids);
        for (TunnelNameResult t : l4) {
            byId.put(t.getId(), t);
        }
        Set<Long> p3 = l4.stream().map(TunnelNameResult::getParentId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (p3.isEmpty()) {
            return byId;
        }
        List<TunnelNameResult> l3 = tunnelNameResultService.listByIds(p3);
        for (TunnelNameResult t : l3) {
            byId.put(t.getId(), t);
        }
        Set<Long> p2 = l3.stream().map(TunnelNameResult::getParentId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (p2.isEmpty()) {
            return byId;
        }
        List<TunnelNameResult> l2 = tunnelNameResultService.listByIds(p2);
        for (TunnelNameResult t : l2) {
            byId.put(t.getId(), t);
        }
        return byId;
    }
}
