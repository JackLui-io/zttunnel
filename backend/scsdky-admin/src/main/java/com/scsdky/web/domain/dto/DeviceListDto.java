package com.scsdky.web.domain.dto;

import com.scsdky.common.annotation.Excel;
import lombok.Data;

@Data
public class DeviceListDto {
    /**
     * 隧道id
     */
    private Long tunnelId;

    /**
     * 隧道名称
     */
    private String tunnelName;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 设备类型（字典值）
     */
    private String deviceType;
}
