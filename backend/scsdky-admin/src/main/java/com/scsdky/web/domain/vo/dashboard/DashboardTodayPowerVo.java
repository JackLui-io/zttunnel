package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Dashboard 今日数据汇总（中央信息框：今日用电量、今日节电量、今日碳减排）
 */
@Data
@ApiModel("Dashboard 今日数据汇总")
public class DashboardTodayPowerVo {

    @ApiModelProperty("今日用电量（kWh）")
    private BigDecimal todayConsumptionKwh;

    @ApiModelProperty("今日节电量（kWh）")
    private BigDecimal todaySavingKwh;

    @ApiModelProperty("今日碳减排（kg）")
    private BigDecimal todayCarbonReductionKg;
}
