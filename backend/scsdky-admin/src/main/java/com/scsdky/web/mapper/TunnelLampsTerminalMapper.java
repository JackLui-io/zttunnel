package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.dto.DeviceListDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TunnelLampsTerminal
 */
public interface TunnelLampsTerminalMapper extends BaseMapper<TunnelLampsTerminal> {

    List<TunnelLampsTerminal> getDeviceLamp(@Param("tunnelDevicelistTunnelinfo") TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo, @Param("deviceDto") DeviceDto deviceDto);

    /**
     * 根据版本号查询数据
     * @param version 版本号
     * @return
     */
    List<DeviceListDto> selectListByVersion(Long version);

    /**
     * 通过灯具终端id获取隧道与边缘计算终端的关系表
     * @param uniqueId 灯具终端id
     * @return
     */
    TunnelDevicelistTunnelinfo queryTunnelDevicelistTunnelinfo(Long uniqueId);
}




