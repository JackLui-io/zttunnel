package com.scsdky.web.domain;

import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tubo
 * 洞外照度
 * @date 2023/09/18
 */
@Data
public class PowerLightVo {

    @ApiModelProperty(value = "月份")
    private Integer month;

    @ApiModelProperty(value = "总用电--实际耗电量")
    private BigDecimal totalLight;

    @ApiModelProperty(value = "总节电== 原设计-实际")
    private BigDecimal totalEconomyLight;

    @ApiModelProperty(value = "理论碳减排率")
    private BigDecimal theoreticalCarbonEmissionReduction;
}
