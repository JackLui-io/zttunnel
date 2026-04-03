package com.scsdky.web.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 设备告警统计DTO（SQL查询结果）
 * 用于接收SQL统计计算的各项指标
 * 
 * @author system
 */
@Data
public class DeviceAlarmStatisticsDto {
    
    /**
     * 设备类型：0=边缘计算终端，1=灯具设备
     */
    private Integer typeId;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 按时上报(0)的次数
     */
    private Integer count0;
    
    /**
     * 延迟上报(1)的次数
     */
    private Integer count1;
    
    /**
     * 未上报(2)的次数
     */
    private Integer count2;
    
    /**
     * 最新状态（第一条记录的状态）
     */
    private Integer latestStatus;
    
    /**
     * 最近3次中status=0的次数
     */
    private Integer recent3Count0;
    
    /**
     * 最近3次中status=2的次数
     */
    private Integer recent3Count2;
    
    /**
     * 连续未上报次数（从最新开始连续计算status=2的次数）
     */
    private Integer consecutiveMissed;
    
    /**
     * 最新记录的更新时间
     */
    private Date latestUpdateTime;
    
    /**
     * 记录总数（应该是12）
     */
    private Integer totalCount;
}

