package com.scsdky.web.domain.vo.dashboard;

import lombok.Data;

/**
 * 隧道在指定时间范围内有亮灯数据的天数（tunnel_edge_compute_data，type=1）
 */
@Data
public class TunnelLightingDaysVo {
    /** 隧道 id */
    private Long tunnelId;
    /** 有数据的天数 */
    private Integer daysInMonth;
}
