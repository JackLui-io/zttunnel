package com.scsdky.web.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author tubo
 * 车流或车速返回类
 * @date 2023/09/18
 */
@Data
public class TrafficFlowOrSpeedVo {

    @ApiModelProperty(value = "日期")
    @Excel(name = "日期")
    private String uploadTime;

    @ApiModelProperty(value = "小时")
    private Integer hour;

    @ApiModelProperty(value = "每日车流量")
    @Excel(name = "每日车流量")
    private Integer trafficFlow;

    @ApiModelProperty(value = "最大值车流量")
    @Excel(name = "最大车流")
    private Integer maxTrafficFlow;
    @ApiModelProperty(value = "最小值车流量")
    @Excel(name = "最小车流")
    private Integer minTrafficFlow;

    @ApiModelProperty(value = "平均车流量")
    @Excel(name = "平均车流量")
    private Integer avgTrafficFlow;

    @ApiModelProperty(value = "最大值-速度")
    @Excel(name = "最大车速")
    private Integer maxSpeed;

    @ApiModelProperty(value = "最小值-速度")
    @Excel(name = "最小车速")
    private Integer minSpeed;

    @ApiModelProperty(value = "平均值-速度")
    @Excel(name = "平均车速")
    private Integer avgSpeed;


    private Long devicelistId;


}
