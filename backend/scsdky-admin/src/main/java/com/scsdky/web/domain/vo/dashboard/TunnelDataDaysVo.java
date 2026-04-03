package com.scsdky.web.domain.vo.dashboard;

import lombok.Data;

import java.time.LocalDate;

/**
 * 隧道在 tunnel_carbon_day_push 中有电表数据的日期范围（用于 userPowerOverview 按“有数据天数”计算）
 */
@Data
public class TunnelDataDaysVo {
    /** 隧道 id（tunnel_name_result.id） */
    private Long tunnelId;
    /** 当年最早有数据的日期 */
    private LocalDate firstDate;
    /** 当年最晚有数据的日期 */
    private LocalDate lastDate;
}
