package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelDevice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.DeviceDto;
import com.scsdky.web.domain.vo.DeviceTypeAndStatusVo;
import com.scsdky.web.domain.vo.DeviceTypeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TTunnelDevice
 */
public interface TunnelDeviceMapper extends BaseMapper<TunnelDevice> {

    /**
     * 获取设备列表
     * @param deviceDto
     * @return
     */
    List<TunnelDevice> getDeviceListByPage(DeviceDto deviceDto);

    /**
     * 统计各个状态下的设备数量
     * @param tunnelId
     * @return
     */
    DeviceTypeVo countByTunnelId(Integer tunnelId);

    /**
     * 设备数量
     * @param analyzeDto
     */
    List<DeviceTypeAndStatusVo> getDeviceGroupByTypeAndStatus(AnalyzeDto analyzeDto);

    /**
     * Dashboard 设备状态分布：按隧道ID列表汇总在线/离线/故障数量
     * @param tunnelIds 隧道ID列表（level-4），为空时返回全0
     */
    DeviceTypeVo countByTunnelIds(@Param("tunnelIds") List<Long> tunnelIds);
}




