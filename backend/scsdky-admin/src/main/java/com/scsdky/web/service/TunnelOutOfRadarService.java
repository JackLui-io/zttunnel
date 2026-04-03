package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelOutOfRadar;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.DeviceDto;

import java.util.List;

/**
 *
 */
public interface TunnelOutOfRadarService extends IService<TunnelOutOfRadar> {

    List<TunnelOutOfRadar> getDeviceDwld(DeviceDto deviceDto);

    /**
     * 编辑洞外雷达
     * @param tunnelOutOfRadar
     * @return
     */
    boolean updateDeviceDwld(TunnelOutOfRadar tunnelOutOfRadar);
}
