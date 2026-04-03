package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelLampsEdgeComputing;
import com.scsdky.web.domain.TunnelTriggerLampsData;
import com.scsdky.web.service.TunnelTriggerLampsDataService;
import com.scsdky.web.mapper.TunnelTriggerLampsDataMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author tubo
 */
@Service
public class TunnelTriggerLampsDataServiceImpl extends ServiceImpl<TunnelTriggerLampsDataMapper, TunnelTriggerLampsData>
    implements TunnelTriggerLampsDataService{

    @Override
    public List<Map<String, Object>> selectLampsStatus(List<TunnelLampsEdgeComputing> tunnelLampsEdgeComputings) {
        return this.getBaseMapper().selectLampsStatus(tunnelLampsEdgeComputings);
    }
}




