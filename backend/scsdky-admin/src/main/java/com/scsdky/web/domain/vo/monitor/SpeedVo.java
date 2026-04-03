package com.scsdky.web.domain.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author tubo
 * 监控--车速
 * @date 2023/09/27
 */
@Data
public class SpeedVo {

    @ApiModelProperty(value = "小时")
    private String hour;

    @ApiModelProperty(value = "平均车速")
    private String avgSpeed;

}
