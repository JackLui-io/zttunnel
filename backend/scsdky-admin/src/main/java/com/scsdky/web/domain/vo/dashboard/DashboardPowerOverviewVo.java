package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard 用户用电/节电概览响应
 */
@Data
@ApiModel("Dashboard 用电节电概览")
public class DashboardPowerOverviewVo {

    @ApiModelProperty("年度概览")
    private AnnualOverview annualOverview;

    @ApiModelProperty("月度数据")
    private List<MonthlyItem> monthlyData;

    @Data
    @ApiModel("年度概览")
    public static class AnnualOverview {
        @ApiModelProperty("年度总耗电量（kWh）")
        private BigDecimal totalConsumption;
        @ApiModelProperty("年总理论节电量（kWh）")
        private BigDecimal totalSaving;
        @ApiModelProperty("年总理论节电率（%）")
        private BigDecimal powerSavingRate;
        @ApiModelProperty("年总理论碳减排率（%）")
        private BigDecimal carbonReductionRate;
    }

    @Data
    @ApiModel("月度数据项")
    public static class MonthlyItem {
        @ApiModelProperty("月份 1-12")
        private Integer month;
        @ApiModelProperty("当月用电量（kWh）")
        private BigDecimal consumption;
        @ApiModelProperty("当月节电量（kWh）")
        private BigDecimal saving;
        @ApiModelProperty("当月亮灯时长削减率（%），(原设计亮灯时长-实际亮灯时长)/原设计×100")
        private BigDecimal lightUpReductionRate;
    }
}
