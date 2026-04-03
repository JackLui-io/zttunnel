package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 本月理论亮灯时长削减率（用户管理所有隧道汇总）
 */
@Data
@ApiModel("本月理论亮灯时长削减率")
public class DashboardLightUpReductionVo {

    @ApiModelProperty("削减率（%），(原设计亮灯时长-实际亮灯时长)/原设计×100")
    private BigDecimal lightUpReductionRate;
}
