package com.scsdky.web.domain.dto;

import lombok.Data;

/**
 * 设备巡检结果DTO
 * 用于存储每个设备的所有巡检结果
 * 
 * @author system
 */
@Data
public class DeviceInspectionResultDto {
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备类型：0=边缘计算终端，1=灯具设备
     */
    private Integer typeId;
    
    /**
     * 设备显示名称（包含设备ID、回路编号等信息）
     */
    private String displayName;
    
    /**
     * 所有巡检结果字符串（从最新到最旧，1=按时上报，0=未按时上报）
     * 例如：1110011101（列出该设备在tunnel_check_alarm表中的所有status记录）
     */
    private String statusString;
}

