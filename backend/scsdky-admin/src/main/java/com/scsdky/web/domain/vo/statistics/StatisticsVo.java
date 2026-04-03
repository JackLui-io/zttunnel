package com.scsdky.web.domain.vo.statistics;

import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tubo
 * 统计分析返回类
 * @date 2023/09/18
 */
@Data
public class StatisticsVo {

    @ApiModelProperty(value = "实际耗电量")
    @Excel(name = "实际耗电量")
    private Double actualPowerConsumption;

    @ApiModelProperty(value = "实际单位里程耗电量")
    @Excel(name = "实际单位里程耗电量")
    private Integer actualUnitPowerConsumption;

    @ApiModelProperty(value = "实际运行功率")
    @Excel(name = "实际运行功率")
    private BigDecimal actualOperatingPower;

    @ApiModelProperty(value = "实际亮灯时间")
    @Excel(name = "实际亮灯时间")
    private BigDecimal actualLightUpTime;

    @ApiModelProperty(value = "实际碳排放量")
    @Excel(name = "实际碳排放量")
    private BigDecimal actualCarbonEmission;

    @ApiModelProperty(value = "原设计耗电量")
    @Excel(name = "原设计耗电量")
    private Integer originalPowerConsumption;

    @ApiModelProperty(value = "原单位里程耗电量")
    @Excel(name = "原单位里程耗电量")
    private Integer originalUnitPowerConsumption;

    @ApiModelProperty(value = "原设计运行功率")
    @Excel(name = "原设计运行功率")
    private Integer originalOperatingPower;

    @ApiModelProperty(value = "原设计亮灯时间")
    @Excel(name = "原设计亮灯时间")
    private Integer originalLightUpTime;

    @ApiModelProperty(value = "原设计碳排放量")
    @Excel(name = "原设计碳排放量")
    private BigDecimal originalCarbonEmission;

    @ApiModelProperty(value = "理论节电率")
    @Excel(name = "理论节电率")
    private BigDecimal theoreticalPowerSavingRate;

    @ApiModelProperty(value = "理论总功率消减")
    @Excel(name = "理论总功率消减")
    private BigDecimal theoreticalTotalPowerReduction;

    @ApiModelProperty(value = "理论削减运行功率")
    @Excel(name = "理论削减运行功率")
    private BigDecimal theoreticalOperatingPowerReduction;

    @ApiModelProperty(value = "理论亮灯时间削减")
    @Excel(name = "理论亮灯时间削减")
    private BigDecimal theoreticalLightUpTimeReduction;

    @ApiModelProperty(value = "理论碳减排率")
    @Excel(name = "理论碳减排率")
    private BigDecimal theoreticalCarbonEmissionReduction;

}
