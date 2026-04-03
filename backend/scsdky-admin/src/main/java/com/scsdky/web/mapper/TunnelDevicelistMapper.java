package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelDevicelist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.dto.DeviceListDto;
import com.scsdky.web.domain.vo.DeviceTypeVo;
import com.scsdky.web.enums.FileDeviceTypeEnum;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Entity generator.domain.Tunnel_devicelist
 */
public interface TunnelDevicelistMapper extends BaseMapper<TunnelDevicelist> {

    /**
     * 根据版本号及设备类型查询数据
     *
     * @param version 版本号
     * @param deviceTypeId 设备类型id
     * @return
     */
    List<DeviceListDto> selectListByVersion(@Param("version") Long version, @Param("deviceTypeId") String deviceTypeId);

    /**
     * Dashboard 设备状态分布：按用户隧道（t_user_tunnel + tunnel_devicelist_tunnelinfo type=2）统计
     */
    DeviceTypeVo countDeviceStatusByUserId(@Param("userId") Long userId);

    /**
     * Dashboard 设备状态分布：admin 全量
     */
    DeviceTypeVo countDeviceStatusAll();

    /**
     * 修改主键设备号（须在关联表 devicelist_id 已同步为 newId 之后调用，或库中无外键约束）
     */
    @Update("UPDATE tunnel_devicelist SET device_id = #{newId} WHERE device_id = #{oldId}")
    int updateDevicePrimaryKey(@Param("oldId") Long oldId, @Param("newId") Long newId);
}




