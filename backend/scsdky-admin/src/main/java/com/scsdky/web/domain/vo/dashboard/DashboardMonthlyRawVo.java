package com.scsdky.web.domain.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Mapper 返回的月度原始数据（用于聚合）
 */
@Data
public class DashboardMonthlyRawVo {
    private Integer month;
    private BigDecimal totalLight;
    private BigDecimal totalEconomyLight;
    private BigDecimal origCarbon;
    private BigDecimal actualCarbon;
}
