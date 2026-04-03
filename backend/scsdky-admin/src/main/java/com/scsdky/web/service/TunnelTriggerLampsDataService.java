package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelLampsEdgeComputing;
import com.scsdky.web.domain.TunnelTriggerLampsData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author tubo
 */
public interface TunnelTriggerLampsDataService extends IService<TunnelTriggerLampsData> {

    /**
     * 根据灯具ID查询最近的状态
     * @param tunnelLampsEdgeComputings
     * @return
     */
    List<Map<String, Object>> selectLampsStatus(List<TunnelLampsEdgeComputing> tunnelLampsEdgeComputings);
}
