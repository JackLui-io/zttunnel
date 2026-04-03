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
public class StatisticsExcelVo {

    /**
     * 开始事件
     */
    private String startDate;
    /**
     * 结束事件
     */
    private String endDate;

    /**
     * Excel标题
     */
    private String title;

    @ApiModelProperty(value = "实际耗电量")
    private Double actualPowerConsumption;
    @ApiModelProperty(value = "理论耗电量")
    private Integer originalPowerConsumption;
    @ApiModelProperty(value = "理论节电量")
    private BigDecimal theoreticalPowerSaving;
    @ApiModelProperty(value = "理论节电率")
    private BigDecimal theoreticalPowerSavingRate;

    @ApiModelProperty(value = "实际运行功率")
    private BigDecimal actualOperatingPower;
    @ApiModelProperty(value = "设计功率")
    private String originalOperatingPower;
    @ApiModelProperty(value = "功率削减量")
    private BigDecimal theoreticalOperatingPowerReduction;
    @ApiModelProperty(value = "理论总功率消减率")
    private BigDecimal theoreticalTotalPowerReduction;

    @ApiModelProperty(value = "实际时长")
    private BigDecimal actualLightUpTime;
    @ApiModelProperty(value = "理论时长")
    private Integer originalLightUpTime;
    @ApiModelProperty(value = "时长削减量")
    private BigDecimal theoreticalLightUpTimeReduction;
    @ApiModelProperty(value = "时长削减率")
    private BigDecimal theoreticalLightUpTimeReductionRate;

    @ApiModelProperty(value = "实际碳排放")
    private BigDecimal actualCarbonEmission;
    @ApiModelProperty(value = "理论碳排放")
    private BigDecimal originalCarbonEmission;
    @ApiModelProperty(value = "碳减排量")
    private BigDecimal theoreticalCarbonEmissionReduction;
    @ApiModelProperty(value = "碳减排率")
    private BigDecimal theoreticalCarbonEmissionReductionRate;


}
