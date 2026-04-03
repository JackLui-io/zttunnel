package com.scsdky.web.domain.vo.dashboard;

import lombok.Data;

/**
 * Dashboard 按月亮灯时长汇总（lighting_time 单位 0.1h，×0.1 得小时）
 */
@Data
public class MonthlyLightingVo {
    /** 月份 1-12 */
    private Integer month;
    /** lighting_time 原始汇总（×0.1 为实际亮灯小时） */
    private Long totalLightingTime;
}
