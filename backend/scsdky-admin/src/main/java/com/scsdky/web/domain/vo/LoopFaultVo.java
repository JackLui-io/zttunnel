package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class LoopFaultVo {

    @ApiModelProperty(value = "设备总数")
    private Long deviceCount;

    @ApiModelProperty(value = "设备在线数")
    private Long deviceOnline;


    @ApiModelProperty(value = "设备离线数")
    private Long deviceOffline;

    @ApiModelProperty(value = "设备故障数")
    private Long deviceBreakdown;
}
