package com.scsdky.web.domain.vo.dashboard;

import lombok.Data;

/**
 * 隧道在某月有电表数据的天数（用于 userPowerOverview 月度节电量计算）
 */
@Data
public class TunnelDataDaysByMonthVo {
    /** 隧道 id */
    private Long tunnelId;
    /** 月份 1-12 */
    private Integer month;
    /** 该月有数据的天数 */
    private Integer daysInMonth;
}
