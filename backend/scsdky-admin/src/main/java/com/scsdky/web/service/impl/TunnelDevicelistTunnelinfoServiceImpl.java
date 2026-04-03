package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.mapper.TunnelDevicelistTunnelinfoMapper;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author tubo
 */
@Service
//@DataSource(value = DataSourceType.SLAVE)
public class TunnelDevicelistTunnelinfoServiceImpl extends ServiceImpl<TunnelDevicelistTunnelinfoMapper, TunnelDevicelistTunnelinfo> implements TunnelDevicelistTunnelinfoService{

    @Override
    public boolean saveObject(TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo) {
        return save(tunnelDevicelistTunnelinfo);
    }

    @Override
    public List<Long> listDistinctTunnelIdsByDevicelistIdRange(long minDevicelistId, long maxDevicelistId) {
        return baseMapper.listDistinctTunnelIdByDevicelistIdRange(minDevicelistId, maxDevicelistId);
    }

    @Override
    public List<Long> listDistinctTunnelIdsByDevicelist9916CharPrefix() {
        return baseMapper.listDistinctTunnelIdByDevicelistId9916CharPrefix();
    }
}




