package com.scsdky.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备巡检DTO
 * 用于存储设备ID和对应的期望上报间隔
 * 
 * @author system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInspectionDto {
    
    /**
     * 设备ID（unique_id）
     */
    private Long deviceId;
    
    /**
     * 期望上报间隔（秒）
     */
    private Integer expectedInterval;
    
    /**
     * 设备类型：0=边缘计算终端，1=灯具终端
     */
    private Integer type;
}

