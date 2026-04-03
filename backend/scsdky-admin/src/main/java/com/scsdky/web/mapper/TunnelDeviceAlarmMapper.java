package com.scsdky.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.TunnelDeviceAlarm;
import com.scsdky.web.domain.dto.DeviceAlarmStatisticsDto;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 设备巡检告警记录表 Mapper接口
 * 
 * @author system
 */
@Mapper
public interface TunnelDeviceAlarmMapper extends BaseMapper<TunnelDeviceAlarm> {
    
    /**
     * 查询指定日期范围内每个设备的最新告警记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每个设备的最新告警记录
     */
    List<TunnelDeviceAlarm> selectLatestAlarmsByDateRange(@Param("startTime") Date startTime, @Param("endTime") Date endTime);
    
    /**
     * 查询每个设备的最近N次健康检查记录
     * 
     * @param limitCount 每个设备查询的记录数（如12次）
     * @return 每个设备的最近N次健康检查记录
     */
    List<TunnelDeviceAlarm> selectRecentAlarmsByDevice(@Param("limitCount") int limitCount);
    
    /**
     * 使用SQL统计每个设备的健康检查指标（优化版本）
     * 计算各状态次数、最新状态、最近3次状态、连续未上报次数等
     * 
     * @param limitCount 每个设备查询的记录数（如12次）
     * @return 每个设备的统计指标
     */
    List<DeviceAlarmStatisticsDto> selectDeviceAlarmStatistics(@Param("limitCount") int limitCount);
    
    /**
     * 清空 tunnel_check_alarm 表
     */
    @Delete("TRUNCATE TABLE tunnel_check_alarm")
    void truncateTable();
}

