package com.scsdky.web.service;

import com.scsdky.web.domain.vo.PlaceholderEdgeTunnelVo;

import java.util.List;

/**
 * 「绑定列表」：由 {@code tunnel_devicelist_tunnelinfo} 中 {@code devicelist_id} 十进制以 9916 开头 的记录关联的 {@code tunnel_id} 汇总展示。
 */
public interface TunnelPlaceholderBindService {

    List<PlaceholderEdgeTunnelVo> listPlaceholderEdgeBindings();

    /**
     * {@code tunnel_devicelist_tunnelinfo} 中 {@code devicelist_id} 十进制表示以 9916 开头的全部 distinct {@code tunnel_id}。
     */
    List<Long> listTunnelIdsWith9916PrefixedDevicelist();
}
