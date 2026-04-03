package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface TunnelDevicelistTunnelinfoService extends IService<TunnelDevicelistTunnelinfo> {

    boolean saveObject(TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo);

    List<Long> listDistinctTunnelIdsByDevicelistIdRange(long minDevicelistId, long maxDevicelistId);

    List<Long> listDistinctTunnelIdsByDevicelist9916CharPrefix();
}
