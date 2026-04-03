package com.scsdky.web.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 设备告警汇总DTO
 * 用于统计设备健康状态变化
 * 
 * @author system
 */
@Data
public class DeviceAlarmSummaryDto {
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备类型：0=边缘计算终端，1=灯具设备
     */
    private Integer typeId;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备显示名称（包含设备ID、回路编号等信息）
     */
    private String displayName;
    
    /**
     * 健康状态：0=健康，1=延迟，3=掉线
     */
    private Integer status;
    
    /**
     * 恢复时间（如果是恢复设备）
     */
    private Date recoverTime;
    
    /**
     * 未上报时长（小时）
     */
    private Long hoursSinceLastReport;
}

