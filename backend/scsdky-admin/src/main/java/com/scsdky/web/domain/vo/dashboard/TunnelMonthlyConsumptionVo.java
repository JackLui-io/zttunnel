package com.scsdky.web.domain.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 隧道月度耗电量（用于能耗排行）
 */
@Data
public class TunnelMonthlyConsumptionVo {
    private Long tunnelId;
    private String tunnelName;
    private Integer month;
    private BigDecimal consumptionKwh;
}
