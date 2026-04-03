package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Dashboard 隧道概况
 */
@Data
public class DashboardTunnelOverviewVo {

    @ApiModelProperty(value = "高速公路数量(条)")
    private Integer highwayCount;

    @ApiModelProperty(value = "隧道总数(条)")
    private Integer tunnelCount;

    @ApiModelProperty(value = "总里程(km)")
    private java.math.BigDecimal totalMileage;
}
