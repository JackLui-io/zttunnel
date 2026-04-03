package com.scsdky.web.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 电表
 */
@Data
public class TunnelEleMeter {
    private Long id;

    /**
     * 设备编号
     */
    private String deviceNum;
    /**
     * 电量
     */
    private Integer quantityEle;
    /**
     * 功率
     */
    private Integer power;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 采集时间
     */
    private Date uploadTime;
}
