package com.scsdky.web.domain.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tubo
 * 洞外照度
 * @date 2023/09/18
 */
@Data
public class LightOutsideVo {

    @ApiModelProperty(value = "小时")
    private String hour;

    @ApiModelProperty(value = "洞外照度")
    private BigDecimal outside;

    @ApiModelProperty(value = "调光比列")
    private BigDecimal dimmingRadio;
}
