package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Dashboard 待处理统计（RightSidebar 待处理卡片）
 */
@Data
public class DashboardPendingCountsVo {

    @ApiModelProperty(value = "系统通知数量 type=1")
    private Integer systemNotice;

    @ApiModelProperty(value = "操作通知数量 type=2")
    private Integer operationNotice;

    @ApiModelProperty(value = "实时警报数量 type=3")
    private Integer realtimeAlarm;

    @ApiModelProperty(value = "设备告警数量 type=4")
    private Integer deviceAlarm;
}
