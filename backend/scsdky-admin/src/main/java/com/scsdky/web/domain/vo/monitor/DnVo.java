package com.scsdky.web.domain.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * 监控--电能
 * @date 2023/09/27
 */
@Data
public class DnVo {

    @ApiModelProperty(value = "小时")
    private Integer hour;

    @ApiModelProperty(value = "耗电量")
    private String powerConsumption;

    @ApiModelProperty(value = "理论节电量")
    private String theoreticalPowerSavings;
}
