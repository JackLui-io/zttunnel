package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Dashboard 消息通知单条
 */
@Data
public class DashboardMessageItemVo {

    @ApiModelProperty(value = "时间 yyyy-MM-dd HH:mm:ss")
    private String time;

    @ApiModelProperty(value = "通知内容")
    private String text;

    @ApiModelProperty(value = "类型 2操作 3实时警报 4设备（仅系统通知有）")
    private Integer type;
}
