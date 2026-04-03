package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Dashboard 设备状态分布
 */
@Data
public class DashboardDeviceStatusVo {

    @ApiModelProperty(value = "设备总数")
    private Long total;

    @ApiModelProperty(value = "在线数量")
    private Long online;

    @ApiModelProperty(value = "离线数量（总数-在线）")
    private Long offline;

    @ApiModelProperty(value = "在线占比(%)")
    private java.math.BigDecimal onlinePercent;

    @ApiModelProperty(value = "离线占比(%)")
    private java.math.BigDecimal offlinePercent;
}
