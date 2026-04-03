package com.scsdky.web.domain.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author tubo
 * 监控--车速
 * @date 2023/09/27
 */
@Data
public class TrafficVo {

    @ApiModelProperty(value = "小时")
    private Integer hour;

    @ApiModelProperty(value = "车流量")
    private Integer trafficFlow;

    @ApiModelProperty(value = "累计车流量")
    private Integer totalFlow;

    @ApiModelProperty(value = "小时")
    private String hourUpload;

    /**
     * 每小时的车速
     */
    private List<TrafficSpeedVo> trafficSpeedVoList;

}
