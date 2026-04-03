package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelDevicelist;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.TunnelDevicelistVo;

import java.util.List;

/**
 *
 */
public interface TunnelDevicelistService extends IService<TunnelDevicelist> {

    boolean saveObject(TunnelDevicelist tunnelDevicelist);

    TunnelDevicelist getOneObject(String deviceId);

    boolean updateObject(TunnelDevice tunnelDevice);

    List<TunnelDevicelistVo> getDevicelist(DeviceDto deviceDto);

    /**
     * 多隧道聚合边缘控制器(1)/电能终端(2)，按 deviceId 去重。
     */
    List<TunnelDevicelistVo> listDevicelistByTunnelIds(List<Long> tunnelIds, Integer type);

    /**
     * 占位设备号改为真实设备号：先批量更新各表 devicelist_id，再更新 tunnel_devicelist 主键。
     */
    boolean rebindDeviceId(Long oldDeviceId, Long newDeviceId);
}
