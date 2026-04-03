package com.scsdky.web.domain.vo;

import lombok.Data;

/**
 * @author tubo
 * @date 2023/12/27
 */
@Data
public class DeviceTypeAndStatusVo {

    private String deviceType;

    private String deviceStatus;

    private Long deviceOnline;

    private Long deviceOffline;

    private Long deviceBreakdown;

    private Long num;
}
