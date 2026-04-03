package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelLampsEdgeComputing;
import com.scsdky.web.mapper.TunnelLampsEdgeComputingMapper;
import com.scsdky.web.service.TunnelLampsEdgeComputingService;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
//@DataSource(value = DataSourceType.SLAVE)
public class TunnelLampsEdgeComputingServiceImpl extends ServiceImpl<TunnelLampsEdgeComputingMapper, TunnelLampsEdgeComputing> implements TunnelLampsEdgeComputingService{

    @Override
    public boolean saveObject(TunnelLampsEdgeComputing tunnelLampsEdgeComputing) {
        return save(tunnelLampsEdgeComputing);
    }
}




